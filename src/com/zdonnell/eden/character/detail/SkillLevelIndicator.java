package com.zdonnell.eden.character.detail;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.zdonnell.androideveapi.character.sheet.ApiSkill;
import com.zdonnell.androideveapi.character.skill.queue.ApiSkillQueueItem;
import com.zdonnell.eden.helpers.Tools;

public class SkillLevelIndicator extends View {

    private static final int SKILL_LEVEL_MAX = 5;
    private static final int BASE_COLOR = Color.rgb(70, 70, 70);

    private int width, height;

    private boolean skillInfoObtained;

    private int activeSkillColor;

    private int currentLevel = -1;

    private boolean isTraining;

    private int borderWidth, boxPadding, boxWidth;

    private Paint paint = new Paint();

    /* Default constructors */
    public SkillLevelIndicator(Context context) {
        super(context);
    }

    public SkillLevelIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SkillLevelIndicator(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    public void provideSkillInfo(ApiSkill skill, boolean isTraining, int activeSkillColor) {
        this.isTraining = isTraining;
        currentLevel = skill.getLevel();

        this.activeSkillColor = activeSkillColor;
        skillInfoObtained = true;
    }

    public void provideSkillInfo(ApiSkillQueueItem skill, boolean isTraining, int activeSkillColor) {
        this.isTraining = isTraining;
        currentLevel = skill.getLevel();

        this.activeSkillColor = activeSkillColor;
        skillInfoObtained = true;
    }

    public void reset() {
        this.isTraining = false;
        this.currentLevel = -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(BASE_COLOR);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAlpha(255);

        canvas.drawRect(0, 0, width, height, paint);

        if (skillInfoObtained) drawSkillBoxes(canvas);
        if (isTraining) invalidate();
    }

    private void drawSkillBoxes(Canvas canvas) {
        paint.setStyle(Style.FILL);

        for (int boxNum = 0; boxNum < currentLevel; boxNum++) {
            if (boxNum == currentLevel - 1) {
                paint.setColor(activeSkillColor);

                if (isTraining) {
                    int alphaValue = (int) (155 + Math.sin(System.currentTimeMillis() / 350.0) * 100.0);
                    paint.setAlpha(alphaValue);
                }
            }

            float left = borderWidth + ((boxNum + 1) * boxPadding) + (boxNum * boxWidth);
            float top = borderWidth + boxPadding;
            float right = left + boxWidth;
            float bottom = height - (borderWidth + boxPadding);

			/* If math is off by a a rounding error, correct the last segment */
            if (right > width - (borderWidth + boxPadding))
                right = width - (borderWidth + boxPadding);

            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        borderWidth = Tools.dp2px(2, getContext());
        boxPadding = Tools.dp2px(1, getContext());

        float totalBoxSpace = width - (borderWidth * 2) - (boxPadding * (SKILL_LEVEL_MAX + 1));
        boxWidth = (int) (totalBoxSpace / SKILL_LEVEL_MAX);
    }
}
