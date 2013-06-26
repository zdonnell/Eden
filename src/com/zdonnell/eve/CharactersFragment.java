package com.zdonnell.eve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zdonnell.androideveapi.character.skill.queue.ApiSkillQueueItem;
import com.zdonnell.androideveapi.character.skill.queue.SkillQueueResponse;
import com.zdonnell.androideveapi.eve.conquerablestationlist.ApiStation;
import com.zdonnell.androideveapi.eve.conquerablestationlist.StationListResponse;
import com.zdonnell.androideveapi.exception.ApiException;
import com.zdonnell.androideveapi.link.ApiExceptionCallback;
import com.zdonnell.androideveapi.link.ILoadingActivity;
import com.zdonnell.androideveapi.link.account.EdenEveCharacter;
import com.zdonnell.androideveapi.link.character.ApiCharacter;
import com.zdonnell.androideveapi.link.eve.ApiEve;
import com.zdonnell.eve.helpers.ImageURL;
import com.zdonnell.eve.helpers.Tools;
import com.zdonnell.eve.staticdata.StationDatabase;
import com.zdonnell.eve.staticdata.StationInfo;

/**
 * Fragment to display the list currently active characters
 * 
 * @author Zach
 *
 */
public class CharactersFragment extends Fragment {
	
	HashMap<View, Integer> viewCharacterMap = new HashMap<View, Integer>();
	
	private SparseArray<Long> queueTimesRemaining = new SparseArray<Long>();
		
	/**
	 * Global value to store the number of columns the GridView is displaying
	 */
	private int columns;
	
	/**
	 * As with {@link #columns} this value stores the size (in pixels) of each column in the main GridView once calculated.
	 */
	private int[] calculatedColumnWidths;
	
	/**
	 * Reference to the database of characters and character info.
	 */
	private CharacterDB charDB;
	
	/**
	 * Reference to the current context
	 */
	private Context context;
		
	private EdenEveCharacter[] characters;
		
	private GridView charGrid;
	
	private int sortType;
	
	private SharedPreferences prefs;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{		
		setRetainInstance(true);
		
		/* setup some Fragment wide objects */
		context = inflater.getContext();
		charDB = new CharacterDB(context);
		
		prefs = context.getSharedPreferences("eden", Context.MODE_PRIVATE);
		sortType = prefs.getInt("sort type", CharacterSort.ALPHA);
				
		loadStationInfo();
		
		/* Setup the GridView properties and link with the CursorAdapater */
		View mainView = (View) inflater.inflate(R.layout.characters_fragment, container, false);
		charGrid = (GridView) mainView.findViewById(R.id.charGrid);		
		
		columns = calcColumns((Activity) context);
		charGrid.setNumColumns(columns);
		
		characters = charDB.getEnabledCharactersAsArray();
		updateSort(sortType);
						
		return mainView;
	}
	
	public void refreshChars()
	{
		viewCharacterMap.clear();
		characters = charDB.getEnabledCharactersAsArray();
		updateSort(sortType);
	}
	
	public void updateSort(int sortType)
	{
		this.sortType = sortType;
		prefs.edit().putInt("sort type", sortType).commit();
		
		switch (sortType)
		{
		case CharacterSort.ALPHA:
			Arrays.sort(characters, new CharacterSort.Alpha());
			break;
		case CharacterSort.ALPHA_REVERSE:
			Arrays.sort(characters, Collections.reverseOrder(new CharacterSort.Alpha()));
			break;
		case CharacterSort.QUEUETIME:
			Arrays.sort(characters, new CharacterSort.TrainingTimeRemaining());
			break;
		case CharacterSort.QUEUETIME_REVERSE:
			Arrays.sort(characters, Collections.reverseOrder(new CharacterSort.TrainingTimeRemaining()));
			break;
		}
		
		viewCharacterMap.clear();
		charGrid.setAdapter(new CharacterArrayAdapter(context, R.layout.character_tile, characters));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

	private class CharacterArrayAdapter extends ArrayAdapter<EdenEveCharacter>
	{
		private int layoutResId;
		
		public CharacterArrayAdapter(Context context, int textViewResourceId, EdenEveCharacter[] objects) 
		{
			super(context, textViewResourceId, objects);
			layoutResId = textViewResourceId;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) 
			{
				convertView = inflater.inflate(layoutResId, null, false);
				
				/* handle view configuration */
				int calculatedWidth = calculatedColumnWidths[position % columns];
				convertView.setLayoutParams(new AbsListView.LayoutParams(calculatedWidth, calculatedColumnWidths[0]));
			}
			
			/* get the character at the current position */
			final EdenEveCharacter currentCharacter = getItem(position);
			
			/* Establish some basic values / info for use later */
			final int characterID = (int) currentCharacter.getCharacterID();
			final int corpID = (int) currentCharacter.getCorporationID();	
			
			TextView charName = (TextView) convertView.findViewById(R.id.char_tile_name);
			charName.setText(currentCharacter.getName());
			
			Integer viewsLastID = viewCharacterMap.get(convertView);
			if (viewsLastID == null || viewCharacterMap.get(convertView) != characterID)
			{
				loadPortrait(convertView, position, characterID);
				loadCorpLogo(convertView, position, corpID);				
				setupQueueTimeRemaining(currentCharacter, convertView);	
				
				viewCharacterMap.put(convertView, characterID);
			}
			
			/* configure the onClick action */
			convertView.setOnClickListener(new View.OnClickListener() 
			{	
				public void onClick(View v) 
				{					
					Intent intent = new Intent(context, CharacterSheetActivity.class);
					String[] CharacterInfo = new String[4];
					CharacterInfo[0] = String.valueOf(characterID);
					CharacterInfo[1] = String.valueOf(currentCharacter.getApiAuth().getKeyID());
					CharacterInfo[2] = currentCharacter.getApiAuth().getVCode();
					CharacterInfo[3] = String.valueOf(corpID);
					
					intent.putExtra("character", CharacterInfo);
	            	startActivity(intent);
				}
			});
			
			return convertView;
		}
		
		/**
		 * Handles the acquisition of the character portrait
		 * 
		 * @param mainView
		 * @param characterID
		 */
		private void loadPortrait(final View mainView, int position, final int characterID)
		{
			final ImageView portrait = (ImageView) mainView.findViewById(R.id.char_image);			
			portrait.setImageBitmap(null);

			ImageLoader.getInstance().displayImage(ImageURL.forChar(characterID), portrait);
			
			/* Set the correct size for the ImageView */
			int width = mainView.getLayoutParams().width;
			int height = mainView.getLayoutParams().height;
			portrait.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		}
		
		/**
		 * Handles the acquisition of the characters corporation logo
		 * 
		 * @param mainView
		 * @param corpID
		 */
		private void loadCorpLogo(final View mainView, int position, int corpID)
		{
			final ImageView corpLogo = (ImageView) mainView.findViewById(R.id.corp_image);
			
			ImageLoader.getInstance().displayImage(ImageURL.forCorp(corpID), corpLogo);
			//Picasso.with(context).load(ImageURL.forCorp(corpID)).into(corpLogo);
		}
		
		/**
		 * Handles getting the time remaining in the queue
		 *  
		 * @param character
		 * @param mainView
		 */
		private void setupQueueTimeRemaining(final EdenEveCharacter character, View mainView)
		{
			final TextView timeRemainingTextView = (TextView) mainView.findViewById(R.id.char_tile_training);			
			timeRemainingTextView.setText("");
						
			new ApiCharacter(context, character.getApiAuth()).getSkillQueue(new ApiExceptionCallback<SkillQueueResponse>((ILoadingActivity) getActivity()) 
			{
				@Override
				public void onUpdate(SkillQueueResponse response) 
				{
					ArrayList<ApiSkillQueueItem> skillQueueList = new ArrayList<ApiSkillQueueItem>();
					skillQueueList.addAll(response.getAll());
					
					long timeUntilQueueEmpty = skillQueueList.isEmpty() ? 0 : Tools.timeUntilUTCTime(skillQueueList.get(skillQueueList.size() - 1).getEndTime());
					queueTimesRemaining.put((int) character.getCharacterID(), timeUntilQueueEmpty);
					charDB.setCharQueueTime((int) character.getCharacterID(), timeUntilQueueEmpty);
					
					if (timeUntilQueueEmpty > 24 * 60 * 60 * 1000) 
					{
						timeRemainingTextView.setText(Html.fromHtml("<FONT COLOR='#99CC00'>" + Tools.millisToEveFormatString(timeUntilQueueEmpty) + "</FONT>"));
					}
					else if (timeUntilQueueEmpty > 0)
					{
						timeRemainingTextView.setText(Html.fromHtml("<FONT COLOR='#FFBB33'>" + Tools.millisToEveFormatString(timeUntilQueueEmpty) + "</FONT>"));
					}
					else timeRemainingTextView.setText(Html.fromHtml("<FONT COLOR='#FF4444'>Skill Queue Empty</FONT>"));
				}

				@Override
				public void onError(SkillQueueResponse response, ApiException exception) 
				{
					
				}
			});
		}
	}
	
	/**
	 * Determines how many columns to display in the main gridView based on screen density and pixel count
	 * 
	 * @param context
	 * @return number of columns
	 */
	private int calcColumns(Activity context) 
	{	
		Point screenDimensions = new Point(0, 0);
		context.getWindowManager().getDefaultDisplay().getSize(screenDimensions);
		
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		float logicalDensity = metrics.density;
		int width = screenDimensions.x;
		
		float dp = (float) width / logicalDensity;
		
		int columns = (int) Math.round(dp / 200f);
		if (columns < 2) columns = 2;
		
		calculatedColumnWidths = new int[columns];
		
		int widthForSeperation = columns - 1;
		double widthForColumns = screenDimensions.x - widthForSeperation;
		
		int totalColumnWidthUsed = 0, roundedColumnWidth = 0;
		
		for (int x = 0; x < columns; x++) {
			roundedColumnWidth = (int) Math.floor(widthForColumns / (double) columns);
			calculatedColumnWidths[x] = roundedColumnWidth;
			
			totalColumnWidthUsed += roundedColumnWidth;
			
			if (x == columns - 1 && totalColumnWidthUsed != widthForColumns) calculatedColumnWidths[x] += 1;
		}
		
		return columns;
	}
	
	
	private void loadStationInfo()
	{		
		new ApiEve(context).conqStationsList(new ApiExceptionCallback<StationListResponse>((NavDrawerActivity) getActivity())
		{
			@Override
			public void onUpdate(StationListResponse response) 
			{				
				Map<Integer, ApiStation> eveapiStationMap = response.getStations();
				SparseArray<StationInfo> edenStationInfoSet = new SparseArray<StationInfo>(eveapiStationMap.size());
			
				for (ApiStation station : eveapiStationMap.values())
				{
					StationInfo edenStation = new StationInfo();
					edenStation.stationID = station.getStationID();
					edenStation.stationName = station.getStationName();
					edenStation.stationTypeID = station.getStationTypeID();
					
					edenStationInfoSet.put(edenStation.stationID, edenStation);
				}
				
				new InsertStationInfoAsyncTask(context, edenStationInfoSet).execute();	
			}

			@Override
			public void onError(StationListResponse response, ApiException exception) { /* TODO */ }
			
		});
	}
	
	private class InsertStationInfoAsyncTask extends AsyncTask<Void, Void, Void>
	{
		Context context;
		SparseArray<StationInfo> stationInfo;
		
		public InsertStationInfoAsyncTask(Context context, SparseArray<StationInfo> stationInfo)
		{			
			this.context = context;
			this.stationInfo = stationInfo;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			new StationDatabase(context).insertStationInfo(stationInfo);	

			return null;
		}
	}
}