package com.zdonnell.eve.character.detail;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.character.AssetsEntity.Item;
import com.zdonnell.eve.api.priceservice.PriceService;
import com.zdonnell.eve.staticdata.api.StaticData;
import com.zdonnell.eve.staticdata.api.TypeInfo;

public class InventoryListFragment extends Fragment implements IAssetsSubFragment 
{
	
	/**
	 * Refrence to the layout to use for list item construction
	 */
	private final int stationRowResourceID = R.layout.char_detail_assets_list_item;
	
	/**
	 * Fragment Context (Activity Context)
	 */
	private Context context;
	
	/**
	 * Array of stations
	 */
	private AssetsEntity[] currentItemList;
	
	private SparseArray<String> currentTypeNames = new SparseArray<String>();
	
	private SparseArray<TypeInfo> currentTypeInfo;
	
	private SparseArray<Float> currentValues;

	
	/**
	 * The main list view for the station list layout
	 */
	private GridView itemGridView;
	
	private boolean isFragmentCreated = false;
	
	private boolean initialLoadComplete = false;
	
	private ParentAssetsFragment parentFragment;
	
	private TextView parentAssetName, itemCount, valueOfItems;
	
	private InventoryArrayAdapter adapter;
	
	@Override
	public void setParent(ParentAssetsFragment parent)
	{
		this.parentFragment = parent;
	}
	
	@Override
	public void assetsUpdated(AssetsEntity[] assets) 
	{			
		this.currentItemList = assets;
		if (isFragmentCreated) updateGridView();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets, container, false);
    	
    	itemGridView = (GridView) inflatedView.findViewById(R.id.char_detail_assets_list);
    	itemGridView.setNumColumns(4);
    	
    	parentAssetName = (TextView) inflatedView.findViewById(R.id.char_detail_assets_inventory_parentName);
    	itemCount = (TextView) inflatedView.findViewById(R.id.char_detail_assets_inventory_itemCount);
    	valueOfItems = (TextView) inflatedView.findViewById(R.id.char_detail_assets_inventory_itemValue);
    	
    	parentAssetName.setText(parentFragment.getCurrentParentName());
    	
    	if (!initialLoadComplete && currentItemList != null) updateGridView();
    	
    	isFragmentCreated = true;
    	return inflatedView;
    }
	
	private void updateGridView()
	{
		if (adapter == null)
		{	
			itemCount.setText(currentItemList.length + " items");
					
			adapter = new InventoryArrayAdapter(context, stationRowResourceID, currentItemList);
			
			/* Pull the typeIDs from the array of assets into their own array */
			final Integer[] typeIDs = new Integer[currentItemList.length];
			for (int x = 0; x < currentItemList.length; x++) typeIDs[x] = currentItemList[x].attributes().typeID;
			
			new StaticData(context).getTypeInfo(new APICallback<SparseArray<TypeInfo>>()
			{
				@Override
				public void onUpdate(SparseArray<TypeInfo> retTypeInfo) 
				{					
					/* 
					 * The returned String array matches the order provided by the input typeID array.
					 * This will pair them up in a SparseArray so the type name strings can be accessed by typeID
					 */
					for (int i = 0; i < typeIDs.length; i++) currentTypeNames.put(typeIDs[i], retTypeInfo.get(typeIDs[i]).typeName);
					
					currentTypeInfo = retTypeInfo;
					adapter.obtainedTypeNames();
					
					calculatePrices(currentItemList);
				}
			}, typeIDs);
			
			itemGridView.setAdapter(adapter);
			initialLoadComplete = true;
		}
		
		parentFragment.getActivity().runOnUiThread(new Runnable() 
		{
	        @Override
	        public void run() 
	        {
	        	adapter.notifyDataSetChanged();
	        }
	    });
	}
	
	private void calculatePrices(final AssetsEntity[] items)
	{
		ArrayList<Integer> typeIDsList = new ArrayList<Integer>();
		final SparseIntArray typeIDsCount = new SparseIntArray();
		
		for (int i = 0; i < items.length; i++)
		{
			int typeID = items[i].attributes().typeID;
			
			/* The item is on the market, and has a price */
			if (currentTypeInfo.get(typeID).marketGroupID != -1)
			{
				if (!typeIDsList.contains(typeID)) typeIDsList.add(typeID);
				
				int currentTypeCount = typeIDsCount.get(typeID);
				typeIDsCount.put(typeID, currentTypeCount + items[i].attributes().quantity);
			}
			
		}
		
		final Integer[] typeIDs = new Integer[typeIDsList.size()];
		typeIDsList.toArray(typeIDs);
		
		PriceService.getInstance(context).getValues(typeIDs, new APICallback<SparseArray<Float>>() 
		{
			@Override
			public void onUpdate(SparseArray<Float> updatedData)
			{				
				currentValues = updatedData;
				
				double totalValue = 0;

				for (int typeID : typeIDs)
				{
					int quantity = typeIDsCount.get(typeID);
					
					try 
					{
						float value = updatedData.get(typeID);			
						totalValue += value * quantity;
					}
					catch (NullPointerException e) { /* e.printStackTrace(); */ }
				}
				
				DecimalFormat twoDForm = new DecimalFormat("#,###.##");				
				valueOfItems.setText(twoDForm.format(totalValue) + " ISK");
			}
		});
	}
	
	private class InventoryArrayAdapter extends ArrayAdapter<AssetsEntity>
	{
		private int layoutResID;
		
		private LayoutInflater inflater;
		    	
    	HashMap<TextView, Integer> typeNameMappings = new HashMap<TextView, Integer>();
    	
    	boolean typeNamesLoaded = false;
    	
		public InventoryArrayAdapter(Context context, int layoutResourceID, AssetsEntity[] items) 
		{
			super(context, layoutResourceID, items);
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layoutResID = layoutResourceID;			
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LinearLayout itemView;
			
			/* Determine if we recyle the old view, or inflate a new one */
			if (convertView == null) itemView = (LinearLayout) inflater.inflate(layoutResID, parent, false);
			else itemView = (LinearLayout) convertView;
			
			final Item assetItem = (Item) getItem(position);
			
			setupAsset(itemView, assetItem);
			
			itemView.setOnClickListener(new View.OnClickListener() 
			{	
				@Override
				public void onClick(View v) 
				{
					if (assetItem.containsAssets()) 
					{
						ArrayList<AssetsEntity> listAssets = assetItem.getContainedAssets();
						AssetsEntity[] subAssets = new AssetsEntity[listAssets.size()];
						
						listAssets.toArray(subAssets);
						
						parentFragment.setCurrentParentName(currentTypeNames.get(assetItem.attributes().typeID));
						parentFragment.updateChild(subAssets, 1, false);
					}
				}
			});
			
			return itemView;
		}
		
		public void obtainedTypeNames()
		{
			typeNamesLoaded = true;
			
			/* Update Type Names */
			for (TextView textView : typeNameMappings.keySet())
			{
				int typeID = typeNameMappings.get(textView);
				textView.setText(currentTypeNames.get(typeID));
			}			
		}
		
		private void setupAsset(final LinearLayout rootView, AssetsEntity.Item item)
		{
			boolean isPackaged = !item.attributes().singleton;
			final int typeID = item.attributes().typeID;
			int count = item.attributes().quantity;
			
			/* Grab references to the views needing update */
			final TextView text = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_name);
			final ImageView icon = (ImageView) rootView.findViewById(R.id.char_detail_assets_list_item_typeIcon);
			final TextView quantity = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_count);
			final LinearLayout iconBorder = (LinearLayout) rootView.findViewById(R.id.char_detail_assets_list_item_typeIconBorder);
			
			typeNameMappings.put(text, typeID);
			if (typeNamesLoaded) text.setText(currentTypeNames.get(typeID));
			
			icon.setTag(typeID);
			icon.setImageBitmap(null);
			
			ImageService.getInstance(context).getTypes(new IconObtainedCallback() 
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					if (((Integer) icon.getTag()).intValue() == typeID)
					{											
						icon.setImageBitmap(bitmaps.get(typeID));	
						
						float colWidthWithPadding = (float) itemGridView.getWidth() / (float) itemGridView.getNumColumns();
						int imageWidth = (int) (colWidthWithPadding - (itemGridView.getPaddingLeft() + itemGridView.getPaddingRight()));
						
						icon.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));
					}
				}
			}, typeID);
			
			quantity.setVisibility(isPackaged ? View.VISIBLE : View.INVISIBLE);
			iconBorder.setBackgroundColor(isPackaged ? Color.parseColor("#000000") : Color.parseColor("#000000"));
			
			if (isPackaged) quantity.setText(String.valueOf(count));
		}
	}

	@Override
	public SparseArray<String> getNames() 
	{			
		return currentTypeNames;
	}

	@Override
	public SparseArray<Float> getValues() 
	{
		return currentValues;
	}
}
