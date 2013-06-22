package com.zdonnell.eve;

import java.util.Comparator;

import com.zdonnell.androideveapi.account.characters.EveCharacter;
import com.zdonnell.androideveapi.link.account.EdenEveCharacter;

public class CharacterSort {
	
	public static final int QUEUETIME = 0;
	public static final int QUEUETIME_REVERSE = 1;
	public static final int ALPHA = 2;
	public static final int ALPHA_REVERSE = 3;
	
	public static final String[] sortNames = new String[4];
	static
	{
		sortNames[QUEUETIME] = "Queue Time Remaining";
		sortNames[QUEUETIME_REVERSE] = "Queue Time Remaining (Reversed)";
		sortNames[ALPHA] = "Alphabetical";
		sortNames[ALPHA_REVERSE] = "Alphabetical (Reversed)";	
	}

	public static class Alpha implements Comparator<EveCharacter>
	{
		public int compare(EveCharacter lhs, EveCharacter rhs) 
		{
			return lhs.getName().compareTo(rhs.getName());
		}
	}
	
	public static class TrainingTimeRemaining implements Comparator<EdenEveCharacter>
	{		
		public int compare(EdenEveCharacter lhs, EdenEveCharacter rhs)
		{
			Long lhsTimeRemaining = lhs.getQueueTimeRemaining();
			Long rhsTimeRemaining = rhs.getQueueTimeRemaining();
			
			return rhsTimeRemaining.compareTo(lhsTimeRemaining);
		}
	}
}
