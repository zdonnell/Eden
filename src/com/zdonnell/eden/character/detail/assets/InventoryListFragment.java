package com.zdonnell.eden.character.detail.assets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zdonnell.androideveapi.link.character.AssetsEntity;
import com.zdonnell.androideveapi.link.character.AssetsEntity.Item;
import com.zdonnell.eden.R;
import com.zdonnell.eden.TypeInfoActivity;
import com.zdonnell.eden.helpers.BasicOnTouchListener;
import com.zdonnell.eden.helpers.ImageURL;
import com.zdonnell.eden.staticdata.StationInfo;
import com.zdonnell.eden.staticdata.TypeInfo;

public class InventoryListFragment extends Fragment implements IAssetsSubFragment
{
	private static final int GRID = 0;
	private static final int LIST = 1;
	private static final int LIST_COMPACT = 2;
	
	public static final String[] layoutTypes = new String[3];
	static 
	{
		layoutTypes[GRID] = "Grid";
		layoutTypes[LIST] = "List";
		layoutTypes[LIST_COMPACT] = "Compact List";
	}
	
	/**
	 * Refrence to the layout to use for list item construction
	 */
	private int stationRowResourceID = R.layout.char_detail_assets_list_item;
	
	private int displayType;
	
	private LinearLayout rootView;
	
	/**
	 * Fragment Context (Activity Context)
	 */
	private Context context;
	
	/**
	 * Array of stations
	 */
	private AssetsEntity[] currentItemList;
		
	/**
	 * The main list view for the station list layout
	 */
	private AbsListView absListView;
	
	private boolean isFragmentCreated = false;
	
	private boolean initialLoadComplete = false;
	
	private ParentAssetsFragment parentFragment;
	
	private TextView parentAssetName, itemCount, valueOfItems;
	
	private ImageView parentAssetIcon;
	
	private InventoryArrayAdapter adapter;
	
	private int[] savedScrollPoint;
	
	public void setParent(ParentAssetsFragment parent)
	{
		this.parentFragment = parent;
	}
	
	public void assetsUpdated(AssetsEntity[] assets) 
	{			
		this.currentItemList = assets;
		if (isFragmentCreated) updateView();
	}
	
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	if (parentFragment == null) parentFragment = (ParentAssetsFragment) getParentFragment();
		
		context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_assets, container, false);
    	this.rootView = inflatedView;
    	
    	setupMainView(inflatedView, -1); /* passing -1 triggers the layout type to be determined from saved prefs */
    	updateParentInfo();
    	
    	itemCount = (TextView) inflatedView.findViewById(R.id.char_detail_assets_inventory_itemCount);
    	valueOfItems = (TextView) inflatedView.findViewById(R.id.char_detail_assets_inventory_itemValue);
    	
    	//parentAssetName.setText(parentFragment.getCurrentParentName());
    	
    	if (!initialLoadComplete && currentItemList != null) updateView();
    	
    	isFragmentCreated = true;
    	return inflatedView;
    }
	
	private void updateParentInfo()
	{
    	parentAssetName = (TextView) rootView.findViewById(R.id.char_detail_assets_inventory_parentName);
    	parentAssetIcon = (ImageView) rootView.findViewById(R.id.char_detail_assets_inventory_parentIcon);
		
		AssetsEntity parent = parentFragment.getCurrentParent();
		
		if (parent instanceof AssetsEntity.Station)
		{
			AssetsEntity.Station parentStation = (AssetsEntity.Station) parent;		
			StationInfo parentStationInfo = parentFragment.getStationInfo().get(parentStation.getLocationID());

			if (parentStationInfo != null)
			{
				parentAssetName.setText(parentStationInfo.stationName);
				
				ImageLoader.getInstance().displayImage(ImageURL.forType(parentStationInfo.stationTypeID), parentAssetIcon);
			}
		}
		else if (parent instanceof AssetsEntity.Item)
		{
			AssetsEntity.Item parentItem = (AssetsEntity.Item) parent;
			TypeInfo parentItemInfo = parentFragment.getTypeInfo().get(parentItem.attributes().typeID);
			
			if (parentItemInfo != null) parentAssetName.setText(parentItemInfo.typeName);

			ImageLoader.getInstance().displayImage(ImageURL.forType(parentItem.attributes().typeID), parentAssetIcon);
		}
	}
	
	private void setupMainView(LinearLayout rootView, int layoutType)
	{
		SharedPreferences prefs = context.getSharedPreferences("eden_assets_preferences", Context.MODE_PRIVATE);
		
		if (layoutType < 0) displayType = prefs.getInt("display_type", GRID);
		else 
		{
			displayType = layoutType;
			prefs.edit().putInt("display_type", layoutType).commit();
		}
		
		GridView gridView = (GridView) rootView.findViewById(R.id.char_detail_assets_grid);
		ListView listView = (ListView) rootView.findViewById(R.id.char_detail_assets_list);
		
		listView.setVisibility(View.GONE);
		gridView.setVisibility(View.GONE);
		
		switch (displayType)
		{
		case GRID:
	    	absListView = gridView;
	    	((GridView) absListView).setNumColumns(4);
	    	stationRowResourceID = R.layout.char_detail_assets_grid_item;
			break;
	    	
		case LIST:
	    	absListView = listView;
	    	stationRowResourceID = R.layout.char_detail_assets_list_item;
			break;
			
		case LIST_COMPACT:
	    	absListView = listView;
	    	stationRowResourceID = R.layout.char_detail_assets_listcompact_item;
			break;
		}
		
		absListView.setVisibility(View.VISIBLE);
	}
	
	private void updateView()
	{
		if (adapter == null)
		{				
			itemCount.setText(currentItemList.length + " items");
					
			adapter = new InventoryArrayAdapter(context, stationRowResourceID, currentItemList);
			absListView.setAdapter(adapter);
		
			if (parentFragment.getPrices() != null && parentFragment.getPrices().size() > 0) calculatePrices(currentItemList);
			
			initialLoadComplete = true;
		}
		else
		{
			adapter = new InventoryArrayAdapter(context, stationRowResourceID, currentItemList);
			absListView.setAdapter(adapter);
		}
		
		if (savedScrollPoint != null)
		{
			if (absListView instanceof ListView) ((ListView) absListView).setSelectionFromTop(savedScrollPoint[0], savedScrollPoint[1]);		
			else if (absListView instanceof GridView) ((GridView) absListView).setSelection(savedScrollPoint[0]);
			savedScrollPoint = null;
		}
	}
	
	private void calculatePrices(final AssetsEntity[] items)
	{
		final SparseIntArray typeIDsCount = new SparseIntArray();
				
		for (int i = 0; i < items.length; i++)
		{
			int typeID = items[i].attributes().typeID;
			
			/* The item is on the market, and has a price */
			if (parentFragment.getTypeInfo().get(typeID) != null && parentFragment.getTypeInfo().get(typeID).marketGroupID != -1)
			{				
				int currentTypeCount = typeIDsCount.get(typeID);
				typeIDsCount.put(typeID, currentTypeCount + items[i].attributes().quantity);
			}
			
			if (items[i].containsAssets()) countChildAssets(items[i], typeIDsCount);
		}

		double totalValue = 0;
		
		for (int i = 0; i < typeIDsCount.size(); ++i)
		{
			int typeID = typeIDsCount.keyAt(i);
			int quantity = typeIDsCount.get(typeID);
			
			try 
			{
				float value = parentFragment.getPrices().get(typeID);			
				totalValue += value * quantity;
			}
			catch (NullPointerException e) { /* e.printStackTrace(); */ }
		}
		
		DecimalFormat twoDForm = new DecimalFormat("#,###.##");				
		valueOfItems.setText(twoDForm.format(totalValue) + " ISK");
	}
	
	private void countChildAssets(AssetsEntity parent, SparseIntArray typeIDsCount)
	{
		ArrayList<AssetsEntity> containedAssets = parent.getContainedAssets();
				
		for (AssetsEntity entity : containedAssets)
		{
			int typeID = entity.attributes().typeID;
			
			/* The item is on the market, and has a price */
			if (parentFragment.getTypeInfo() != null && parentFragment.getTypeInfo().get(typeID) != null && parentFragment.getTypeInfo().get(typeID).marketGroupID != -1)
			{				
				int currentTypeCount = typeIDsCount.get(typeID);
				typeIDsCount.put(typeID, currentTypeCount + entity.attributes().quantity);
			}
			
			if (entity.containsAssets()) countChildAssets(entity, typeIDsCount);			
		}
	}
	
	private class InventoryArrayAdapter extends ArrayAdapter<AssetsEntity>
	{
		private int layoutResID;
		
		private LayoutInflater inflater;
		
		DecimalFormat twoDForm = new DecimalFormat("#,###");				
		    	
    	HashMap<TextView, Integer> typeNameMappings = new HashMap<TextView, Integer>();
    	HashMap<TextView, Integer> typeValueMappings = new HashMap<TextView, Integer>();
    	
    	boolean typeNamesLoaded = (parentFragment.getTypeInfo() != null && parentFragment.getTypeInfo().size() > 0);
    	boolean typeValuesLoaded = (parentFragment.getPrices() != null && parentFragment.getPrices().size() > 0);
    	
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
				public void onClick(View v) 
				{
					if (assetItem.containsAssets()) 
					{
						ArrayList<AssetsEntity> listAssets = assetItem.getContainedAssets();
						AssetsEntity[] subAssets = new AssetsEntity[listAssets.size()];
						
						listAssets.toArray(subAssets);
						
						parentFragment.setCurrentParent(assetItem);
						parentFragment.updateChild(subAssets, 1, false, false);
					}
				}
			});
			
			if (assetItem.containsAssets()) 
			{
				itemView.setOnTouchListener(new BasicOnTouchListener());
			}
			
			final Intent intent = new Intent(context, TypeInfoActivity.class);
			intent.putExtra("typeID", assetItem.attributes().typeID);
			
			itemView.setOnLongClickListener(new View.OnLongClickListener() 
			{	
				public boolean onLongClick(View v) 
				{
	            	startActivity(intent);
					return true;
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
				textView.setText(parentFragment.getTypeInfo().get(typeID).typeName);
			}			
		}
		
		public void obtainedTypeValues()
		{
			typeNamesLoaded = true;
			
			/* Update Type Names */
			for (TextView textView : typeValueMappings.keySet())
			{
				int typeID = typeValueMappings.get(textView);
				textView.setText(parentFragment.getTypeInfo().get(typeID).typeName);
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
			
			typeNameMappings.put(text, typeID);
			
			if (typeNamesLoaded) 
			{
				if (parentFragment.getTypeInfo().get(typeID) != null) text.setText(parentFragment.getTypeInfo().get(typeID).typeName);
				else text.setText("Type ID: " + typeID);
			}
			else text.setText(String.valueOf(typeID));
			
			if (displayType != LIST_COMPACT) configureIcon(icon, typeID);
			if (displayType == LIST)
			{
				final TextView iskValue = (TextView) rootView.findViewById(R.id.char_detail_assets_list_item_value);
				
				if (typeValuesLoaded)
				{
					iskValue.setText(twoDForm.format(calculateValue(item)) + " ISK");
				}
			}
			
			quantity.setVisibility(isPackaged ? View.VISIBLE : View.GONE);
			
			if (isPackaged) 
			{
				if (displayType == GRID)
				{
					quantity.setText(String.valueOf(count));
				}
				else
				{
					quantity.setText("x " + String.valueOf(twoDForm.format(count)));
				}
			}
		}
		
		private float calculateValue(AssetsEntity entity)
		{
			float totalValue = 0;
			
			try { totalValue = parentFragment.getPrices().get(entity.attributes().typeID) * entity.attributes().quantity; }
			catch (NullPointerException e) { /* just catch the error if the type doesn't have a value */ }
			
			if (entity.containsAssets())
			{
				for (AssetsEntity childEntity : entity.getContainedAssets())
				{
					totalValue += calculateValue(childEntity);
				}
			}
			
			return totalValue;
		}
		
		private void configureIcon(final ImageView icon, final int typeID)
		{
			icon.setTag(typeID);
			icon.setImageBitmap(null);
			
			ImageLoader.getInstance().displayImage(ImageURL.forType(typeID), icon);
			
			if (displayType == GRID)
			{
				float colWidthWithPadding = (float) absListView.getWidth() / (float) ((GridView) absListView).getNumColumns();
				int imageWidth = (int) (colWidthWithPadding - (absListView.getPaddingLeft() + absListView.getPaddingRight()));
				
				icon.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));
			}
		}
	}

	public SparseArray<String> getNames() 
	{		
		SparseArray<TypeInfo> typeInfo = parentFragment.getTypeInfo();
		SparseArray<String> typeNames = new SparseArray<String>(typeInfo.size());
		
		for (int i = 0; i < typeInfo.size(); ++i)
		{
			typeNames.put(typeInfo.keyAt(i), typeInfo.valueAt(i).typeName);
		}
		
		return typeNames;
	}

	public SparseArray<Float> getValues() 
	{
		return parentFragment.getPrices();
	}

	public void obtainedPrices() 
	{
		adapter.obtainedTypeValues();
		calculatePrices(currentItemList);
	}

	public void obtainedTypeInfo() 
	{
		adapter.obtainedTypeNames();
		updateParentInfo();
	}

	public void updateLayoutStyle(int type) 
	{
		setupMainView(rootView, type);
		adapter = null; /* force refresh */
		updateView();
	}

	public void obtainedStationInfo() 
	{
		updateParentInfo();
	}

	public int[] getScrollPoint() 
	{
		int index = absListView.getFirstVisiblePosition();
	    View v = absListView.getChildAt(0);
	    int top = (v == null) ? 0 : v.getTop();
		
		return new int[] { index, top };
	}

	public void setScrollPoint(int[] scrollPoint) 
	{
		if (adapter != null)
		{
			if (absListView instanceof ListView) ((ListView) absListView).setSelectionFromTop(scrollPoint[0], scrollPoint[1]);		
			else if (absListView instanceof GridView) ((GridView) absListView).setSelection(scrollPoint[0]);
		}
		else
		{
			savedScrollPoint = scrollPoint;	
		}
	}
}
