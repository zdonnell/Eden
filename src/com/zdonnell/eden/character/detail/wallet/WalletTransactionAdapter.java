package com.zdonnell.eden.character.detail.wallet;

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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zdonnell.androideveapi.shared.wallet.transactions.ApiWalletTransaction;
import com.zdonnell.eden.R;
import com.zdonnell.eden.helpers.ImageURL;

public class WalletTransactionAdapter extends ArrayAdapter<ApiWalletTransaction> {
    private LayoutInflater inflater;

    private final int RED = Color.parseColor("#FF4444");
    private final int GREEN = Color.parseColor("#449944");

    private NumberFormat formatter = NumberFormat.getInstance();

    private final static int layoutID = R.layout.char_detail_wallet_list_item_transaction;

    public WalletTransactionAdapter(Context context, ApiWalletTransaction[] entries) {
        super(context, layoutID, entries);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ApiWalletTransaction transaction = getItem(position);

        // Determine if we recycle the old view, or inflate a new one
        if (convertView == null)
            convertView = (LinearLayout) inflater.inflate(layoutID, parent, false);

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

        ImageLoader.getInstance().displayImage(ImageURL.forType(transaction.getTypeID()), typeIcon);

        return convertView;
    }
}