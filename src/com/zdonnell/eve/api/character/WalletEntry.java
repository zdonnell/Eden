package com.zdonnell.eve.api.character;

public class WalletEntry {	
	
	/**
	 * Date and Time the entry occurred
	 */
	protected String dateTime;
	
	public String dateTime() { return dateTime; }
	
	public class Journal extends WalletEntry
	{
		
	}
	
	public static class Transaction extends WalletEntry
	{
		public static final int BUY = 0;
		public static final int SELL = 1;
		
		private long transactionID;
		private int quantity;
		
		private int typeID;
		private String typeName;
		
		private double price;
		private String stationName;
		
		private int transactionType;
		
		public Transaction(String dateTime, long transactionID, int quantity, int typeID, String typeName, double price, String stationName, int transactionType)
		{
			this.dateTime = dateTime;
			this.transactionID = transactionID;
			this.quantity = quantity;
			this.typeID = typeID;
			this.typeName = typeName;
			this.price = price;
			this.stationName = stationName;
		}
		
		public long transactionID() { return transactionID; }
		public int quantity() { return quantity; };
		
		public int typeID() { return typeID; };
		public String typeName() { return typeName; };
		
		public double price() { return price; };
		public String stationName() { return stationName; };
		
		public int transactionType() { return transactionType; }
	}
}
