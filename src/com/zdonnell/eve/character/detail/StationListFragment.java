package com.zdonnell.eve.character.detail;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.character.AssetsEntity.Station;

public class StationListFragment extends Fragment implements IAssetsSubFragment 
{
	
	/**
	 * Reference to the layout to use for list item construction
	 */
	private final int stationRowResourceID = R.layout.char_detail_assets_stations_list_item;
	
	/**
	 * Fragment Context (Activity Context)
	 */
	private Context context;
	
	/**
	 * Array of stations
	 */
	private AssetsEntity[] currentStationList;
	
	/**
	 * The main list view for the station list layout
	 */
	private ListView stationListView;
	
	private boolean isFragmentCreated = false;
	
	private boolean initialLoadComplete = false;
	
	private ParentAssetsFragment parentFragment;

	
	@Override
	public void setParent(ParentAssetsFragment parent) 
	{
		this.parentFragment = parent;
	}
	
	@Override
	public void assetsUpdated(AssetsEntity[] assets) 
	{	
		this.currentStationList = assets;
		if (isFragmentCreated) updateListView();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets_stations, container, false);
    	
    	stationListView = (ListView) inflatedView.findViewById(R.id.char_detail_assets_stations_list);
    	
    	if (!initialLoadComplete && currentStationList != null) updateListView();
    	
    	isFragmentCreated = true;
    	return inflatedView;
    }
	
	private void updateListView()
	{
		stationListView.setAdapter(new StationArrayAdapter(context, stationRowResourceID, currentStationList));
		initialLoadComplete = true;
	}
	
	private class StationArrayAdapter extends ArrayAdapter<AssetsEntity>
	{
		private int layoutResID;
		
		private LayoutInflater inflater;
		
		public StationArrayAdapter(Context context, int layoutResourceID, AssetsEntity[] stationList) 
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
			
			TextView stationName = (TextView) itemView.findViewById(R.id.station_assets_list_item_name);
			TextView assetCount = (TextView) itemView.findViewById(R.id.station_assets_list_item_count);
			
			final Station curStation = (Station) getItem(position);
			
			stationName.setText(String.valueOf(curStation.getLocationID()));
			assetCount.setText(curStation.getContainedAssets().size() + " items");
			
			itemView.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					if (curStation.containsAssets()) 
					{
						ArrayList<AssetsEntity> listAssets = curStation.getContainedAssets();
						AssetsEntity[] subAssets = new AssetsEntity[listAssets.size()];
						
						listAssets.toArray(subAssets);
						
						parentFragment.setCurrentParentName("Station: " + curStation.getLocationID());
						parentFragment.updateChild(subAssets, 1, false);
					}
				}
			});
			
			return itemView;
		}
	}
}
