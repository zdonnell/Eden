package com.zdonnell.eve;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zdonnell.eve.api.account.Account;
import com.zdonnell.eve.api.account.EveCharacter;

public class CharacterTabFragment extends Fragment {

	private int columns;
	
	private CharacterDB charDB;
	
	private ImageService imageService;
	
	private Context context;
	
	private int[] calculatedColumnWidths;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		context = inflater.getContext();
		
		charDB = new CharacterDB(context);
		imageService = new ImageService(context);

		if (this.getArguments().getInt("tab") == 1) 
		{
			Account slick50zd1 = new Account(1171726, "G87RoqlTiVG7ecrLSLuehJnBl0VjRG11xYppONMOu9GpbHghCqcgqk3n81egdAGm", context);
			//new GetCharacters().execute(slick50zd1);
		} 
		else 
		{
			Account mercenoid22 = new Account(1171729, "4QsVKhpkQcM20jU1AahjcGzYFCSJljYFXld5X0wgLV8pYPJMeQRvQAUdDnSGhKvK", context);
			//new GetCharacters().execute(mercenoid22);
		}
		
		View main = (View) inflater.inflate(R.layout.character_fragment, container, false);
		GridView charGrid = (GridView) main.findViewById(R.id.charGrid);
		
		columns = calcColumns((Activity) context);
		
		charGrid.setNumColumns(columns);
		
		charGrid.setAdapter(new CharacterCursorAdapater(inflater.getContext(), charDB.allCharacters()));

		return main;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

	private class GetCharacters extends	AsyncTask<Account, Integer, ArrayList<EveCharacter>> 
	{	
		protected ArrayList<EveCharacter> doInBackground(Account... accounts) { return accounts[0].characters(); }

		protected void onPostExecute(ArrayList<EveCharacter> characters) 
		{
			for (EveCharacter character : characters) charDB.addCharacter(character);
		}
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
			
			TextView corpName = (TextView) view.findViewById(R.id.char_tile_training);
			corpName.setText(cursor.getString(3));
			
			view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, SheetItemListActivity.class);
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
}