package com.zdonnell.eve.api.character;

public abstract class WalletEntry {	
	
	/**
	 * Date and Time the entry occurred
	 */
	protected String dateTime;
	
	public class Journal extends WalletEntry
	{
		
	}
	
	public class Transaction extends WalletEntry
	{
		private long refID;
		private int refTypeID;		
		
	}
}
