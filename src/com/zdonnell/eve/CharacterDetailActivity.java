package com.zdonnell.eve;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.character.detail.AttributesFragment;
import com.zdonnell.eve.character.detail.ParentAssetsFragment;
import com.zdonnell.eve.character.detail.SkillQueueFragment;
import com.zdonnell.eve.character.detail.WalletFragment;

public class CharacterDetailActivity extends BaseActivity implements ActionBar.TabListener {

    public CharacterDetailActivity(int titleRes) {
		super(titleRes);
		// TODO Auto-generated constructor stub
	}
    
    public CharacterDetailActivity() {
		super(R.string.app_name);
		// TODO Auto-generated constructor stub
	}

	private APICharacter assembledChar;
	
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	  super.onCreate(savedInstanceState);
          setContentView(R.layout.character_detail);
          
  		setSlidingActionBarEnabled(true);
    	
    	String[] characterInfo = getIntent().getExtras().getStringArray("character");
    	assembledChar = new APICharacter(new APICredentials(Integer.valueOf(characterInfo[1]), characterInfo[2]), Integer.valueOf(characterInfo[0]), getBaseContext());
    
        final ActionBar actionBar = getActionBar();
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(new CharacterDB(getBaseContext()).getCharacterName(Integer.valueOf(characterInfo[0])));
                
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

    	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);    	
        
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
        	View tabView = inflater.inflate(R.layout.char_detail_tab_view, mViewPager, false);
        	((TextView) tabView.findViewById(R.id.char_detail_tab_view_text)).setText(mSectionsPagerAdapter.getPageTitle(i));
        	((ImageView) tabView.findViewById(R.id.char_detail_tab_view_icon)).setImageResource(CharacterSheetFragment.sheetItemImageIDs[i]);
        	
            actionBar.addTab(
                    actionBar.newTab()
                            .setCustomView(tabView)
                            .setTabListener(this));
        }
        
        
        mViewPager.setCurrentItem(getIntent().getExtras().getInt("position"));
    }
	
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        invalidateOptionsMenu();
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    
	/**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    	
    	private static final int COUNT = 5;
    	
    	private ParentAssetsFragment assetsFragment;
    	
    	public ParentAssetsFragment assetsFragment() { return assetsFragment; }
    	    	
        public SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int i) 
        {
        	Fragment fragment;
        	
        	switch (i)
        	{
        	case CharacterSheetFragment.SKILLS:
        		fragment = new AttributesFragment(assembledChar);
        		break;
        	case CharacterSheetFragment.SKILL_QUEUE:
        		fragment = new SkillQueueFragment(assembledChar);
        		break;
        	case CharacterSheetFragment.ATTRIBUTES:
        		fragment = new AttributesFragment(assembledChar);
        		break;
        	case CharacterSheetFragment.WALLET:
            	fragment = new WalletFragment(assembledChar);
        		break;
        	case CharacterSheetFragment.ASSETS:
            	fragment = assetsFragment = new ParentAssetsFragment(assembledChar);
        		break;
        	default:
        		fragment = new AttributesFragment(assembledChar);
        		break;
        	}
        	        	        	
			return fragment;
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) 
        {
            return CharacterSheetFragment.sheetItems[position].toUpperCase();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	boolean keyPressSwallowed = false;
    	
    	if (keyCode == KeyEvent.KEYCODE_BACK)
        {
    		if (mViewPager.getCurrentItem() == CharacterSheetFragment.ASSETS)
    		{
    			keyPressSwallowed = mSectionsPagerAdapter.assetsFragment().backKeyPressed();
    		}

        }
    	
    	if (!keyPressSwallowed) return super.onKeyDown(keyCode, event);
		else return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	
    	return true;
    }
}
