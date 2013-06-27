package com.zdonnell.eve.staticdata;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stationInfo")
public class StationInfo implements IStaticDataType {

	@DatabaseField(id = true)
	public int stationID;

	@DatabaseField
	public int stationTypeID;

	@DatabaseField
	public String stationName;

	public int uniqueId() {
		return stationID;
	}

	public String uniqueIdName() {
		return "stationID";
	}
}
