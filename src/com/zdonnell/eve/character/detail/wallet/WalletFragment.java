package com.zdonnell.eve.character.detail.wallet;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiAuthorization;
import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.shared.wallet.journal.ApiJournalEntry;
import com.beimin.eveapi.shared.wallet.journal.WalletJournalResponse;
import com.beimin.eveapi.shared.wallet.transactions.ApiWalletTransaction;
import com.beimin.eveapi.shared.wallet.transactions.WalletTransactionsResponse;
import com.zdonnell.eve.BaseActivity;
import com.zdonnell.eve.CharacterDetailActivity;
import com.zdonnell.eve.R;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.character.APICharacter;
import com.zdonnell.eve.character.detail.DetailFragment;

public class WalletFragment extends DetailFragment {
    
	public static final int TRANSACTION = 0;
	public static final int JOURNAL = 1;
	
    public static String[] displayTypeNames = new String[2];
	static
	{
		displayTypeNames[TRANSACTION] = "Wallet Transactions";
		displayTypeNames[JOURNAL] = "Journal Entries";
	}
		    
    private APICharacter character;
        
    private Context context;
    
    private ListView walletListView;
            
    private String characterName;
    
	private NumberFormat formatter = NumberFormat.getInstance();
            	
	private TextView walletBalance;
	
	private SharedPreferences prefs;

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
    	
    	characterName = getArguments().getString("characterName");
    	
    	walletBalance = (TextView) inflatedView.findViewById(R.id.char_detail_wallet_balance);
    	
    	walletListView = (ListView) inflatedView.findViewById(R.id.char_detail_wallet_listview);
    	
    	loadData();
    	    	
    	return inflatedView;
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
    	character.getWalletJournal(new APIExceptionCallback<WalletJournalResponse>((BaseActivity) getActivity())
    	{
			@Override
			public void onUpdate(WalletJournalResponse response) 
			{
				Set<ApiJournalEntry> entrySet = response.getAll();
				ApiJournalEntry[] entryArray = new ApiJournalEntry[entrySet.size()];
				entrySet.toArray(entryArray);
				
				Arrays.sort(entryArray, new WalletSort.Journal.DateTime());
				walletListView.setAdapter(new WalletJournalAdapter(context, entryArray, characterName));
			}

			@Override
			public void onError(WalletJournalResponse response, ApiException exception) 
			{
				
			}
    	});
    }
    
    public void loadInTransactions()
    {
    	character.getWalletTransactions(new APIExceptionCallback<WalletTransactionsResponse>((BaseActivity) getActivity())
    	{
			@Override
			public void onUpdate(WalletTransactionsResponse response) 
			{
				Set<ApiWalletTransaction> transactionSet = response.getAll();
				ApiWalletTransaction[] transactionsArray = new ApiWalletTransaction[transactionSet.size()];
				transactionSet.toArray(transactionsArray);
				
				Arrays.sort(transactionsArray, new WalletSort.Transactions.DateTime());
				walletListView.setAdapter(new WalletTransactionAdapter(context, transactionsArray));
			}

			@Override
			public void onError(WalletTransactionsResponse response, ApiException exception) 
			{
				
			}
    	});
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
    	
    	character.getCharacterSheet(new APIExceptionCallback<CharacterSheetResponse>((BaseActivity) getActivity())
    	{
			@Override
			public void onUpdate(CharacterSheetResponse response) 
			{
				walletBalance.setText(formatter.format(response.getBalance()) + " ISK");
			}

			@Override
			public void onError(CharacterSheetResponse response, ApiException exception) 
			{
				
			}
    	});
	}
}
