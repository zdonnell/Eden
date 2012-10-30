package com.zdonnell.eve;

import android.os.Bundle;


public class CharactersActivity extends BaseActivity {

	public CharactersActivity() {
		super(R.string.character_grid_activity_title);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set the Above View
		setContentView(R.layout.content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, new CharactersFragment())
		.commit();
		
		setSlidingActionBarEnabled(true);
	}
	
}
