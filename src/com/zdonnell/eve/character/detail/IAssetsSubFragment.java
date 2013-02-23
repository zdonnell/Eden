package com.zdonnell.eve.character.detail;

import android.util.SparseArray;

import com.zdonnell.eve.api.character.AssetsEntity;

public interface IAssetsSubFragment 
{
	public void setParent(ParentAssetsFragment parent);
	
	public void assetsUpdated(AssetsEntity[] assets);
	
	public void obtainedPrices();
	
	public void obtainedTypeInfo();
	
	public SparseArray<String> getNames();
	
	public SparseArray<Float> getValues();	
}
