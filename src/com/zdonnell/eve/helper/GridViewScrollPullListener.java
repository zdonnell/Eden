package com.zdonnell.eve.helper;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;

public class GridViewScrollPullListener implements AbsListView.OnTouchListener {

	private static final int UP = -1;
	private static final int DOWN = 1;
	
	private boolean isScrolling = false;
	
	private boolean hasStoppedScrollingUp;
	
	private boolean hasStoppedScrollingDown;
	
	private float scrollDownStopPoint;
	
	private float scrollUpStopPoint;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		GridView gridView = (GridView) v;
		
		if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			Log.d("CAN SCROLL UP: ", "" + gridView.canScrollVertically(UP));	
			Log.d("CAN SCROLL DOWN: ", "" + gridView.canScrollVertically(DOWN));	
			
			if (!isScrolling) isScrolling = true;
			
			if (hasStoppedScrollingUp)
			{
				
			}
			
			if (hasStoppedScrollingDown) 
			{
				
			}
			
			/* 
			 * This specifically checks if the GridView has stopped scrolling, but has not yet notified us
			 * If that's the case, mark this spot as where the scroll stopped in either direction, to calculate
			 * touch pull offsets
			 */
			if (!gridView.canScrollVertically(UP) && !hasStoppedScrollingUp) scrollUpStopPoint = event.getY();
			if (!gridView.canScrollVertically(DOWN) && !hasStoppedScrollingDown) scrollDownStopPoint = event.getY();
			
			/* Set whether the list is scroll locked in either direction */
			hasStoppedScrollingUp = !gridView.canScrollVertically(UP);
			hasStoppedScrollingDown = !gridView.canScrollVertically(DOWN);
		}
			
		return false;
	}

}
