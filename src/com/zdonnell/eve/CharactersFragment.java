package com.zdonnell.eve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.account.Account;
import com.zdonnell.eve.api.account.EveCharacter;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.QueuedSkill;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.helpers.TimeRemainingCountdown;
import com.zdonnell.eve.helpers.Tools;
import com.zdonnell.eve.staticdata.api.StationDatabase;
import com.zdonnell.eve.staticdata.api.StationInfo;

public class CharactersFragment extends Fragment {

	private static final int CURRENT_ID_DISPLAYED = 1;
	
	HashMap<View, Integer> viewCharacterMap = new HashMap<View, Integer>();
		
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
	
	/**
	 * Reference to the ImageService singleton, handles acquisition of character portraits and corp logos.
	 */
	private ImageService imageService;
	
	/**
	 * This SparseArray keeps track of what TextView a given characterID is currently using for the queue timer.
	 * This is needed because the CountdownTimer may continue to update a view that is now being reused for a different character.
	 */
	private SparseArray<TimeRemainingCountdown> cachedTrainingTime = new SparseArray<TimeRemainingCountdown>(10);
		
	private EveCharacter[] characters;
	
	private CharacterArrayAdapter arrayAdapter;
	
	private GridView charGrid;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{		
		setRetainInstance(true);
		
		/* setup some Fragment wide objects */
		context = inflater.getContext();
		imageService = ImageService.getInstance(context);
		charDB = new CharacterDB(context);
		
		/* test function to load characters from API keys */
		if (true) loadStationInfo();
		
		/* Setup the GridView properties and link with the CursorAdapater */
		View mainView = (View) inflater.inflate(R.layout.characters_fragment, container, false);
		charGrid = (GridView) mainView.findViewById(R.id.charGrid);		
		
		characters = charDB.getEnabledCharactersAsArray();
		
		arrayAdapter = new CharacterArrayAdapter(context, R.layout.character_tile, characters);
		charGrid.setAdapter(arrayAdapter);
						
		columns = calcColumns((Activity) context);
		charGrid.setNumColumns(columns);
		
		return mainView;
	}
	
	public void refreshChars()
	{
		viewCharacterMap.clear();
		
		arrayAdapter = new CharacterArrayAdapter(context, R.layout.character_tile, charDB.getEnabledCharactersAsArray());
		charGrid.setAdapter(arrayAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

	private class CharacterArrayAdapter extends ArrayAdapter<EveCharacter>
	{
		private int layoutResId;
		
		public CharacterArrayAdapter(Context context, int textViewResourceId, EveCharacter[] objects) 
		{
			super(context, textViewResourceId, objects);
			layoutResId = textViewResourceId;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if (convertView == null) convertView = inflater.inflate(layoutResId, parent, false);
			
			/* get the character at the current position */
			EveCharacter currentCharacter = getItem(position);
			
			/* Establish some basic values / info for use later */
			final int characterID = currentCharacter.charID;
			final int corpID = currentCharacter.corpID;	
			final APICredentials credentials = new APICredentials(currentCharacter.keyID, currentCharacter.vCode);
			APICharacter character = new APICharacter(credentials, characterID, context);
			
			/* handle view configuration */
			int calculatedWidth = calculatedColumnWidths[position % columns];
			convertView.setLayoutParams(new AbsListView.LayoutParams(calculatedWidth, calculatedColumnWidths[0]));
			
			TextView charName = (TextView) convertView.findViewById(R.id.char_tile_name);
			charName.setText(currentCharacter.name);
			
			Integer viewsLastID = viewCharacterMap.get(convertView);
			if (viewsLastID == null || viewCharacterMap.get(convertView) != characterID)
			{
				loadPortrait(convertView, position, characterID);
				loadCorpLogo(convertView, position, corpID);				
				setupQueueTimeRemaining(character, convertView);	
				
				viewCharacterMap.put(convertView, characterID);
			}
			
			/* configure the onClick action */
			convertView.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					Intent intent = new Intent(context, CharacterSheetActivity.class);
					String[] CharacterInfo = new String[4];
					CharacterInfo[0] = String.valueOf(characterID);
					CharacterInfo[1] = String.valueOf(credentials.keyID);
					CharacterInfo[2] = credentials.verificationCode;
					CharacterInfo[3] = String.valueOf(corpID);
					
					intent.putExtra("character", CharacterInfo);
	            	startActivity(intent);
				}
			});
			
			return convertView;
		}
		
		/**
		 * Handles the acquisition of the character portrait from the local instance of the {@link ImageService}
		 * 
		 * @param mainView
		 * @param characterID
		 */
		private void loadPortrait(final View mainView, int position, final int characterID)
		{
			final ImageView portrait = (ImageView) mainView.findViewById(R.id.char_image);
			Integer currentID = (Integer) mainView.getTag();
			
			portrait.setImageBitmap(null);

			imageService.getPortraits(new ImageService.IconObtainedCallback() 
			{	
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					portrait.setImageBitmap(bitmaps.valueAt(0));
					viewCharacterMap.put(mainView, characterID);
					if (mainView.getAlpha() == 0) mainView.setAlpha(1);
				}
			}, false, characterID);
			
			/* Set the correct size for the ImageView */
			int width = mainView.getLayoutParams().width;
			int height = mainView.getLayoutParams().height;
			portrait.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		}
		
		/**
		 * Handles the acquisition of the characters corporation logo from the local instance of the {@link ImageService}
		 * 
		 * @param mainView
		 * @param corpID
		 */
		private void loadCorpLogo(final View mainView, int position, int corpID)
		{
			final ImageView corpLogo = (ImageView) mainView.findViewById(R.id.corp_image);
			
			imageService.getCorpLogos(new ImageService.IconObtainedCallback() 
			{	
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					corpLogo.setImageBitmap(bitmaps.valueAt(0));
					corpLogo.setTag(bitmaps.keyAt(0));
				}
			}, true, corpID);
		}
		
		/**
		 * Handles getting the time remaining in the queue, and linking the correct timer to the TextView
		 * 
		 * TODO explain more about the link involving {@link CharactersFragment#cachedTrainingTime}
		 * 
		 * @param character
		 * @param mainView
		 */
		private void setupQueueTimeRemaining(APICharacter character, View mainView)
		{
			final int characterID = character.id();
			final TextView timeRemainingTextView = (TextView) mainView.findViewById(R.id.char_tile_training);			
			
			/* if the character already has an established timer, tell it to update the new TextView */
			if (cachedTrainingTime.get(characterID) != null) cachedTrainingTime.get(characterID).updateTextView(timeRemainingTextView);
			
			/* else get the time remaining in the queue and setup a CountdownTimer for it */
			else character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() 
			{
				@Override
				public void onUpdate(ArrayList<QueuedSkill> updatedData) 
				{	
					long timeUntilQueueEmpty = updatedData.isEmpty() ? 0 : Tools.timeUntilUTCTime(updatedData.get(updatedData.size() - 1).endTime);
					
					TimeRemainingCountdown timer = new TimeRemainingCountdown(timeUntilQueueEmpty, 1000, timeRemainingTextView);					
					if (cachedTrainingTime.get(characterID) != null) 
					{
						cachedTrainingTime.get(characterID).cancel();
						cachedTrainingTime.remove(characterID);
					}
					
					cachedTrainingTime.put(characterID, timer);
					timer.start();
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
		new Eve(context).getConquerableStations(new APICallback<SparseArray<StationInfo>>() {

			@Override
			public void onUpdate(SparseArray<StationInfo> stationInfo)
			{
				new InsertStationInfoAsyncTask(context, stationInfo).execute();	
			}
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