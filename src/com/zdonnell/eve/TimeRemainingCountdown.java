package com.zdonnell.eve;

import android.os.CountDownTimer;
import android.text.Html;
import android.widget.TextView;

public class TimeRemainingCountdown extends CountDownTimer
{
	private TextView view;
	private boolean finished = false;
	
	public TimeRemainingCountdown(long millisInFuture, long countDownInterval, TextView view) {
		super(millisInFuture, countDownInterval);
		this.view = view;
	}
	
	public void updateTextView(TextView view)
	{
		if (finished) view.setText(Html.fromHtml("<FONT COLOR='#FF4444'>Skill Queue Empty</FONT>"));
		this.view = view;
	}

	@Override
	public void onFinish() {
		view.setText(Html.fromHtml("<FONT COLOR='#FF4444'>Skill Queue Empty</FONT>"));
		finished = true;
	}

	@Override
	public void onTick(long millisUntilFinished) 
	{
		if (millisUntilFinished < 24 * 60 * 60 * 1000 && millisUntilFinished > 0) 
		{
			view.setText(Html.fromHtml("<FONT COLOR='#FFBB33'>" + Tools.millisToEveFormatString(millisUntilFinished) + "</FONT>"));
		}
		else view.setText(Html.fromHtml("<FONT COLOR='#99CC00'>" + Tools.millisToEveFormatString(millisUntilFinished) + "</FONT>"));
	}
}
