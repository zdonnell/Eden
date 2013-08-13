package com.zdonnell.eden.character.detail.wallet;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdonnell.androideveapi.eve.reftypes.ApiRefType;
import com.zdonnell.androideveapi.shared.wallet.journal.ApiJournalEntry;
import com.zdonnell.eden.R;

public class WalletJournalAdapter extends ArrayAdapter<ApiJournalEntry> {
	private LayoutInflater inflater;

	private final int RED = Color.parseColor("#FF4444");
	private final int GREEN = Color.parseColor("#449944");

	private final static int layout = R.layout.char_detail_wallet_list_item_journal;

	private NumberFormat formatter = NumberFormat.getInstance();

	private SparseArray<ApiRefType> refTypes;

	private Map<TextView, Integer> refTypeMap = new HashMap<TextView, Integer>();

	private String characterName;

	public WalletJournalAdapter(Context context, ApiJournalEntry[] entries, String characterName, SparseArray<ApiRefType> refTypes) {
		super(context, layout, entries);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.characterName = characterName;

		formatter.setMaximumFractionDigits(2);
		formatter.setMinimumFractionDigits(2);

		this.refTypes = refTypes;
	}

	public void provideRefTypes(SparseArray<ApiRefType> refTypes) {
		this.refTypes = refTypes;
		for (TextView textView : refTypeMap.keySet()) {
			textView.setText(refTypes.get(refTypeMap.get(textView).intValue()).getRefTypeName());
		}
		refTypeMap.clear();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Determine if we recycle the old view, or inflate a new one
		if (convertView == null)
			convertView = (LinearLayout) inflater.inflate(layout, parent, false);

		prepareEntry(convertView, getItem(position));

		return convertView;
	}

	private void prepareEntry(View itemView, ApiJournalEntry entry) {
		final TextView journalType = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalType);
		final TextView description = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalDescription);
		final TextView amount = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalAmount);
		final TextView balance = (TextView) itemView.findViewById(R.id.char_detail_wallet_list_item_journalBalance);

		if (refTypes != null) {
			journalType.setText(refTypes.get(entry.getRefType().getId()).getRefTypeName());
		} else {
			journalType.setText(entry.getRefType().name());
			refTypeMap.put(journalType, entry.getRefType().getId());
		}
		description.setText(generateDescription(entry));

		amount.setTextColor(entry.getAmount() < 0 ? RED : GREEN);
		amount.setText(formatter.format(entry.getAmount()) + " ISK");

		balance.setText(formatter.format(entry.getBalance()) + " ISK");
	}

	private String generateDescription(ApiJournalEntry entry) {
		switch ((int) entry.getRefType().getId()) {
			case 2:
				return entry.getOwnerName1() + " bought stuff from you";
			case 10:
				if (entry.getOwnerName1().equals(characterName))
					return "You deposited cash into " + entry.getOwnerName2() + "'s account";
				else return entry.getOwnerName1() + " deposited cash into your account";
			default:
				return "";
		}
	}
}
