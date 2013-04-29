package com.zdonnell.eve;

import java.text.NumberFormat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beimin.eveapi.exception.ApiException;
import com.beimin.eveapi.server.ServerStatusResponse;
import com.zdonnell.eve.apilink.APICallback;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.server.Server;
import com.zdonnell.eve.helpers.BasicOnTouchListener;

public class SlideMenuFragment extends ListFragment {

	private final static int CHARS = 0;
	//private final static int CORPS = 1;
	private final static int ACCOUNTS = 1;
	private final static int SETTINGS = 2;
	
	/**
	 * Array of Strings for the menu item titles
	 */
	private static String[] mainMenuItems = new String[3];
	static
	{
		mainMenuItems[CHARS] = "Characters";
		//mainMenuItems[CORPS] = "Corporations";
		mainMenuItems[ACCOUNTS] = "API Keys";
		mainMenuItems[SETTINGS] = "Settings";
	}
	
	/**
	 * Array of Resource IDs for the menu item icons
	 */
	private static int[] ItemImageResources = new int[3];
	static
	{
		ItemImageResources[CHARS] = R.drawable.characters_icon;
		//ItemImageResources[CORPS] = R.drawable.corporations_icon;
		ItemImageResources[ACCOUNTS] = R.drawable.accounts_icon;
		ItemImageResources[SETTINGS] = R.drawable.settings_icon;
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View root = inflater.inflate(R.layout.list, null);
		setServerStatus(root);
		return root;
	}

	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity(), mainMenuItems);
		setListAdapter(adapter);
	}

	public class SampleAdapter extends ArrayAdapter<String> 
	{
		public SampleAdapter(Context context, String[] items) 
		{
			super(context, 0, items);
		}

		public View getView(final int position, View convertView, ViewGroup parent) 
		{
			if (convertView == null) 
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			
			convertView.setOnTouchListener(new BasicOnTouchListener());
			convertView.setOnClickListener(new View.OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					Intent intent;
					
					switch (position)
					{
					case CHARS:
						intent = new Intent(getActivity(), CharactersActivity.class);
						break;
					case ACCOUNTS:
						intent = new Intent(getActivity(), APIKeysActivity.class);
						break;
					case SETTINGS:
						intent = new Intent(getActivity(), SettingsActivity.class);
						break;
					default:
						intent = null;
						break;
					}
					
					startActivity(intent);
					((BaseActivity) getActivity()).toggle();
				}
			});
			
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(ItemImageResources[position]);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position));

			return convertView;
		}
	}
	
	private void setServerStatus(View root)
    {
        final TextView serverStatusText = (TextView) root.findViewById(R.id.server_status);
        
        new Server(getActivity()).status(new APIExceptionCallback<ServerStatusResponse>((BaseActivity) getActivity()) 
        {
			@Override
			public void onUpdate(ServerStatusResponse serverStatus) {
				if (serverStatus.isServerOpen()) 
				{
					NumberFormat nf = NumberFormat.getInstance();
					serverStatusText.setText(Html.fromHtml("<B><FONT COLOR='#669900'>ONLINE</FONT></B> " + nf.format(serverStatus.getOnlinePlayers())));
				}
				else
				{
					serverStatusText.setText(Html.fromHtml("<B><FONT COLOR='#CC0000'>OFFLINE</FONT></B>"));
				}
			}

			@Override
			public void onError(ServerStatusResponse response, ApiException exception) 
			{
				
			}
		}); 
    }
}
