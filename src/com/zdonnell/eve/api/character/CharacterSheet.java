package com.zdonnell.eve.api.character;

/**
 * TODO Implement other character sheet fields
 * 
 * @author Zach
 *
 */
public class CharacterSheet {
	
	private int characterID;
	
	private String dateOfBirth;
	
	private int cloneSkillPoints;
	
	private String cloneName;
	
	private AttributeEnhancer[] attributeEnhancers;
	
	private int[] attributes;
	
	private double walletBalance;
	
	public CharacterSheet(int characterID)
	{
		this.characterID = characterID;
	}
	
	public void setCloneInfo(String cloneName, int cloneSkillPoints)
	{
		this.cloneName = cloneName;
		this.cloneSkillPoints = cloneSkillPoints;
	}
	
	public void setAttributeInfo(AttributeEnhancer[] attributeEnhancers, int[] attributes)
	{
		this.attributeEnhancers = attributeEnhancers;
		this.attributes = attributes;
	}
	
	public void setWalletBalance(double walletBalance)
	{
		this.walletBalance = walletBalance;
	}
	
	public String getDateOfBirth() { return dateOfBirth; }
	public int getCloneSkillPoints() { return cloneSkillPoints; }
	public String getCloneName() { return cloneName; }
	public AttributeEnhancer[] getAttributeEnhancers() { return attributeEnhancers; }
	public int[] getAttributes() { return attributes; }
	public double getWalletBalance() { return walletBalance; }
	public int getCharacterID() { return characterID; }
	
	/**
	 * 
	 * @author Zach
	 *
	 */
	public static class AttributeEnhancer
	{
		public final static int MEMORY = 0;
		public final static int PERCEPTION = 1;
		public final static int WILLPOWERY = 2;
		public final static int INTELLIGENCE = 3;
		public final static int CHARISMA = 4;
		
		public final String augmentatorName;
		public final int augmentatorValue;
		
		public AttributeEnhancer(String augmentatorName, int augmentatorValue)
		{
			this.augmentatorName = augmentatorName;
			this.augmentatorValue = augmentatorValue;
		}
	}
}