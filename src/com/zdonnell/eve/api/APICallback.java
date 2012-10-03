package com.zdonnell.eve.api;

/**
 * Extend this class to provide instructions for an {@link APIObject} request
 * to call when finished
 * 
 * @author zachd
 *
 * @param <T> The Generic type the onUpdate method should take and provide to the callback
 * code
 */
public abstract class APICallback<T> 
{
	public abstract void onUpdate(T updatedData);
}