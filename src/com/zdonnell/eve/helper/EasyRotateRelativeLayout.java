package com.zdonnell.eve.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class EasyRotateRelativeLayout extends RelativeLayout {
	
	public final static int X_AXIS = 0;
	public final static int Y_AXIS = 0;
	public final static int Z_AXIS = 0;

	public EasyRotateRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public EasyRotateRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public EasyRotateRelativeLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}
	
	public void setRotation(int angle, int axis)
	{
		switch (axis)
		{
		case X_AXIS:
			break;
		}
	}
}
