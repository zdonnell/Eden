package com.zdonnell.eve.character.detail;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.eve.Eve;

public class AssetsListFragment extends Fragment {
    
    private APICharacter character;
        
    private Context context;
    
    GridView assetsGridView;
    
    private ImageService imageService;
        
    /**
     * Constructor
     * 
     * @param character the {@link APICharacter} to build the Attribute info from
     */
    public AssetsListFragment(APICharacter character) 
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
    	imageService = ImageService.getInstance(context);
    	
    	
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets, container, false);
    	assetsGridView = (GridView) inflatedView.findViewById(R.id.char_detail_assets_list);
    	
    	character.getAssetsList(new APICallback<AssetsEntity[]>()
    	{
			@Override
			public void onUpdate(AssetsEntity[] locationArray) 
			{
				AssetsStationsListAdapter adapter = new AssetsStationsListAdapter(context, R.layout.char_detail_assets_list_item, locationArray);
				assetsGridView.setAdapter(adapter);
			}
    	});
    	    	
    	return inflatedView;
    }    
    
    private class AssetsStationsListAdapter extends ArrayAdapter<AssetsEntity>
    {
    	int resourceID;
    	
    	SparseArray<String> typeNames;
    	
    	HashMap<TextView, Integer> typeNameMappings = new HashMap<TextView, Integer>();
    	
    	boolean typeNamesLoaded = false;
    	    	    	
    	public AssetsStationsListAdapter(Context context, int textViewResourceID, AssetsEntity[] assetLocations) {
			super(context, textViewResourceID, assetLocations);
			this.resourceID = textViewResourceID;
			
			typeNames = new SparseArray<String>(assetLocations.length);
			
			if (assetLocations[0] instanceof AssetsEntity.Item)
			{
				final int[] typeIDs = new int[assetLocations.length];
				for (int x = 0; x < assetLocations.length; x++) typeIDs[x] = assetLocations[x].attributes().typeID;
				
				new Eve(context).getTypeName(new APICallback<String[]>()
				{
					@Override
					public void onUpdate(String[] retTypeNames) 
					{
						for (int i = 0; i < typeIDs.length; i++) typeNames.put(typeIDs[i], retTypeNames[i]);
						updateDisplayedViews();
					}
				}, typeIDs);
			}
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{			
			final AssetsEntity assetsEntity = getItem(position);
			
			LinearLayout itemView; 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			/* Determine if we recyle the old view, or inflate a new one */
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
						
						AssetsStationsListAdapter assetsAdapter = new AssetsStationsListAdapter(context, R.layout.char_detail_assets_list_item, assetsAsArray);
						assetsGridView.setAdapter(assetsAdapter);
					}
				}
			});
			
			return itemView;
		}
		
		private void updateDisplayedViews()
		{
			typeNamesLoaded = true;
			
			/* Update Type Names */
			for (TextView textView : typeNameMappings.keySet())
			{
				int typeID = typeNameMappings.get(textView);
				textView.setText(typeNames.get(typeID));
			}
		}
		
		private void setupStation(LinearLayout rootView, AssetsEntity.Station station)
		{
			/* Grab references to the views needing update */
			final TextView text = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_name);
			final TextView quantity = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_count);
			
			text.setText("LocationID: " + station.getLocationID());
			
			quantity.setText(String.valueOf(station.getContainedAssets().size()));
		}
		
		private void setupAsset(LinearLayout rootView, AssetsEntity.Item item)
		{
			boolean isPackaged = !item.attributes().singleton;
			int typeID = item.attributes().typeID;
			int count = item.attributes().quantity;
			
			/* Grab references to the views needing update */
			final TextView text = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_name);
			final ImageView icon = (ImageView) rootView.findViewById(R.id.char_detail_assets_list_item_typeIcon);
			final TextView quantity = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_count);
			final LinearLayout iconBorder = (LinearLayout) rootView.findViewById(R.id.char_detail_assets_list_item_typeIconBorder);
			
			typeNameMappings.put(text, typeID);
			if (typeNamesLoaded) text.setText(typeNames.get(typeID));
			
			icon.setTag(typeID);
			
			ImageService.getInstance(context).getTypes(new ImageService.IconObtainedCallback() 
			{	
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					if (bitmaps.get((Integer) icon.getTag()) != null);
					{
						icon.setLayoutParams(new LinearLayout.LayoutParams(icon.getWidth(), icon.getWidth()));
						icon.setImageBitmap(bitmaps.valueAt(0));
					}
				}
			}, typeID);
			
			quantity.setVisibility(isPackaged ? View.VISIBLE : View.INVISIBLE);
			iconBorder.setBackgroundColor(isPackaged ? Color.parseColor("#666666") : Color.parseColor("#aaaaaa"));
			
			if (isPackaged) quantity.setText(String.valueOf(count));
		}
    }
}
