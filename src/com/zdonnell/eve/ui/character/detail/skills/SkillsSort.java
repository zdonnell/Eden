package com.zdonnell.eve.ui.character.detail.skills;

import java.util.Comparator;

import com.zdonnell.eve.character.detail.SkillGroup;
import com.zdonnell.eve.eve.SkillInfo;

public class SkillsSort 
{
	public static class SkillGroupAlpha implements Comparator<SkillGroup>
	{		
		@Override
		public int compare(SkillGroup lhs, SkillGroup rhs) 
		{
			return lhs.groupName().compareToIgnoreCase(rhs.groupName());
		}
	}
	
	public static class SkillInfoAlpha implements Comparator<SkillInfo>
	{		
		@Override
		public int compare(SkillInfo lhs, SkillInfo rhs) 
		{
			return lhs.typeName().compareToIgnoreCase(rhs.typeName());
		}
	}
}
