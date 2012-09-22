package com.zdonnell.eve.fit;

/**
 * 
 * 
 * @author Zach Donnell
 * 
 */
public class Fit {

	Module[] rigSlots, lowSlots, midSlots, highSlots;

	String name;

	/**
	 * Constructor TODO update javadoc
	 * 
	 * @param ship
	 * @param name
	 */
	Fit(Ship ship, String name) {

		rigSlots = new Module[ship.getRigSlotCount()];
		lowSlots = new Module[ship.getLowSlotCount()];
		midSlots = new Module[ship.getMidSlotCount()];
		highSlots = new Module[ship.getHighSlotCount()];

		this.name = name;
	}

}
