package com.zdonnell.eve.character.detail;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.character.AssetsEntity.Station;

public class StationListFragment extends Fragment implements IAssetsSubFragment 
{
	
	/**
	 * Refrence to the layout to use for list item construction
	 */
	private final int stationRowResourceID = R.layout.char_detail_assets_list_item;
	
	/**
	 * Fragment Context (Activity Context)
	 */
	private Context context;
	
	/**
	 * Array of stations
	 */
	private Station[] currentStationList;
	
	/**
	 * The main list view for the station list layout
	 */
	private ListView stationListView;
	
	
	@Override
	public void assetsUpdated(AssetsEntity[] assets) 
	{	
		this.currentStationList = (Station[]) assets;
		stationListView.setAdapter(new StationArrayAdapter(context, stationRowResourceID, currentStationList));
	}
	
	
	private class StationArrayAdapter extends ArrayAdapter<Station>
	{
		private int layoutResID;
		
		private LayoutInflater inflater;
		
		public StationArrayAdapter(Context context, int layoutResourceID, Station[] stationList) 
		{
			super(context, layoutResourceID, stationList);
			this.layoutResID = layoutResourceID;
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LinearLayout itemView;
			
			/* Determine if we recyle the old view, or inflate a new one */
			if (convertView == null) itemView = (LinearLayout) inflater.inflate(layoutResID, parent, false);
			else itemView = (LinearLayout) convertView;
			
			return itemView;
		}
	}
}
