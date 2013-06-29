package com.zdonnell.eden.helpers;


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
		if (view != null) 
		{
			if (finished) 
			{
				view.setText(Html.fromHtml("<FONT COLOR='#FF4444'>Skill Queue Empty</FONT>"));
			}
			else view.setText("");
		}
		this.view = view;
	}

	@Override
	public void onFinish() {
		if (view != null) view.setText(Html.fromHtml("<FONT COLOR='#FF4444'>Skill Queue Empty</FONT>"));
		finished = true;
	}

	@Override
	public void onTick(long millisUntilFinished) 
	{
		if (millisUntilFinished < 24 * 60 * 60 * 1000 && millisUntilFinished > 0) 
		{
			if (view != null) view.setText(Html.fromHtml("<FONT COLOR='#FFBB33'>" + Tools.millisToEveFormatString(millisUntilFinished) + "</FONT>"));
		}
		else if (view != null) view.setText(Html.fromHtml("<FONT COLOR='#99CC00'>" + Tools.millisToEveFormatString(millisUntilFinished) + "</FONT>"));
	}
}
