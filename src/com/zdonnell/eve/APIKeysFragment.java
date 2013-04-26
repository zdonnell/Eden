package com.zdonnell.eve;

import java.util.ArrayList;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.account.EveCharacter;
import com.zdonnell.eve.helpers.BasicOnTouchListener;

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

/**
 * Fragment that displays API Keys and linked Characters
 * 
 * @author zachd
 * 
 * @see {@link APIKeysActivity}
 *
 */
public class APIKeysFragment extends Fragment 
{
	/**
	 * List of all {@link APICredentials} 
	 */
	private ArrayList<APICredentials> apiCredsList;
	
	/**
	 * {@link SparseArray} of {@link ArrayList} of {@link EveCharacter}
	 * 
	 * The root SparseArray is indexed by API Key.  Getting an element in the SparseArray (i.e. characters.get(keyID))
	 * will return an ArrayList that should be used to store the individual EveCharacters belonging to the provided keyID
	 */
	private SparseArray<ArrayList<EveCharacter>> characters = new SparseArray<ArrayList<EveCharacter>>();
	
	/**
	 * reference to the character database from which the API Key and character data is aquired
	 */
	private CharacterDB charDB;
	
	/**
	 * reference to the ListView used to display the API Keys and related characters	
	 */
	private ListView apiKeyListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootLayoutView = inflater.inflate(R.layout.api_keys_fragment, null);
		apiKeyListView = (ListView) rootLayoutView.findViewById(R.id.characters_edit_characters_list);
		
		getData();
		apiKeyListView.setAdapter(new APIKeyListAdapter(getActivity(), R.layout.characters_edit_characters_list_item, apiCredsList, characters));
		
		return rootLayoutView;
	}

	/**
	 * Loads the data for the list of API Keys and stores information in global variables
	 * 
	 * @see {@link #characters}
	 * @see {@link #apiCredsList}
	 */
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
			
			if (!doesAPIKeyExistInList(apiKey, vCode, apiCredsList)) apiCredsList.add(new APICredentials(apiKey, vCode));

			if (characters.get(apiKey) == null) characters.put(apiKey, new ArrayList<EveCharacter>());
			characters.get(apiKey).add(new EveCharacter(charName, charID, null, 0, 0, null));
		}
		c.close();
	}
	
	/**
	 * Determines if the provided apiKey/vCode exist in the provided API Credentials list.
	 * 
	 * @see {@link #apiCredsList}
	 * 
	 * @param apiKey the API Key to check for
	 * @param vCode the Verification Code to check for
	 * @param apiCredsList the list to check against
	 * @return True if the key/vCode pair exists in the provided API Credentials list.
	 */
	private boolean doesAPIKeyExistInList(int apiKey, String vCode, ArrayList<APICredentials> apiCredsList)
	{
		for (APICredentials c : apiCredsList)
		{
			if (c.keyID == apiKey && c.verificationCode.equals(vCode)) return true;
		}
		
		return false;
	}
	
	/**
	 * Called if the list of API Keys has become outdated for any reason.  The information will be reqeusted
	 * from the {@link CharacterDB} again, and the {@link APIKeyListAdapter} will be updated with the new data.
	 */
	private void updateList()
	{				
		getData();
		
		APIKeyListAdapter currentAdapter = (APIKeyListAdapter) apiKeyListView.getAdapter();
		currentAdapter.clear();
		currentAdapter.addAll(apiCredsList);
		currentAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Publicly accessible method for refreshing data
	 */
	public void refresh()
	{
		updateList();
	}
	
	/**
	 * ArrayAdapter to use to bind character and API Key data to a ListView
	 * 
	 * @author zachd
	 *
	 */
	private class APIKeyListAdapter extends ArrayAdapter<APICredentials>
	{
		/**
		 * reference the layoutID to use for individual ListView rows
		 */
		int listItemLayoutID;
		
		/**
		 * {@link SparseArray} of {@link ArrayList} of {@link EveCharacter}
		 * 
		 * The root SparseArray is indexed by API Key.  Getting an element in the SparseArray (i.e. characters.get(keyID))
		 * will return an ArrayList that should be used to store the individual EveCharacters belonging to the provided keyID
		 */
		SparseArray<ArrayList<EveCharacter>> characters;
		
		/**
		 * Stores whether a given character is currently monitored or "on"
		 */
		SparseArray<Boolean> charOn = new SparseArray<Boolean>();
		
		/**
		 * Constructor
		 * 
		 * @param context
		 * @param textViewResourceId the layoutID to use for individual ListView rows
		 * @param keys the list of {@link APICredential} objects to build the list from
		 * @param characters Lists of Characters indexed by their keyID
		 * 
		 * @see {@link #characters}
		 */
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
			
			int keyID = getItem(position).keyID;
			
			TextView apiKeyText = (TextView) convertView.findViewById(R.id.characters_edit_characters_list_item_apikey);
			apiKeyText.setText(String.valueOf(getItem(position).keyID));
			
			ImageView deleteImage = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_delete_icon);
			setupDeleteIcon(deleteImage, keyID);
			
			final ImageView[] portraits = new ImageView[3];
			portraits[0] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait1);
			portraits[1] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait2);
			portraits[2] = (ImageView) convertView.findViewById(R.id.characters_edit_characters_list_item_portrait3);
			
			// This root view may be reused, so we need to set all 3 character portraits to invisible, and only turn back on
			// enough for this keyID
			for (int i = 0; i < 3; ++i) portraits[i].setVisibility(View.GONE);
			
			final Integer[] charIDs = new Integer[characters.get(keyID).size()];
			for (int i = 0; i < characters.get(keyID).size(); ++i)
			{
				charIDs[i] = characters.get(keyID).get(i).charID;
				charOn.put(charIDs[i], charDB.isCharEnabled(charIDs[i]));
								
				initializePortraitImageView(portraits[i], charIDs[i]);
			}	
			
			setCharacterPortraits(portraits, charIDs, keyID);
			
			return convertView;
		}
		
		/**
		 * Binds the delete icon for a row to the appropriate keyID
		 * 
		 * @param deleteIcon the ImageView to bind
		 * @param keyID the ID of the API Key that will be deleted when the specified deleteIcon is pressed
		 */
		private void setupDeleteIcon(ImageView deleteIcon, final int keyID)
		{
			deleteIcon.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					((APIKeysActivity) getActivity()).showDeleteKeyDialog(keyID);
				}
			});	
			
			deleteIcon.setOnTouchListener(new BasicOnTouchListener());
		}
		
		/**
		 * Sets the initial visibility and transparency for the portrait of the provided character
		 * 
		 * @param portrait the ImageView to bind to the characterID
		 * @param characterID the ID of the character to be displayed
		 */
		private void initializePortraitImageView(ImageView portrait, final Integer characterID)
		{
			portrait.setTag(characterID);
			portrait.setAlpha(charOn.get(characterID) ? 1f : 0.25f);
			
			portrait.setOnClickListener(new View.OnClickListener() 		
			{		
				@Override
				public void onClick(View v) 
				{	
					// toggle the boolean storing whether the char is enabled
					boolean charIsEnabled = !charOn.get(characterID);
					charOn.put(characterID, charIsEnabled);
					
					((ImageView) v).setAlpha(charIsEnabled ? 1f : 0.25f);
					charDB.setCharEnabled(characterID, charIsEnabled);
				}
			});
		}
		
		/**
		 * Sets the obtained character portraits for the given keyID
		 * 
		 * @param portraits An Array of length 3 that contains references to all 3 ImageView slots.
		 * @param charIDs An Array of Integers representing the character IDs to load
		 * @param keyID the ID of API Key that the provided characters belong to
		 */
		private void setCharacterPortraits(final ImageView[] portraits, final Integer[] charIDs, final int keyID)
		{
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
		}
	}
}
