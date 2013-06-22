package com.zdonnell.eve.character.detail.queue;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.view.View;

import com.zdonnell.androideveapi.character.skill.queue.ApiSkillQueueItem;
import com.zdonnell.eve.R;
import com.zdonnell.eve.helpers.Tools;

public class SkillQueueBar extends View
{    	
	private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	
	private final int keyColor = Color.rgb(0, 0, 0);
	
	private int manual_padding;
	
	private Context context;
	
	private int width, height;
	
	private Paint paint;
	
	private boolean queueObtained = false;
	
	private ArrayList<ApiSkillQueueItem> skillQueue;
	
	private int[] colors;
	
	public SkillQueueBar(Context context) 
	{
		super(context);
		this.context = context;
		
		colors = new int[2];
		colors[0] = getResources().getColor(R.color.primary_accent_color);
		colors[1] = getResources().getColor(R.color.secondary_accent_color);
		
		manual_padding = Tools.dp2px(10, context);
		
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
	}
	
	/**
	 * Sets the skillQueue to draw from. Forces a redraw.
	 * 
	 * @param skillQueue
	 */
	public void setQueue(ArrayList<ApiSkillQueueItem> skillQueue)
	{
		this.skillQueue = skillQueue;
		queueObtained = true;
		
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
		drawBar(canvas);
		drawKey(canvas);
	}
	
	/**
	 * Specifically just draws the solid bar
	 * 
	 * @param canvas
	 */
	private void drawBar(Canvas canvas)
	{				
		int queuePosition = 0;
		int currentBarPosition = manual_padding;
		
		// If we have been given a queue and it's not empty
		if (queueObtained && skillQueue.size() > 0)
		{			
			for (ApiSkillQueueItem skill : skillQueue)
			{
				long startAdvanceValue = (queuePosition == 0) ? 0 : Tools.timeUntilUTCTime(skill.getStartTime());
								
				double percentOfBar = (double) (Tools.timeUntilUTCTime(skill.getEndTime()) - startAdvanceValue) / (double) DAY_IN_MILLIS;
				int secLength = (int) (percentOfBar * width);
				
				int start = currentBarPosition;
				int end = start + secLength;
				
				if (end > width + manual_padding) end = width + manual_padding;	// Cap it at max length
						
				paint.setColor(colors[queuePosition % 2]);
				paint.setStyle(Style.FILL);
				canvas.drawRect(start, manual_padding, end, height / 2, paint);
				
				currentBarPosition = end;
				
				if (Tools.timeUntilUTCTime(skill.getEndTime()) > DAY_IN_MILLIS) break;
				
				++queuePosition;
			}
			
			// If there is room left in the 24 hour window, fill it with grey
			if (currentBarPosition < width + manual_padding)
			{
				paint.setColor(Color.GRAY);
				canvas.drawRect(currentBarPosition, manual_padding, width + manual_padding, height / 2, paint);
			}
		}
		
		// If the queue is empty fill it with grey
		else
		{
			paint.setColor(Color.GRAY);
			canvas.drawRect(currentBarPosition, manual_padding, width + manual_padding, height / 2, paint);
		}
		
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(keyColor);
		canvas.drawRect(manual_padding, manual_padding, manual_padding + width, height / 2, paint);
		
	}
	
	/**
	 * Draws key related items: Ticks, bottom hour indicators
	 * 
	 * @param canvas
	 */
	private void drawKey(Canvas canvas)
	{
		paint.setStrokeWidth(2);
		paint.setColor(keyColor);
		paint.setAntiAlias(true);
		
		paint.setTypeface(Typeface.DEFAULT);
		paint.setTextSize(Tools.dp2px(14, context));
		
		// Draw the tick marks
		for (int x = 0; x <= 24; x++)
		{
			int xLoc = (int) (x / 24f * width) + manual_padding;
			if (x == 24) xLoc = width + manual_padding - 1;
			
			canvas.drawLine(xLoc, height / 2, xLoc, height * 0.6f, paint);
		}
		
		// Draw hour indicators
		paint.setColor(Color.rgb(150, 150, 150));
		paint.setStyle(Style.FILL);
		
		paint.setTextAlign(Align.LEFT);
		canvas.drawText("0", manual_padding, height * 0.8f, paint);
		paint.setTextAlign(Align.CENTER);
		canvas.drawText("12", manual_padding + (width / 2f), height * 0.8f, paint);
		paint.setTextAlign(Align.RIGHT);
		canvas.drawText("24", manual_padding + width, height * 0.8f, paint);
	}
	
	@Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
	{
        super.onSizeChanged(w, h, oldw, oldh);
        width = w - (manual_padding * 2);
        height = (int) (h * 1.1f);
    }
}