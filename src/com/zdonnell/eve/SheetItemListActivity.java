package com.zdonnell.eve;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;

public class SheetItemListActivity extends FragmentActivity
        implements SheetItemListFragment.Callbacks {

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
        
        ((SheetItemListFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setCharacter(assembledChar);
        
        if (findViewById(R.id.sheetitem_detail_container) != null) 
        {
            mTwoPane = true;
            ((SheetItemListFragment) getSupportFragmentManager().findFragmentById(R.id.sheetitem_list)).setActivateOnItemClick(true);
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
    public void onItemSelected(String id) {
        if (mTwoPane) {
            
            Fragment fragment = null;
            
            switch (Integer.valueOf(id))
            {
            case SheetItemListFragment.ASSETS:
            	break;
            case SheetItemListFragment.ATTRIBUTES:
            	break;
            case SheetItemListFragment.SKILL_QUEUE:
            	fragment = new SkillQueueDetailFragment(assembledChar);
            	break;
            case SheetItemListFragment.SKILLS:
            	break;
            case SheetItemListFragment.WALLET:
            	break;
            default:
            	fragment = new SkillQueueDetailFragment(assembledChar);
            	break;
            }
            
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sheetitem_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, SheetItemDetailActivity.class);
            detailIntent.putExtra(SkillQueueDetailFragment.ARG_ITEM_ID, id);
            detailIntent.putExtra("character", characterInfo);
            startActivity(detailIntent);
        }
    }
}
