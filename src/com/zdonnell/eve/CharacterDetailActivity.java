package com.zdonnell.eve;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.character.detail.AttributesFragment;
import com.zdonnell.eve.character.detail.InventoryListFragment;
import com.zdonnell.eve.character.detail.InventorySort;
import com.zdonnell.eve.character.detail.ParentAssetsFragment;
import com.zdonnell.eve.character.detail.SkillQueueFragment;
import com.zdonnell.eve.character.detail.SkillsFragment;
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
    
    private int searchOpenedAtLevel;

	private APICharacter assembledChar;
	
	private CharacterDetailActivity activity;
	
	public SearchView searchView;
		
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
          
    	
    	String[] characterInfo = getIntent().getExtras().getStringArray("character");
    	assembledChar = new APICharacter(new APICredentials(Integer.valueOf(characterInfo[1]), characterInfo[2]), Integer.valueOf(characterInfo[0]), getBaseContext());
    
        final ActionBar actionBar = getActionBar();
        activity = this;
        
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
        
  		setSlidingActionBarEnabled(true);

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
    	
    	private SkillsFragment skillsFragment;
    	public SkillsFragment skillsFragment() { return skillsFragment; }
    	    	
        public SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int i) 
        {
        	Fragment fragment;
        	
        	Bundle characterDetails = new Bundle();
        	characterDetails.putInt("keyID", assembledChar.getCredentials().keyID);
        	characterDetails.putString("vCode", assembledChar.getCredentials().verificationCode);
        	characterDetails.putInt("characterID", assembledChar.id());
        	
        	switch (i)
        	{
        	case CharacterSheetFragment.SKILLS:
        		fragment = skillsFragment = new SkillsFragment();
        		break;
        	case CharacterSheetFragment.SKILL_QUEUE:
        		fragment = new SkillQueueFragment();
        		break;
        	case CharacterSheetFragment.ATTRIBUTES:
        		fragment = new AttributesFragment(assembledChar);
        		break;
        	case CharacterSheetFragment.WALLET:
            	fragment = new WalletFragment(assembledChar);
        		break;
        	case CharacterSheetFragment.ASSETS:
            	fragment = assetsFragment = new ParentAssetsFragment();
        		break;
        	default:
        		fragment = new AttributesFragment(assembledChar);
        		break;
        	}
        	     
        	fragment.setArguments(characterDetails);

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
    public boolean dispatchKeyEvent(KeyEvent event)
    {
    	int keyCode = event.getKeyCode();
    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
        {    		
    		if (mViewPager.getCurrentItem() == CharacterSheetFragment.ASSETS)
    		{
    			if (searchOpenedAtLevel < mSectionsPagerAdapter.assetsFragment().parentStack.size() && !searchView.isIconified())
	    		{    				
    				mSectionsPagerAdapter.assetsFragment().backKeyPressed();
    				return true;
	    		}
    		}
        }
    	
		return super.dispatchKeyEvent(event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater menuInflater = getMenuInflater();        	
        
    	switch (getActionBar().getSelectedNavigationIndex())
        {
        case CharacterSheetFragment.ASSETS:
        	menuInflater.inflate(R.menu.char_detail_assetsasset_actionbar_items, menu);
        		
        	searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView(); 
        	MenuItem searchViewMenuItem = menu.getItem(0);
        	searchView.setOnQueryTextListener(new SearchQueryUpdatedListener());
        	searchViewMenuItem.setOnActionExpandListener(new OnActionExpandListener(){

				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) 
				{
					if (getActionBar().getSelectedNavigationIndex() == CharacterSheetFragment.ASSETS)
		            {
		        		ParentAssetsFragment assetsFragment = mSectionsPagerAdapter.assetsFragment();
		        		assetsFragment.updateSearchFilter(null);
		            }
					return true;
				}

				@Override
				public boolean onMenuItemActionExpand(MenuItem item) 
				{
					searchOpenedAtLevel = mSectionsPagerAdapter.assetsFragment().parentStack.size();
					return true;
				}
        		
        	});
        	            	
        	searchView.setOnKeyListener(new OnKeyListener() 
        	{
				@Override
				public boolean onKey(View view, int keyCode, KeyEvent arg2) 
				{									
					if (keyCode == KeyEvent.KEYCODE_BACK)
			        {						
						if (searchOpenedAtLevel < mSectionsPagerAdapter.assetsFragment().parentStack.size())
			    		{
			    			return false;
			    		}
			    		else return true;
			        }
					return true;
				}
        		
        	});
        	        	
        	break;
        case CharacterSheetFragment.SKILLS:
        	menuInflater.inflate(R.menu.char_detail_skills_actionbar_items, menu);
        	break;
        }
    	
    	return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item) {
    	
    	int curTab = getActionBar().getSelectedNavigationIndex();
    	
	    switch (item.getItemId())
	    {
	    case R.id.skill_list:
	    	new SkillList().show(getSupportFragmentManager(), "Skill List Dialog");
	    	break;
	    case R.id.sort_by:
	    	new SortByDialog().show(getSupportFragmentManager(), "Sort By Dialog");
	    	break;
	    case R.id.layout_style:
	        new LayoutDialog().show(getSupportFragmentManager(), "Layout Type Dialog");
	        break;
	    }
	    return true;
    }
    
    
    @SuppressLint("ValidFragment")
	private class SkillList extends DialogFragment
    {
		@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) 
    	{
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    builder.setTitle("Show")
    	           .setItems(SkillsFragment.skillOptions, new DialogInterface.OnClickListener() 
		           {
		               public void onClick(DialogInterface dialog, int which) 
		               {
		            	   if (getActionBar().getSelectedNavigationIndex() == CharacterSheetFragment.SKILLS)
			               {
			               		mSectionsPagerAdapter.skillsFragment().updateSkillDisplay(which);
			               }
		               }
		           }
    	   );
    	    
    	    return builder.create();
    	}
    }
    
    @SuppressLint("ValidFragment")
	private class SortByDialog extends DialogFragment
    {
		@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) 
    	{
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    builder.setTitle("Sort By")
    	           .setItems(InventorySort.sortNames, new DialogInterface.OnClickListener() 
		           {
		               public void onClick(DialogInterface dialog, int which) 
		               {
		            	   if (getActionBar().getSelectedNavigationIndex() == CharacterSheetFragment.ASSETS)
			               {
			               		ParentAssetsFragment assetsFragment = mSectionsPagerAdapter.assetsFragment();
			               		assetsFragment.updateSort(which);
			               }
		               }
		           }
    	   );
    	    
    	    return builder.create();
    	}
    }
    
	@SuppressLint("ValidFragment")
    private class LayoutDialog extends DialogFragment
    {
    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) 
    	{
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    builder.setTitle("Layout Style")
    	           .setItems(InventoryListFragment.layoutTypes, new DialogInterface.OnClickListener() 
		           {
		               public void onClick(DialogInterface dialog, int which) 
		               {
		            	   if (getActionBar().getSelectedNavigationIndex() == CharacterSheetFragment.ASSETS)
			               {
			               		ParentAssetsFragment assetsFragment = mSectionsPagerAdapter.assetsFragment();
			               		assetsFragment.updateLayoutStyle(which);
			               }
		               }
		           }
    	   );
    	    
    	    return builder.create();
    	}
    }
    
    private class SearchQueryUpdatedListener implements OnQueryTextListener
    {
		@Override
		public boolean onQueryTextChange(String searchString) 
		{			
			ifStillOnAssetsSearch(searchString);
			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String searchString) 
		{
			ifStillOnAssetsSearch(searchString);
			return false;
		}
		
		private void ifStillOnAssetsSearch(String searchString)
		{
			if (getActionBar().getSelectedNavigationIndex() == CharacterSheetFragment.ASSETS)
            {
        		ParentAssetsFragment assetsFragment = mSectionsPagerAdapter.assetsFragment();
        		assetsFragment.updateSearchFilter(searchString);
            }
		}
    }
}
