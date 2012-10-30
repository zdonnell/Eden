package com.zdonnell.eve;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.widget.TextView;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.server.Server;

public class MainActivityOld extends FragmentActivity {

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
        setContentView(R.layout.activity_main_strip);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ActionBar aBar = getActionBar();
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        aBar.setDisplayShowTitleEnabled(false);
       
        /* Load in the TQ status info to the actionBar */
        aBar.setCustomView(R.layout.tq_status);
        setServerStatus();
        
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        
        customizePagerStrip((PagerTitleStrip) findViewById(R.id.pager_title_strip));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_strip, menu);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    	
    	private static final int COUNT = 2;
    	    	
        public SectionsPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int i) 
        {
        	Fragment fragment;
        	
        	if (i == 0) fragment = new CharactersFragment();
        	else fragment = new CorporationsFragment();
        	        	
			return fragment;
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) 
        {
            switch (position) 
            {
                case 0: return getString(R.string.title_characters).toUpperCase();
                case 1: return getString(R.string.title_corporations).toUpperCase();
            }
            return null;
        }
    }
    
    private void customizePagerStrip(PagerTitleStrip titleStrip)
    {
    	//titleStrip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
    }
    
    private void setServerStatus()
    {
    	Server server = new Server(getBaseContext());
        final TextView serverStatus = (TextView) findViewById(R.id.server_status);
		server.status(new APICallback<String[]>() {
			@Override
			public void onUpdate(String[] updatedData) {
				if (updatedData[0].equals("True")) 
				{
					NumberFormat nf = NumberFormat.getInstance();
					serverStatus.setText(Html.fromHtml("<B><FONT COLOR='#669900'>ONLINE</FONT></B> " + nf.format(Integer.parseInt(updatedData[1]))));
				}
				else
				{
					serverStatus.setText(Html.fromHtml("<B><FONT COLOR='#CC0000'>OFFLINE</FONT></B>"));
				}
			}
		}); 
    }
}
