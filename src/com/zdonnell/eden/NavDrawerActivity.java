package com.zdonnell.eden;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.androideveapi.link.ILoadingActivity;
import com.zdonnell.eden.helpers.BasicOnTouchListener;

public abstract class NavDrawerActivity extends FragmentActivity implements ILoadingActivity {

	private int mTitleRes;
	protected ListFragment mFrag;

	private int itemsLoadingCount = 0;

	private Activity activity;

	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private final static int CHARS = 0;
	//private final static int CORPS = 1;
	private final static int ACCOUNTS = 1;
	//private final static int SETTINGS = 2;

	/**
	 * Array of Strings for the menu item titles
	 */
	private static String[] mainMenuItems = new String[2];

	static {
		mainMenuItems[CHARS] = "Characters";
		//mainMenuItems[CORPS] = "Corporations";
		mainMenuItems[ACCOUNTS] = "API Keys";
		//mainMenuItems[SETTINGS] = "Settings";
	}

	/**
	 * Array of Resource IDs for the menu item icons
	 */
	private static int[] ItemImageResources = new int[2];

	static {
		ItemImageResources[CHARS] = R.drawable.characters_icon;
		//ItemImageResources[CORPS] = R.drawable.corporations_icon;
		ItemImageResources[ACCOUNTS] = R.drawable.accounts_icon;
		//ItemImageResources[SETTINGS] = R.drawable.settings_icon;
	}

	public NavDrawerActivity(int titleRes) {
		mTitleRes = titleRes;
		activity = this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.drawer_layout);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);


		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new SampleAdapter(activity, mainMenuItems));

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		setTitle(mTitleRes);

		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
			case R.id.refresh_loading:
				refresh();
				break;
			case R.id.settings:
				Intent intent = new Intent(activity, SettingsActivity.class);
				startActivity(intent);

				break;
		}

		return super.onOptionsItemSelected(item);
	}

	protected abstract void refresh();

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem refreshIcon = menu.findItem(R.id.refresh_loading);
		if (itemsLoadingCount > 0) {
			refreshIcon.setActionView(R.layout.progress);
		} else refreshIcon.collapseActionView();

		return true;
	}

	public void dataLoading() {
		itemsLoadingCount++;
		if (itemsLoadingCount == 1) {
			invalidateOptionsMenu();
		}
	}

	public void loadingFinished(boolean dataError) {
		itemsLoadingCount--;
		if (itemsLoadingCount == 0) {
			invalidateOptionsMenu();
		}
	}

	public class SampleAdapter extends ArrayAdapter<String> {
		public SampleAdapter(Context context, String[] items) {
			super(context, 0, items);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}

			convertView.setOnTouchListener(new BasicOnTouchListener());
			convertView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent;

					switch (position) {
						case CHARS:
							intent = new Intent(activity, CharactersActivity.class);
							break;
						case ACCOUNTS:
							intent = new Intent(activity, APIKeysActivity.class);
							break;
						default:
							intent = null;
							break;
					}

					startActivity(intent);
					mDrawerLayout.closeDrawers();
				}
			});

			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(ItemImageResources[position]);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position));

			return convertView;
		}
	}
}
