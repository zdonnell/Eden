package com.zdonnell.eve.character.detail.mail;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.beimin.eveapi.character.mail.messages.ApiMailMessage;
import com.beimin.eveapi.character.mail.messages.MailMessagesResponse;
import com.beimin.eveapi.core.ApiAuthorization;
import com.beimin.eveapi.exception.ApiException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zdonnell.eve.R;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.character.APICharacter;
import com.zdonnell.eve.character.detail.DetailFragment;
import com.zdonnell.eve.helpers.ImageURL;

/**
 * This Fragment is used to display the Mail for a character.
 * 
 * @author Zach
 *
 */
public class MailFragment extends DetailFragment 
{
	ListView mailList;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {    	
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_mail, container, false);

    	mailList = (ListView) inflatedView.findViewById(R.id.char_detail_mail_list);

    	loadData();
    	
    	return inflatedView;
    }  
    
    @Override
	public void loadData() 
	{   
    	ApiAuthorization apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), getArguments().getInt("characterID"), getArguments().getString("vCode"));
		new APICharacter(context, apiAuth).getMailHeaders(new APIExceptionCallback<MailMessagesResponse>(parentActivity)
		{
			@Override
			public void onUpdate(MailMessagesResponse response) 
			{					
				Set<ApiMailMessage> mailHeaders = response.getAll();
				ApiMailMessage[] mailHeadersArray = new ApiMailMessage[mailHeaders.size()];
				
				mailHeaders.toArray(mailHeadersArray);
				Arrays.sort(mailHeadersArray, new MailSort.Date());
				
				mailList.setAdapter(new MailAdapter(context, R.layout.char_detail_mail_list_item, mailHeadersArray));
			}

			@Override
			public void onError(MailMessagesResponse response, ApiException exception) 
			{
				// TODO add UI element notifying user of error
			}
		});
	}
    
    /**
     * 
     * 
     * @author Zach
     *
     */
    private class MailAdapter extends ArrayAdapter<ApiMailMessage>
    {
    	final private int layoutResId;
    	
    	public MailAdapter(Context context, int layoutResId, ApiMailMessage[] objects) 
		{
			super(context, layoutResId, objects);
			this.layoutResId = layoutResId;
		}
    	
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) convertView = inflater.inflate(layoutResId, null, false);
			
			TextView subjectTextView = (TextView) convertView.findViewById(R.id.char_detail_mail_list_item_text);
			TextView subTextTextView = (TextView) convertView.findViewById(R.id.char_detail_mail_list_item_subText);
			
			ImageView portrait = (ImageView) convertView.findViewById(R.id.char_detail_mail_list_item_icon);
			ImageLoader.getInstance().displayImage(ImageURL.forChar((int) getItem(position).getSenderID()), portrait);
			
			subjectTextView.setText(getItem(position).getTitle());
			subTextTextView.setText(formatter.format(getItem(position).getSentDate()));
			
			return convertView;
		}
    }
}
