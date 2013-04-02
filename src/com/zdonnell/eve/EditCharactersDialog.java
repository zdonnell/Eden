package com.zdonnell.eve;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.account.EveCharacter;


public class EditCharactersDialog extends DialogFragment 
{
	APICredentials[] keys;
	
	SparseArray<ArrayList<EveCharacter>> characters = new SparseArray<ArrayList<EveCharacter>>();
	
	public EditCharactersDialog()
	{
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		getData();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View root = inflater.inflate(R.layout.characters_edit_characters, null);
		ListView apiKeyList = (ListView) root.findViewById(R.id.characters_edit_characters_list);
		apiKeyList.setAdapter(new APIKeyListAdapter(getActivity(), R.layout.characters_edit_characters_list_item, keys, characters));
		
		builder.setView(root)
			.setNegativeButton("Done", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int id) 
				{
					dismiss();
				}
			});    
		
	    return builder.create();
	}
	
	private void getData()
	{
		CharacterDB charDB = new CharacterDB(getActivity());
		
		ArrayList<APICredentials> apiCredsList = new ArrayList<APICredentials>();
		
		Cursor c = charDB.allCharacters();
		while (c.moveToNext())
		{
			int charID = c.getInt(c.getColumnIndex(CharacterDB.CHAR_TABLE_EVEID));
			String charName = c.getString(c.getColumnIndex(CharacterDB.CHAR_TABLE_NAME));
			int apiKey = c.getInt(c.getColumnIndex(CharacterDB.CHAR_TABLE_KEYID));
			String vCode = c.getString(c.getColumnIndex(CharacterDB.CHAR_TABLE_VCODE));
			
			if (!keyAccountedFor(apiKey, vCode, apiCredsList)) apiCredsList.add(new APICredentials(apiKey, vCode));

			if (characters.get(apiKey) == null) characters.put(apiKey, new ArrayList<EveCharacter>());
			characters.get(apiKey).add(new EveCharacter(charName, charID, null, 0));
		}
		c.close();
		
		keys = new APICredentials[apiCredsList.size()];
		apiCredsList.toArray(keys);
	}
	
	private boolean keyAccountedFor(int apiKey, String vCode, ArrayList<APICredentials> apiCredsList)
	{
		for (APICredentials c : apiCredsList)
		{
			if (c.keyID == apiKey && c.verificationCode.equals(vCode)) return true;
		}
		
		return false;
	}
	
	private class APIKeyListAdapter extends ArrayAdapter<APICredentials>
	{
		int listItemLayoutID;
		
		SparseArray<ArrayList<EveCharacter>> characters;
		
		public APIKeyListAdapter(Context context, int textViewResourceId, APICredentials[] keys, SparseArray<ArrayList<EveCharacter>> characters) 
		{
			super(context, textViewResourceId, keys);
			this.characters = characters;
			listItemLayoutID = textViewResourceId;
		}	
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			if (convertView == null) convertView = View.inflate(getContext(), listItemLayoutID, null);
			
			final int keyID = getItem(position).keyID;
			
			TextView apiKeyText = (TextView) convertView.findViewById(R.id.characters_edit_characters_list_item_apikey);
			
			final ImageView[] portraits = new ImageView[3];
			
			portraits[0] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait1);
			portraits[1] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait2);
			portraits[2] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait3);
			
			apiKeyText.setText("API Key: " + getItem(position).keyID);
						
			final Integer[] charIDs = new Integer[characters.get(keyID).size()];
			for (int i = 0; i < characters.get(keyID).size(); ++i)
			{
				charIDs[i] = characters.get(keyID).get(i).charID;
			}
			
			ImageService.getInstance(getContext()).getPortraits(new IconObtainedCallback()
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					for (int i = 0; i < characters.get(keyID).size(); ++i)
					{
						if (i < 3) portraits[i].setImageBitmap(bitmaps.get(charIDs[i]));
					}
				}
			}, charIDs);
			
			return convertView;
		}
	}
}
