package com.zdonnell.eve;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public abstract class BaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected ListFragment mFrag;
	
	private int itemsLoadingCount = 0;
	
	public BaseActivity(int titleRes) {
		mTitleRes = titleRes;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  

		
		setTitle(mTitleRes);

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		mFrag = new SlideMenuFragment();
		t.replace(R.id.menu_frame, mFrag);
		t.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.actionbar_home_width);
		sm.setFadeDegree(0.75f);

		// customize the ActionBar
		if (Build.VERSION.SDK_INT >= 11) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		case R.id.refresh_loading:
			refresh();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public class PagerAdapter extends FragmentPagerAdapter {
		private List<Fragment> mFragments = new ArrayList<Fragment>();
		private ViewPager mPager;

		public PagerAdapter(FragmentManager fm, ViewPager vp) {
			super(fm);
			mPager = vp;
			mPager.setAdapter(this);
		}

		public void addTab(Fragment frag) {
			mFragments.add(frag);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}
	}
	
	protected abstract void refresh();
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem refreshIcon = menu.findItem(R.id.refresh_loading);
		if (itemsLoadingCount > 0) 
		{
			refreshIcon.setActionView(R.layout.progress);
		}
		else refreshIcon.collapseActionView();
		
		return true;
	}
	
	public void dataLoading()
	{
		itemsLoadingCount++;		
		if (itemsLoadingCount == 1)
		{
			invalidateOptionsMenu();
		}
	}
	
	public void loadingFinished(boolean dataError)
	{
		itemsLoadingCount--;		
		if (itemsLoadingCount == 0)
		{
			invalidateOptionsMenu();
		}
	}
}
