package com.zdonnell.eve;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CharactersActivity extends BaseActivity {

	private CharactersFragment currentCharactersFragment;
	
	public CharactersActivity() 
	{
		super(R.string.character_grid_activity_title);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		/**
		 * Load the fragment into the activity
		 */
		setContentView(R.layout.content_frame);
		
		currentCharactersFragment = new CharactersFragment();
		
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, currentCharactersFragment)
		.commit();
		
		Intent receivedIntent = getIntent();
		Uri data = receivedIntent.getData();
		
		if (data != null)
		{
			int keyID = Integer.valueOf(data.getQueryParameter("keyID"));
			String vCode = data.getQueryParameter("vCode");
			
	    	new AddAPIDialog().setKey(keyID, vCode).show(getSupportFragmentManager(), "Skill List Dialog");
		}
		
		setSlidingActionBarEnabled(true);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater menuInflater = getMenuInflater();        	
        menuInflater.inflate(R.menu.characters_actionbar_items, menu);
        
        return true;
    }
	
	public boolean onOptionsItemSelected (MenuItem item) {
    	
		super.onOptionsItemSelected(item);
		
	    switch (item.getItemId())
	    {
	    case R.id.sort_by:
	    	new SortByDialog().show(getSupportFragmentManager(), "Sort By Dialog");
	    	break;
	    }
	    return true;
    }
	
	public void refreshCharactersList()
	{
		currentCharactersFragment.refreshChars();
	}
	
	 @SuppressLint("ValidFragment")
	private class SortByDialog extends DialogFragment
    {
		@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) 
    	{
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    builder.setTitle("Sort By")
    	           .setItems(CharacterSort.sortNames, new DialogInterface.OnClickListener() 
		           {
		               public void onClick(DialogInterface dialog, int which) 
		               {
		            	   currentCharactersFragment.updateSort(which);
		               }
		           }
    	   );
    	    
    	    return builder.create();
    	}
    }

	@Override
	protected void refresh() {
		this.currentCharactersFragment.refreshChars();
		
	}
}
