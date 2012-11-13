package com.zdonnell.eve.character.detail;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
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
    	    	    	
    	public AssetsStationsListAdapter(Context context, int textViewResourceID, AssetsEntity[] assetLocations) {
			super(context, textViewResourceID, assetLocations);
			this.resourceID = textViewResourceID;
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
				
			/* Grab references to the views needing update */
			TextView text = (TextView) itemView.findViewById(R.id.char_detail_assets_list_item_name);
			
			if (assetsEntity instanceof AssetsEntity.Station)
			{
				AssetsEntity.Station station = (AssetsEntity.Station) assetsEntity;
				text.setText("LocationID: " + station.getLocationID());			
			}
			else if (assetsEntity instanceof AssetsEntity.Item)
			{
				
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
						
						AssetsListAdapter assetsAdapter = new AssetsListAdapter(context, R.layout.char_detail_assets_list_item, assetsAsArray);
						assetsGridView.setAdapter(assetsAdapter);
					}
				}
			});
			
			return itemView;
		}
    }
    
    private class AssetsListAdapter extends ArrayAdapter<AssetsEntity>
    {
    	int resourceID;
    	
    	private AssetsEntity[] assetsList;
    	
    	public AssetsListAdapter(Context context, int textViewResourceID, AssetsEntity[] assetLocations) {
			super(context, textViewResourceID, assetLocations);
			this.resourceID = textViewResourceID;
			this.assetsList = assetLocations;
		}
    			
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{			
			LinearLayout itemView; 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			/* Determine if we recyle the old view, or inflate a new one */
			if (convertView == null) itemView = (LinearLayout) inflater.inflate(resourceID, parent, false);
			else itemView = (LinearLayout) convertView;
				
			/* Grab references to the views needing update */
			final TextView text = (TextView) itemView.findViewById(R.id.char_detail_assets_list_item_name);
			final ImageView icon = (ImageView) itemView.findViewById(R.id.char_detail_assets_list_item_typeIcon);
			final TextView quantity = (TextView) itemView.findViewById(R.id.char_detail_assets_list_item_count);
			
			new Eve(context).getTypeName(new APICallback<String[]>(){

				@Override
				public void onUpdate(String[] updatedData) {
					text.setText(updatedData[0]);
				}
				
			}, new int[] { assetsList[position].attributes().typeID });
			
			text.setText("typeID: " + assetsList[position].attributes().typeID);
			itemView.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					if (assetsList[position].containsAssets())
					{
						ArrayList<AssetsEntity> containedAssets = assetsList[position].getContainedAssets();
						AssetsEntity[] assetsAsArray = new AssetsEntity[containedAssets.size()];
						containedAssets.toArray(assetsAsArray);
						
						AssetsListAdapter assetsAdapter = new AssetsListAdapter(context, R.layout.char_detail_assets_list_item, assetsAsArray);
						assetsGridView.setAdapter(assetsAdapter);
					}
				}
			});
			
			icon.setTag(assetsList[position].attributes().typeID);
			
			new ImageService(context).getTypes(new ImageService.IconObtainedCallback() {
				
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) {
					if (bitmaps.get((Integer) icon.getTag()) != null);
					{
						icon.setImageBitmap(bitmaps.valueAt(0));
						icon.setLayoutParams(new LinearLayout.LayoutParams(icon.getWidth(), icon.getWidth()));
					}
				}
			}, assetsList[position].attributes().typeID);
			
			if (assetsList[position].attributes().singleton) quantity.setVisibility(View.INVISIBLE);
			else
			{
				quantity.setText(assetsList[position].attributes().quantity + "");
			}
			
			return itemView;
		}
    }
}
