package com.zdonnell.eve;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiAuthorization;
import com.zdonnell.eve.apilink.character.APICharacter;
import com.zdonnell.eve.character.detail.assets.ParentAssetsFragment;
import com.zdonnell.eve.character.detail.attributes.AttributesFragment;
import com.zdonnell.eve.character.detail.queue.SkillQueueFragment;
import com.zdonnell.eve.character.detail.skills.SkillsFragment;
import com.zdonnell.eve.character.detail.wallet.WalletFragment;

public class CharacterSheetActivity extends NavDrawerActivity implements CharacterSheetFragment.Callbacks 
{
	public CharacterSheetActivity(int titleRes) 
	{
		super(titleRes);
	}
	
	public CharacterSheetActivity() 
	{
		super(R.string.app_name);
	}
	
	private boolean mTwoPane;
            
    APICharacter assembledChar;
    
    private String[] characterInfo;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_sheetitem_list);
        		
		characterInfo = getIntent().getExtras().getStringArray("character");
        
        ApiAuth<?> apiAuth = new ApiAuthorization(Integer.valueOf(characterInfo[1]), Long.valueOf(characterInfo[0]), characterInfo[2]);
        assembledChar = new APICharacter(getBaseContext(), apiAuth);
        ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame)).setCharacter(assembledChar);
        
        getActionBar().setTitle(new CharacterDB(this).getCharacterName(assembledChar.getApiAuth().getCharacterID().intValue()));
        getActionBar().setSubtitle(new CharacterDB(this).getCorpName(assembledChar.getApiAuth().getCharacterID().intValue()));      
        
        /*if (findViewById(R.id.sheetitem_detail_container) != null) 
        {
            mTwoPane = true;
            ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setActivateOnItemClick(true);
        }*/
        
    }

    public void onItemSelected(int id) 
    {
        if (mTwoPane) 
        {
        	Fragment fragment;
        	
        	Bundle characterDetails = new Bundle();
        	characterDetails.putInt("keyID", assembledChar.getApiAuth().getKeyID());
        	characterDetails.putString("vCode", assembledChar.getApiAuth().getVCode());
        	characterDetails.putInt("characterID", assembledChar.getApiAuth().getCharacterID().intValue());
        	
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
        } 
        else 
        {
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
   
	@Override
	protected void refresh() 
	{
        ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame)).setCharacter(assembledChar);
	}
}
