package com.zdonnell.eve.character.detail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zdonnell.eve.BaseActivity;
import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.priceservice.PriceService;
import com.zdonnell.eve.staticdata.api.StaticData;
import com.zdonnell.eve.staticdata.api.StationInfo;
import com.zdonnell.eve.staticdata.api.TypeInfo;

@SuppressLint("ValidFragment")
public class ParentAssetsFragment extends Fragment {
    
    public final static int STATION = 0;
    public final static int ASSET = 1;
	
	private APICharacter character;
        
    private Context context;
    
    private SparseIntArray typeIDCounts;
    
    private Integer[] uniqueTypeIDs;
                    
    public Stack<AssetsEntity[]> parentStack = new Stack<AssetsEntity[]>();
    
    private AssetsEntity[] currentAssets;
    
    private IAssetsSubFragment childFragment;
    
    private String currentParentName;
    
    private SparseArray<StationInfo> currentStationInfo = new SparseArray<StationInfo>();
    
    private SparseArray<TypeInfo> typeInfo = new SparseArray<TypeInfo>();
    
    private SparseArray<Float> prices = new SparseArray<Float>();
    
    private BaseActivity parentActivity;
    
    private String searchFilter;
    
    private ProgressDialog initialLoadDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	
    	int keyID = getArguments().getInt("keyID");
    	String vCode = getArguments().getString("vCode");
    	int characterID = getArguments().getInt("characterID");
    	character = new APICharacter(new APICredentials(keyID, vCode), characterID, context);
    	
    	parentActivity = (BaseActivity) context;
    	
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets_childfragment_frame, container, false);
    	
    	FragmentTransaction loadStationList = this.getChildFragmentManager().beginTransaction();

    	childFragment = new StationListFragment();
    	childFragment.setParent(this);
    	
    	loadStationList.replace(R.id.char_detail_assets_childfragment_layoutFrame, (Fragment) childFragment);
    	loadStationList.commit();
    	
    	initialLoadDialog = ProgressDialog.show(context, "Loading", "Obtaining Asset List");
    	
    	character.getAssetsList(new APICallback<AssetsEntity[]>()
    	{
			@Override
			public void onUpdate(AssetsEntity[] locationArray) 
			{
				Arrays.sort(locationArray, new InventorySort.Count());
				currentAssets = locationArray;
				
				initialLoadDialog.setMessage("Getting Asset Type Information");
				prepareAssets(locationArray);
			}
    	});   	
    	
    	return inflatedView;
    }
    
    public void setCurrentParentName(String name) { this.currentParentName = name; }
    public String getCurrentParentName() { return currentParentName; }

    
    public void updateChild(AssetsEntity[] newAssetsSet, int type, boolean isBack, boolean isSearchUpdate)
    {    	    	    	
		SharedPreferences prefs = context.getSharedPreferences("eden_assets_preferences", Context.MODE_PRIVATE);
    	
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
    	
    	AssetsEntity[] assetsToPass;
    	if (searchFilter == null) assetsToPass = newAssetsSet;
    	else assetsToPass = searchAssets(newAssetsSet);
    	//sortAssets(prefs.getInt("sort_type", InventorySort.ALPHA), assetsToPass);
    	
    	nextFragment.assetsUpdated(assetsToPass);
    	
    	if (currentAssets != null && !isBack && !isSearchUpdate) parentStack.push(currentAssets);
    	currentAssets = newAssetsSet;
    	
    	nextFragment.setParent(this);
    	childFragment = nextFragment;
    	
    	loadNextAssets.replace(R.id.char_detail_assets_childfragment_layoutFrame, (Fragment) nextFragment);
    	loadNextAssets.commit();
    	
    	//if (!isSearchUpdate) parentActivity.invalidateOptionsMenu();
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
				initialLoadDialog.setMessage("Checking Price Values");
				obtainedTypeInfo(rTypeInfo);
			}
    	}, uniqueTypeIDs);
    	
    	/* ImageService.getInstance(context).getTypes(null, uniqueTypeIDs); */
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
				childFragment.assetsUpdated(currentAssets);
				childFragment.obtainedPrices();
				initialLoadDialog.dismiss();
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
   
    public void sortAssets(int sortType, AssetsEntity[] assets)
    {
    	switch (sortType)
    	{
    	case InventorySort.COUNT:
    		Arrays.sort(assets, new InventorySort.Count());
    		break;
    	case InventorySort.COUNT_REVERSE:
    		Arrays.sort(assets, Collections.reverseOrder(new InventorySort.Count()));
    		break;
    	case InventorySort.ALPHA:
    		Arrays.sort(assets, new InventorySort.Alpha(childFragment.getNames()));
    		break;
    	case InventorySort.ALPHA_REVERSE:
    		Arrays.sort(assets, Collections.reverseOrder(new InventorySort.Alpha(childFragment.getNames())));
    		break;
    	case InventorySort.VALUE:
    		Arrays.sort(assets, new InventorySort.Value(childFragment.getValues()));
    		break;
    	case InventorySort.VALUE_REVERSE:
    		Arrays.sort(assets, Collections.reverseOrder(new InventorySort.Value(childFragment.getValues())));
    		break;
    	}
    }
    
    public void updateSort(int sortType)
    {
		SharedPreferences prefs = context.getSharedPreferences("eden_assets_preferences", Context.MODE_PRIVATE);
    	prefs.edit().putInt("sort_type", sortType).commit();
    	
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
    
    public void updateLayoutStyle(int layoutType)
    {
    	childFragment.updateLayoutStyle(layoutType);
    }
    
    public void updateSearchFilter(String searchQuery)
    {
    	searchFilter = searchQuery;
    	updateChild(currentAssets, assetsType(), false, true);
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
			updateChild(assets, parentStack.isEmpty() ? STATION : ASSET, true, false);
			
			return true;
    	}
		
		return false;
	}
	
	public int assetsType()
	{
		return (currentAssets[0] instanceof AssetsEntity.Station ? STATION : ASSET);
	}
	
	private AssetsEntity[] searchAssets(AssetsEntity[] assetsToSearch)
	{
		ArrayList<AssetsEntity> containingAssets = new ArrayList<AssetsEntity>();
		
		for (AssetsEntity entity : assetsToSearch)
		{
			if (containsSearchString(entity)) containingAssets.add(entity);
		}
		
		AssetsEntity[] filteredAssets = new AssetsEntity[containingAssets.size()];
		containingAssets.toArray(filteredAssets);
		
		return filteredAssets;
	}
	
	private boolean containsSearchString(AssetsEntity entity)
	{
		if (entity instanceof AssetsEntity.Station)
		{
			for (AssetsEntity childEntity : entity.getContainedAssets())
			{
				if (containsSearchString(childEntity)) return true;
			}
		}
		else if (entity instanceof AssetsEntity.Item)
		{
			AssetsEntity.Item item = (AssetsEntity.Item) entity;
			
			if (typeInfo.get(item.attributes().typeID).typeName.toLowerCase().contains(searchFilter.toLowerCase())) return true;
			if (item.containsAssets())
			{
				for (AssetsEntity childEntity : entity.getContainedAssets())
				{
					if (containsSearchString(childEntity)) return true;
				}
			}
		}
		
		return false;
	}
}
