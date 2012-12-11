package com.zdonnell.eve.character.detail;

import com.zdonnell.eve.api.character.AssetsEntity;

public interface IAssetsSubFragment {

	public void setParent(ParentAssetsFragment parent);
	
	public void assetsUpdated(AssetsEntity[] assets);
	
}
