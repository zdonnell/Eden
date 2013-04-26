package com.zdonnell.eve;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

public class APIKeysActivity extends BaseActivity {

	/**
	 * A reference to the current {@link Fragment} loaded in the activity
	 * 
	 * @see {@link #refresh()}
	 */
	private APIKeysFragment currentFragment;
	
	/**
	 * reference to the default shared preferences, so the {@link AddKeyInfoDialog} display setting can be read and set.
	 */
	private SharedPreferences prefs;
	
	public APIKeysActivity(int titleRes)
	{
		super(titleRes);
	}
	
	public APIKeysActivity()
	{
		super(R.string.api_keys);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		// Load the fragment into the activity
		setContentView(R.layout.content_frame);
		currentFragment = new APIKeysFragment();
		
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, currentFragment)
		.commit();
		
		handleRecievedAPIKey();
		
		setSlidingActionBarEnabled(true);
	}
	
	/**
	 * Checks to see if the incoming Intent has provided a keyID/vCode combo
	 * (i.e.) a user clicked a link in the form of eve://api.eveonline.com/?apiKey=KEYID&vCode=VCODE
	 * <br><br>
	 * If they have it will load an {@link AddAPIDialog} and pass it the key Info
	 */
	private void handleRecievedAPIKey()
	{
		Intent receivedIntent = getIntent();
		Uri data = receivedIntent.getData();
		if (data != null)
		{
			int keyID = Integer.valueOf(data.getQueryParameter("keyID"));
			String vCode = data.getQueryParameter("vCode");
			
	    	new AddAPIDialog().setKey(keyID, vCode).show(getSupportFragmentManager(), "Add Key Dialog");
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater menuInflater = getMenuInflater();        	
        menuInflater.inflate(R.menu.api_keys, menu);
        
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		case R.id.manual_key_entry:
	    	new AddAPIDialog().show(getSupportFragmentManager(), "Add Key Dialog");
			break;
		/*  TODO reenable when CCP fixes "create predefined access mask key" link
		 	case R.id.create_new_key:
			Intent createNewKey = new Intent(Intent.ACTION_VIEW, Uri.parse("http://community.eveonline.com/support/api-key/CreatePredefined?accessMask=" + R.string.default_access_mask));
		    startActivity(createNewKey);			
		    break;*/
		case R.id.add_existing_key:
			boolean showInfoDialog = prefs.getBoolean("show_add_api_key_info", true);
			
			if (showInfoDialog)
			{
				new AddKeyInfoDialog().show(getSupportFragmentManager(), "Add Key Info Dialog");	
			}
			else
			{
				Intent addExistingKey = new Intent(Intent.ACTION_VIEW, Uri.parse("http://community.eveonline.com/support/api-key"));
         	   	startActivity(addExistingKey);
			}
		    break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void refresh() 
	{
		currentFragment.refresh();
	}
	
	@SuppressLint("ValidFragment")
	/**
	 * Dialog Builder to create a dialog explaining to users how they can install an API Key
	 * once they are taken off to the EVE Online website
	 * 
	 * @author zachd
	 *
	 */
	private class AddKeyInfoDialog extends DialogFragment
    {
		@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) 
    	{
			View checkBoxView = View.inflate(getActivity(), R.layout.add_api_key_info_checkbox_layout, null);
			final CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.add_api_key_info_checkbox);
						
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    builder.setTitle("Add Existing API Key")
    	           .setMessage("You will be redirected to the EVE Online website.  Once logged in you may add a key to Eden by pressing the 'Install' Button next to any key you have created.")
    	           .setCancelable(false)
    	           .setView(checkBoxView)
    	           .setPositiveButton("Proceed", new DialogInterface.OnClickListener() 
    	           {
    	               public void onClick(DialogInterface dialog, int id) 
    	               {
    	            	   Intent addExistingKey = new Intent(Intent.ACTION_VIEW, Uri.parse("http://community.eveonline.com/support/api-key"));
    	            	   startActivity(addExistingKey);  
    	            	   
    	            	   prefs.edit().putBoolean("show_add_api_key_info", !checkBox.isChecked()).commit();
    	       		   }
    	           })
    	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
    	           {
    	               public void onClick(DialogInterface dialog, int id) 
    	               {
    	                    dialog.cancel();
    	               }
    	           });
    	           
    	    return builder.create();
    	}
    }
	
	public void showDeleteKeyDialog(int keyID)
	{
		new ConfirmDeleteKeyDialog(keyID).show(getSupportFragmentManager(), "Delete Key Dialog");	
	}
	
	@SuppressLint("ValidFragment")
	/**
	 * Dialog Builder to create a dialog warning users what happens when they delete a key
	 * 
	 * @author zachd
	 *
	 */
	private class ConfirmDeleteKeyDialog extends DialogFragment
    {
		private int keyIDToDelete;
		
		public ConfirmDeleteKeyDialog(int keyID)
		{
			super();
			this.keyIDToDelete = keyID;
		}
		
		@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) 
    	{						
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    builder.setTitle("Delete API Key")
    	           .setMessage("All data associated with this key will be removed.  Are you sure you would like to delete this key?")
    	           .setCancelable(false)
    	           .setPositiveButton("Delete", new DialogInterface.OnClickListener() 
    	           {
    	               public void onClick(DialogInterface dialog, int id) 
    	               {
    	            	   new CharacterDB(getActivity()).deleteCharactersByKeyID(keyIDToDelete);
    	            	   currentFragment.refresh();
    	       		   }
    	           })
    	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
    	           {
    	               public void onClick(DialogInterface dialog, int id) 
    	               {
    	                    dialog.cancel();
    	               }
    	           });
    	           
    	    return builder.create();
    	}
    }
}
