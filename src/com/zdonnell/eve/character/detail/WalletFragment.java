package com.zdonnell.eve.character.detail;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.ImageService.IconObtainedCallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.AssetsEntity.Station;
import com.zdonnell.eve.api.character.CharacterSheet.AttributeEnhancer;
import com.zdonnell.eve.api.character.WalletEntry;
import com.zdonnell.eve.api.character.WalletEntry.Transaction;
import com.zdonnell.eve.eve.Eve;

public class WalletFragment extends Fragment {
    
    
    private APICharacter character;
        
    private Context context;
    
    private ListView walletListView;
    
    private WalletEntry[] walletEntries;
    
    private SparseArray<String> refTypes;
    
    private String characterName;
    
	private NumberFormat formatter = NumberFormat.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_wallet, container, false);
    	
    	formatter.setMaximumFractionDigits(2);
    	formatter.setMinimumFractionDigits(2);

    	character = new APICharacter(new APICredentials(getArguments().getInt("keyID"), getArguments().getString("vCode")), getArguments().getInt("characterID"), context);
    	characterName = getArguments().getString("characterName");
    	
    	final TextView walletBalance = (TextView) inflatedView.findViewById(R.id.char_detail_wallet_balance);
    	
    	new Eve(context).getRefTypes(new APICallback<SparseArray<String>>()
    	{
			@Override
			public void onUpdate(SparseArray<String> updatedData) 
			{
				refTypes = updatedData;
				dataUpdated();
			}
    	});
    	
    	character.getWalletTransactions(new APICallback<WalletEntry.Transaction[]>() 
    	{
			@Override
			public void onUpdate(Transaction[] updatedData) 
			{
				/*walletEntries = updatedData;
				Arrays.sort(walletEntries, new WalletSort.DateTime());
				dataUpdated();*/
			}
    	});
    	
    	character.getWalletJournal(new APICallback<WalletEntry.Journal[]>() 
    	{
			@Override
			public void onUpdate(WalletEntry.Journal[] updatedData) 
			{
				walletEntries = updatedData;
				Arrays.sort(walletEntries, new WalletSort.DateTime());
				dataUpdated();
			}
    	});
    	
    	character.getCharacterSheet(new APICallback<CharacterSheet>()
    	{
			@Override
			public void onUpdate(CharacterSheet updatedData) 
			{
				walletBalance.setText(formatter.format(updatedData.getWalletBalance()) + " ISK");
			}
    	});
    	
    	walletListView = (ListView) inflatedView.findViewById(R.id.char_detail_wallet_listview);
    	
    	return inflatedView;
    }  
    
    private void dataUpdated()
    {
    	if (walletEntries != null)
    	{
    		if (walletEntries[0] instanceof WalletEntry.Journal)
    		{
    			if (refTypes != null) walletListView.setAdapter(new WalletEntryAdapter(context, R.layout.char_detail_assets_list_item, walletEntries));
    		}
    		else walletListView.setAdapter(new WalletEntryAdapter(context, R.layout.char_detail_assets_list_item, walletEntries));
    	}
    }
    
    private class WalletEntryAdapter extends ArrayAdapter<WalletEntry>
    {
		private LayoutInflater inflater;
		
		private final int RED = Color.parseColor("#FF4444");
		private final int GREEN = Color.parseColor("#449944");	
			
		private final int journalLayout = R.layout.char_detail_wallet_list_item_journal;
		private final int transactionLayout = R.layout.char_detail_wallet_list_item_transaction;
    	
		public WalletEntryAdapter(Context context, int textViewResourceId, WalletEntry[] entries) 
		{
			super(context, textViewResourceId, entries);
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LinearLayout itemView;
			WalletEntry entry = getItem(position);
			
			boolean isJournal = entry instanceof WalletEntry.Journal; 
			
			/* Determine if we recyle the old view, or inflate a new one */
			if (convertView == null) itemView = (LinearLayout) inflater.inflate(isJournal ? journalLayout : transactionLayout, parent, false);
			else itemView = (LinearLayout) convertView;
			
			if (isJournal) prepareJournalEntry(itemView, (WalletEntry.Journal) entry);
			else prepareTransactionEntry(itemView, (WalletEntry.Transaction) entry);
			
			return itemView;
		}
		
		private void prepareJournalEntry(LinearLayout itemView, WalletEntry.Journal entry)
		{
			final TextView journalType = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalType);
			final TextView description = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalDescription);
			final TextView amount = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalAmount);
			final TextView balance = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalBalance);

			journalType.setText(refTypes.get(entry.refTypeID()));
			description.setText(generateDescription(entry));
			
			amount.setTextColor(entry.amount() < 0 ? RED : GREEN);
			amount.setText(formatter.format(entry.amount()) + " ISK");
			
			balance.setText(formatter.format(entry.balance()) + " ISK");

		}
		
		private String generateDescription(WalletEntry.Journal entry)
		{
			switch (entry.refTypeID())
			{
			case 2: return entry.ownerName1() + " bought stuff from you";
			case 10: 
				if (entry.ownerName1().equals(characterName)) return "You deposited cash into " + entry.ownerName2() + "'s account";
				else return entry.ownerName1() + " deposited cash into your account";
			default: return "";
			}
		}
		
		private void prepareTransactionEntry(LinearLayout itemView, WalletEntry.Transaction entry)
		{
			final ImageView typeIcon = (ImageView) itemView.findViewById(R.id.char_detail_wallet_list_item_transaction_icon);
			final TextView typeNameCount = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_itemname);
			final TextView stationName = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_stationname);
			final TextView price = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_itemvalue);
			
			typeNameCount.setText(entry.quantity() + "x " + entry.typeName());
			stationName.setText(entry.stationName());
			//quantity.setText("quantity: " + entry.quantity());
			
			boolean isBuy = entry.transactionType() == WalletEntry.Transaction.BUY;
			String formattedValue = formatter.format(entry.price() * entry.quantity());
			String priceString = (isBuy ? "-" : "+") + formattedValue + " ISK";
			price.setText(priceString);
			price.setTextColor(isBuy ? Color.parseColor("#FF4444") : Color.parseColor("#449944"));
			
			typeIcon.setImageBitmap(null);
			ImageService.getInstance(context).getTypes(new IconObtainedCallback()
			{
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) 
				{
					typeIcon.setImageBitmap(bitmaps.valueAt(0));
				}	
			}, entry.typeID());
		}
    }
}
