package com.zdonnell.eve.character.detail;

import java.util.Comparator;

import android.util.SparseArray;

import com.zdonnell.eve.api.character.AssetsEntity;

public class InventorySort 
{
	public static final int COUNT = 0;
	public static final int COUNT_REVERSE = 1;
	public static final int ALPHA = 2;
	public static final int ALPHA_REVERSE = 3;
	public static final int VALUE = 4;
	public static final int VALUE_REVERSE = 5;
	
	public static final String[] sortNames = new String[6];
	static
	{
		sortNames[COUNT] = "Quantity";
		sortNames[COUNT_REVERSE] = "Quantity (Reversed)";
		sortNames[ALPHA] = "Alphabetical";
		sortNames[ALPHA_REVERSE] = "Alphabetical (Reversed)";	
		sortNames[VALUE] = "ISK Value";
		sortNames[VALUE_REVERSE] = "ISK Value (Reversed)";	
	}
	
	public static class Count implements Comparator<AssetsEntity>
	{
		@Override
		public int compare(AssetsEntity lhs, AssetsEntity rhs) 
		{
			if (lhs instanceof AssetsEntity.Item)
			{
				AssetsEntity.Item leftItem = (AssetsEntity.Item) lhs;
				AssetsEntity.Item rightItem = (AssetsEntity.Item) rhs;
				
				if (leftItem.attributes().quantity < rightItem.attributes().quantity) return 1;
				else if (leftItem.attributes().quantity > rightItem.attributes().quantity) return -1;
				else return 0;
			}
			else
			{
				AssetsEntity.Station leftStation = (AssetsEntity.Station) lhs;
				AssetsEntity.Station rightStation = (AssetsEntity.Station) rhs;
				
				if (leftStation.getContainedAssets().size() < rightStation.getContainedAssets().size()) return 1;
				else if (leftStation.getContainedAssets().size() > rightStation.getContainedAssets().size()) return -1;
				else return 0;
			}
		}
	}
	
	public static class Alpha implements Comparator<AssetsEntity>
	{
		/**
		 * Stores the names of the items, to use for comparison.  This information is not in the AssetEntity
		 */
		private SparseArray<String> typeNames;
		
		
		public Alpha(SparseArray<String> typeNames)
		{
			super();
			this.typeNames = typeNames;
		}
		
		@Override
		public int compare(AssetsEntity lhs, AssetsEntity rhs) 
		{
			if (lhs instanceof AssetsEntity.Item)
			{
				AssetsEntity.Item leftItem = (AssetsEntity.Item) lhs;
				AssetsEntity.Item rightItem = (AssetsEntity.Item) rhs;
				
				int lhID = leftItem.attributes().typeID;
				int rhID = rightItem.attributes().typeID;
				
				return typeNames.get(lhID).compareTo(typeNames.get(rhID));
			}
			else
			{
				AssetsEntity.Station leftStation = (AssetsEntity.Station) lhs;
				AssetsEntity.Station rightStation = (AssetsEntity.Station) rhs;
				
				int lhID = leftStation.getLocationID();
				int rhID = rightStation.getLocationID();
				
				return typeNames.get(lhID).compareTo(typeNames.get(rhID));
			}
		}
	}
	
	public static class Value implements Comparator<AssetsEntity>
	{
		/**
		 * Stores the names of the items, to use for comparison.  This information is not in the AssetEntity
		 */
		private SparseArray<Float> values;
		
		
		public Value(SparseArray<Float> typeValues)
		{
			super();
			this.values = typeValues;
		}
		
		@Override
		public int compare(AssetsEntity lhs, AssetsEntity rhs) 
		{
			if (lhs instanceof AssetsEntity.Item)
			{
				AssetsEntity.Item leftItem = (AssetsEntity.Item) lhs;
				AssetsEntity.Item rightItem = (AssetsEntity.Item) rhs;
				
				int lhID = leftItem.attributes().typeID;
				int rhID = rightItem.attributes().typeID;
				
				float lhValue = values.get(lhID) * leftItem.attributes().quantity;
				float rhValue = values.get(rhID) * rightItem.attributes().quantity;
				
				if (lhValue < rhValue) return 1;
				else if (lhValue > rhValue) return -1;
				else return 0;
			}
			else
			{
				AssetsEntity.Station leftStation = (AssetsEntity.Station) lhs;
				AssetsEntity.Station rightStation = (AssetsEntity.Station) rhs;
				
				int lhID = leftStation.getLocationID();
				int rhID = rightStation.getLocationID();
				
				if (values.get(lhID) < values.get(rhID)) return 1;
				else if (values.get(lhID) > values.get(rhID)) return -1;
				else return 0;
			}
		}
	}
}
