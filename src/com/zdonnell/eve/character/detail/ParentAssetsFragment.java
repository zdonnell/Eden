package com.zdonnell.eve.character.detail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.priceservice.PriceService;
import com.zdonnell.eve.staticdata.api.StaticData;
import com.zdonnell.eve.staticdata.api.StationInfo;
import com.zdonnell.eve.staticdata.api.TypeInfo;

public class ParentAssetsFragment extends Fragment {
    
    private final static int STATION = 0;
    private final static int ASSET = 1;
	
	private APICharacter character;
        
    private Context context;
    
    private SparseIntArray typeIDCounts;
    
    private Integer[] uniqueTypeIDs;
                    
    private Stack<AssetsEntity[]> parentStack = new Stack<AssetsEntity[]>();
    
    private AssetsEntity[] currentAssets;
    
    private IAssetsSubFragment childFragment;
    
    private String currentParentName;
    
    private SparseArray<StationInfo> currentStationInfo = new SparseArray<StationInfo>();
    
    private SparseArray<TypeInfo> typeInfo = new SparseArray<TypeInfo>();
    
    private SparseArray<Float> prices = new SparseArray<Float>();
            
    /**
     * Constructor
     * 
     * @param character the {@link APICharacter} to build the Attribute info from
     */
    public ParentAssetsFragment(APICharacter character) 
    {
    	this.character = character;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets_childfragment_frame, container, false);
    	
    	FragmentTransaction loadStationList = this.getChildFragmentManager().beginTransaction();

    	childFragment = new StationListFragment();
    	childFragment.setParent(this);
    	
    	loadStationList.replace(R.id.char_detail_assets_childfragment_layoutFrame, (Fragment) childFragment);
    	loadStationList.commit();
    	
    	character.getAssetsList(new APICallback<AssetsEntity[]>()
    	{
			@Override
			public void onUpdate(AssetsEntity[] locationArray) 
			{
				Arrays.sort(locationArray, new InventorySort.Count());
				currentAssets = locationArray;
				
				prepareAssets(locationArray);
			}
    	});   	
    	
    	return inflatedView;
    }
    
    public void setCurrentParentName(String name) { this.currentParentName = name; }
    public String getCurrentParentName() { return currentParentName; }

    
    public void updateChild(AssetsEntity[] newAssetsSet, int type, boolean isBack)
    {    	
    	FragmentTransaction loadNextAssets = this.getChildFragmentManager().beginTransaction();
    	IAssetsSubFragment nextFragment = null;
    	
    	switch (type)
    	{
    	case STATION: // Station
    		nextFragment = new StationListFragment();
    		break;
    	case ASSET: // Item
    		nextFragment = new InventoryListFragment();
    		break;
    	}
    	
    	nextFragment.assetsUpdated(newAssetsSet);
    	
    	if (currentAssets != null && !isBack) parentStack.push(currentAssets);
    	currentAssets = newAssetsSet;
    	
    	nextFragment.setParent(this);
    	childFragment = nextFragment;
    	
    	loadNextAssets.replace(R.id.char_detail_assets_childfragment_layoutFrame, (Fragment) nextFragment);
    	loadNextAssets.commit();
    }
    
    private void prepareAssets(AssetsEntity[] locations)
    {
    	/* Get the station IDs and get the info for them first */
    	ArrayList<Integer> stationIDsList = new ArrayList<Integer>(locations.length);
    	
    	for (AssetsEntity entity : locations)
    	{
    		AssetsEntity.Station station = (AssetsEntity.Station) entity;
    		if (station.getLocationID() > 60000000) stationIDsList.add(station.getLocationID());
    	}
    	
    	Integer[] stationIDs = new Integer[stationIDsList.size()];
    	stationIDsList.toArray(stationIDs);
    	
    	getStationItems(stationIDs);
    	getTypeItems(locations);
    }
    
    private void getStationItems(Integer[] stationIDs)
    {
    	/* Start by getting the information for the Stations
    	 * This will get us the Station Names and the typeIDs for the station icons
    	 */
    	new StaticData(context).getStationInfo(new APICallback<SparseArray<StationInfo>>() 
    	{
			@Override
			public void onUpdate(SparseArray<StationInfo> stationInformation) 
			{
				/* set this so sub fragments can pull from it later */
				currentStationInfo = stationInformation;
				
				ArrayList<Integer> uniqueStationTypeIDsList = new ArrayList<Integer>();
				
				for (int i = 0; i < stationInformation.size(); ++i)
				{
					int stationTypeID = stationInformation.valueAt(i).stationTypeID;
					if (!uniqueStationTypeIDsList.contains(stationTypeID)) uniqueStationTypeIDsList.add(stationTypeID);
				}
				
				Integer[] uniqueStationTypeIDs = new Integer[uniqueStationTypeIDsList.size()];
				for (int i = 0; i < uniqueStationTypeIDsList.size(); ++i)
				{
					uniqueStationTypeIDs[i] = uniqueStationTypeIDsList.get(i);
				}
				
				/* Put the station images in memory for the stations sub fragment */
				new ImageService(context).getTypes(null, uniqueStationTypeIDs);
				
				/* Give the assets list to the stations fragment */
				childFragment.assetsUpdated(currentAssets);
			}
    	}, stationIDs);
    }
    
    private void getTypeItems(AssetsEntity[] locations)
    {    	
    	ArrayList<Integer> uniqueTypeIDsList = new ArrayList<Integer>();
    	typeIDCounts = new SparseIntArray();
    	
    	for (AssetsEntity entity : locations)
    	{
    		countAssets(entity, uniqueTypeIDsList);
    	}
    	
    	Integer[] uniqueTypeIDs = new Integer[uniqueTypeIDsList.size()];
    	uniqueTypeIDsList.toArray(uniqueTypeIDs);
    	
    	new StaticData(context).getTypeInfo(new APICallback<SparseArray<TypeInfo>>()
    	{
			@Override
			public void onUpdate(SparseArray<TypeInfo> rTypeInfo) 
			{
				obtainedTypeInfo(rTypeInfo);
			}
    	}, uniqueTypeIDs);
    	
    	new ImageService(context).getTypes(null, uniqueTypeIDs);
    }
    
    private void obtainedTypeInfo(SparseArray<TypeInfo> typeInfo)
    {
		this.typeInfo = typeInfo;
		childFragment.obtainedTypeInfo();
		
		/* With the type info obtained, grab prices for the valid typeIDs */
		ArrayList<Integer> marketTypeIDsList = new ArrayList<Integer>();
		for (int i = 0; i < typeInfo.size(); ++i)
		{
			if (typeInfo.valueAt(i).marketGroupID != -1) marketTypeIDsList.add(typeInfo.keyAt(i));
		}
		
		Integer[] marketTypeIDs = new Integer[marketTypeIDsList.size()];
		marketTypeIDsList.toArray(marketTypeIDs);
		
		PriceService.getInstance(context).getValues(marketTypeIDs, new APICallback<SparseArray<Float>>() 
		{
			@Override
			public void onUpdate(SparseArray<Float> updatedData) 
			{
				prices = updatedData;
				childFragment.obtainedPrices();
			}
		});
    }
    
    private void countAssets(AssetsEntity entity, ArrayList<Integer> uniqueTypeIDsList)
    {
    	if (entity instanceof AssetsEntity.Item)
    	{
    		int typeID = entity.attributes().typeID;
    		int quantity = entity.attributes().quantity;
    		
    		if (!uniqueTypeIDsList.contains(typeID)) uniqueTypeIDsList.add(typeID);
    		typeIDCounts.put(typeID, typeIDCounts.get(typeID) + quantity);
    	}
    	
    	if (entity.containsAssets())
    	{
    		for (AssetsEntity childEntity : entity.getContainedAssets()) countAssets(childEntity, uniqueTypeIDsList);
    	}
    }
    
    public void updateSort(int sortType)
    {
    	switch (sortType)
    	{
    	case InventorySort.COUNT:
    		Arrays.sort(currentAssets, new InventorySort.Count());
    		break;
    	case InventorySort.COUNT_REVERSE:
    		Arrays.sort(currentAssets, Collections.reverseOrder(new InventorySort.Count()));
    		break;
    	case InventorySort.ALPHA:
    		Arrays.sort(currentAssets, new InventorySort.Alpha(childFragment.getNames()));
    		break;
    	case InventorySort.ALPHA_REVERSE:
    		Arrays.sort(currentAssets, Collections.reverseOrder(new InventorySort.Alpha(childFragment.getNames())));
    		break;
    	case InventorySort.VALUE:
    		Arrays.sort(currentAssets, new InventorySort.Value(childFragment.getValues()));
    		break;
    	case InventorySort.VALUE_REVERSE:
    		Arrays.sort(currentAssets, Collections.reverseOrder(new InventorySort.Value(childFragment.getValues())));
    		break;
    	}
    	
    	childFragment.assetsUpdated(currentAssets);
    }
    
    public SparseArray<StationInfo> getStationInfo()
    {
    	return currentStationInfo;
    }
    
    public SparseArray<TypeInfo> getTypeInfo()
    {
    	return typeInfo;
    }
    
    public SparseArray<Float> getPrices()
    {
    	return prices;
    }
    

	public boolean backKeyPressed() 
	{
		if (!parentStack.empty()) 
    	{
			AssetsEntity[] assets = parentStack.pop();
			updateChild(assets, parentStack.isEmpty() ? STATION : ASSET, true);
			
			return true;
    	}
		
		return false;
	}
}
