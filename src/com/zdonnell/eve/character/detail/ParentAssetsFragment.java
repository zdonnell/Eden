package com.zdonnell.eve.character.detail;

import java.util.Stack;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.AssetsEntity;

public class ParentAssetsFragment extends Fragment {
    
    private APICharacter character;
        
    private Context context;
    
    GridView assetsGridView;
    
    private float viewWidth;
            
    private Stack<AssetsEntity[]> parentStack = new Stack<AssetsEntity[]>();
    
    private IAssetsSubFragment childFragment;
    
    private String currentParentName;
        
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
				childFragment.assetsUpdated(locationArray);
				//calculateAssetValue(locationArray);
			}
    	});   	
    	
    	return inflatedView;
    }
    
    public void setCurrentParentName(String name) { this.currentParentName = name; }
    public String getCurrentParentName() { return currentParentName; }

    
    /*private void calculateAssetValue(AssetsEntity[] assets)
    {
    	ArrayList<Integer> uniqueTypeIDs = new ArrayList<Integer>();
    	
    	for (AssetsEntity entity : assets)
    	{
    		uniqueTypeIDs.addAll(grabNestedTypeIDs(entity));
    	}
    	
    	
    }
    
    private ArrayList<Integer> grabNestedTypeIDs(AssetsEntity entity)
    {
    	ArrayList<Integer> nestedTypeIDs = new ArrayList<Integer>();
    	
    	if (entity instanceof AssetsEntity.Station)
    	{
    		AssetsEntity.Station station = (AssetsEntity.Station) entity;
    		if (station.containsAssets())
    		{
    			for (AssetsEntity subEntity : station.getContainedAssets()) nestedTypeIDs.addAll(grabNestedTypeIDs(subEntity));
    		}
    	}
    	else if (entity instanceof AssetsEntity.Item)
    	{
    		AssetsEntity.Item item = (AssetsEntity.Item) entity;
    		int typeID = item.attributes().typeID;
    		
    		if (!nestedTypeIDs.contains(typeID)) nestedTypeIDs.add(typeID);
    		
    		if (item.containsAssets())
    		{
    			for (AssetsEntity subEntity : item.getContainedAssets()) nestedTypeIDs.addAll(grabNestedTypeIDs(subEntity));
    		}
    	}
    	
    	return nestedTypeIDs;
    }*/
    
    public void updateChild(AssetsEntity[] newAssetsSet, int type)
    {
    	FragmentTransaction loadNextAssets = this.getChildFragmentManager().beginTransaction();
    	IAssetsSubFragment nextFragment = null;
    	
    	switch (type)
    	{
    	case 0: // Station
    		nextFragment = new StationListFragment();
    		break;
    	case 1: // Item
    		nextFragment = new InventoryListFragment();
    		break;
    	}
    	
    	nextFragment.assetsUpdated(newAssetsSet);
    	nextFragment.setParent(this);
    	
    	loadNextAssets.replace(R.id.char_detail_assets_childfragment_layoutFrame, (Fragment) nextFragment);
    	loadNextAssets.commit();
    }

	public boolean backKeyPressed() {
		
		if (!parentStack.empty()) 
    	{
    		
    	}
		
		return false;
	}
}
