package com.zdonnell.eve.character.detail;

import java.util.Comparator;

import com.zdonnell.eve.api.character.AssetsEntity;

public class InventorySort 
{
	public static class Count implements Comparator<AssetsEntity>
	{
		@Override
		public int compare(AssetsEntity lhs, AssetsEntity rhs) 
		{
			if (lhs instanceof AssetsEntity.Item)
			{
				AssetsEntity.Item leftItem = (AssetsEntity.Item) lhs;
				AssetsEntity.Item rightItem = (AssetsEntity.Item) rhs;
				
				if (leftItem.attributes().quantity < rightItem.attributes().quantity) return -1;
				else if (leftItem.attributes().quantity > rightItem.attributes().quantity) return 1;
				else return 0;
			}
			else
			{
				AssetsEntity.Station leftStation = (AssetsEntity.Station) lhs;
				AssetsEntity.Station rightStation = (AssetsEntity.Station) rhs;
				
				if (leftStation.getContainedAssets().size() < rightStation.getContainedAssets().size()) return -1;
				else if (leftStation.getContainedAssets().size() > rightStation.getContainedAssets().size()) return 1;
				else return 0;
			}
		}
	}
}
