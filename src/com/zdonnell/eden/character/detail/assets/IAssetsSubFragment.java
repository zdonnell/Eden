package com.zdonnell.eden.character.detail.assets;

import android.util.SparseArray;

import com.zdonnell.androideveapi.link.character.AssetsEntity;

public interface IAssetsSubFragment {
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
