package com.zdonnell.eve;

import java.util.ArrayList;

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
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
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
		
	/**
	 * Accounts used for testing purposes
	 * 
	 * TODO remove these once account management has been implemented
	 */
	private Account slick50zd1, mercenoid22, xsteveo243x;	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{		
		setRetainInstance(true);
		
		/* setup some Fragment wide objects */
		context = inflater.getContext();
		imageService = ImageService.getInstance(context);
		charDB = new CharacterDB(context);
		
		/* test function to load characters from API keys */
		if (false) loadCharacters();
		if (true) loadStationInfo();
		
		/* Setup the GridView properties and link with the CursorAdapater */
		View mainView = (View) inflater.inflate(R.layout.characters_fragment, container, false);
		GridView charGrid = (GridView) mainView.findViewById(R.id.charGrid);
		charGrid.setAdapter(new CharacterCursorAdapater(inflater.getContext(), charDB.allCharacters()));
						
		columns = calcColumns((Activity) context);
		charGrid.setNumColumns(columns);
		
		return mainView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

	/**
	 * CursorAdapter that handles the population of the character list
	 * 
	 * @author zachd
	 *
	 */
	private class CharacterCursorAdapater extends CursorAdapter 
	{		
		public CharacterCursorAdapater(Context context, Cursor c) 
		{
			super(context, c, false);
		}
		
		/**
		 * configures the view provided, with info from the cursor, for use in the main {@link GridView}
		 * 
		 * @param view
		 * @param context
		 * @param cursor
		 */
		@Override
		public void bindView(final View view, final Context context, Cursor cursor) 
		{		
			/* Establish some basic values / info for use later */
			final int characterID = cursor.getInt(2);
			final int corpID = cursor.getInt(4);	
			final APICredentials credentials = new APICredentials(cursor.getInt(5), cursor.getString(6));
			APICharacter character = new APICharacter(credentials, cursor.getInt(2), context);
			
			/* handle view configuration */
			int calculatedWidth = calculatedColumnWidths[cursor.getPosition() % columns];
			view.setLayoutParams(new AbsListView.LayoutParams(calculatedWidth, calculatedColumnWidths[0]));
			
			TextView charName = (TextView) view.findViewById(R.id.char_tile_name);
			charName.setText(cursor.getString(1));
			
			loadPortrait(view, characterID);
			loadCorpLogo(view, corpID);				
			setupQueueTimeRemaining(character, view);				
			
			/* configure the onClick action */
			view.setOnClickListener(new View.OnClickListener() 
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
		}

		/**
		 * if there is not a view to reuse, inflate a new one for use in the main {@link GridView}
		 * 
		 * @param view
		 * @param context
		 * @param cursor
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) 
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.character_tile, parent, false);

			v.setLayoutParams(new AbsListView.LayoutParams(parent.getWidth()/columns, parent.getWidth()/columns));
			//v.setAlpha(0);
			
			bindView(v, context, cursor);
			return v;
		}
		
		/**
		 * Handles the acquisition of the character portrait from the local instance of the {@link ImageService}
		 * 
		 * @param mainView
		 * @param characterID
		 */
		private void loadPortrait(final View mainView, int characterID)
		{
			final ImageView portrait = (ImageView) mainView.findViewById(R.id.char_image);
			
			imageService.getPortraits(new ImageService.IconObtainedCallback() 
			{	
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					portrait.setImageBitmap(bitmaps.valueAt(0));
					if (mainView.getAlpha() == 0) mainView.setAlpha(1);
				}
			}, characterID);
			
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
		private void loadCorpLogo(final View mainView, int corpID)
		{
			final ImageView corpLogo = (ImageView) mainView.findViewById(R.id.corp_image);
			imageService.getCorpLogos(new ImageService.IconObtainedCallback() 
			{	
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					corpLogo.setImageBitmap(bitmaps.valueAt(0));
				}
			}, corpID);
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
	
	
	
	/**
	 * TODO Remove function once account loading finished
	 * 
	 * Testing function
	 */
	private void loadCharacters()
	{
		slick50zd1 = new Account(1171726, "G87RoqlTiVG7ecrLSLuehJnBl0VjRG11xYppONMOu9GpbHghCqcgqk3n81egdAGm", context);
		mercenoid22 = new Account(1171729, "4QsVKhpkQcM20jU1AahjcGzYFCSJljYFXld5X0wgLV8pYPJMeQRvQAUdDnSGhKvK", context);
		xsteveo243x = new Account(961364, "a73k2c5HvvwXhKhSRLzDQb8emKtRflovg51niFQSns9X8RT7y8ZbSzgRgQExUZnW", context);
				
		slick50zd1.characters(new APICallback<ArrayList<EveCharacter>>() {
			@Override
			public void onUpdate(ArrayList<EveCharacter> updatedData) {
				for (EveCharacter character : updatedData) charDB.addCharacter(character, slick50zd1.getCredentials());				
			}
		});
		mercenoid22.characters(new APICallback<ArrayList<EveCharacter>>() {
			@Override
			public void onUpdate(ArrayList<EveCharacter> updatedData) {
				for (EveCharacter character : updatedData) charDB.addCharacter(character, mercenoid22.getCredentials());				
			}
		});
		xsteveo243x.characters(new APICallback<ArrayList<EveCharacter>>() {
			@Override
			public void onUpdate(ArrayList<EveCharacter> updatedData) {
				for (EveCharacter character : updatedData) charDB.addCharacter(character, xsteveo243x.getCredentials());				
			}
		});
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