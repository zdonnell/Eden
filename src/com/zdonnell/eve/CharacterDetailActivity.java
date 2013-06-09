package com.zdonnell.eve;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.zdonnell.eve.character.detail.DetailFragment;
import com.zdonnell.eve.character.detail.InventorySort;
import com.zdonnell.eve.character.detail.assets.InventoryListFragment;
import com.zdonnell.eve.character.detail.assets.ParentAssetsFragment;
import com.zdonnell.eve.character.detail.attributes.AttributesFragment;
import com.zdonnell.eve.character.detail.mail.MailFragment;
import com.zdonnell.eve.character.detail.queue.SkillQueueFragment;
import com.zdonnell.eve.character.detail.skills.SkillsFragment;
import com.zdonnell.eve.character.detail.wallet.WalletFragment;

public class CharacterDetailActivity extends NavDrawerActivity implements ActionBar.TabListener {

    public CharacterDetailActivity(int titleRes) {
		super(titleRes);
		// TODO Auto-generated constructor stub
	}
    
    public CharacterDetailActivity() {
		super(R.string.app_name);
		// TODO Auto-generated constructor stub
	}
    
    private int searchOpenedAtLevel;
		
	public SearchView searchView;
	
	private String characterName;
		
	boolean hasLoaded = false;
		
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
    
    PagerTitleStrip mViewPagerTitleStrip;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.character_detail);
             	    
        final ActionBar actionBar = getActionBar();
        
    	String[] characterInfo = getIntent().getExtras().getStringArray("character");
        
        CharacterDB charDB = new CharacterDB(getBaseContext());
        characterName = charDB.getCharacterName(Integer.valueOf(characterInfo[0]));
        String corpName = charDB.getCorpName(Integer.valueOf(characterInfo[0]));
        
        actionBar.setTitle(characterName);
        actionBar.setSubtitle(corpName);
        
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.content_frame);
        mViewPagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title);
        mViewPagerTitleStrip.setNonPrimaryAlpha(0.3f);
        mViewPagerTitleStrip.setTextSpacing((int) (displayMetrics.widthPixels/4f));
        
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        // When swiping between different sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();

            }
        });
        
        mViewPager.setCurrentItem(getIntent().getExtras().getInt("position"));
    }
	
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    	
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        invalidateOptionsMenu();
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    	
    }
    
	/**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    	
    	private static final int COUNT = 6;
    	
    	private ParentAssetsFragment assetsFragment;
    	public ParentAssetsFragment assetsFragment() { return assetsFragment; }
    	
    	private MailFragment mailFragment;
    	public MailFragment mailFragment() { return mailFragment; }
    	
    	private SkillsFragment skillsFragment;
    	public SkillsFragment skillsFragment() { return skillsFragment; }
    	
    	private WalletFragment walletFragment;
    	public WalletFragment walletFragment() { return walletFragment; }
    	
    	private SkillQueueFragment skillQueueFragment;
    	public SkillQueueFragment skillQueueFragment() { return skillQueueFragment; }
    	
    	private AttributesFragment attributesFragment;
    	public AttributesFragment attributesFragment() { return attributesFragment; }
    	    	
        public SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int i) 
        {
        	Fragment fragment;
        	
        	String[] characterInfo = getIntent().getExtras().getStringArray("character");
        	
        	Bundle characterDetails = new Bundle();
        	characterDetails.putInt("keyID", Integer.valueOf(characterInfo[1]));
        	characterDetails.putString("vCode", characterInfo[2]);
        	characterDetails.putInt("characterID", Integer.valueOf(characterInfo[0]));
        	characterDetails.putString("characterName", characterName);
        	
        	switch (i)
        	{
        	case CharacterSheetFragment.MAIL:
        		fragment = mailFragment = new MailFragment();
        		break;
        	case CharacterSheetFragment.SKILLS:
        		fragment = skillsFragment = new SkillsFragment();
        		break;
        	case CharacterSheetFragment.SKILL_QUEUE:
        		fragment = skillQueueFragment = new SkillQueueFragment();
        		break;
        	case CharacterSheetFragment.ATTRIBUTES:
        		fragment = attributesFragment = new AttributesFragment();
        		break;
        	case CharacterSheetFragment.WALLET:
            	fragment = walletFragment = new WalletFragment();
        		break;
        	case CharacterSheetFragment.ASSETS:
            	fragment = assetsFragment = new ParentAssetsFragment();
        		break;
        	default:
        		fragment = new AttributesFragment();
        		break;
        	}
        	     
        	fragment.setArguments(characterDetails);
        	hasLoaded = true;
        	
			return fragment;
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) 
        {
            return CharacterSheetFragment.sheetItems[position].toUpperCase(Locale.US);
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
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	super.onPrepareOptionsMenu(menu);
    	
    	MenuItem skillsDisplay = menu.findItem(R.id.skill_list);
    	MenuItem walletType = menu.findItem(R.id.wallet_type);
    	MenuItem assetsSortBy = menu.findItem(R.id.sort_by);
    	MenuItem assetsLayoutStyle = menu.findItem(R.id.layout_style);
    	MenuItem search = menu.findItem(R.id.menu_search);
    	
    	skillsDisplay.setVisible(false);
    	walletType.setVisible(false);
    	assetsSortBy.setVisible(false);
    	assetsLayoutStyle.setVisible(false);
    	search.setVisible(false);
    	
    	switch (mViewPager.getCurrentItem())
	    {
	    case CharacterSheetFragment.SKILLS:
	    	skillsDisplay.setVisible(true);
    		break;
    	case CharacterSheetFragment.SKILL_QUEUE:
    		break;
    	case CharacterSheetFragment.ATTRIBUTES:
    		break;
    	case CharacterSheetFragment.WALLET:
        	walletType.setVisible(true);
    		break;
    	case CharacterSheetFragment.ASSETS:
    		assetsSortBy.setVisible(true);
        	assetsLayoutStyle.setVisible(true);
        	search.setVisible(true);
    		break;
	    }
    	
		return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuInflater menuInflater = getMenuInflater();        	
        
    	menuInflater.inflate(R.menu.char_detail, menu);
    		
    	searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView(); 
    	MenuItem searchViewMenuItem = menu.getItem(0);
    	searchView.setOnQueryTextListener(new SearchQueryUpdatedListener());
    	searchViewMenuItem.setOnActionExpandListener(new OnActionExpandListener(){
			public boolean onMenuItemActionCollapse(MenuItem item) 
			{
				if (mViewPager.getCurrentItem() == CharacterSheetFragment.ASSETS)
	            {
	        		ParentAssetsFragment assetsFragment = mSectionsPagerAdapter.assetsFragment();
	        		assetsFragment.updateSearchFilter(null);
	            }
				return true;
			}

			public boolean onMenuItemActionExpand(MenuItem item) 
			{
				searchOpenedAtLevel = mSectionsPagerAdapter.assetsFragment().parentStack.size();
				return true;
			}
    		
    	});
    	            	
    	searchView.setOnKeyListener(new OnKeyListener() 
    	{
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
        	        	
    	return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item) {
    	 
    	super.onOptionsItemSelected(item);
    	
	    switch (item.getItemId())
	    {
	    case R.id.skill_list:
	    	new SkillList().show(getSupportFragmentManager(), "Skill List Dialog");
	    	break;
	    case R.id.wallet_type:
	    	new WalletType().show(getSupportFragmentManager(), "Skill List Dialog");
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
	private class WalletType extends DialogFragment
    {
		@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) 
    	{
    	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	    builder.setTitle("Show")
    	           .setItems(WalletFragment.displayTypeNames, new DialogInterface.OnClickListener() 
		           {
		               public void onClick(DialogInterface dialog, int which) 
		               {
		            	   if (mViewPager.getCurrentItem() == CharacterSheetFragment.WALLET)
			               {
			               		mSectionsPagerAdapter.walletFragment().updateWalletType(which);
			               }
		               }
		           }
    	   );
    	    
    	    return builder.create();
    	}
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
		            	   if (mViewPager.getCurrentItem() == CharacterSheetFragment.SKILLS)
			               {
			               		mSectionsPagerAdapter.skillsFragment().updateSkillDisplayMode(which);
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
		            	   if (mViewPager.getCurrentItem() == CharacterSheetFragment.ASSETS)
			               {
			               		ParentAssetsFragment assetsFragment = mSectionsPagerAdapter.assetsFragment();
			               		Log.d("DIALOG FRAGMENT", "UPDATE SORT");
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
		            	   if (mViewPager.getCurrentItem() == CharacterSheetFragment.ASSETS)
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
		public boolean onQueryTextChange(String searchString) 
		{			
			ifStillOnAssetsSearch(searchString);
			return false;
		}

		public boolean onQueryTextSubmit(String searchString) 
		{
			ifStillOnAssetsSearch(searchString);
			return false;
		}
		
		private void ifStillOnAssetsSearch(String searchString)
		{
			if (mViewPager.getCurrentItem() == CharacterSheetFragment.ASSETS)
            {
        		ParentAssetsFragment assetsFragment = mSectionsPagerAdapter.assetsFragment();
        		if (assetsFragment.currentAssets != null) assetsFragment.updateSearchFilter(searchString);
            }
		}
    }

	@Override
	protected void refresh() 
	{
		switch (mViewPager.getCurrentItem())
		{
		case CharacterSheetFragment.ASSETS:
			((DetailFragment) mSectionsPagerAdapter.assetsFragment()).loadData();
			break;
		case CharacterSheetFragment.ATTRIBUTES:
			((DetailFragment) mSectionsPagerAdapter.attributesFragment()).loadData();
			break;
		case CharacterSheetFragment.SKILL_QUEUE:
			((DetailFragment) mSectionsPagerAdapter.skillQueueFragment()).loadData();
			break;
		case CharacterSheetFragment.SKILLS:
			((DetailFragment) mSectionsPagerAdapter.skillsFragment()).loadData();
			break;
		case CharacterSheetFragment.WALLET:
			((DetailFragment) mSectionsPagerAdapter.walletFragment()).loadData();
			break;
		}
	}
}
