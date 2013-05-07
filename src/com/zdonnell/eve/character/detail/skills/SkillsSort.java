package com.zdonnell.eve.character.detail.skills;

import java.util.Comparator;

import com.beimin.eveapi.eve.skilltree.ApiSkill;
import com.beimin.eveapi.eve.skilltree.ApiSkillGroup;

public class SkillsSort 
{
	public static class SkillGroupAlpha implements Comparator<ApiSkillGroup>
	{		
		@Override
		public int compare(ApiSkillGroup lhs, ApiSkillGroup rhs) 
		{
			return lhs.getGroupName().compareToIgnoreCase(rhs.getGroupName());
		}
	}
	
	public static class SkillInfoAlpha implements Comparator<ApiSkill>
	{		
		@Override
		public int compare(ApiSkill lhs, ApiSkill rhs) 
		{
			return lhs.getTypeName().compareToIgnoreCase(rhs.getTypeName());
		}
	}
}
