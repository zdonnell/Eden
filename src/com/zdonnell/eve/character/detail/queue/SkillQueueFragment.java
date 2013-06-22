package com.zdonnell.eve.character.detail.queue;

import java.util.ArrayList;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.androideveapi.character.skill.queue.ApiSkillQueueItem;
import com.zdonnell.androideveapi.character.skill.queue.SkillQueueResponse;
import com.zdonnell.androideveapi.core.ApiAuthorization;
import com.zdonnell.androideveapi.exception.ApiException;
import com.zdonnell.androideveapi.link.APICallback;
import com.zdonnell.androideveapi.link.APIExceptionCallback;
import com.zdonnell.androideveapi.link.ILoadingActivity;
import com.zdonnell.androideveapi.link.character.APICharacter;
import com.zdonnell.eve.R;
import com.zdonnell.eve.TypeInfoActivity;
import com.zdonnell.eve.character.detail.DetailFragment;
import com.zdonnell.eve.character.detail.SkillLevelIndicator;
import com.zdonnell.eve.helpers.TimeRemainingCountdown;
import com.zdonnell.eve.helpers.Tools;
import com.zdonnell.eve.staticdata.StaticData;
import com.zdonnell.eve.staticdata.TypeInfo;

/**
 * This Fragment is used to display the Skill Queue of a Character
 * 
 * @author zachd
 *
 */
public class SkillQueueFragment extends DetailFragment {
	
	/**
	 * The ListView that stores the queue listing
	 */
	private ListView skillQueueListView;
            
	/**
	 * Rerefence to current skill Queue
	 */
    private ArrayList<ApiSkillQueueItem> skillQueue = new ArrayList<ApiSkillQueueItem>();
       
    /**
     * The View that creates the large queue bar
     */
    private SkillQueueBar skillQueueBar;
    
    /**
     * TextView that displays the current time remaining in the queue
     */
	private TextView queueTimeRemaining;
	
	/**
	 * TextView that displays the number of skills in the queue
	 */
	private TextView queueLength;

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {    	    	
    	LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.char_detail_skillqueue, container, false);
    	
    	queueTimeRemaining = (TextView) rootView.findViewById(R.id.queue_time_remaining_text);
    	queueLength = (TextView) rootView.findViewById(R.id.skill_queue_size_text);
    	skillQueueListView = (ListView) rootView.findViewById(R.id.char_detail_queue_list);

    	skillQueueBar = new SkillQueueBar(context);
    	skillQueueBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dp2px(80, inflater.getContext())));
    	rootView.addView(skillQueueBar, 0);
    	
    	loadData();
    	    	
    	return rootView;
    }
    
    /**
     * Takes a skillQueue and provides it to the  {@link ListView} responsible for displaying
     * the queued skills.
     * 
     * @param skillQueue an {@link ArrayList} of {@link QueuedSkill} objects to build the listview from
     */
    private void updateQueueList(ArrayList<ApiSkillQueueItem> skillQueue)
    {    	
		skillQueueBar.setQueue(skillQueue);

    	long timeRemainingInQueue = Tools.timeUntilUTCTime(skillQueue.get(skillQueue.size() - 1).getEndTime());
		new TimeRemainingCountdown(timeRemainingInQueue, 1000, queueTimeRemaining).start();
		queueTimeRemaining.setVisibility(View.VISIBLE);
		
		queueLength.setText(skillQueue.size() + " Skill(s) in Queue");
    	
    	ApiSkillQueueItem[] rawArraySkillQueue = new ApiSkillQueueItem[skillQueue.size()];
    	rawArraySkillQueue = skillQueue.toArray(rawArraySkillQueue);
    	    	
    	skillQueueListView.setAdapter(new SkillQueueListAdapter(context, R.layout.char_detail_skillqueue_list_item, rawArraySkillQueue));
    }
    
    @Override
	public void loadData() 
	{  
		// Load in Character Sheet to get Character Skills
		ApiAuthorization apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), getArguments().getInt("characterID"), getArguments().getString("vCode"));
		new APICharacter(context, apiAuth).getSkillQueue(new APIExceptionCallback<SkillQueueResponse>(parentActivity)
		{
			@Override
			public void onUpdate(SkillQueueResponse response) 
			{
				Set<ApiSkillQueueItem> queue = response.getAll();
				
				if (!queue.isEmpty())
				{
					skillQueue.clear();
					skillQueue.addAll(queue);
					
					updateQueueList(skillQueue);
				}
				else
				{
					queueTimeRemaining.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onError(SkillQueueResponse response, ApiException exception) 
			{
				
			}
		});
	}
    
    /**
     * This Adapter binds the Skill Queue data to {@link #skillQueueListView}
     * 
     * @author zachd
     *
     */
    private class SkillQueueListAdapter extends ArrayAdapter<ApiSkillQueueItem>
	{
		/**
		 * ID of the resource to inflate for the entire row
		 */
		int resourceID;
		
		public SkillQueueListAdapter(Context context, int viewResourceID, ApiSkillQueueItem[] skillQueue) 
		{
			super(context, viewResourceID, skillQueue);			
			this.resourceID = viewResourceID;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if (convertView == null) 
			{
				convertView = (LinearLayout) inflater.inflate(resourceID, parent, false);
				((LinearLayout) convertView).addView(new SkillQueueSegment(context), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dp2px(3, inflater.getContext())));
			}
			
			// Alternate Skill Queue Row Background Color
			convertView.setBackgroundColor((position % 2 == 1) ? Color.parseColor("#242424") : Color.parseColor("#181818"));
			
			setTextElements(convertView, getItem(position));
			configureGraphicElements(convertView, getItem(position));
			configureTouchEvents(convertView, getItem(position).getTypeID());
									
			return convertView;
		}
		
		/**
		 * Sets up the graphical elements for the Queued Skill.
		 * 
		 * @param mainView
		 * @param queuedSkill
		 * 
		 * @see {@link SkillQueueSegment}
		 * @see {@link SkillLevelIndicator}
		 */
		private void configureGraphicElements(View mainView, ApiSkillQueueItem queuedSkill)
		{
			SkillQueueSegment skillQueueSegment = (SkillQueueSegment) ((LinearLayout) mainView).getChildAt(1);
			skillQueueSegment.setQueue(queuedSkill, getPosition(queuedSkill));

			// Configure Skill Level Indicator View
			SkillLevelIndicator skillLevelIndicator = (SkillLevelIndicator) mainView.findViewById(R.id.skill_level_indicator);
			skillLevelIndicator.provideSkillInfo(queuedSkill, getPosition(queuedSkill) == 0, context.getResources().getColor(R.color.primary_accent_color));
		}
		
		/**
		 * Sets the text elements for the specified row in the Skill Queue.
		 * 
		 * @param mainView
		 * @param queuedSkill
		 */
		private void setTextElements(View mainView, final ApiSkillQueueItem queuedSkill)
		{
			// Skill Level Text
			TextView skillLevel = (TextView) mainView.findViewById(R.id.skill_level_text);			
			skillLevel.setText("Level " + queuedSkill.getLevel());
			
			// Skill Name Text
			final TextView skillName = (TextView) mainView.findViewById(R.id.skillqueue_detail_list_item_skillname);
			new StaticData(context).getTypeInfo(new APICallback<SparseArray<TypeInfo>>((ILoadingActivity) getActivity())
			{
				@Override
				public void onUpdate(SparseArray<TypeInfo> updatedData) 
				{
					if (updatedData.valueAt(0) == null) skillName.setText("Skill ID: " + queuedSkill.getTypeID());
					else skillName.setText(updatedData.valueAt(0).typeName);
				}
			}, queuedSkill.getTypeID());
		}
		
		/**
		 * Sets the onLongClickListener for the specified row in the Skill Queue.
		 * 
		 * @param rowView
		 * @param typeID
		 */
		private void configureTouchEvents(View rowView, int typeID)
		{
			final Intent intent = new Intent(context, TypeInfoActivity.class);
			intent.putExtra("typeID", typeID);
			
			rowView.setOnLongClickListener(new View.OnLongClickListener() 
			{	
				public boolean onLongClick(View v) 
				{
	            	startActivity(intent);
					return true;
				}
			});
		}
	}
}