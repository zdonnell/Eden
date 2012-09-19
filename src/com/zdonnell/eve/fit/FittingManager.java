package com.zdonnell.eve.fit;

import java.util.HashMap;
import java.util.Map;

/**
 * The main class your Interface will interact with.
 * 
 * @author zachd
 *
 */
public class FittingManager {
	
	/**
	 * The current fit being used.
	 */
	private Fit activeFit;
	
	/**
	 * Maps the CCP Database ID of the skill, to a location in the skills array.
	 */
	private Map<Integer, Integer> skillMap;
	
	
	private short[] skills;
	
	/**
	 * Constructor
	 */
	FittingManager() {
		
	}
	

	public int addModule(Module module, int slot) {
		
		// TODO Fix return value
		
		calculateStats();
		return 0;
	}
	
	/**
	 * Removes the specified module from the active fit
	 * 
	 * @param moduleID
	 * @return
	 */
	public boolean removeModule(int moduleID) {
		
		// TODO Fix return value
		
		calculateStats();
		return true;
	}
	
	/**
	 * 
	 * @param skills
	 * @param skillMap
	 */
	public void applySkills(short[] skills, Map<Integer, Integer> skillMap) {
		this.skills = skills;
		this.skillMap = skillMap;
		
		calculateStats();
	}
	
	private void calculateStats() {
		
	}
}
