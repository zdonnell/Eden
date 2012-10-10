package com.zdonnell.eve.characterdetail;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.QueuedSkill;

public class SkillQueueView extends View 
{
	APICharacter character;
	
	ArrayList<QueuedSkill> skillQueue;
	
	public SkillQueueView(Context context, APICharacter character) 
	{
		super(context);
		
		character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() 
		{
			@Override
			public void onUpdate(ArrayList<QueuedSkill> updatedData) 
			{
				skillQueue = updatedData;
				updateSkillQueue();
			}
		});
	}
	
	
	private void updateSkillQueue()
	{
		
	}
}
