package com.zdonnell.eve;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.detail.attributes.AttributesFragment;
import com.zdonnell.eve.detail.skillqueue.SkillQueueFragment;

public class CharacterSheetActivity extends FragmentActivity
        implements CharacterSheetFragment.Callbacks {

    private boolean mTwoPane;
    
    private APICharacter assembledChar;
    
    private String[] characterInfo;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheetitem_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        characterInfo = getIntent().getExtras().getStringArray("character");
        assembledChar = new APICharacter(new APICredentials(Integer.valueOf(characterInfo[1]), characterInfo[2]), Integer.valueOf(characterInfo[0]), getBaseContext());
        
        getActionBar().setTitle(new CharacterDB(this).getCharacterName(assembledChar.id()));
        
        ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setCharacter(assembledChar);
        
        if (findViewById(R.id.sheetitem_detail_container) != null) 
        {
            mTwoPane = true;
            ((CharacterSheetFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setActivateOnItemClick(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            
            Fragment fragment = null;
            
            switch (id)
            {
            case CharacterSheetFragment.ASSETS:
            	break;
            case CharacterSheetFragment.ATTRIBUTES:
            	fragment = new AttributesFragment(assembledChar);
            	break;
            case CharacterSheetFragment.SKILL_QUEUE:
            	fragment = new SkillQueueFragment(assembledChar);
            	break;
            case CharacterSheetFragment.SKILLS:
            	break;
            case CharacterSheetFragment.WALLET:
            	break;
            default:
            	fragment = new SkillQueueFragment(assembledChar);
            	break;
            }
            
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
}
