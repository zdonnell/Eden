package com.zdonnell.eve.character.detail.wallet;

import java.text.NumberFormat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beimin.eveapi.shared.wallet.transactions.ApiWalletTransaction;
import com.squareup.picasso.Picasso;
import com.zdonnell.eve.R;
import com.zdonnell.eve.helpers.ImageURL;

public class WalletTransactionAdapter extends ArrayAdapter<ApiWalletTransaction>
{
	private LayoutInflater inflater;
	private Context context;
	
	private final int RED = Color.parseColor("#FF4444");
	private final int GREEN = Color.parseColor("#449944");	
	
	private NumberFormat formatter = NumberFormat.getInstance();
		
	private final static int layoutID = R.layout.char_detail_wallet_list_item_transaction;
	
	public WalletTransactionAdapter(Context context, ApiWalletTransaction[] entries) 
	{
		super(context, layoutID, entries);
		
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{			
		ApiWalletTransaction transaction = getItem(position);
		
		// Determine if we recycle the old view, or inflate a new one
		if (convertView == null) convertView = (LinearLayout) inflater.inflate(layoutID, parent, false);
		
		final ImageView typeIcon = (ImageView) convertView.findViewById(R.id.char_detail_wallet_list_item_transaction_icon);
		final TextView typeNameCount = (TextView) convertView.findViewById(R.id.char_detail_wallet_list_item_itemname);
		final TextView stationName = (TextView) convertView.findViewById(R.id.char_detail_wallet_list_item_stationname);
		final TextView price = (TextView) convertView.findViewById(R.id.char_detail_wallet_list_item_itemvalue);
		
		typeNameCount.setText(transaction.getQuantity() + "x " + transaction.getTypeName());
		stationName.setText(transaction.getStationName());
		
		boolean isBuy = transaction.getTransactionType().equals("buy");
		
		String formattedValue = formatter.format(transaction.getPrice() * transaction.getQuantity());
		String priceString = (isBuy ? "-" : "+") + formattedValue + " ISK";
		price.setText(priceString);
		price.setTextColor(isBuy ? RED : GREEN);
		
		typeIcon.setImageDrawable(null);
		Picasso.with(context).load(ImageURL.forType(transaction.getTypeID())).into(typeIcon);
		
		return convertView;
	}
}