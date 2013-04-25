package com.zdonnell.eve;

import java.util.ArrayList;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.account.EveCharacter;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class APIKeysFragment extends Fragment 
{
	private Context context;

	ArrayList<APICredentials> apiCredsList;
	
	SparseArray<ArrayList<EveCharacter>> characters = new SparseArray<ArrayList<EveCharacter>>();
	
	CharacterDB charDB;
	
	boolean refreshRequired = false;
	
	ListView apiKeyList;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{		
		setRetainInstance(true);
		context = inflater.getContext();

		getData();
		
		View mainView = inflater.inflate(R.layout.api_keys_fragment, null);
		apiKeyList = (ListView) mainView.findViewById(R.id.characters_edit_characters_list);
		apiKeyList.setAdapter(new APIKeyListAdapter(getActivity(), R.layout.characters_edit_characters_list_item, apiCredsList, characters));
		
		return mainView;
	}

	private void getData()
	{
		charDB = new CharacterDB(getActivity());
		
		characters.clear();
		apiCredsList = new ArrayList<APICredentials>();
		
		Cursor c = charDB.allCharacters();
		while (c.moveToNext())
		{
			int charID = c.getInt(c.getColumnIndex(CharacterDB.CHAR_TABLE_EVEID));
			String charName = c.getString(c.getColumnIndex(CharacterDB.CHAR_TABLE_NAME));
			int apiKey = c.getInt(c.getColumnIndex(CharacterDB.CHAR_TABLE_KEYID));
			String vCode = c.getString(c.getColumnIndex(CharacterDB.CHAR_TABLE_VCODE));
			
			if (!keyAccountedFor(apiKey, vCode, apiCredsList)) apiCredsList.add(new APICredentials(apiKey, vCode));

			if (characters.get(apiKey) == null) characters.put(apiKey, new ArrayList<EveCharacter>());
			characters.get(apiKey).add(new EveCharacter(charName, charID, null, 0, 0, null));
		}
		c.close();
		
	}
	
	private boolean keyAccountedFor(int apiKey, String vCode, ArrayList<APICredentials> apiCredsList)
	{
		for (APICredentials c : apiCredsList)
		{
			if (c.keyID == apiKey && c.verificationCode.equals(vCode)) return true;
		}
		
		return false;
	}
	
	private void updateList()
	{		
		refreshRequired = false;
		
		getData();
		
		APIKeyListAdapter currentAdapter = (APIKeyListAdapter) apiKeyList.getAdapter();
		currentAdapter.clear();
		currentAdapter.addAll(apiCredsList);
		currentAdapter.notifyDataSetChanged();
	}
	
	public void refresh()
	{
		updateList();
	}
	
	private class APIKeyListAdapter extends ArrayAdapter<APICredentials>
	{
		int listItemLayoutID;
		
		SparseArray<ArrayList<EveCharacter>> characters;
		
		public APIKeyListAdapter(Context context, int textViewResourceId, ArrayList<APICredentials> keys, SparseArray<ArrayList<EveCharacter>> characters) 
		{
			super(context, textViewResourceId, keys);
			this.characters = characters;
			listItemLayoutID = textViewResourceId;
		}	
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = View.inflate(getContext(), listItemLayoutID, null);
				convertView.setClickable(false);
				convertView.setOnClickListener(null);
			}
			
			final int keyID = getItem(position).keyID;
			
			TextView apiKeyText = (TextView) convertView.findViewById(R.id.characters_edit_characters_list_item_apikey);
			ImageView deleteImage = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_delete_icon);
			
			final ImageView[] portraits = new ImageView[3];
			final boolean charOn[] = new boolean[3];
			
			portraits[0] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait1);
			portraits[1] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait2);
			portraits[2] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait3);
			
			for (int i = 0; i < 3; ++i) portraits[i].setVisibility(View.GONE);
			
			apiKeyText.setText(String.valueOf(getItem(position).keyID));
			deleteImage.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					charDB.deleteCharactersByKeyID(keyID);
					updateList();
				}
			});	
			
			final Integer[] charIDs = new Integer[characters.get(keyID).size()];
			for (int i = 0; i < characters.get(keyID).size(); ++i)
			{
				charIDs[i] = characters.get(keyID).get(i).charID;
				portraits[i].setTag(charIDs[i]);
				charOn[i] = charDB.isCharEnabled(charIDs[i]);
				
				final int charNum = i;
				
				portraits[i].setAlpha(charOn[i] ? 1f : 0.25f);
				portraits[i].setOnClickListener(new View.OnClickListener() 		
				{		
					@Override
					public void onClick(View v) 
					{
						refreshRequired = true;
						
						charOn[charNum] = !charOn[charNum];
						((ImageView) v).setAlpha(charOn[charNum] ? 1f : 0.25f);
						charDB.setCharEnabled(charIDs[charNum], charOn[charNum]);
					}
				});
			}	
			
			ImageService.getInstance(getContext()).getPortraits(new IconObtainedCallback()
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					for (int i = 0; i < characters.get(keyID).size(); ++i)
					{
						portraits[i].setVisibility(View.VISIBLE);						
						if (i < 3 && portraits[i].getTag().equals(charIDs[i])) 
						{
							portraits[i].setImageBitmap(bitmaps.get(charIDs[i]));
						}
					}
				}
			}, true, charIDs);
			
			return convertView;
		}
	}
}
