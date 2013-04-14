package com.zdonnell.eve;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.account.Account;
import com.zdonnell.eve.api.account.EveCharacter;

public class AddAPIDialog extends DialogFragment 
{
	APICredentials[] keys;
	
	SparseArray<ArrayList<EveCharacter>> characters = new SparseArray<ArrayList<EveCharacter>>();
	
	CharacterDB charDB;
	
	boolean refreshRequired = false, passedKey = false;
	
	int passedKeyID;
	
	String passedVCode;
	
	boolean[] loadCharAsEnabled = new boolean[3];
	
	EveCharacter[] loadedCharacters = new EveCharacter[3];
	
	private Button getCharsButton, addCharsButton;
	
	APICredentials loadedCredentials;
	
	public AddAPIDialog()
	{
		
	}
	
	public AddAPIDialog setKey(int keyID, String vCode)
	{
		passedKeyID = keyID;
		passedVCode = vCode;
		
		passedKey = true;
		
		return this;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View root = inflater.inflate(R.layout.characters_add_characters, null);
		final LinearLayout dynamicContentArea = (LinearLayout) root.findViewById(R.id.characters_add_characters_dynamic_content);
		
		getCharsButton = (Button) root.findViewById(R.id.characters_add_characters_get_button);
		addCharsButton = (Button) root.findViewById(R.id.characters_add_characters_add_button);
		
		final EditText keyIDField = (EditText) root.findViewById(R.id.characters_add_characters_api_field);
		final EditText vCodeField = (EditText) root.findViewById(R.id.characters_add_characters_vcode_field);
		
		getCharsButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				passedKeyID = Integer.parseInt(keyIDField.getText().toString());
				passedVCode = vCodeField.getText().toString();
				
				keyIDField.setEnabled(false);
				vCodeField.setEnabled(false);
				
				getCharsButton.setEnabled(false);
				
				loadCharacters(dynamicContentArea);
			}
		});
		
		addCharsButton.setOnClickListener(new View.OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{
				saveCharacters();
				((CharactersActivity) getActivity()).refreshCharactersList();
				AddAPIDialog.this.dismiss();
			}
		});
		
		charDB = new CharacterDB(getActivity());
		
		if (passedKey)
		{	
			keyIDField.setText(String.valueOf(passedKeyID));
			vCodeField.setText(passedVCode);
			
			loadCharacters(dynamicContentArea);
		}
		
		builder.setView(root).setTitle("Add API Key");		
				
	    return builder.create();
	}
	
	private void loadCharacters(final LinearLayout dynamicContentArea)
	{
		dynamicContentArea.removeAllViews();
		
		loadCharAsEnabled[0] = loadCharAsEnabled[1] = loadCharAsEnabled[2] = true;
		
		final Account newCreds = new Account(passedKeyID, passedVCode, getActivity());
		loadedCredentials = new APICredentials(passedKeyID, passedVCode);
		
		newCreds.characters(new APICallback<ArrayList<EveCharacter>>() 
		{
			@Override
			public void onUpdate(ArrayList<EveCharacter> updatedData) 
			{				
				int charIndex = 0;
				for (EveCharacter character : updatedData) 
				{
					getCharsButton.setVisibility(View.GONE);
					addCharsButton.setVisibility(View.VISIBLE);
					
					final int finalCharIndex = charIndex;
					loadedCharacters[charIndex] = character;
					
					LinearLayout characterTile = (LinearLayout) View.inflate(getActivity(), R.layout.characters_add_characters_character_tile, null);
					dynamicContentArea.addView(characterTile);
					
					final ImageView characterIcon = (ImageView) characterTile.findViewById(R.id.characters_add_characters_character_tile_image);
					final TextView characterName = (TextView) characterTile.findViewById(R.id.characters_add_characters_character_tile_name);
					
					characterName.setText(character.name);
					
					ImageService.getInstance(getActivity()).getPortraits(new IconObtainedCallback() 
					{
						@Override
						public void iconsObtained(SparseArray<Bitmap> bitmaps) 
						{
							characterIcon.setImageBitmap(bitmaps.valueAt(0));
						}						
					}, true, character.charID);
					
					characterTile.setOnClickListener(new View.OnClickListener() 
					{
						@Override
						public void onClick(View arg0) 
						{
							loadCharAsEnabled[finalCharIndex] = !loadCharAsEnabled[finalCharIndex];
							characterIcon.setAlpha(loadCharAsEnabled[finalCharIndex] ? 1f : 0.25f);
							characterName.setAlpha(loadCharAsEnabled[finalCharIndex] ? 1f : 0.25f);
						}
					});
					
					++charIndex;
				}
			}
		});
	}
	
	public void saveCharacters()
	{
		for (int i = 0; i < 3; i++)
		{
			if (loadedCharacters[i] != null) charDB.addCharacter(loadedCharacters[i], loadedCredentials, loadCharAsEnabled[i]);
		}
	}
}