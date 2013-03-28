package com.zdonnell.eve.api.character;

public class WalletEntry {	
	
	/**
	 * Date and Time the entry occurred
	 */
	protected String dateTime;
	
	public String dateTime() { return dateTime; }
	
	public static class Journal extends WalletEntry
	{
		private long refID;
		private int refTypeID;
		
		private String ownerName1, ownerName2;
		private int ownerID1, ownerID2;
		
		private String argName1;
		private int argID1;
		
		private double amount, balance;
		private String reason;
		
		private int taxReceiverID;
		private double taxAmount;
		
		public Journal(String dateTime, long refID, int refTypeID, String ownerName1, String ownerName2, int ownerID1, int ownerID2,
				String argName1, int argID1, double amount, double balance, String reason, int taxReceiverID, double taxAmount)
		{
			this.dateTime = dateTime;
			this.refID = refID;
			this.refTypeID = refTypeID;
			this.ownerName1 = ownerName1;
			this.ownerName2 = ownerName2;
			this.ownerID1 = ownerID1;
			this.ownerID2 = ownerID2;
			this.argName1 = argName1;
			this.argID1 = argID1;
			this.amount = amount;
			this.balance = balance;
			this.reason = reason;
			this.taxReceiverID = taxReceiverID;
			this.taxAmount = taxAmount;
		}
		
		public long refID() { return refID; }
		public int refTypeID() { return refTypeID; }
		
		public String ownerName1() { return ownerName1; }
		public String ownerName2() { return ownerName2; }
		
		public int ownerID1() { return ownerID1; }
		public int ownerID2() { return ownerID2; }
		
		public String argName1() { return argName1; }
		public int argID1() { return argID1; }

		public double amount() { return amount; }
		public double balance() { return balance; }
		
		public String reason() { return reason; }
		
		public int taxReceiverID() { return taxReceiverID; }
		public double taxAmount() { return taxAmount; }
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
			this.transactionType = transactionType;
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
