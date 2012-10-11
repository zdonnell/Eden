package com.zdonnell.eve;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.characterdetail.SkillQueueFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class SheetItemDetailActivity extends FragmentActivity {

    private APICharacter assembledChar;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheetitem_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) 
        {
            Fragment fragment = null;
            String id = getIntent().getStringExtra(SkillQueueFragment.ARG_ITEM_ID); 
            String[] characterInfo = getIntent().getStringArrayExtra("character"); 
            
            assembledChar = new APICharacter(new APICredentials(Integer.valueOf(characterInfo[1]), characterInfo[2]), Integer.valueOf(characterInfo[0]), getBaseContext());
            
            switch (Integer.valueOf(id) - 1)
            {
            case SheetItemListFragment.ASSETS:
            	break;
            case SheetItemListFragment.ATTRIBUTES:
            	break;
            case SheetItemListFragment.SKILL_QUEUE:
            	fragment = new SkillQueueFragment(assembledChar);
            	break;
            case SheetItemListFragment.SKILLS:
            	break;
            case SheetItemListFragment.WALLET:
            	break;
            default:
            	fragment = new SkillQueueFragment(assembledChar);
            	break;
            }
            
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sheetitem_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, SheetItemListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
