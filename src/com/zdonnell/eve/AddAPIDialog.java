package com.zdonnell.eve;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zdonnell.androideveapi.account.characters.CharactersResponse;
import com.zdonnell.androideveapi.account.characters.EveCharacter;
import com.zdonnell.androideveapi.core.ApiAuth;
import com.zdonnell.androideveapi.core.ApiAuthorization;
import com.zdonnell.androideveapi.exception.ApiException;
import com.zdonnell.androideveapi.link.APIExceptionCallback;
import com.zdonnell.androideveapi.link.account.Account;
import com.zdonnell.eve.helpers.ImageURL;

public class AddAPIDialog extends DialogFragment
{
	/**
	 * Manages a list of TextWatchers that, when all not empty will enable a button
	 */
	private static class EnableButtonMultiWatcher {
		/**
		 * Watches for empty text 
		 */
		private class EmptyTextWatcher implements TextWatcher
		{
			// Assume invalid at start
			private boolean valid = false;
			
			public void afterTextChanged(Editable editable)
			{
				// Text is valid when the string isn't empty
				valid = (editable.length() != 0);
				
				EnableButtonMultiWatcher.this.check();
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				// Do nothing
			}

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// Do nothing
			}
			
			public boolean isValid()
			{
				return valid;
			}
			
			public void setValid(boolean valid)
			{
				this.valid = valid;
			}
		}
		
		private ArrayList<EmptyTextWatcher> watchers = new ArrayList<EmptyTextWatcher>();
		private Button button;
		
		public EnableButtonMultiWatcher(Button button)
		{
			this.button = button;
			
			// First check
			check();
		}
		
		public TextWatcher createWatcher()
		{
			EmptyTextWatcher newWatcher = new EmptyTextWatcher();
			watchers.add(newWatcher);
			
			return newWatcher;
		}
		
		public void addTextView(TextView textView)
		{
			EmptyTextWatcher watcher = (EmptyTextWatcher)createWatcher();
			
			textView.addTextChangedListener(watcher);
			watcher.setValid(textView.getText().length() != 0);
			
			check();
		}
		
		public void check()
		{
			// Assume valid until one is invalid
			boolean valid = true;
			for(EmptyTextWatcher watcher : watchers)
				valid &= watcher.isValid();
			
			button.setEnabled(valid);
		}
	}
		
	SparseArray<ArrayList<EveCharacter>> characters = new SparseArray<ArrayList<EveCharacter>>();
		
	boolean refreshRequired = false, passedKey = false;
	
	int keyID;
	
	String vCode;
	
	boolean[] loadCharAsEnabled = new boolean[3];
	
	EveCharacter[] loadedCharacters = new EveCharacter[3];
	
	private Button getCharsButton, addCharsButton;
	
	private ApiAuth<?> apiAuth;
	
	public AddAPIDialog setKey(int keyID, String vCode)
	{
		this.keyID = keyID;
		this.vCode = vCode;
		
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

		final EnableButtonMultiWatcher getCharsWatcher = new EnableButtonMultiWatcher(getCharsButton);
		final EditText keyIDField = (EditText) root.findViewById(R.id.characters_add_characters_api_field);
		final EditText vCodeField = (EditText) root.findViewById(R.id.characters_add_characters_vcode_field);
		
		getCharsWatcher.addTextView(keyIDField);
		getCharsWatcher.addTextView(vCodeField);
		
		getCharsButton.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v)
			{
				keyID = Integer.parseInt(keyIDField.getText().toString());
				vCode = vCodeField.getText().toString();
				
				keyIDField.setEnabled(false);
				vCodeField.setEnabled(false);
				
				getCharsButton.setEnabled(false);
				
				loadCharacters(dynamicContentArea);
			}
		});
		
		addCharsButton.setOnClickListener(new View.OnClickListener() 
		{			
			public void onClick(View v) 
			{
				saveCharacters();
				((APIKeysActivity) getActivity()).refresh();
				AddAPIDialog.this.dismiss();
			}
		});
				
		if (passedKey)
		{	
			keyIDField.setVisibility(View.GONE);
			vCodeField.setVisibility(View.GONE);
						
			loadCharacters(dynamicContentArea);
		}
		
		builder.setView(root).setTitle("Add API Key");		
				
	    return builder.create();
	}
	
	private void loadCharacters(final LinearLayout dynamicContentArea)
	{
		dynamicContentArea.removeAllViews();
		
		// All characters are set to "enabled" status
		loadCharAsEnabled[0] = loadCharAsEnabled[1] = loadCharAsEnabled[2] = true;
		
		apiAuth = new ApiAuthorization(keyID, vCode);
		new Account(apiAuth, getActivity()).getCharacters(new APIExceptionCallback<CharactersResponse>(null) 
		{
			@Override
			public void onUpdate(CharactersResponse response) 
			{
				int charIndex = 0;
				for (EveCharacter character : response.getAll()) 
				{
					getCharsButton.setVisibility(View.GONE);
					addCharsButton.setVisibility(View.VISIBLE);
					
					final int finalCharIndex = charIndex;
					loadedCharacters[charIndex] = character;
					
					LinearLayout characterTile = (LinearLayout) View.inflate(getActivity(), R.layout.characters_add_characters_character_tile, null);
					dynamicContentArea.addView(characterTile);
					
					final ImageView characterIcon = (ImageView) characterTile.findViewById(R.id.characters_add_characters_character_tile_image);
					final TextView characterName = (TextView) characterTile.findViewById(R.id.characters_add_characters_character_tile_name);
					
					characterName.setText(character.getName());
					
					ImageLoader.getInstance().displayImage(ImageURL.forChar((int) character.getCharacterID()), characterIcon);
					
					characterTile.setOnClickListener(new View.OnClickListener() 
					{
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

			@Override
			public void onError(CharactersResponse response, ApiException exception) 
			{
				// TODO add UI indication that response failed
			}
		});
	}
	
	/**
	 * Saves all characters to the characters database
	 */
	public void saveCharacters()
	{
		CharacterDB charDB = new CharacterDB(getActivity());
		
		for (int i = 0; i < 3; i++)
		{
			if (loadedCharacters[i] != null) charDB.addCharacter(loadedCharacters[i], apiAuth, loadCharAsEnabled[i]);
		}
	}
}