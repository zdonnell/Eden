package com.zdonnell.eve;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.zdonnell.eve.api.account.Account;
import com.zdonnell.eve.api.account.EveCharacter;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.QueuedSkill;

public class CharacterTabFragment extends Fragment {

	private int columns;

	boolean loadChars = false;
	
	private CharacterDB charDB;
	
	private ImageService imageService;
	
	private Context context;
	
	private int[] calculatedColumnWidths;
		
	private HashMap<Integer, TimeRemainingCountdown> cachedTrainingTime = new HashMap<Integer, TimeRemainingCountdown>();
	
	Account slick50zd1, mercenoid22, slpeterson;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		context = inflater.getContext();
		
		charDB = new CharacterDB(context);
		imageService = new ImageService(context);
		
		setupImagePreCache();
		if (false) loadCharacters();
		
		View main = (View) inflater.inflate(R.layout.character_fragment, container, false);
		GridView charGrid = (GridView) main.findViewById(R.id.charGrid);
		charGrid.setAdapter(new CharacterCursorAdapater(inflater.getContext(), charDB.allCharacters()));
		
		columns = calcColumns((Activity) context);
		charGrid.setNumColumns(columns);
		
		return main;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

	private class CharacterCursorAdapater extends CursorAdapter 
	{		
		public CharacterCursorAdapater(Context context, Cursor c) 
		{
			super(context, c, false);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void bindView(View view, final Context context, Cursor cursor) 
		{		
			final int characterID = cursor.getInt(2);
			
			final APICredentials credentials = new APICredentials(cursor.getInt(5), cursor.getString(6));
			APICharacter character = new APICharacter(credentials, cursor.getInt(2), context);
			
			int calculatedWidth = calculatedColumnWidths[cursor.getPosition() % columns];
			view.setLayoutParams(new AbsListView.LayoutParams(calculatedWidth, calculatedColumnWidths[0]));
			
			ImageView portrait = (ImageView) view.findViewById(R.id.char_image);
			imageService.setPortrait(portrait, cursor.getInt(2), ImageService.CHAR);
			int width = view.getLayoutParams().width;
			int height = view.getLayoutParams().height;
			portrait.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
			
			ImageView corpLogo = (ImageView) view.findViewById(R.id.corp_image);
			imageService.setPortrait(corpLogo, cursor.getInt(4), ImageService.CORP);
			
			TextView charName = (TextView) view.findViewById(R.id.char_tile_name);
			charName.setText(cursor.getString(1));
			
			final TextView corpName = (TextView) view.findViewById(R.id.char_tile_training);			
			
			if (cachedTrainingTime.containsKey(character.id()))
			{
				cachedTrainingTime.get(character.id()).updateTextView(corpName);
			} 
			else character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() 
			{
				@Override
				public void onUpdate(ArrayList<QueuedSkill> updatedData) {
					long timeUntilQueueEmpty = -1;
					
					if (updatedData.isEmpty()) timeUntilQueueEmpty = 0;
					else
					{
						try {
							timeUntilQueueEmpty = Tools.timeUntilUTCTime(updatedData.get(updatedData.size() - 1).endTime);
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					
					TimeRemainingCountdown timer = new TimeRemainingCountdown(timeUntilQueueEmpty, 1000, corpName);					
					if (cachedTrainingTime.containsKey(characterID))
					{
						cachedTrainingTime.remove(characterID).cancel();
					} 
					cachedTrainingTime.put(characterID, timer);
					timer.start();
				}
			});	
			
			view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, SheetItemListActivity.class);
					
					String[] CharacterInfo = new String[3];
					CharacterInfo[0] = String.valueOf(characterID);
					CharacterInfo[1] = String.valueOf(credentials.keyID);
					CharacterInfo[2] = credentials.verificationCode;
					
					intent.putExtra("character", CharacterInfo);
	            	 startActivity(intent);
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) 
		{
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.character_tile, parent, false);

			v.setLayoutParams(new AbsListView.LayoutParams(parent.getWidth()/columns, parent.getWidth()/columns));

			bindView(v, context, cursor);
			return v;
		}
	}
	
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
			
			if (x == columns - 1 && totalColumnWidthUsed != widthForColumns)
			{
				calculatedColumnWidths[x] += 1;
			}
		}
		
		return columns;
	}
	
	/**
	 * Compiles information to be sent to the ImageService to pre cache
	 * portraits and corp logos.
	 */
	private void setupImagePreCache()
	{
		Cursor charCursor = charDB.allCharacters();
		int characterNum = charCursor.getCount();
		
		int[][] preCacheArray = new int[characterNum*2][2];
		while (charCursor.moveToNext())
		{
			int position = charCursor.getPosition();
			
			preCacheArray[position][0] = charCursor.getInt(2);
			preCacheArray[position][1] = ImageService.CHAR;
			preCacheArray[position + characterNum][0] = charCursor.getInt(4);
			preCacheArray[position + characterNum][1] = ImageService.CORP;
		}
		charCursor.close();
		imageService.preCache(preCacheArray);
	}
	
	/**
	 * Testing function
	 */
	private void loadCharacters()
	{
		slick50zd1 = new Account(1171726, "G87RoqlTiVG7ecrLSLuehJnBl0VjRG11xYppONMOu9GpbHghCqcgqk3n81egdAGm", context);
		mercenoid22 = new Account(1171729, "4QsVKhpkQcM20jU1AahjcGzYFCSJljYFXld5X0wgLV8pYPJMeQRvQAUdDnSGhKvK", context);
		slpeterson = new Account(339772, "4hlNY5h45OhfTgT6lo9RyXO4jEysiTDRYTXsEPenRBIuAheea3TBNn5LxnatkFjU", context);
				
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
		slpeterson.characters(new APICallback<ArrayList<EveCharacter>>() {
			@Override
			public void onUpdate(ArrayList<EveCharacter> updatedData) {
				for (EveCharacter character : updatedData) charDB.addCharacter(character, slpeterson.getCredentials());				
			}
		});
	}
}