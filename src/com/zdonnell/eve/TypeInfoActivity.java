package com.zdonnell.eve;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.staticdata.api.StationDatabase;
import com.zdonnell.eve.staticdata.api.StationInfo;

import android.os.Bundle;
import android.util.SparseArray;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;


public class TypeInfoActivity extends BaseActivity {

	public TypeInfoActivity() 
	{
		super(R.string.type_info);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		int typeID = getIntent().getIntExtra("typeID", 0);
		
		TypeInfoFragment fragment = new TypeInfoFragment();
		
		Bundle arguments = new Bundle();
		arguments.putInt("typeID", typeID);
		fragment.setArguments(arguments);
		
		/**
		 * Load the fragment into the activity
		 */
		setContentView(R.layout.content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.commit();
		
		
		setSlidingActionBarEnabled(true);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater menuInflater = getSupportMenuInflater(); 
    	menuInflater.inflate(R.menu.type_info, menu);
    	
    	return true;
    }

	@Override
	protected void refresh() 
	{
	
	}
	
}
