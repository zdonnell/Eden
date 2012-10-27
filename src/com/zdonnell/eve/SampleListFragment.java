package com.zdonnell.eve;

import java.text.NumberFormat;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.server.Server;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SampleListFragment extends ListFragment {

	private final static int CHARS = 0;
	private final static int CORPS = 1;
	private final static int ACCOUNTS = 2;
	private final static int SETTINGS = 3;
	
	/**
	 * Array of Strings for the menu item titles
	 */
	private static String[] mainMenuItems = new String[4];
	static
	{
		mainMenuItems[CHARS] = "Characters";
		mainMenuItems[CORPS] = "Corporations";
		mainMenuItems[ACCOUNTS] = "Accounts";
		mainMenuItems[SETTINGS] = "Settings";
	}
	
	/**
	 * Array of Resource IDs for the menu item icons
	 */
	private static int[] ItemImageResources = new int[4];
	static
	{
		ItemImageResources[CHARS] = R.drawable.zach;
		ItemImageResources[CORPS] = R.drawable.zach;
		ItemImageResources[ACCOUNTS] = R.drawable.zach;
		ItemImageResources[SETTINGS] = R.drawable.zach;
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.list, null);
		setServerStatus(root);
		return root;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity(), mainMenuItems);
		setListAdapter(adapter);
	}

	public class SampleAdapter extends ArrayAdapter<String> {

		public SampleAdapter(Context context, String[] items) {
			super(context, 0, items);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(ItemImageResources[position]);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position));

			return convertView;
		}
	}
	
	private void setServerStatus(View root)
    {
    	Server server = new Server(getActivity());
        final TextView serverStatus = (TextView) root.findViewById(R.id.server_status);
		server.status(new APICallback<String[]>() {
			@Override
			public void onUpdate(String[] updatedData) {
				if (updatedData[0].equals("True")) 
				{
					NumberFormat nf = NumberFormat.getInstance();
					serverStatus.setText(Html.fromHtml("<B><FONT COLOR='#669900'>ONLINE</FONT></B> " + nf.format(Integer.parseInt(updatedData[1]))));
				}
				else
				{
					serverStatus.setText(Html.fromHtml("<B><FONT COLOR='#CC0000'>OFFLINE</FONT></B>"));
				}
			}
		}); 
    }
}