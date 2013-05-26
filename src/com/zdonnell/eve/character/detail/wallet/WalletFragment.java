package com.zdonnell.eve.character.detail.wallet;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiAuthorization;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.shared.wallet.journal.ApiJournalEntry;
import com.beimin.eveapi.shared.wallet.journal.WalletJournalResponse;
import com.squareup.picasso.Picasso;
import com.zdonnell.eve.BaseActivity;
import com.zdonnell.eve.CharacterDetailActivity;
import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.WalletEntry;
import com.zdonnell.eve.api.character.WalletEntry.Transaction;
import com.zdonnell.eve.apilink.APICallback;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.character.APICharacter;
import com.zdonnell.eve.character.detail.DetailFragment;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.helpers.ImageURL;

public class WalletFragment extends DetailFragment {
    
	public static final int TRANSACTION = 0;
	public static final int JOURNAL = 1;
	
    public static String[] displayTypeNames = new String[2];
	static
	{
		displayTypeNames[TRANSACTION] = "Wallet Transactions";
		displayTypeNames[JOURNAL] = "Journal Entries";
	}
	
	private CharacterDetailActivity parentActivity;
	
    private com.zdonnell.eve.api.character.APICharacter oldCharacter;
    
    private APICharacter character;
        
    private Context context;
    
    private ListView walletListView;
    
    private WalletEntry[] walletEntries;
    
    private SparseArray<String> refTypes;
    
    private String characterName;
        
    private HashMap<ImageView, Integer> visibleViews = new HashMap<ImageView, Integer>();
    
	private NumberFormat formatter = NumberFormat.getInstance();
	
	TextView walletBalance;
	
	SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {    	
    	context = inflater.getContext();
    	prefs = context.getSharedPreferences("eden_wallet_preferences", Context.MODE_PRIVATE);
    	
    	parentActivity = (CharacterDetailActivity) getActivity();
    	
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_wallet, container, false);
    	
    	formatter.setMaximumFractionDigits(2);
    	formatter.setMinimumFractionDigits(2);

    	ApiAuth<?> apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), (long) getArguments().getInt("characterID"), getArguments().getString("vCode"));
    	character = new APICharacter(context, apiAuth);
    	
    	oldCharacter = new com.zdonnell.eve.api.character.APICharacter(new APICredentials(getArguments().getInt("keyID"), getArguments().getString("vCode")), getArguments().getInt("characterID"), context);
    	characterName = getArguments().getString("characterName");
    	
    	walletBalance = (TextView) inflatedView.findViewById(R.id.char_detail_wallet_balance);
    	
    	walletListView = (ListView) inflatedView.findViewById(R.id.char_detail_wallet_listview);
    	
    	loadData();
    	    	
    	return inflatedView;
    }  
    
    public void fillImageViews()
    {
		for (ImageView icon : visibleViews.keySet())
		{
			Integer imageTypeID = (Integer) icon.getTag();
			if (imageTypeID != null)
			{
				Picasso.with(context).load(ImageURL.forType(imageTypeID)).into(icon);
			}
		}
    }
    
    public void updateWalletType(int type)
    {
		prefs.edit().putInt("wallet_type", type).commit();
    	
    	switch (type)
    	{
    	case TRANSACTION:
    		loadInTransactions();
    		break;
    	case JOURNAL:
    		loadInJournal();
    		break;
    	}
    }
    
    public void loadInJournal()
    {
    	if (parentActivity.dataCache.getJournalEntries() != null) 
    	{
    		walletEntries = parentActivity.dataCache.getJournalEntries(); 
			dataUpdated();
    		return;
    	}
    	
    	character.getWalletJournal(new APIExceptionCallback<WalletJournalResponse>((BaseActivity) getActivity())
    	{
			@Override
			public void onUpdate(WalletJournalResponse response) 
			{
				Set<ApiJournalEntry> entrySet = response.getAll();
				ApiJournalEntry[] entryArray = new ApiJournalEntry[entrySet.size()];
				entrySet.toArray(entryArray);
				
				Arrays.sort(entryArray, new WalletJournalSort.DateTime());
				walletListView.setAdapter(new WalletJournalAdapter(context, entryArray, characterName));
			}

			@Override
			public void onError(WalletJournalResponse response, ApiException exception) 
			{
				
			}
    	});
    	
    	/*oldCharacter.getWalletJournal(new APICallback<WalletEntry.Journal[]>((BaseActivity) getActivity()) 
    	{
			@Override
			public void onUpdate(WalletEntry.Journal[] updatedData) 
			{
				walletEntries = updatedData;
				Arrays.sort(walletEntries, new WalletSort.DateTime());
				
				parentActivity.dataCache.cacheJournalEntries(updatedData);
				dataUpdated();
			}
    	});*/
    }
    
    public void loadInTransactions()
    {
    	if (parentActivity.dataCache.getTransactions() != null) 
    	{
    		walletEntries = parentActivity.dataCache.getTransactions(); 
			dataUpdated();
    		return;
    	}
    	
    	oldCharacter.getWalletTransactions(new APICallback<WalletEntry.Transaction[]>((BaseActivity) getActivity()) 
    	{
			@Override
			public void onUpdate(Transaction[] updatedData) 
			{
				walletEntries = updatedData;
				//Arrays.sort(walletEntries, new WalletJournalSort.DateTime());
				
				parentActivity.dataCache.cacheTransactions(updatedData);
				dataUpdated();
			}
    	});
    }
    
    private void dataUpdated()
    {
    	if (walletEntries != null && walletEntries.length > 0)
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
			typeIcon.setTag(Integer.valueOf(entry.typeID()));
			visibleViews.put(typeIcon, entry.typeID());

			Picasso.with(context).load(ImageURL.forType(entry.typeID())).into(typeIcon);
		}
    }

	@Override
	public void loadData() 
	{
		switch (prefs.getInt("wallet_type", JOURNAL))
    	{
    	case JOURNAL: 
    		loadInJournal();
    		break;
    	case TRANSACTION: 
    		loadInTransactions();
    		break;
    	}
    	
    	// Needed to set wallet balance
    	oldCharacter.getCharacterSheet(new APICallback<CharacterSheet>((BaseActivity) getActivity())
    	{
			@Override
			public void onUpdate(CharacterSheet updatedData) 
			{
				walletBalance.setText(formatter.format(updatedData.getWalletBalance()) + " ISK");
			}
    	});
    	
    	// Needed for the base description of wallet journal entry types
    	new Eve(context).getRefTypes(new APICallback<SparseArray<String>>((BaseActivity) getActivity())
    	{
			@Override
			public void onUpdate(SparseArray<String> updatedData) 
			{
				refTypes = updatedData;
				dataUpdated();
			}
    	});
	}
}
