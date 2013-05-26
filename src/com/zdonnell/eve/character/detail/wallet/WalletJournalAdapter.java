package com.zdonnell.eve.character.detail.wallet;

import java.text.NumberFormat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beimin.eveapi.shared.wallet.journal.ApiJournalEntry;
import com.zdonnell.eve.R;

public class WalletJournalAdapter extends ArrayAdapter<ApiJournalEntry>
{
	private LayoutInflater inflater;
	
	private final int RED = Color.parseColor("#FF4444");
	private final int GREEN = Color.parseColor("#449944");	
		
	private final static int layout = R.layout.char_detail_wallet_list_item_journal;
	
	private NumberFormat formatter = NumberFormat.getInstance();
	
	private String characterName;
	
	public WalletJournalAdapter(Context context, ApiJournalEntry[] entries, String characterName) 
	{
		super(context, layout, entries);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		this.characterName = characterName;
		
		formatter.setMaximumFractionDigits(2);
    	formatter.setMinimumFractionDigits(2);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{				
		/* Determine if we recyle the old view, or inflate a new one */
		if (convertView == null) convertView = (LinearLayout) inflater.inflate(layout, parent, false);
		
		prepareEntry(convertView, getItem(position));
		
		return convertView;
	}
	
	private void prepareEntry(View itemView, ApiJournalEntry entry)
	{
		final TextView journalType = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalType);
		final TextView description = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalDescription);
		final TextView amount = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalAmount);
		final TextView balance = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalBalance);

		journalType.setText(entry.getRefType().name());
		description.setText(generateDescription(entry));
		
		amount.setTextColor(entry.getAmount() < 0 ? RED : GREEN);
		amount.setText(formatter.format(entry.getAmount()) + " ISK");
		
		balance.setText(formatter.format(entry.getBalance()) + " ISK");  
	}
	
	private String generateDescription(ApiJournalEntry entry)
	{
		switch ((int) entry.getRefType().getId())
		{
		case 2: return entry.getOwnerName1() + " bought stuff from you";
		case 10: 
			if (entry.getOwnerName1().equals(characterName)) return "You deposited cash into " + entry.getOwnerName2() + "'s account";
			else return entry.getOwnerName1() + " deposited cash into your account";
		default: return "";
		}
	}
}
