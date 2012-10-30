package com.zdonnell.eve.character.detail;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.QueuedSkill;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.helpers.TimeRemainingCountdown;
import com.zdonnell.eve.helpers.Tools;

public class SkillQueueFragment extends Fragment {
    
    private static final int LIGHT = 0;
	private static final int DARK = 1;
	
	/**
	 * The ListView that stores the queue listing
	 */
	private ListView skillQueueList;
	
	private static int[] colors = new int[2];
	static
	{
		colors[LIGHT] = Color.parseColor("#FFBB33");
		colors[DARK] = Color.parseColor("#FF8800");
	}
        
    private APICharacter character;
    
    private ArrayList<QueuedSkill> skillQueue;
    
    private Context context;
    
    public SkillQueueFragment(APICharacter character) 
    {
    	this.character = character;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_skillqueue, container, false);
    	final SkillQueueBar skillQueueBar = new SkillQueueBar(inflater.getContext(), colors);
    	final TextView queueTimeRemaining = (TextView) inflatedView.findViewById(R.id.queue_time_remaining_text);
    	final TextView queueLength = (TextView) inflatedView.findViewById(R.id.skill_queue_size_text);
    	
    	inflatedView.addView(skillQueueBar, 1);
    	skillQueueBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dp2px(80, inflater.getContext())));
    	
    	skillQueueList = (ListView) inflatedView.findViewById(R.id.char_detail_queue_list);
    	skillQueueList.setDivider(context.getResources().getDrawable(R.drawable.divider_grey));
    	skillQueueList.setDividerHeight(1);
    	
    	character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() 
    	{
			@Override
			public void onUpdate(ArrayList<QueuedSkill> skillQueue) 
			{
				skillQueueBar.setQueue(skillQueue);
				long timeRemainingInQueue = Tools.timeUntilUTCTime(skillQueue.get(skillQueue.size() - 1).endTime);
				new TimeRemainingCountdown(timeRemainingInQueue, 1000, queueTimeRemaining).start();
				
				queueLength.setText(skillQueue.size() + " Skill(s) in Queue");
				
				updateQueueList(skillQueue);
			}
    	});
    	    	
    	return inflatedView;
    }
    
    /**
     * Takes a skillQueue and provides it to the  {@link ListView} responsible for displaying
     * the queued skills.
     * 
     * @param skillQueue an {@link ArrayList} of {@link QueuedSkill} objects to build the listview from
     */
    private void updateQueueList(ArrayList<QueuedSkill> skillQueue)
    {
    	this.skillQueue = skillQueue;
    	
    	QueuedSkill[] rawArraySkillQueue = new QueuedSkill[skillQueue.size()];
    	rawArraySkillQueue = skillQueue.toArray(rawArraySkillQueue);
    	    	
    	skillQueueList.setAdapter(new SkillQueueListAdapter(context, R.layout.char_detail_skillqueue_list_item, rawArraySkillQueue));
    }
    
    
    private class SkillQueueListAdapter extends ArrayAdapter<QueuedSkill>
	{
    	QueuedSkill[] skillQueue;
		
		/**
		 * ID of the resource to inflate for the entire row
		 */
		int resourceID;
		
		public SkillQueueListAdapter(Context context, int viewResourceID, QueuedSkill[] skillQueue) 
		{
			super(context, viewResourceID, skillQueue);			
			this.skillQueue = skillQueue;
			this.resourceID = viewResourceID;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			final ArrayList<QueuedSkill> currentSkillQueue = SkillQueueFragment.this.skillQueue;
			
			LinearLayout preparedView; 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
			SkillQueueSegment skillQueueSegment;
			
			if (convertView != null) 
			{
				preparedView = (LinearLayout) convertView;
				
				skillQueueSegment = (SkillQueueSegment) preparedView.getChildAt(1);
				skillQueueSegment.setQueue(currentSkillQueue, position);				
			}
			else 
			{
				preparedView = (LinearLayout) inflater.inflate(resourceID, parent, false);
				
				skillQueueSegment = new SkillQueueSegment(context, colors);
				
				skillQueueSegment.setQueue(currentSkillQueue, position);
				preparedView.addView(skillQueueSegment);
				skillQueueSegment.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dp2px(3, inflater.getContext())));
			}
			
			/* Configure Skill Level Indicator View */
			SkillLevelIndicator skillLevelIndicator = (SkillLevelIndicator) preparedView.findViewById(R.id.skill_level_indicator);
			skillLevelIndicator.provideSkillInfo(currentSkillQueue.get(position), position == 0, colors[0]);
			
			/* Alternate Skill Queue Row Background Color */
			preparedView.setBackgroundColor((position % 2 == 1) ? Color.parseColor("#CCCCCC") : Color.parseColor("#DDDDDD")); 
			
			final TextView skillName = (TextView) preparedView.findViewById(R.id.skillqueue_detail_list_item_skillname);
			TextView skillLevel = (TextView) preparedView.findViewById(R.id.skill_level_text);			
			skillLevel.setText("Level " + currentSkillQueue.get(position).skillLevel);
			
			new Eve(context).getTypeName(new APICallback<String[]>() 
			{
				@Override
				public void onUpdate(String[] typeNames) 
				{
					skillName.setText(typeNames[0]);
				}
			}, new int[] { skillQueue[position].skillID });
									
			return preparedView;
		}
	}
    
    public class SkillQueueSegment extends View
    {    	
    	private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    				
    	int width, height;
    	
    	private Paint paint = new Paint();;
    	
    	private int manual_padding = 10;
    		
    	private ArrayList<QueuedSkill> skillQueue;
    	
    	private int skillNumber;
    	
    	int[] colors;
    	
    	public SkillQueueSegment(Context context, int[] colors) 
    	{
    		super(context);
    		this.colors = colors;
    				
    		manual_padding = Tools.dp2px(10, context);
    		
    		paint.setStyle(Paint.Style.FILL);
    	}
    	
    	/**
    	 * Sets the skillQueue to draw from. Forces a redraw.
    	 * 
    	 * @param skillQueue
    	 */
    	public void setQueue(ArrayList<QueuedSkill> skillQueue, int skillNumber)
    	{
    		this.skillQueue = skillQueue;
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
    		
    		timeUntilStart = Tools.timeUntilUTCTime(skillQueue.get(skillNumber).startTime);
    		timeUntilEnd = Tools.timeUntilUTCTime(skillQueue.get(skillNumber).endTime);
    		
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
        public void onSizeChanged (int w, int h, int oldw, int oldh){
            super.onSizeChanged(w, h, oldw, oldh);
            width = w;
            height = h;
        }
    }

    public class SkillQueueBar extends View
    {    	
    	private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    	
    	private static final int keyColor = Color.DKGRAY;
    	
    	private int manual_padding;
    	
    	private Context context;
    	
    	private int width, height;
    	
    	private Paint paint;
    	
    	private boolean queueObtained = false;
    	
    	private ArrayList<QueuedSkill> skillQueue;
    	
    	private int[] colors;
    	
    	public SkillQueueBar(Context context, int[] colors) 
    	{
    		super(context);
    		this.colors = colors;
    		this.context = context;
    		
    		manual_padding = Tools.dp2px(10, context);
    		
    		paint = new Paint();
    		paint.setStyle(Paint.Style.FILL);
    	}
    	
    	/**
    	 * Sets th skillQueue to draw from. Forces a redraw.
    	 * 
    	 * @param skillQueue
    	 */
    	public void setQueue(ArrayList<QueuedSkill> skillQueue)
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
    		
    		/* If we have been given a queue and it's not empty */
    		if (queueObtained && skillQueue.size() > 0)
    		{			
    			for (QueuedSkill skill : skillQueue)
    			{
    				long startAdvanceValue = (queuePosition == 0) ? 0 : Tools.timeUntilUTCTime(skill.startTime);
    								
    				double percentOfBar = (double) (Tools.timeUntilUTCTime(skill.endTime) - startAdvanceValue) / (double) DAY_IN_MILLIS;
    				int secLength = (int) (percentOfBar * width);
    				
    				int start = currentBarPosition;
    				int end = start + secLength;
    				
    				if (end > width + manual_padding) end = width + manual_padding;	/* Cap it at max length */	
    						
    				paint.setColor(colors[queuePosition % 2]);
    				paint.setStyle(Style.FILL);
    				canvas.drawRect(start, manual_padding, end, height / 2, paint);
    				
    				currentBarPosition = end;
    				
    				if (Tools.timeUntilUTCTime(skill.endTime) > DAY_IN_MILLIS) break;
    				
    				++queuePosition;
    			}
    			
    			/* If there is room left in the 24 hour window, fill it with grey */
    			if (currentBarPosition < width + manual_padding)
    			{
    				paint.setColor(Color.GRAY);
    				canvas.drawRect(currentBarPosition, manual_padding, width + manual_padding, height / 2, paint);
    			}
    		}
    		
    		/* If the queue is empty fill it with grey */
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
    		
    		/* Draw the tick marks */
    		for (int x = 0; x <= 24; x++)
    		{
    			int xLoc = (int) (x / 24f * width) + manual_padding;
    			if (x == 24) xLoc = width + manual_padding - 1;
    			
    			canvas.drawLine(xLoc, height / 2, xLoc, height * 0.6f, paint);
    		}
    		
    		/* Draw hour indicators */
    		paint.setColor(Color.DKGRAY);
    		paint.setStyle(Style.FILL);
    		
    		paint.setTextAlign(Align.LEFT);
    		canvas.drawText("0", manual_padding, height * 0.8f, paint);
    		paint.setTextAlign(Align.CENTER);
    		canvas.drawText("12", manual_padding + (width / 2f), height * 0.8f, paint);
    		paint.setTextAlign(Align.RIGHT);
    		canvas.drawText("24", manual_padding + width, height * 0.8f, paint);
    	}
    	
    	@Override
        public void onSizeChanged (int w, int h, int oldw, int oldh){
            super.onSizeChanged(w, h, oldw, oldh);
            width = w - (manual_padding * 2);
            height = (int) (h * 1.1f);
        }
    }
}