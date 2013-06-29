package com.zdonnell.eden.helpers;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.view.MotionEvent;
import android.view.View;

public class BasicOnTouchListener implements View.OnTouchListener 
{
	int originalBackground;
	
	public boolean onTouch(View v, MotionEvent event) 
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			Drawable background = v.getBackground();
			if (background == null)
			{
				originalBackground = -1;
			}
			else
			{
				if (background instanceof ColorDrawable) originalBackground = ((ColorDrawable) v.getBackground()).getColor();
				else originalBackground = ((PaintDrawable) v.getBackground()).getPaint().getColor();
			}
			
			v.setBackgroundColor(Color.parseColor("#FF8800"));
		}
		else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_SCROLL)
		{
			if (originalBackground != -1) v.setBackgroundColor(originalBackground);
			else v.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return false;
	}
}
