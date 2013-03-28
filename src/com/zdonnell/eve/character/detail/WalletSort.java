package com.zdonnell.eve.character.detail;

import java.util.Comparator;

import com.zdonnell.eve.api.character.WalletEntry;

public class WalletSort 
{	
	public static class DateTime implements Comparator<WalletEntry>
	{
		@Override
		public int compare(WalletEntry lhs, WalletEntry rhs) 
		{			
			// the wallet entry dateTime string is properly formatted such that
			// a simple alpha numeric sort will suffice
			return rhs.dateTime().compareTo(lhs.dateTime());
		}
	}
}
