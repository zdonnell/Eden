package com.zdonnell.eve;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
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
import com.zdonnell.eve.helpers.BaseCallback;
import com.zdonnell.eve.helpers.TimeRemainingCountdown;
import com.zdonnell.eve.helpers.Tools;

public class CharactersFragment extends Fragment {

	private int columns;

	boolean loadChars = false;
	
	private CharacterDB charDB;
		
	private Context context;
	
	private ImageService imageService;
	
	View main;
	
	private int[] calculatedColumnWidths;
		
	private HashMap<Integer, TimeRemainingCountdown> cachedTrainingTime = new HashMap<Integer, TimeRemainingCountdown>();
	
	Account slick50zd1, mercenoid22, slpeterson;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		setRetainInstance(true);
		
		context = inflater.getContext();
		imageService = ImageService.getInstance(context);
		
		charDB = new CharacterDB(context);
				
		if (false) loadCharacters();
		
		main = (View) inflater.inflate(R.layout.characters_fragment, container, false);
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
		public void bindView(final View view, final Context context, Cursor cursor) 
		{		
			final int characterID = cursor.getInt(2);
			final int corpID = cursor.getInt(4);
						
			final APICredentials credentials = new APICredentials(cursor.getInt(5), cursor.getString(6));
			APICharacter character = new APICharacter(credentials, cursor.getInt(2), context);
			
			int calculatedWidth = calculatedColumnWidths[cursor.getPosition() % columns];
			view.setLayoutParams(new AbsListView.LayoutParams(calculatedWidth, calculatedColumnWidths[0]));
			
			final ImageView portrait = (ImageView) view.findViewById(R.id.char_image);
			imageService.getPortraits(new ImageService.IconObtainedCallback() 
			{	
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					portrait.setImageBitmap(bitmaps.valueAt(0));
					if (view.getAlpha() == 0) view.setAlpha(1);
				}
			}, characterID);
			
			int width = view.getLayoutParams().width;
			int height = view.getLayoutParams().height;
			portrait.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
			
			final ImageView corpLogo = (ImageView) view.findViewById(R.id.corp_image);
			imageService.getCorpLogos(new ImageService.IconObtainedCallback() 
			{	
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					corpLogo.setImageBitmap(bitmaps.valueAt(0));
				}
			}, corpID);
									
			TextView charName = (TextView) view.findViewById(R.id.char_tile_name);
			charName.setText(cursor.getString(1));
			
			final TextView corpName = (TextView) view.findViewById(R.id.char_tile_training);			
			
			if (cachedTrainingTime.containsKey(character.id()))	cachedTrainingTime.get(character.id()).updateTextView(corpName);
			else character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() 
			{
				@Override
				public void onUpdate(ArrayList<QueuedSkill> updatedData) 
				{	
					long timeUntilQueueEmpty = updatedData.isEmpty() ? 0 : Tools.timeUntilUTCTime(updatedData.get(updatedData.size() - 1).endTime);
					
					TimeRemainingCountdown timer = new TimeRemainingCountdown(timeUntilQueueEmpty, 1000, corpName);					
					if (cachedTrainingTime.containsKey(characterID)) cachedTrainingTime.remove(characterID).cancel();
					
					cachedTrainingTime.put(characterID, timer);
					timer.start();
				}
			});	
			
			view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, CharacterSheetActivity.class);
					
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
			v.setAlpha(0);
			
			bindView(v, context, cursor);
			return v;
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