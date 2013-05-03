package com.zdonnell.eve.character.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SquareLinearLayout extends LinearLayout {

	public SquareLinearLayout(Context context) 
	{
		super(context);
	}
	
	public SquareLinearLayout(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		/* Force it to be a square */
	    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}

}
