package com.zdonnell.eve.characterdetail;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.TimeRemainingCountdown;
import com.zdonnell.eve.Tools;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.QueuedSkill;
import com.zdonnell.eve.dummy.DummyContent;

public class SkillQueueFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    
    private static final int LIGHT = 0;
	private static final int DARK = 1;
	
	private static int[] colors = new int[2];
	static
	{
		colors[LIGHT] = Color.parseColor("#33B5E5");
		colors[DARK] = Color.parseColor("#0099CC");
	}

    DummyContent.DummyItem mItem;
        
    private APICharacter character;
    
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
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_skillqueue, container, false);
    	final SkillQueueBar skillQueueBar = new SkillQueueBar(inflater.getContext(), colors);
    	final TextView queueTimeRemaining = (TextView) inflatedView.findViewById(R.id.queue_time_remaining_text);
    	
    	inflatedView.addView(skillQueueBar);
    	skillQueueBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Tools.dp2px(80, inflater.getContext())));
    	
    	character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() 
    	{
			@Override
			public void onUpdate(ArrayList<QueuedSkill> skillQueue) 
			{
				skillQueueBar.setQueue(skillQueue);
				long timeRemainingInQueue = Tools.timeUntilUTCTime(skillQueue.get(skillQueue.size() - 1).endTime);
				new TimeRemainingCountdown(timeRemainingInQueue, 1000, queueTimeRemaining).start();
			}
    	});
    	
    	return inflatedView;
    }
}
