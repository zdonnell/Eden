package com.zdonnell.eve;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.staticdata.api.StationDatabase;
import com.zdonnell.eve.staticdata.api.StationInfo;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class CharactersActivity extends BaseActivity {

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
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new CharactersFragment())
		.commit();
		
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
    	
	    switch (item.getItemId())
	    {
	    case R.id.add_characters:
	    	new EditCharactersDialog().show(getSupportFragmentManager(), "Skill List Dialog");
	    	break;
	    case R.id.edit_characters:
	    	new EditCharactersDialog().show(getSupportFragmentManager(), "Skill List Dialog");
	    	break;
	    }
	    return true;
    }
	
	
}
