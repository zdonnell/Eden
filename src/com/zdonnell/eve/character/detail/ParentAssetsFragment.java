package com.zdonnell.eve.character.detail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiAuthorization;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.shared.assetlist.AssetListResponse;
import com.beimin.eveapi.shared.assetlist.EveAsset;
import com.zdonnell.eve.BaseActivity;
import com.zdonnell.eve.R;
import com.zdonnell.eve.apilink.APICallback;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.character.APICharacter;
import com.zdonnell.eve.apilink.character.AssetsEntity;
import com.zdonnell.eve.priceservice.PriceService;
import com.zdonnell.eve.staticdata.StaticData;
import com.zdonnell.eve.staticdata.StationInfo;
import com.zdonnell.eve.staticdata.TypeInfo;

/**
 * This Fragment serves as the root assets element.
 * 
 * @author Zach
 *
 */
public class ParentAssetsFragment extends DetailFragment 
{    
    public final static int STATION = 0;
    public final static int ASSET = 1;
		
    /**
     * API Object for requesting assets
     */
	private APICharacter character;
                                    
	/**
	 * Stack of Asset sets that represent each level of assets the 
	 * user has traversed.  Calling {@link Stack#pop} would return
	 * and remove the last set of assets viewed before the current.
	 */
    public Stack<AssetsEntity[]> parentStack = new Stack<AssetsEntity[]>();
    
    /**
     * Stack of AssetEntities that representing the hierarchy of
     * assets that were opened.  Calling {@link Stack#pop} would return
     * the AssetEntity that contains the currently viewed assets.
     */
    Stack<AssetsEntity> parentItemStack = new Stack<AssetsEntity>();
    
    /**
     * Stack of int arrays used to restore the position of the list
     * when the user backs up a level.
     * 
     * @see ListView#setSelectionFromTop
     * @see IAssetsSubFragment#setScrollPoint(int[])
     */
    Stack<int[]> scrollPointStack = new Stack<int[]>();

    /**
     * Reference to the currently displayed Asset Set.  This is used
     * when the sorting is updated, and reset with the new sorted version.
     */
    public AssetsEntity[] currentAssets;
        
    /**
     * Reference to the nested Fragment currently displaying the assets.
     * 
     * @see StationListFragment
     * @see InventoryListFragment
     */
    private IAssetsSubFragment childFragment;
    
    /**
     * Collection of Info for stations.  This data is used by the {@link #childFragment}
     */
    private SparseArray<StationInfo> currentStationInfo = new SparseArray<StationInfo>();
    
    /**
     * Collection of Info for items.  This data is used by the {@link #childFragment}
     */
    private SparseArray<TypeInfo> typeInfo = new SparseArray<TypeInfo>();
    
    /**
     * Collection of prices for items.  This data is used by the {@link #childFragment}
     */
    private SparseArray<Float> prices = new SparseArray<Float>();
    
    /**
     * If the search view is open, this String will be set to the current text in the
     * search view
     * 
     * @see #updateSearchFilter(String)
     */
    private String searchFilter;
        
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {    	
    	int keyID = getArguments().getInt("keyID");
    	String vCode = getArguments().getString("vCode");
    	int characterID = getArguments().getInt("characterID");
    	
    	ApiAuth<?> apiAuth = new ApiAuthorization(keyID, characterID, vCode);
    	character = new APICharacter(context, apiAuth);
    	    	
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets_childfragment_frame, container, false);

    	loadData();
    	
    	return inflatedView;
    }
    
    /**
     * Sets the specified asset as the parent of the current level.
     * (i.e. the container of the assets currently being viewed)
     * 
     * @param asset the parent asset.
     */
    public void setCurrentParent(AssetsEntity asset) 
    { 
    	parentItemStack.push(asset);
    }
    
    /**
     * gets the current parent asset
     * 
     * @return
     */
    public AssetsEntity getCurrentParent() 
    { 
    	try { return parentItemStack.peek(); }
    	catch (Exception e) { return null; }
   	}
    
    /**
     * Called when the assets "screen" needs to change.  This will load in a Fragment of either Station
     * Or Asset Items type.  This can be used to go forward (clicking into an asset) or backward (when the back
     * button is pressed) into parent assets.
     * 
     * @param newAssetsSet
     * @param type
     * @param isBack
     * @param isSearchUpdate
     */
    public void updateChild(AssetsEntity[] newAssetsSet, int type, boolean isBack, boolean isSearchUpdate)
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

    	AssetsEntity[] assetsToPass;
    	if (searchFilter == null) assetsToPass = newAssetsSet;
    	else assetsToPass = searchAssets(newAssetsSet);
    	
    	nextFragment.assetsUpdated(assetsToPass);
    	
    	if (currentAssets != null && !isBack && !isSearchUpdate) 
    	{
    		parentStack.push(currentAssets);
    		scrollPointStack.push(childFragment.getScrollPoint());
    	}
    	currentAssets = newAssetsSet;
    	
    	nextFragment.setParent(this);
    	childFragment = nextFragment;
    	
    	loadNextAssets.replace(R.id.char_detail_assets_childfragment_layoutFrame, (Fragment) nextFragment);
    	loadNextAssets.commit();
	}
    
    /**
     * Filters out locations that are "in space" from the list and then uses the list
     * to get the station and type info for all items.
     * 
     * @param locations
     */
    private void prepareAssets(AssetsEntity[] locations)
    {
    	ArrayList<Integer> stationIDsList = new ArrayList<Integer>(locations.length);
    	
    	// This will filter out items that aren't in a station
    	// TODO enable support for in space items
    	for (AssetsEntity entity : locations)
    	{
    		AssetsEntity.Station station = (AssetsEntity.Station) entity;
    		if (station.getLocationID() > 60000000) stationIDsList.add(station.getLocationID());
    	}
    	
    	Integer[] stationIDs = new Integer[stationIDsList.size()];
    	stationIDsList.toArray(stationIDs);
    	
    	getStationInfo(stationIDs);
    	getTypeInfo(locations);
    }
    
    /**
     * Given a list of stations IDs, obtains the static data information for them
     * 
     * @param stationIDs
     * @see ParentAssetsFragment#currentStationInfo
     */
    private void getStationInfo(final Integer[] stationIDs)
    {
		new StaticData(context).getStationInfo(new APICallback<SparseArray<StationInfo>>((BaseActivity) getActivity()) 
    	{
			@Override
			public void onUpdate(SparseArray<StationInfo> stationInformation) 
			{
				currentStationInfo = stationInformation;
				childFragment.obtainedStationInfo();
			}
    	}, stationIDs);
    }
    
    /**
     * Given the entire list of assets, obtains the static data type information
     * for all possible items.
     * 
     * @param locations
     * @see #typeInfo
     */
    private void getTypeInfo(AssetsEntity[] locations)
    {    	
    	ArrayList<Integer> uniqueTypeIDsList = new ArrayList<Integer>();
    	
    	for (AssetsEntity entity : locations) findUniqueTypeIDs(entity, uniqueTypeIDsList);
    	
    	Integer[] uniqueTypeIDs = new Integer[uniqueTypeIDsList.size()];
    	uniqueTypeIDsList.toArray(uniqueTypeIDs);
    	
    	new StaticData(context).getTypeInfo(new APICallback<SparseArray<TypeInfo>>((BaseActivity) getActivity())
    	{
			@Override
			public void onUpdate(SparseArray<TypeInfo> rTypeInfo) 
			{
				typeInfo = rTypeInfo;
				getPriceInformation(rTypeInfo);
				childFragment.obtainedTypeInfo();
			}
    	}, uniqueTypeIDs);
    }
    
    /**
     * Gets prices for the SparseArray of items provided, filtering out
     * items that are not on the market.
     * 
     * @param typeInfo
     * @see #prices
     */
    private void getPriceInformation(SparseArray<TypeInfo> typeInfo)
    {		
		// Filter out typeIDs that are not in the market
		ArrayList<Integer> marketTypeIDsList = new ArrayList<Integer>();
		for (int i = 0; i < typeInfo.size(); ++i)
		{
			if (typeInfo.valueAt(i).marketGroupID != -1) marketTypeIDsList.add(typeInfo.keyAt(i));
		}
		
		Integer[] marketTypeIDs = new Integer[marketTypeIDsList.size()];
		marketTypeIDsList.toArray(marketTypeIDs);
				
		PriceService.getInstance(context).getValues(marketTypeIDs, new APICallback<SparseArray<Float>>((BaseActivity) getActivity()) 
		{
			@Override
			public void onUpdate(SparseArray<Float> updatedData) 
			{
				prices = updatedData;
				childFragment.obtainedPrices();
			}
		});
    }
    
    /**
     * Finds the unique TypeIDs in the provided asset and each of it's children
     * 
     * @param entity the assets item to start checking through
     * @param uniqueTypeIDsList an ArrayList that the function will use to insert unique values into
     */
    private void findUniqueTypeIDs(AssetsEntity entity, ArrayList<Integer> uniqueTypeIDsList)
    {
    	if (entity instanceof AssetsEntity.Item)
    	{
    		int typeID = entity.attributes().typeID;
    		if (!uniqueTypeIDsList.contains(typeID)) uniqueTypeIDsList.add(typeID);
    	}
    	
    	if (entity.containsAssets())
    	{
    		for (AssetsEntity childEntity : entity.getContainedAssets()) findUniqueTypeIDs(childEntity, uniqueTypeIDsList);
    	}
    }
    
    /**
     * Sorts the current asset list, and updates the sort preferences.
     * 
     * @param sortType
     */
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
    
    /**
     * Used to notify the current {@link #childFragment} that the search
     * string has changed, refresh with the new parameters
     * 
     * @param searchQuery the new string to use as the filter
     */
    public void updateSearchFilter(String searchQuery)
    {
    	searchFilter = searchQuery;
    	updateChild(currentAssets, assetsType(), false, true);
    }

    /**
     * Called to handle back key presses.  If the back key is pressed while at the station
     * listing, the back press will be handled as normal by the system.  However if the user
     * is in a nested set of assets, the back press will take us up one level.
     * 
     * @return
     */
	public boolean backKeyPressed() 
	{
		if (!parentStack.empty()) 
    	{
			AssetsEntity[] assets = parentStack.pop();
			parentItemStack.pop();
			updateChild(assets, parentStack.isEmpty() ? STATION : ASSET, true, false);
			childFragment.setScrollPoint(scrollPointStack.pop());
						
			return true;
    	}
		
		return false;
	}
	
	/**
	 * Searches the provided assets and provides a filtered list of assets
	 * with names matching the current search string.
	 * 
	 * @param assetsToSearch
	 * @return a set of the filtered assets, or an empty set if no assets contained the
	 * search string
	 */
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
	
	/**
	 * Checks to see if the currently specified search string is found in the provided
	 * asset or any of it's children
	 * 
	 * @param entity the asset item to check
	 * @return true if the current asset type name or any of it's children's type name
	 * contains the search string. 
	 * 
	 * @see #searchFilter
	 */
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
			
			if (typeInfo == null || typeInfo.get(item.attributes().typeID) == null) return false;
			
			if (typeInfo.get(item.attributes().typeID).typeName.toLowerCase(Locale.US).contains(searchFilter.toLowerCase(Locale.US))) return true;
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

	@Override
	public void loadData() 
	{				
	    parentStack = new Stack<AssetsEntity[]>();
	    parentItemStack = new Stack<AssetsEntity>();
	    scrollPointStack = new Stack<int[]>();
	    currentStationInfo = new SparseArray<StationInfo>();
	    typeInfo = new SparseArray<TypeInfo>();
	    prices = new SparseArray<Float>();
	    searchFilter = null;
	    
	    character.getAssets(new APIExceptionCallback<AssetListResponse>((BaseActivity) getActivity())
	    {
			@Override
			public void onUpdate(AssetListResponse response) 
			{
				AssetsEntity[] locationArray = eveApiResponseToEdenData(response);
				
				FragmentTransaction loadStationList = ParentAssetsFragment.this.getChildFragmentManager().beginTransaction();

		    	childFragment = new StationListFragment();
		    	childFragment.setParent(ParentAssetsFragment.this);
		    	
		    	loadStationList.replace(R.id.char_detail_assets_childfragment_layoutFrame, (Fragment) childFragment);
		    	loadStationList.commit();
				
				Arrays.sort(locationArray, new InventorySort.Count());
				currentAssets = locationArray;
				
				prepareAssets(locationArray);
				childFragment.assetsUpdated(currentAssets);
			}

			@Override
			public void onError(AssetListResponse response, ApiException exception) 
			{
				
			}
	    });
	}
	
	/**
	 * Takes the {@link AssetListResponse} provided by eveapi and converts it to
	 * the format Eden used pre-eveapi integration.
	 * 
	 * @param response
	 * @return
	 */
	private AssetsEntity[] eveApiResponseToEdenData(AssetListResponse response)
	{
		SparseArray<ArrayList<AssetsEntity>> arrangedAssets = new SparseArray<ArrayList<AssetsEntity>>();
		Set<EveAsset<EveAsset<?>>> rawAssetSet = response.getAll();
				
		for (EveAsset<EveAsset<?>> asset : rawAssetSet)
		{			
			ArrayList<AssetsEntity> assetsAtLocation = arrangedAssets.get(asset.getLocationID().intValue(), new ArrayList<AssetsEntity>());
			assetsAtLocation.add(convertAsset(asset));
			arrangedAssets.put(asset.getLocationID().intValue(), assetsAtLocation);
		}
		
		AssetsEntity[] locationArray = new AssetsEntity[arrangedAssets.size()];
		for (int i = 0; i < arrangedAssets.size(); ++i)
		{
			int locationID = arrangedAssets.keyAt(i);
			ArrayList<AssetsEntity> assetsAtLocation = arrangedAssets.valueAt(i);
						
			locationArray[i] = new AssetsEntity.Station(assetsAtLocation, locationID);
		}
		
		return locationArray;
	}
	
	/**
	 * Converts an eveapi {@link EveAsset} to Eden's native {@link AssetsEntity} including
	 * any contained assets.
	 * 
	 * @param asset
	 * @return
	 */
	private AssetsEntity convertAsset(EveAsset<?> asset)
	{
		ArrayList<AssetsEntity> convertedChildAssets = new ArrayList<AssetsEntity>();
		
		for (EveAsset<?> nestedAsset : asset.getAssets()) convertedChildAssets.add(convertAsset(nestedAsset));
		if (convertedChildAssets.isEmpty()) convertedChildAssets = null;
		
		AssetsEntity.AssetAttributes assetAttributes = new AssetsEntity.AssetAttributes();
		assetAttributes.flag = asset.getFlag();
		assetAttributes.locationID = asset.getLocationID().intValue();
		assetAttributes.quantity = asset.getQuantity();
		assetAttributes.singleton = asset.getSingleton();
		assetAttributes.typeID = asset.getTypeID();
		
		AssetsEntity.Item convertedAsset = new AssetsEntity.Item(convertedChildAssets, assetAttributes);
		
		return convertedAsset;
	}
	
	public int assetsType() 
	{ 
		if (currentAssets.length == 0) return STATION;
		else return (currentAssets[0] instanceof AssetsEntity.Station ? STATION : ASSET);
	}
	
	public SparseArray<StationInfo> getStationInfo() { return currentStationInfo; }
    
    public SparseArray<TypeInfo> getTypeInfo() { return typeInfo; }
    
    public SparseArray<Float> getPrices() { return prices; }
}
