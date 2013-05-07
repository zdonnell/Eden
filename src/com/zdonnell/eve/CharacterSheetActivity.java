package com.zdonnell.eve;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.character.detail.AttributesFragment;
import com.zdonnell.eve.character.detail.ParentAssetsFragment;
import com.zdonnell.eve.character.detail.WalletFragment;
import com.zdonnell.eve.character.detail.queue.SkillQueueFragment;
import com.zdonnell.eve.character.detail.skills.SkillsFragment;

public class CharacterSheetActivity extends BaseActivity
        implements CharacterSheetFragment.Callbacks {

	public CharacterSheetActivity(int titleRes) {
		super(titleRes);
		// TODO Auto-generated constructor stub
	}
	
	public CharacterSheetActivity() {
		super(R.string.app_name);

	}
	
	private CharacterSheetActivity activity;

	private boolean mTwoPane;
    
    private APICharacter assembledChar;
    
    private String[] characterInfo;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_sheetitem_list);
        
		setSlidingActionBarEnabled(true);
		
		activity = this;

        characterInfo = getIntent().getExtras().getStringArray("character");
        assembledChar = new APICharacter(new APICredentials(Integer.valueOf(characterInfo[1]), characterInfo[2]), Integer.valueOf(characterInfo[0]), getBaseContext());
        
        getActionBar().setTitle(new CharacterDB(this).getCharacterName(assembledChar.id()));
                
        ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setCharacter(assembledChar, Integer.valueOf(characterInfo[3]));
                
        /*if (findViewById(R.id.sheetitem_detail_container) != null) 
        {
            mTwoPane = true;
            ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setActivateOnItemClick(true);
        }*/
        
    }

    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            
        	Fragment fragment;
        	
        	Bundle characterDetails = new Bundle();
        	characterDetails.putInt("keyID", assembledChar.getCredentials().keyID);
        	characterDetails.putString("vCode", assembledChar.getCredentials().verificationCode);
        	characterDetails.putInt("characterID", assembledChar.id());
        	
        	switch (id)
        	{
        	case CharacterSheetFragment.SKILLS:
        		fragment = new SkillsFragment();
        		break;
        	case CharacterSheetFragment.SKILL_QUEUE:
        		fragment = new SkillQueueFragment();
        		break;
        	case CharacterSheetFragment.ATTRIBUTES:
        		fragment = new AttributesFragment();
        		break;
        	case CharacterSheetFragment.WALLET:
            	fragment = new WalletFragment();
        		break;
        	case CharacterSheetFragment.ASSETS:
        		fragment = new ParentAssetsFragment();
        		break;
        	default:
        		fragment = new AttributesFragment();
        		break;
        	}
        	
        	fragment.setArguments(characterDetails);
        	        	            
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sheetitem_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, CharacterDetailActivity.class);
            detailIntent.putExtra("position", id);
            detailIntent.putExtra("character", characterInfo);
            startActivity(detailIntent);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater menuInflater = getMenuInflater(); 
    	menuInflater.inflate(R.menu.character_sheet, menu);
    	
    	return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item) {
    	
    	super.onOptionsItemSelected(item);
    	
	    switch (item.getItemId())
	    {
	    /*case R.id.notification_toggle:
	    	ImageService.getInstance(this).getPortraits(new IconObtainedCallback()
	    	{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
			    	buildNotification(bitmaps.valueAt(0));
				}
	    	}, true, assembledChar.id());
	    	
	    	break;*/
	    }
		return true;
    }
    
    private void buildNotification(Bitmap charPortrait)
    {
    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_launcher)
    	        .setLargeIcon(charPortrait)
    	        .setContentTitle("My notification")
    	        .setOngoing(true)
    	        .setContentText("Hello World!");
    	// Creates an explicit intent for an Activity in your app
    	Intent resultIntent = new Intent(this, CharactersActivity.class);

    	// The stack builder object will contain an artificial back stack for the
    	// started Activity.
    	// This ensures that navigating backward from the Activity leads out of
    	// your application to the Home screen.
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	// Adds the back stack for the Intent (but not the Intent itself)
    	stackBuilder.addParentStack(CharactersActivity.class);
    	// Adds the Intent that starts the Activity to the top of the stack
    	stackBuilder.addNextIntent(resultIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(
    	            0,
    	            PendingIntent.FLAG_UPDATE_CURRENT
    	        );
    	mBuilder.setContentIntent(resultPendingIntent);
    	NotificationManager mNotificationManager =
    	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	// mId allows you to update the notification later on.
    	mNotificationManager.notify(0, mBuilder.build());
    }

	@Override
	protected void refresh() {
        ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setCharacter(assembledChar, Integer.valueOf(characterInfo[3]));

		
	}
}
