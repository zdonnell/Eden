package com.zdonnell.eve.character.detail;

import android.util.SparseArray;

import com.zdonnell.eve.apilink.character.AssetsEntity;

public interface IAssetsSubFragment 
{
	public void setParent(ParentAssetsFragment parent);
	
	public void assetsUpdated(AssetsEntity[] assets);
	
	public void updateLayoutStyle(int type);
	
	public void obtainedPrices();
	
	public void obtainedTypeInfo();

	public void obtainedStationInfo();
	
	public int[] getScrollPoint();
	
	public void setScrollPoint(int[] scrollPoint);
	
	public SparseArray<String> getNames();
	
	public SparseArray<Float> getValues();	
}
