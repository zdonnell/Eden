package com.zdonnell.eve.character.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.SlideMenuFragment;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.eve.Eve;

public class ParentAssetsFragment extends Fragment {
    
    private APICharacter character;
        
    private Context context;
    
    GridView assetsGridView;
    
    private float viewWidth;
            
    private Stack<AssetsEntity[]> parentStack = new Stack<AssetsEntity[]>();
    
    private IAssetsSubFragment childFragment;
        
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
			}
    	});   	
    	
    	return inflatedView;
    }
    
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
    
    /*private class AssetsStationsListAdapter extends ArrayAdapter<AssetsEntity>
    {
    	int resourceID;
    	
    	private AssetsEntity[] entities;
    	
    	SparseArray<String> typeNames;
    	SparseArray<Bitmap> typeIcons;
    	
    	HashMap<TextView, Integer> typeNameMappings = new HashMap<TextView, Integer>();
    	HashMap<ImageView, Integer> typeImageMappings = new HashMap<ImageView, Integer>();
    	
    	boolean typeNamesLoaded = false;
    	    	    	
    	public AssetsStationsListAdapter(Context context, int textViewResourceID, AssetsEntity[] assetLocations) 
    	{
			super(context, textViewResourceID, assetLocations);
			
			this.entities = assetLocations;
			this.resourceID = textViewResourceID;
			
			typeNames = new SparseArray<String>(assetLocations.length);
			typeIcons = new SparseArray<Bitmap>(assetLocations.length);
			
			/* If these are assets not stations, get the type names /
			if (assetLocations[0] instanceof AssetsEntity.Item)
			{
				/* Pull the typeIDs from the array of assets into their own array /
				final int[] typeIDs = new int[assetLocations.length];
				for (int x = 0; x < assetLocations.length; x++) typeIDs[x] = assetLocations[x].attributes().typeID;
				
				/* get type names /
				new Eve(context).getTypeName(new APICallback<String[]>()
				{
					@Override
					public void onUpdate(String[] retTypeNames) 
					{
						/* 
						 * The returned String array matches the order provided by the input typeID array.
						 * This will pair them up in a SparseArray so the type name strings can be accessed by typeID
						 /
						for (int i = 0; i < typeIDs.length; i++) typeNames.put(typeIDs[i], retTypeNames[i]);
						obtainedTypeNames();
					}
				}, typeIDs);
				
				/* "cache" type icons /
				ImageService.getInstance(context).getTypes(null, typeIDs);
			}
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{			
			final AssetsEntity assetsEntity = getItem(position);
			
			LinearLayout itemView; 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			/* Determine if we recyle the old view, or inflate a new one /
			if (convertView == null) itemView = (LinearLayout) inflater.inflate(resourceID, parent, false);
			else itemView = (LinearLayout) convertView;
			
			if (assetsEntity instanceof AssetsEntity.Station)
			{
				setupStation(itemView, (AssetsEntity.Station) getItem(position));
			}
			else if (assetsEntity instanceof AssetsEntity.Item)
			{
				setupAsset(itemView, (AssetsEntity.Item) getItem(position));
			}
				
			itemView.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					if (assetsEntity.containsAssets()) 
					{
						ArrayList<AssetsEntity> containedAssets = assetsEntity.getContainedAssets();
						AssetsEntity[] assetsAsArray = new AssetsEntity[containedAssets.size()];
						containedAssets.toArray(assetsAsArray);
						
						parentStack.push(entities);
						
						AssetsStationsListAdapter assetsAdapter = new AssetsStationsListAdapter(context, R.layout.char_detail_assets_list_item, assetsAsArray);
						assetsGridView.setAdapter(assetsAdapter);
					}
				}
			});
			
			return itemView;
		}
		
		private void obtainedTypeNames()
		{
			typeNamesLoaded = true;
			
			/* Update Type Names /
			for (TextView textView : typeNameMappings.keySet())
			{
				int typeID = typeNameMappings.get(textView);
				textView.setText(typeNames.get(typeID));
			}
		}
		
		private void setupStation(LinearLayout rootView, AssetsEntity.Station station)
		{
			/* Grab references to the views needing update /
			final TextView text = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_name);
			final TextView quantity = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_count);
			
			text.setText("LocationID: " + station.getLocationID());
			
			quantity.setText(String.valueOf(station.getContainedAssets().size()));
		}
		
		private void setupAsset(final LinearLayout rootView, AssetsEntity.Item item)
		{
			boolean isPackaged = !item.attributes().singleton;
			final int typeID = item.attributes().typeID;
			int count = item.attributes().quantity;
			
			/* Grab references to the views needing update /
			final TextView text = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_name);
			final ImageView icon = (ImageView) rootView.findViewById(R.id.char_detail_assets_list_item_typeIcon);
			final TextView quantity = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_count);
			final LinearLayout iconBorder = (LinearLayout) rootView.findViewById(R.id.char_detail_assets_list_item_typeIconBorder);
			
			typeNameMappings.put(text, typeID);
			if (typeNamesLoaded) text.setText(typeNames.get(typeID));
			
			icon.setTag(typeID);
			icon.setImageBitmap(null);
			
			ImageService.getInstance(context).getTypes(new IconObtainedCallback() 
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					if (((Integer) icon.getTag()).intValue() == typeID)
					{											
						//icon.setLayoutParams(new LinearLayout.LayoutParams((int) viewWidth,(int) viewWidth));
						icon.setImageBitmap(bitmaps.get(typeID));						
					}
				}
			}, typeID);
			
			quantity.setVisibility(isPackaged ? View.VISIBLE : View.INVISIBLE);
			iconBorder.setBackgroundColor(isPackaged ? Color.parseColor("#666666") : Color.parseColor("#aaaaaa"));
			
			if (isPackaged) quantity.setText(String.valueOf(count));
		}
    }*/

	public boolean backKeyPressed() {
		
		if (!parentStack.empty()) 
    	{
    		
    	}
		
		return false;
	}
}
