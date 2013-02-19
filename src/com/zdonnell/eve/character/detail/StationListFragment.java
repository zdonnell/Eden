package com.zdonnell.eve.character.detail;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
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

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.character.AssetsEntity.Station;
import com.zdonnell.eve.api.priceservice.PriceService;
import com.zdonnell.eve.staticdata.api.StaticData;
import com.zdonnell.eve.staticdata.api.StationInfo;

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
	
	private SparseArray<Double> stationValues;
	
	private SparseArray<String> currentStationNames = new SparseArray<String>();
	
	private SparseArray<StationInfo> currentStationInfo = new SparseArray<StationInfo>();
	
	SparseArray<SparseArray<Integer>> itemCounts;
	
	private StationArrayAdapter adapter;
	
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
		adapter = new StationArrayAdapter(context, stationRowResourceID, currentStationList);
		stationListView.setAdapter(adapter);
		
		final Integer[] stationIDs = new Integer[currentStationList.length];
		for (int x = 0; x < currentStationList.length; x++) stationIDs[x] = ((AssetsEntity.Station) currentStationList[x]).getLocationID();
		
		new StaticData(context).getStationInfo(new APICallback<SparseArray<StationInfo>>()
		{
			@Override
			public void onUpdate(SparseArray<StationInfo> retStationInfo) 
			{					
				/* 
				 * The returned String array matches the order provided by the input typeID array.
				 * This will pair them up in a SparseArray so the type name strings can be accessed by typeID
				 */
				for (int i = 0; i < stationIDs.length; i++) currentStationNames.put(stationIDs[i], retStationInfo.get(stationIDs[i]).stationName);
				
				currentStationInfo = retStationInfo;
				adapter.obtainedStationNames();
				
				getIcons();
			}
		}, stationIDs);
		
		initialLoadComplete = true;
		
		//obtainPriceInformation();
	}
	
	private void obtainPriceInformation()
	{
		itemCounts = new SparseArray<SparseArray<Integer>>(currentStationList.length);
		ArrayList<Integer> totalUniqueTypeIDs = new ArrayList<Integer>();
		
		for (AssetsEntity assetsEntity : currentStationList) 
		{
			AssetsEntity.Station station = (AssetsEntity.Station) assetsEntity;
			itemCounts.put(station.getLocationID(), new SparseArray<Integer>());
			
			checkAssets(station.getContainedAssets(), station.getLocationID(), totalUniqueTypeIDs);
		}
		
		/* convert the ArrayList of unique IDs to an actual Array for use in the Price Service */
		Integer[] uniqueIDArray = new Integer[totalUniqueTypeIDs.size()];
		totalUniqueTypeIDs.toArray(uniqueIDArray);
		
		PriceService.getInstance(context).getValues(uniqueIDArray, new APICallback<SparseArray<Float>>() 
		{
			@Override
			public void onUpdate(SparseArray<Float> prices) 
			{
				//calculateStationValues(prices, currentStationList);
				//adapter.obtainedStationValues(stationValues);
			}
		});
	}
	
	private void calculateStationValues(SparseArray<Float> prices, AssetsEntity[] currentStationList)
	{
		stationValues = new SparseArray<Double>(currentStationList.length);
		
		for (AssetsEntity assetsEntity : currentStationList)
		{
			double stationValue = 0;
			
			AssetsEntity.Station station = (AssetsEntity.Station) assetsEntity;
			int stationID = station.getLocationID();
			SparseArray<Integer> curStationAssetCounts = itemCounts.get(stationID);
			int uniqueItemsInStation = curStationAssetCounts.size();
			
			for (int i = 0; i < uniqueItemsInStation; ++i)
			{
				int uniqueItemTypeID = curStationAssetCounts.keyAt(i);
				
				if (prices.get(uniqueItemTypeID) != null)
				{
					stationValue += prices.get(uniqueItemTypeID) * curStationAssetCounts.valueAt(i);
				}
			}
			
			stationValues.put(stationID, stationValue);
		}
	}
	
	private void checkAssets(ArrayList<AssetsEntity> assets, int rootStationID, ArrayList<Integer> totalUniqueIDSet)
	{
		for (AssetsEntity entity : assets)
		{
			/* Grab some attributes of the item */
			AssetsEntity.Item item = (AssetsEntity.Item) entity;
			int typeID = item.attributes().typeID;
			int quantity = item.attributes().quantity;
			
			/* If the item contains assets, recurse through them as well */
			if (item.containsAssets()) checkAssets(item.getContainedAssets(), rootStationID, totalUniqueIDSet);
			
			/* Keep track of how many times each type shows up, specific to each station */
			Integer currentItemCount = itemCounts.get(rootStationID).get(typeID);
			if (currentItemCount != null) itemCounts.get(rootStationID).put(typeID, currentItemCount + quantity);
			else itemCounts.get(rootStationID).put(typeID, quantity);
			
			/* See if the typeID needs to be added to the main list of unique IDs to get prices on */
			if (!totalUniqueIDSet.contains(typeID)) totalUniqueIDSet.add(typeID);
		}
	}
	
	private void getIcons()
	{
		ArrayList<Integer> uniqueStationTypesList = new ArrayList<Integer>();
		
		for (int i = 0; i < currentStationInfo.size(); ++i)
		{
			int stationTypeID = currentStationInfo.valueAt(i).stationTypeID;			
			if (!uniqueStationTypesList.contains(stationTypeID)) uniqueStationTypesList.add(stationTypeID);
		}
		
		int[] uniqueStationTypes = new int[uniqueStationTypesList.size()];
		
		for (int i = 0; i < uniqueStationTypesList.size(); ++i) 
		{
			uniqueStationTypes[i] = uniqueStationTypesList.get(i);
		}
		
		ImageService.getInstance(context).getTypes(new IconObtainedCallback() 
		{
			@Override
			public void iconsObtained(SparseArray<Bitmap> bitmaps) 
			{
				adapter.obtainedStationIcons(bitmaps);
			}
		}, uniqueStationTypes);
	}
	
	private class StationArrayAdapter extends ArrayAdapter<AssetsEntity>
	{
		private int layoutResID;
		
		private LayoutInflater inflater;
		
    	HashMap<TextView, Integer> stationNameMappings = new HashMap<TextView, Integer>();
    	HashMap<TextView, Integer> stationValueMappings = new HashMap<TextView, Integer>();
    	HashMap<ImageView, Integer> stationIconMappings = new HashMap<ImageView, Integer>();
		
		private boolean stationNamesLoaded = false, stationValuesLoaded = false;
		
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
			
			/* Determine if we recyle the old view, or inflate a new one */
			if (convertView == null) itemView = (LinearLayout) inflater.inflate(layoutResID, parent, false);
			else itemView = (LinearLayout) convertView;
			
			TextView stationName = (TextView) itemView.findViewById(R.id.station_assets_list_item_name);
			TextView assetCount = (TextView) itemView.findViewById(R.id.station_assets_list_item_count);
			TextView assetValue = (TextView) itemView.findViewById(R.id.station_assets_list_item_value);
			final ImageView icon = (ImageView) itemView.findViewById(R.id.station_assets_list_item_icon);
			
			assetCount.setText(curStation.getContainedAssets().size() + " items");
			
			stationNameMappings.put(stationName, curStation.getLocationID());
			stationIconMappings.put(icon, curStation.getLocationID());
			stationValueMappings.put(assetValue, curStation.getLocationID());
			
			icon.setTag(curStation.getLocationID());

			if (stationNamesLoaded) 
			{
				stationName.setText(currentStationNames.get(curStation.getLocationID()));
				final int stationTypeID = currentStationInfo.get(curStation.getLocationID()).stationTypeID;
				
				ImageService.getInstance(context).getTypes(new IconObtainedCallback() 
				{
					@Override
					public void iconsObtained(SparseArray<Bitmap> bitmaps) 
					{
						if (((Integer) icon.getTag()).intValue() == curStation.getLocationID())
						{											
							icon.setImageBitmap(bitmaps.get(stationTypeID));	
						}
					}
				}, stationTypeID);
			}
			
			if (stationValuesLoaded) assetValue.setText(stationValues.get(curStation.getLocationID()) + " ISK");
			
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
						
						String stationName;
						if (currentStationNames.get(curStation.getLocationID()) != null) stationName = currentStationNames.get(curStation.getLocationID());
						else stationName = "Station: " + curStation.getLocationID();
						
						parentFragment.setCurrentParentName(stationName);
						parentFragment.updateChild(subAssets, 1, false);
					}
				}
			});
			
			return itemView;
		}
		
		public void obtainedStationNames()
		{
			stationNamesLoaded = true;
			
			/* Update Type Names */
			for (TextView textView : stationNameMappings.keySet())
			{
				int typeID = stationNameMappings.get(textView);
				textView.setText(currentStationNames.get(typeID));
			}			
		}
		
		public void obtainedStationIcons(SparseArray<Bitmap> bitmaps)
		{
			for (ImageView icon : stationIconMappings.keySet())
			{
				int stationID = stationIconMappings.get(icon);
				int stationTypeID = currentStationInfo.get(stationID).stationTypeID;
				
				icon.setImageBitmap(bitmaps.get(stationTypeID));
			}
		}
		
		public void obtainedStationValues(SparseArray<Double> values)
		{
			stationValuesLoaded = true;
			
			for (TextView textView : stationValueMappings.keySet())
			{
				int stationID = stationValueMappings.get(textView);
				textView.setText(values.get(stationID) + " ISK");
			}
		}
	}

	@Override
	public SparseArray<String> getNames() 
	{
		return currentStationNames;
	}

	@Override
	public SparseArray<Float> getValues() 
	{
		/* TODO fix up this method as soon as Station Values are being loaded correctly */
		return null;
	}

}
