package com.zdonnell.eve.notifications;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends IntentService 
{
	public NotificationService(String name) 
	{
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		int characterID = intent.getIntExtra("characterID", 0);
		if (characterID == 0) return; // The characterID was not passed (or not passed correctly); do nothing.
		
		
	}
}
