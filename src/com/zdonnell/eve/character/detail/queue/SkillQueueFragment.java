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

import com.beimin.eveapi.character.skill.queue.ApiSkillQueueItem;
import com.beimin.eveapi.character.skill.queue.SkillQueueResponse;
import com.beimin.eveapi.core.ApiAuthorization;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.BaseActivity;
import com.zdonnell.eve.R;
import com.zdonnell.eve.TypeInfoActivity;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.QueuedSkill;
import com.zdonnell.eve.apilink.APICallback;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.character.detail.DetailFragment;
import com.zdonnell.eve.character.detail.SkillLevelIndicator;
import com.zdonnell.eve.helpers.TimeRemainingCountdown;
import com.zdonnell.eve.helpers.Tools;
import com.zdonnell.eve.staticdata.api.StaticData;
import com.zdonnell.eve.staticdata.api.TypeInfo;

public class SkillQueueFragment extends DetailFragment {
    
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
    
    private ArrayList<ApiSkillQueueItem> skillQueue = new ArrayList<ApiSkillQueueItem>();
            
    SkillQueueBar skillQueueBar;
	TextView queueTimeRemaining;
	TextView queueLength;
        
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {    	
    	character = new APICharacter(new APICredentials(getArguments().getInt("keyID"), getArguments().getString("vCode")), getArguments().getInt("characterID"), context);
    	
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_skillqueue, container, false);
    	skillQueueBar = new SkillQueueBar(inflater.getContext(), colors);
    	queueTimeRemaining = (TextView) inflatedView.findViewById(R.id.queue_time_remaining_text);
    	queueLength = (TextView) inflatedView.findViewById(R.id.skill_queue_size_text);
    	
    	inflatedView.addView(skillQueueBar, 0);
    	skillQueueBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dp2px(80, inflater.getContext())));
    	
    	skillQueueList = (ListView) inflatedView.findViewById(R.id.char_detail_queue_list);
    	
    	loadData();
    	    	
    	return inflatedView;
    }
    
    /**
     * Takes a skillQueue and provides it to the  {@link ListView} responsible for displaying
     * the queued skills.
     * 
     * @param skillQueue an {@link ArrayList} of {@link QueuedSkill} objects to build the listview from
     */
    private void updateQueueList(ArrayList<ApiSkillQueueItem> skillQueue)
    {
    	this.skillQueue = skillQueue;
    	
    	ApiSkillQueueItem[] rawArraySkillQueue = new ApiSkillQueueItem[skillQueue.size()];
    	rawArraySkillQueue = skillQueue.toArray(rawArraySkillQueue);
    	    	
    	skillQueueList.setAdapter(new SkillQueueListAdapter(context, R.layout.char_detail_skillqueue_list_item, rawArraySkillQueue));
    }
    
    
    private class SkillQueueListAdapter extends ArrayAdapter<ApiSkillQueueItem>
	{
    	ApiSkillQueueItem[] skillQueue;
		
		/**
		 * ID of the resource to inflate for the entire row
		 */
		int resourceID;
		
		public SkillQueueListAdapter(Context context, int viewResourceID, ApiSkillQueueItem[] skillQueue) 
		{
			super(context, viewResourceID, skillQueue);			
			this.skillQueue = skillQueue;
			this.resourceID = viewResourceID;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			final ArrayList<ApiSkillQueueItem> currentSkillQueue = SkillQueueFragment.this.skillQueue;
			
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
			preparedView.setBackgroundColor((position % 2 == 1) ? Color.parseColor("#242424") : Color.parseColor("#181818")); 
			
			final TextView skillName = (TextView) preparedView.findViewById(R.id.skillqueue_detail_list_item_skillname);
			TextView skillLevel = (TextView) preparedView.findViewById(R.id.skill_level_text);			
			skillLevel.setText("Level " + currentSkillQueue.get(position).getLevel());
						
			new StaticData(context).getTypeInfo(new APICallback<SparseArray<TypeInfo>>((BaseActivity) getActivity())
			{
				@Override
				public void onUpdate(SparseArray<TypeInfo> updatedData) 
				{
					if (updatedData.valueAt(0) == null) skillName.setText("Skill ID: " + skillQueue[position].getTypeID());
					else skillName.setText(updatedData.valueAt(0).typeName);
				}
			}, skillQueue[position].getTypeID());
			
			final Intent intent = new Intent(context, TypeInfoActivity.class);
			intent.putExtra("typeID", skillQueue[position].getTypeID());
			
			preparedView.setOnLongClickListener(new View.OnLongClickListener() 
			{	
				@Override
				public boolean onLongClick(View v) 
				{
	            	startActivity(intent);
					return true;
				}
			});
									
			return preparedView;
		}
	}

	@Override
	public void loadData() 
	{  
		// Load in Character Sheet to get Character Skills
		ApiAuthorization apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), getArguments().getInt("characterID"), getArguments().getString("vCode"));
		new com.zdonnell.eve.apilink.character.APICharacter(context, apiAuth).getSkillQueue(new APIExceptionCallback<SkillQueueResponse>(parentActivity)
		{
			@Override
			public void onUpdate(SkillQueueResponse response) 
			{
				Set<ApiSkillQueueItem> queue = response.getAll();
				
				if (!queue.isEmpty())
				{
					skillQueue.clear();
					skillQueue.addAll(queue);
					
					skillQueueBar.setQueue(skillQueue);
					updateQueueList(skillQueue);

					long timeRemainingInQueue = Tools.timeUntilUTCTime(skillQueue.get(skillQueue.size() - 1).getEndTime());
					
					queueTimeRemaining.setVisibility(View.VISIBLE);
					new TimeRemainingCountdown(timeRemainingInQueue, 1000, queueTimeRemaining).start();
					
					queueLength.setText(skillQueue.size() + " Skill(s) in Queue");
					
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
}