package com.zdonnell.eve.character.detail.mail;

import java.util.Comparator;

import com.beimin.eveapi.character.mail.messages.ApiMailMessage;

public class MailSort 
{
	public static class Date implements Comparator<ApiMailMessage>
	{		
		public int compare(ApiMailMessage lhs, ApiMailMessage rhs) 
		{
			return rhs.getSentDate().compareTo(lhs.getSentDate());
		}
	}
}
