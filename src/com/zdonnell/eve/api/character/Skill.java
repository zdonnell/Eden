package com.zdonnell.eve.api.character;

public class Skill {
	
	private int typeID, skillPoints, level;
	private boolean published;
	
	public Skill(int typeID, int skillPoints, int level, boolean published)
	{
		this.typeID = typeID;
		this.skillPoints = skillPoints;
		this.level = level;
		this.published = published;
	}
	
	public int getTypeID() { return typeID; }
	public int getSkillPoints() { return skillPoints; }
	public int getLevel() { return level; }
	public boolean isPublished() { return published; }

}
