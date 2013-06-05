package com.zdonnell.eve.apilink;

public interface IApiTask<T> {

	abstract int requestTypeHash();
	
	abstract T buildResponseFromDatabase();
	
}
