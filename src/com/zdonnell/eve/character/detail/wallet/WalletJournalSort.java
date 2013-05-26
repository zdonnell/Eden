package com.zdonnell.eve.character.detail.wallet;

import java.util.Comparator;

import com.beimin.eveapi.shared.wallet.journal.ApiJournalEntry;

public class WalletJournalSort 
{	
	public static class RefID implements Comparator<ApiJournalEntry>
	{
		@Override
		public int compare(ApiJournalEntry lhs, ApiJournalEntry rhs) 
		{			
			if (lhs.getRefID() < rhs.getRefID()) return -1;
			else return 1;
		}
	}
	
	public static class DateTime implements Comparator<ApiJournalEntry>
	{
		@Override
		public int compare(ApiJournalEntry lhs, ApiJournalEntry rhs) 
		{			
			// the wallet entry dateTime string is properly formatted such that
			// a simple alpha numeric sort will suffice
			return rhs.getDate().compareTo(lhs.getDate());
		}
	}
}
