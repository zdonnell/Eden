package com.zdonnell.eve.character.detail.queue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.zdonnell.androideveapi.character.skill.queue.ApiSkillQueueItem;
import com.zdonnell.eve.R;
import com.zdonnell.eve.helpers.Tools;

public class SkillQueueSegment extends View
{    	
	private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
				
	int width, height;
	
	private Paint paint = new Paint();;
	
	private int manual_padding = 10;
		
	private ApiSkillQueueItem skillInQueue;
	
	private int skillNumber;
	
	int[] colors;
	
	public SkillQueueSegment(Context context) 
	{
		super(context);

		colors = new int[2];
		colors[0] = getResources().getColor(R.color.primary_accent_color);
		colors[1] = getResources().getColor(R.color.secondary_accent_color);
		
		manual_padding = Tools.dp2px(10, context);
		
		paint.setStyle(Paint.Style.FILL);
	}
	
	/**
	 * Sets the skillQueue to draw from. Forces a redraw.
	 * 
	 * @param skillQueue
	 */
	public void setQueue(ApiSkillQueueItem skillInQueue, int skillNumber)
	{
		this.skillInQueue = skillInQueue;
		this.skillNumber = skillNumber;
				
		invalidate();
	}
	
	/**
	 * Draws the bar, only refreshed when {@link setQueue} is called
	 * 
	 * @param canvas
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{		
		paint.setColor(Color.parseColor("#AFAFAF"));
		
		long timeUntilStart, timeUntilEnd;
		
		timeUntilStart = Tools.timeUntilUTCTime(skillInQueue.getStartTime());
		timeUntilEnd = Tools.timeUntilUTCTime(skillInQueue.getEndTime());
		
		if (timeUntilStart < 0) timeUntilStart = 0;
		
		paint.setColor(colors[skillNumber % 2]);
		
		/* If the skill fits in the 24 hour period */
		if (timeUntilStart < DAY_IN_MILLIS)
		{
			double percentOfBar = (double) (timeUntilEnd - timeUntilStart) / DAY_IN_MILLIS;
			int widthOfSegment = (int) (percentOfBar * (width - (manual_padding * 2)));
			
			int startOfSegment = manual_padding + (int) (((double) timeUntilStart / (double) DAY_IN_MILLIS) * (width - (manual_padding * 2)));
			int endOfSegment = startOfSegment + widthOfSegment;
			
			if (endOfSegment > width) endOfSegment = width;
			
			canvas.drawRect(startOfSegment, 0, endOfSegment, height, paint);
		}
	}
	
	@Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
	{
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }
}