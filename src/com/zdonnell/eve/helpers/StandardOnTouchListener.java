package com.zdonnell.eve.helpers;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;

public class StandardOnTouchListener implements View.OnTouchListener {

	private Color originalBgColor;
	
	@Override
	public boolean onTouch(View view, MotionEvent event) 
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			view.setBackgroundColor(Color.parseColor("#ff8800"));
		}
		else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_MOVE)
		{
			view.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return false;
	}
}
