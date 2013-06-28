package com.zdonnell.eve.staticdata;

/**
 * Any class used to represent a Static Data item, that is stored via ORMLite
 * must implement this interface.<br>
 * <br>
 * 
 * Implementing classes must correctly specify field names for the corresponding
 * values, so they can be automatically set by the {@link CheckServerDataTask}<br>
 * 
 * @author zach
 * 
 * @see TypeInfo
 * @see StationInfo
 * 
 */
public interface IStaticDataType {
	/**
	 * 
	 * @return the value of the primary key
	 */
	public int uniqueId();

	/**
	 * 
	 * @return the name of the primary key field.
	 */
	public String uniqueIdName();
}
