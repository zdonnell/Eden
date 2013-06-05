package com.zdonnell.eve;

public interface ILoadingActivity {

	abstract public void dataLoading();
	
	abstract public void loadingFinished(boolean dataError);
	
}
