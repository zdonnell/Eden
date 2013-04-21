package com.zdonnell.eve.character.detail;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdonnell.eve.BaseActivity;
import com.zdonnell.eve.CharacterDetailActivity;
import com.zdonnell.eve.R;
import com.zdonnell.eve.TypeInfoActivity;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.Skill;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.eve.SkillInfo;

public class SkillsFragment extends DetailFragment {
    
	public static final int ALL_SKILLS = 0;
	public static final int TRAINED_SKILLS = 1;
	public static final int TRAINABLE_SKILLS = 2;
	
	public static final String[] skillOptions = new String[2];
    private static final int[] baseSPAtLevel = new int[6];

	static
	{
		skillOptions[ALL_SKILLS] = "All Skills";
		skillOptions[TRAINED_SKILLS] = "Trained Skills";
		
		baseSPAtLevel[0] = 0;
		baseSPAtLevel[1] = 250;
		baseSPAtLevel[2] = 1414;
		baseSPAtLevel[3] = 8000;
		baseSPAtLevel[4] = 45255;
		baseSPAtLevel[5] = 256000;
	}
    
    private APICharacter character;
    
    private CharacterDetailActivity parentActivity;  
    
    private Context context;
    
    private SkillGroup[] skillTree;
    
    private SparseArray<Skill> currentSkills;
    
    private ExpandableListView skillsListView;
    
    private SharedPreferences prefs;


    private int mode = 1;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {    	
    	context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_skills, container, false);
    	
    	parentActivity = (CharacterDetailActivity) getActivity();
    	
    	skillsListView = (ExpandableListView) inflatedView.findViewById(R.id.char_detail_skills_list);
    	prefs = context.getSharedPreferences("eden_skills_preferences", Context.MODE_PRIVATE);
    	mode = prefs.getInt("skill_display", TRAINED_SKILLS);
    	
    	character = new APICharacter(new APICredentials(getArguments().getInt("keyID"), getArguments().getString("vCode")), getArguments().getInt("characterID"), context);
    	
    	if (parentActivity.dataCache.getCharacterSheet() != null)
    	{
    		currentSkills = parentActivity.dataCache.getCharacterSheet().getSkills();
			updateSkillList();
    	}
    	else
    	{
	    	character.getCharacterSheet(new APICallback<CharacterSheet>((BaseActivity) getActivity()) 
	    	{
				@Override
				public void onUpdate(CharacterSheet updatedData) 
				{
					currentSkills = updatedData.getSkills();
					parentActivity.dataCache.cacheCharacterSheet(updatedData);
					updateSkillList();
				}
	    	});
    	}
    	
    	if (parentActivity.dataCache.getSkillTree() != null)
    	{
    		skillTree = parentActivity.dataCache.getSkillTree();
			updateSkillList();
    	}
    	else
    	{
    		new Eve(context).getSkillTree(new APICallback<SkillGroup[]>((BaseActivity) getActivity()) 
	    	{
				@Override
				public void onUpdate(SkillGroup[] newSkillTree) 
				{
					skillTree = newSkillTree;
					parentActivity.dataCache.cacheSkillTree(newSkillTree);
					updateSkillList();
				}
	    	});
    	}
    	
    	return inflatedView;
    }   
    
    private void updateSkillList()
    {
    	if (currentSkills != null && skillTree != null)
    	{
    		skillsListView.setAdapter(new SkillsExpandedListAdapter(context, skillTree, currentSkills, mode));
    	}
    }
    
    public void updateSkillDisplay(int mode)
    {
    	this.mode = mode;
    	prefs.edit().putInt("skill_display", mode).commit();
    	updateSkillList();
    }
    
    private class SkillsExpandedListAdapter implements ExpandableListAdapter
    {
        private Context context;
        
        private SkillGroup[] skillTree, skillTreeTrainedSkills;
        
        private SparseArray<Skill> currentSkills;
                
        private HashMap<String, Integer> attributeColors = new HashMap<String, Integer>(5);
        
		private NumberFormat formatter = NumberFormat.getInstance();
		
		private boolean showAll = false;
        
		private LayoutInflater inflater;
        
        private static final int groupLayoutID = R.layout.char_detail_skills_list_item;
        private static final int childLayoutID = R.layout.char_detail_skills_list_item_subskill;

        public SkillsExpandedListAdapter(Context context, SkillGroup[] skillTree, SparseArray<Skill> currentSkills, int mode) 
        {
            this.context = context;
            this.skillTree = skillTree;
            this.currentSkills = currentSkills;
            this.showAll = (mode == 0);
            
            prepareSkillsets();
            
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            attributeColors.put("intelligence", Color.rgb(60, 109, 133));
            attributeColors.put("memory", Color.rgb(140, 104, 158));
            attributeColors.put("charisma", Color.rgb(188, 158, 69));
            attributeColors.put("perception", Color.rgb(60, 140, 101));
            attributeColors.put("willpower", Color.rgb(210, 144, 104));
        }
        
        private void prepareSkillsets()
        {
        	ArrayList<SkillGroup> groupsWithSkillsTrained = new ArrayList<SkillGroup>();
        	for (SkillGroup group : skillTree)
        	{
        		ArrayList<SkillInfo> trainedSkills = new ArrayList<SkillInfo>();
        		for (SkillInfo info : group.containedSkills())
        		{
        			if (currentSkills.get(info.typeID()) != null) trainedSkills.add(info);
        		}
        		
        		if (!trainedSkills.isEmpty())
        		{
        			SkillInfo[] containedSkills = new SkillInfo[trainedSkills.size()];
        			trainedSkills.toArray(containedSkills);
        			
        			SkillGroup newGroup = new SkillGroup(group.groupID(), group.groupName(), containedSkills);
        			groupsWithSkillsTrained.add(newGroup);
        		}
        	}
        	
        	skillTreeTrainedSkills = new SkillGroup[groupsWithSkillsTrained.size()];
        	groupsWithSkillsTrained.toArray(skillTreeTrainedSkills);
        }

        
		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) 
		{
			SkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition].containedSkills()[childPosition];
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) 
		{
			SkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition].containedSkills()[childPosition].typeID();
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
		{
			View preparedView;
			
			if (convertView != null) preparedView = convertView;
			else preparedView = inflater.inflate(childLayoutID, parent, false);
			
			SkillInfo skillInfo = (SkillInfo) getChild(groupPosition, childPosition);
			prepareChild(skillInfo, preparedView);
			
			final Intent intent = new Intent(context, TypeInfoActivity.class);
			intent.putExtra("typeID", skillInfo.typeID());
			
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
		
		protected void prepareChild(SkillInfo skillInfo, View preparedView)
		{
			TextView skillName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillname);
			TextView spText = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillsptext);
			TextView timeUntilNextLevel = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillTimeRemaining);
			TextView primAttribute = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_subitem_primAttr);
			TextView secAttribute = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_subitem_secAttr);
			
			ImageView skillIcon = (ImageView) preparedView.findViewById(R.id.char_detail_skills_subitem_skillIcon);
			
			SkillLevelIndicator levelIndicator = (SkillLevelIndicator) preparedView.findViewById(R.id.skill_level_indicator);
			
			levelIndicator.reset();
			skillName.setText(skillInfo.typeName() + " (" + skillInfo.rank() + "x)");
			
			primAttribute.setTextColor(attributeColors.get(skillInfo.attributes()[0]));
			secAttribute.setTextColor(attributeColors.get(skillInfo.attributes()[1]));
			primAttribute.setText(skillInfo.attributes()[0].substring(0, 1));
			secAttribute.setText(skillInfo.attributes()[1].substring(0, 1));
			
			if (currentSkills.get(skillInfo.typeID()) == null) 
			{
				levelIndicator.setVisibility(View.GONE);
				
				skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skills));
				
				skillName.setAlpha(0.45f);
				spText.setAlpha(0.45f);
				
				boolean preReqsMet = true;
				for (SkillInfo.SkillPreReq preReq : skillInfo.requiredSkills())
				{
					if (currentSkills.get(preReq.typeID()) == null || currentSkills.get(preReq.typeID()).getLevel() < preReq.skillLevel()) preReqsMet = false;
				}
				
				if (preReqsMet) spText.setText("You meet the requirements to train this skill");
				else spText.setText("You do not meet the requirements to train this skill");
			}
			else 
			{
				levelIndicator.setVisibility(View.VISIBLE);
				levelIndicator.provideSkillInfo(currentSkills.get(skillInfo.typeID()), false, Color.rgb(75, 75, 75));
		
				Skill currentSkill = currentSkills.get(skillInfo.typeID());
				if (currentSkill.getLevel() == 5) skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skill_finished_training));
				else 
				{
					if (currentSkill.getSkillPoints() > baseSPAtLevel[currentSkill.getLevel()] * skillInfo.rank())
					{
						skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skill_in_progress));
					}
					else
					{
						skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skill_at_midlevel));
					}
				}
				
				skillName.setAlpha(1);
				spText.setAlpha(1);
				
				spText.setText("SP: " + formatter.format(currentSkill.getSkillPoints()) + " / " + formatter.format(skillInfo.rank() * 256000));
			}
			
		}
		
		@Override
		public int getChildrenCount(int groupPosition) 
		{
			SkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition].containedSkills().length;
		}

		@Override
		public long getCombinedChildId(long groupId, long childId)
		{
			return childId;
		}

		@Override
		public long getCombinedGroupId(long groupId) 
		{
			return groupId;
		}

		@Override
		public Object getGroup(int groupPosition) 
		{
			SkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition];
		}

		@Override
		public int getGroupCount() 
		{
			SkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType.length;
		}

		@Override
		public long getGroupId(int groupPosition) 
		{
			SkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition].groupID();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
		{
			View preparedView;
			
			if (convertView != null) preparedView = convertView;
			else preparedView = inflater.inflate(groupLayoutID, parent, false);
			
			SkillGroup skillGroup = (SkillGroup) getGroup(groupPosition);
			prepareGroup(skillGroup, preparedView, groupPosition);
			
			return preparedView;
		}
		
		protected void prepareGroup(final SkillGroup skillGroup, View preparedView, int groupPosition)
		{
			TextView groupName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupName);
			TextView skillCount = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupSubText);
			TextView groupSP = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupSkillPoints);			

			groupName.setText(skillGroup.groupName());
			
			int totalSkillsCount = getSkillCount(groupPosition); 
			int currentSkillsCount = 0;
			for (SkillInfo info : skillGroup.containedSkills()) if (currentSkills.get(info.typeID()) != null) ++currentSkillsCount;
			
			skillCount.setText("Skills: " + currentSkillsCount + " of " + totalSkillsCount);
			
			int groupSPCount = 0;
			for (SkillInfo info : skillGroup.containedSkills())
			{
				if (currentSkills.get(info.typeID()) != null) groupSPCount += currentSkills.get(info.typeID()).getSkillPoints();
			}
			
			groupSP.setText(formatter.format(groupSPCount) + " SP");
		}
		
		private int getSkillCount(int modifiedPosition)
		{
			if (showAll) return skillTree[modifiedPosition].containedSkills().length;
			else
			{
				for (int i = 0; i < skillTree.length; i++)
				{
					if (skillTreeTrainedSkills[modifiedPosition].groupID() == skillTree[i].groupID())
					{
						return skillTree[i].containedSkills().length;
					}
				}
				
				return 0;
			}
		}

		@Override
		public boolean hasStableIds() 
		{
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) 
		{
			return false;
		}

		@Override
		public boolean isEmpty() 
		{
			return skillTree.length == 0;
		}

		@Override
		public void onGroupCollapsed(int groupPosition) 
		{
			
		}

		@Override
		public void onGroupExpanded(int groupPosition) 
		{
						
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) 
		{

		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) 
		{
			
		}
    }

	@Override
	public void refresh() 
	{   	
		character.getCharacterSheet(new APICallback<CharacterSheet>((BaseActivity) getActivity()) 
    	{
			@Override
			public void onUpdate(CharacterSheet updatedData) 
			{
				currentSkills = updatedData.getSkills();
				parentActivity.dataCache.cacheCharacterSheet(updatedData);
				updateSkillList();
			}
    	});
		
		new Eve(context).getSkillTree(new APICallback<SkillGroup[]>((BaseActivity) getActivity()) 
    	{
			@Override
			public void onUpdate(SkillGroup[] newSkillTree) 
			{
				skillTree = newSkillTree;
				parentActivity.dataCache.cacheSkillTree(newSkillTree);
				updateSkillList();
			}
    	});

	}
}
