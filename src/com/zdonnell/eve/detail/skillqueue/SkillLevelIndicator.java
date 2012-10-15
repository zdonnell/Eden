package com.zdonnell.eve.detail.skillqueue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

import com.zdonnell.eve.Tools;
import com.zdonnell.eve.api.character.QueuedSkill;

public class SkillLevelIndicator extends View {

	private static final int SKILL_LEVEL_MAX = 5;
	private static final int BASE_COLOR = Color.DKGRAY;
	
	private Context context;
	
	private int width, height;
	
	private boolean skillInfoObtained;
	
	private int activeSkillColor;
		
	private int currentLevel = -1;
	
	private boolean nextLevelTraining;
	
	private int borderWidth, boxPadding, boxWidth;
	
	private Paint paint;
		
	public SkillLevelIndicator(Context context, Color activeSkillColor) 
	{
		super(context);
		
		this.context = context;
		this.activeSkillColor = activeSkillColor.hashCode();
		
		paint = new Paint();
		paint.setColor(BASE_COLOR);
	}

	
	public void provideSkillInfo(QueuedSkill skill)
	{
		currentLevel = skill.skillLevel - 1;
		
		
		this.currentLevel = nextLevelTraining ? currentLevel + 1 : currentLevel;
		this.nextLevelTraining = nextLevelTraining;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(borderWidth);
		
		canvas.drawRect(0, 0, width, height, paint);
		
		if (skillInfoObtained) drawSkillBoxes(canvas);
	}
	
	
	private void drawSkillBoxes(Canvas canvas)
	{
		paint.setStyle(Style.FILL);
		
		paint.setColor(BASE_COLOR);
		paint.setAlpha(255);
		
		for (int boxNum = 0; boxNum < currentLevel; boxNum++)
		{			
			if (nextLevelTraining && boxNum == currentLevel - 1) 
			{
				int alphaValue = (int) (205 + Math.sin(System.currentTimeMillis() / 200.0) * 50.0);
				
				paint.setColor(activeSkillColor);
				paint.setAlpha(alphaValue);
			}
			
			float left = borderWidth + boxPadding + (boxNum * boxWidth);
			float top = borderWidth + boxPadding;
			float right = left + boxWidth;
			float bottom = height - (borderWidth + boxPadding);
			
			/* It's possible that our pixel rounding will extend the end of the last skill box 
			 * into the right hand padding.  This will account for that and just set the end of the last
			 * skill box as far as it can without extending into the padding
			 */
			if (right > width - (borderWidth + boxPadding)) right = width - (borderWidth + boxPadding);
			
			canvas.drawRect(left, top, right, bottom, paint);
		}
	}
	
	@Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
	{
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        
        borderWidth = Tools.dp2px(2, context);
        boxPadding = Tools.dp2px(2, context);
        
        float totalBoxSpace = width - (borderWidth * 2) - (boxPadding * (SKILL_LEVEL_MAX + 1));
        boxWidth = (int) (totalBoxSpace / SKILL_LEVEL_MAX);
    }
}
