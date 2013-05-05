package com.zdonnell.eve.character.detail;

import com.zdonnell.eve.eve.SkillInfo;

public class SkillGroup {

	private SkillInfo[] containedSkills;
	
	private int groupID;
	
	private String groupName;
	
	public SkillGroup(int groupID, String groupName, SkillInfo[] containedSkills2)
	{
		this.containedSkills = containedSkills2;
		this.groupID = groupID;
		this.groupName = groupName;
	}
	
	public SkillInfo[] containedSkills() { return containedSkills; }
	public int groupID() { return groupID; }
	public String groupName() { return groupName; }
}
