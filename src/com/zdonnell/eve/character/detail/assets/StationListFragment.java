package com.zdonnell.eve.character.detail.assets;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zdonnell.eve.R;
import com.zdonnell.eve.apilink.character.AssetsEntity;
import com.zdonnell.eve.apilink.character.AssetsEntity.Station;
import com.zdonnell.eve.helpers.BasicOnTouchListener;
import com.zdonnell.eve.helpers.ImageURL;
import com.zdonnell.eve.staticdata.StationInfo;

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
	
	private SparseArray<Long> stationValues;
		
	private SparseArray<StationInfo> currentStationInfo = new SparseArray<StationInfo>();
	
	SparseArray<SparseIntArray> itemCounts;
	
	private StationArrayAdapter adapter;
	
	/**
	 * The main list view for the station list layout
	 */
	private ListView stationListView;
	
	private boolean isFragmentCreated = false;
	
	private boolean initialLoadComplete = false;
	
	private TextView stationsCount, stationsValue;
	
	private ParentAssetsFragment parentFragment;
	
	private int[] savedScrollPoint;

	public void setParent(ParentAssetsFragment parent) 
	{
		this.parentFragment = parent;
	}
	
	public void assetsUpdated(AssetsEntity[] assets) 
	{	
		this.currentStationList = assets;
		if (isFragmentCreated) updateListView();
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
		if (parentFragment == null) parentFragment = (ParentAssetsFragment) getParentFragment();
		currentStationInfo = parentFragment.getStationInfo();
		
		context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets_stations, container, false);
    	
    	stationListView = (ListView) inflatedView.findViewById(R.id.char_detail_assets_stations_list);
    	stationsCount = (TextView) inflatedView.findViewById(R.id.char_detail_assets_stations_parentName);
    	stationsValue = (TextView) inflatedView.findViewById(R.id.char_detail_assets_stations_totalValue);
    	
    	if (!initialLoadComplete && currentStationList != null) updateListView();
    	
    	isFragmentCreated = true;
    	return inflatedView;
    }
	
	private void updateListView()
	{		
		stationsCount.setText(currentStationList.length + " stations");
		
		adapter = new StationArrayAdapter(context, stationRowResourceID, currentStationList);
		stationListView.setAdapter(adapter);
		
		if (savedScrollPoint != null) 
		{
			stationListView.setSelectionFromTop(savedScrollPoint[0], savedScrollPoint[1]);	
			savedScrollPoint = null;
		}
		
		final Integer[] stationIDs = new Integer[currentStationList.length];
		for (int x = 0; x < currentStationList.length; x++) stationIDs[x] = ((AssetsEntity.Station) currentStationList[x]).getLocationID();
		
		countAssets();
		if (parentFragment.getPrices() != null && parentFragment.getPrices().size() > 0) calculateStationValues(currentStationList);
		
		initialLoadComplete = true;
	}
	
	private void countAssets()
	{
		itemCounts = new SparseArray<SparseIntArray>(currentStationList.length);
		
		for (AssetsEntity assetsEntity : currentStationList) 
		{
			AssetsEntity.Station station = (AssetsEntity.Station) assetsEntity;
			itemCounts.put(station.getLocationID(), new SparseIntArray());
			
			countSubAssets(station.getContainedAssets(), station.getLocationID());
		}
	}
	
	private void countSubAssets(ArrayList<AssetsEntity> assets, int rootStationID)
	{
		for (AssetsEntity entity : assets)
		{
			/* Grab some attributes of the item */
			AssetsEntity.Item item = (AssetsEntity.Item) entity;
			int typeID = item.attributes().typeID;
			int quantity = item.attributes().quantity;
			
			/* If the item contains assets, recurse through them as well */
			if (item.containsAssets()) countSubAssets(item.getContainedAssets(), rootStationID);
			
			/* Keep track of how many times each type shows up, specific to each station */
			Integer currentItemCount = itemCounts.get(rootStationID).get(typeID);
			if (currentItemCount != null) itemCounts.get(rootStationID).put(typeID, currentItemCount + quantity);
			else itemCounts.get(rootStationID).put(typeID, quantity);
		}
	}
	
	private void calculateStationValues(AssetsEntity[] currentStationList)
	{
		stationValues = new SparseArray<Long>(currentStationList.length);
		SparseArray<Float> prices = parentFragment.getPrices();
		
		double totalAssetValue = 0;
		
		for (AssetsEntity assetsEntity : currentStationList)
		{
			double stationValue = 0;
			
			AssetsEntity.Station station = (AssetsEntity.Station) assetsEntity;
			int stationID = station.getLocationID();
			SparseIntArray curStationAssetCounts = itemCounts.get(stationID);
			int uniqueItemsInStation = curStationAssetCounts.size();
			
			for (int i = 0; i < uniqueItemsInStation; ++i)
			{
				int uniqueItemTypeID = curStationAssetCounts.keyAt(i);
				
				if (prices.get(uniqueItemTypeID) != null)
				{
					stationValue += prices.get(uniqueItemTypeID) * curStationAssetCounts.valueAt(i);
					totalAssetValue += prices.get(uniqueItemTypeID) * curStationAssetCounts.valueAt(i);
				}
			}
			
			stationValues.put(stationID, Math.round(stationValue));
		}
		
		NumberFormat formatter = NumberFormat.getInstance();
		stationsValue.setText(formatter.format(totalAssetValue) + " ISK");
		
		adapter.obtainedStationValues(stationValues);
	}	
	
	private class StationArrayAdapter extends ArrayAdapter<AssetsEntity>
	{
		private int layoutResID;
		
		private boolean stationValuesLoaded = parentFragment.getPrices() != null && parentFragment.getPrices().size() > 0;
		private boolean stationInfoLoaded = parentFragment.getStationInfo() != null && parentFragment.getStationInfo().size() > 0;
		
		private LayoutInflater inflater;
		
    	HashMap<TextView, Integer> stationValueMappings = new HashMap<TextView, Integer>();
    	HashMap<ImageView, Integer> stationIconMappings = new HashMap<ImageView, Integer>();
    	HashMap<TextView, Integer> stationNameMappings = new HashMap<TextView, Integer>();
				
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
			
			final Station curStation = (Station) getItem(position);
			final int stationID = curStation.getLocationID();
			
			/* Determine if we recyle the old view, or inflate a new one */
			if (convertView == null) itemView = (LinearLayout) inflater.inflate(layoutResID, parent, false);
			else itemView = (LinearLayout) convertView;
			
			TextView stationNameTextView = (TextView) itemView.findViewById(R.id.station_assets_list_item_name);
			TextView assetCount = (TextView) itemView.findViewById(R.id.station_assets_list_item_count);
			TextView assetValue = (TextView) itemView.findViewById(R.id.station_assets_list_item_value);
			final ImageView icon = (ImageView) itemView.findViewById(R.id.station_assets_list_item_icon);
			
			assetCount.setText(curStation.getContainedAssets().size() + " items");
			icon.setTag(curStation.getLocationID());
			
			stationIconMappings.put(icon, curStation.getLocationID());
			stationValueMappings.put(assetValue, curStation.getLocationID());
			stationNameMappings.put(stationNameTextView, curStation.getLocationID());
			
			if (stationInfoLoaded)
			{
				final String stationName = currentStationInfo.get(stationID).stationName;
				final int stationTypeID = currentStationInfo.get(curStation.getLocationID()).stationTypeID;
				
				stationNameTextView.setText(stationName);
				
				Picasso.with(getContext()).load(ImageURL.forType(stationTypeID)).into(icon);
			}
			else
			{
				stationNameTextView.setText(String.valueOf(curStation.getLocationID()));
			}
			
			if (stationValuesLoaded) 
			{
				NumberFormat formatter = NumberFormat.getInstance();
				String valueString = formatter.format(stationValues.get(curStation.getLocationID()));
				
				assetValue.setText(valueString + " ISK");
			}
			
			itemView.setOnClickListener(new View.OnClickListener() 
			{	
				public void onClick(View v) 
				{
					ArrayList<AssetsEntity> listAssets = curStation.getContainedAssets();
					AssetsEntity[] subAssets = new AssetsEntity[listAssets.size()];
					
					listAssets.toArray(subAssets);
					
					parentFragment.setCurrentParent(curStation);
					parentFragment.updateChild(subAssets, 1, false, false);
				}
			});
			
			itemView.setOnTouchListener(new BasicOnTouchListener());
						
			return itemView;
		}
		
		
		public void obtainedStationValues(SparseArray<Long> values)
		{
			stationValuesLoaded = true;
			
			for (TextView textView : stationValueMappings.keySet())
			{
				int stationID = stationValueMappings.get(textView);
				
				NumberFormat formatter = NumberFormat.getInstance();
				
				textView.setText(formatter.format(values.get(stationID)) + " ISK"); 
				textView.setVisibility(View.VISIBLE);
			}
		}
		
		public void obtainedStationInfo()
		{
			stationInfoLoaded = true;
			
			for (TextView textView : stationNameMappings.keySet())
			{
				int stationID = stationNameMappings.get(textView);		
				textView.setText(currentStationInfo.get(stationID).stationName);
			}
		}     
	}

	public SparseArray<String> getNames() 
	{
		SparseArray<String> stationNames = new SparseArray<String>(currentStationInfo.size());
		
		for (int i = 0; i < currentStationInfo.size(); ++i)
		{
			stationNames.put(currentStationInfo.keyAt(i), currentStationInfo.valueAt(i).stationName);
		}
		
		return stationNames;
	}

	public SparseArray<Float> getValues() 
	{
		SparseArray<Float> floatStationValues = new SparseArray<Float>(stationValues.size());
		
		for (int i = 0; i < stationValues.size(); ++i)
		{
			float floatValue = stationValues.valueAt(i);
			floatStationValues.put(stationValues.keyAt(i), floatValue);
		}
		
		return floatStationValues;
	}

	public void obtainedPrices() 
	{
		if (adapter != null) calculateStationValues(currentStationList);
	}

	public void obtainedTypeInfo() 
	{
		
	}

	public void updateLayoutStyle(int type) {
		// TODO Auto-generated method stub
		
	}

	public void obtainedStationInfo() 
	{
		currentStationInfo = parentFragment.getStationInfo();
		if (adapter != null) adapter.obtainedStationInfo();
	}

	public int[] getScrollPoint() 
	{
		int index = stationListView.getFirstVisiblePosition();
	    View v = stationListView.getChildAt(0);
	    int top = (v == null) ? 0 : v.getTop();
		
		return new int[] { index, top };
	}

	public void setScrollPoint(int[] scrollPoint) 
	{
		if (adapter != null)
		{
			stationListView.setSelectionFromTop(scrollPoint[0], scrollPoint[1]);	
		}
		else savedScrollPoint = scrollPoint;
	}
}
