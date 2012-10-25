package com.zdonnell.eve.helper;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
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
			if (hasStoppedScrollingUp)
			{
				for (int x = 0; x < gridView.getNumColumns() * 3 && x < gridView.getCount(); x++)
				{					
					int row = (int) Math.ceil(x / 3d);
					
					View characterTile = (View) gridView.getChildAt(x);
					
					float distanceFromStart = -1 * (event.getY() - scrollUpStopPoint);
					
					float centerX = characterTile.getWidth() / 2;
					float centerY = characterTile.getHeight() / 2;
					
					final Rotate3dAnimation rotation = new Rotate3dAnimation(distanceFromStart * (0.03f / row), distanceFromStart * 0.25f, centerX, centerY, 200.0f, true);
			        rotation.setDuration(1);
			        rotation.setFillAfter(true);
			        rotation.setInterpolator(new AccelerateInterpolator());

			        characterTile.startAnimation(rotation);
					
				}
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
		else if (event.getAction() == MotionEvent.ACTION_UP)
		{
			hasStoppedScrollingUp = hasStoppedScrollingDown = false;
			
			
			for (int x = 0; x < gridView.getNumColumns(); x++)
			{
				View characterTile = (View) gridView.getChildAt(x);
				
				int row = (int) Math.ceil(x / 3d);
				
				float distanceFromStart = -1 * (event.getY() - scrollUpStopPoint);
				
				float centerX = characterTile.getWidth() / 2;
				float centerY = characterTile.getHeight() / 2;
				
				final Rotate3dAnimation rotation = new Rotate3dAnimation(distanceFromStart * 0.03f / row, 0, centerX, centerY, 0, true);
		        rotation.setDuration(150);
		        rotation.setFillAfter(true);
		        rotation.setInterpolator(new AccelerateInterpolator());

		        characterTile.startAnimation(rotation);
				
			}
		}
			
		return false;
	}

}
