package com.zdonnell.eve.api.character;

public class QueuedSkill {

	public final int skillID;
	public final int skillLevel;
	public final int startSP;
	public final int endSP;
	public final String startTime;
	public final String endTime;
	
	public QueuedSkill(int skillID, int skillLevel, int startSP, int endSP,	String startTime, String endTime) 
	{
		this.skillID = skillID;
		this.skillLevel = skillLevel;
		this.startSP = startSP;
		this.endSP = endSP;
		this.startTime = startTime;
		this.endTime = endTime;
	}

}
