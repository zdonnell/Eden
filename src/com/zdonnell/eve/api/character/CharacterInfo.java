package com.zdonnell.eve.api.character;

/**
 * TODO Finish filling out character info fields
 * 
 * @author Zach
 *
 */
public class CharacterInfo {
	
	private int characterID;
	
	private int skillPoints;
	
	private CurrentShipInfo curShip;
	
	private double securityStatus;
	
	private String location;
	
	public CharacterInfo(int characterID)
	{
		this.characterID = characterID;
	}
	
	// Setters
	public void setCurShipInfo(CurrentShipInfo curShip) { this.curShip = curShip; }
	public void setSP(int skillPoints) { this.skillPoints = skillPoints; }
	public void setSecStatus(double secStatus) { this.securityStatus = secStatus; }
	public void setLocation(String location) { this.location = location; }
	
	// Getters
	public CurrentShipInfo getCurShipInfo() { return curShip; }
	public int getSP() { return skillPoints; }
	public double getSecStatus() { return securityStatus; }
	public String getLocation() { return location; }
	
	/**
	 * Wrapper class for curernt ship info
	 * 
	 * @author Zach
	 *
	 */
	public static class CurrentShipInfo
	{
		private String name;
		private String typeName;
		private int typeID;
		
		public CurrentShipInfo(String name, String typeName, int typeID)
		{
			this.name = name;
			this.typeName = typeName;
			this.typeID = typeID;
		}
		
		// Getters
		public String getName() { return name; }
		public String getTypeName() { return typeName; }
		public int getTypeID() { return typeID; }
	}
}
