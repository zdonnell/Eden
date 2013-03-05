package com.zdonnell.eve.eve;

import java.util.ArrayList;

public class SkillInfo 
{
	private boolean published;
	
	private String typeName;
	
	private int typeID;
	
	private String description;
	
	private int rank;
	
	private String primaryAttribute, secondaryAttribute;
	
	private ArrayList<SkillPreReq> requiredSkills;
	
	public SkillInfo(boolean published, int typeID, String typeName, String description, int rank, String primaryAttribute, String secondaryAttribute, ArrayList<SkillPreReq> requiredSkills)
	{
		this.published = published;
		this.typeID = typeID;
		this.typeName = typeName;
		this.description = description;
		this.rank = rank;
		this.primaryAttribute = primaryAttribute;
		this.secondaryAttribute = secondaryAttribute;
		this.requiredSkills = requiredSkills;
	}
	
	public boolean isPublished() { return published; }
	public String description() { return description; }
	public int rank() { return rank; }
	public String[] attributes() { return new String[] { primaryAttribute, secondaryAttribute }; }
	public ArrayList<SkillPreReq> requiredSkills() { return requiredSkills; }
	
	static class SkillPreReq
	{
		private int skillLevel;
		
		private int typeID;
		
		public SkillPreReq(int skillLevel, int typeID)
		{
			this.skillLevel = skillLevel;
			this.typeID = typeID;
		}
		
		private int skillLevel() { return skillLevel; }
		private int typeID() { return typeID; }
	}
}
