package com.zdonnell.eve.detail.skillqueue;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.TimeRemainingCountdown;
import com.zdonnell.eve.Tools;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.QueuedSkill;
import com.zdonnell.eve.dummy.DummyContent;
import com.zdonnell.eve.eve.Eve;

public class SkillQueueFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    
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

    DummyContent.DummyItem mItem;
        
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
    	    	
    	skillQueueList.setAdapter(new SkillQueueListAdapter(context, R.layout.skill_queue_detail_list_item, rawArraySkillQueue));
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
			preparedView.setBackgroundColor((position % 2 == 1) ? Color.parseColor("#CCCCCC") : Color.parseColor("#BBBBBB")); 
			
			final TextView skillName = (TextView) preparedView.findViewById(R.id.skillqueue_detail_list_item_skillname);			
			
			new Eve(context).getTypeName(new APICallback<String[]>() 
			{
				@Override
				public void onUpdate(String[] typeNames) {
					skillName.setText(typeNames[0]);
					
				}
			}, new int[] { skillQueue[position].skillID });
									
			return preparedView;
		}
	}
}
