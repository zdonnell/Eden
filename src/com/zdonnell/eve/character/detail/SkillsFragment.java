package com.zdonnell.eve.character.detail;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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

import com.beimin.eveapi.character.sheet.ApiSkill;
import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.core.ApiAuthorization;
import com.beimin.eveapi.eve.skilltree.ApiRequirement;
import com.beimin.eveapi.eve.skilltree.ApiSkillGroup;
import com.beimin.eveapi.eve.skilltree.CharacterAttribute;
import com.beimin.eveapi.eve.skilltree.SkillTreeResponse;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.BaseActivity;
import com.zdonnell.eve.CharacterDetailActivity;
import com.zdonnell.eve.R;
import com.zdonnell.eve.TypeInfoActivity;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.Skill;
import com.zdonnell.eve.apilink.APICallback;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.eve.Eve;
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
    
    private ApiSkillGroup[] skillTree;
    
    private SparseArray<ApiSkill> currentSkills;
    
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
    	
    	/*if (parentActivity.dataCache.getCharacterSheet() != null)
    	{
    		currentSkills = parentActivity.dataCache.getCharacterSheet().getSkills();
			updateSkillList();
    	}
    	else
    	{*/
    		ApiAuthorization apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), getArguments().getInt("characterID"), getArguments().getString("vCode"));
    		
    		final long startTime = System.currentTimeMillis();
    		new com.zdonnell.eve.apilink.character.APICharacter(context, apiAuth).getCharacterSheet(new APIExceptionCallback<CharacterSheetResponse>(parentActivity)
    		{
				@Override
				public void onUpdate(CharacterSheetResponse response) 
				{	
					long time = System.currentTimeMillis() - startTime;
					Log.d("CHARACTER SHEET", "LOAD TIME: " + time);
					
					SparseArray<ApiSkill> currentTempSkills = new SparseArray<ApiSkill>(response.getSkills().size());
					for (ApiSkill s : response.getSkills())
					{
						currentTempSkills.put(s.getTypeID(), s);
					}
					currentSkills = currentTempSkills;
					updateSkillList();
				}

				@Override
				public void onError(CharacterSheetResponse response, ApiException exception) 
				{
					
				}
    		});
    	//}
    	
    	/*if (parentActivity.dataCache.getSkillTree() != null)
    	{
    		skillTree = parentActivity.dataCache.getSkillTree();
			updateSkillList();
    	}
    	else
    	{*/
    	final long startTime2 = System.currentTimeMillis();
		new Eve(context).skillTree(new APIExceptionCallback<SkillTreeResponse>((BaseActivity) context)
		{
			@Override
			public void onUpdate(SkillTreeResponse response) 
			{
				long time = System.currentTimeMillis() - startTime;
				Log.d("SKILL TREE", "LOAD TIME: " + time);
				
				Set<ApiSkillGroup> apiSkillGroups = response.getAll();
				skillTree = new ApiSkillGroup[apiSkillGroups.size()];

				apiSkillGroups.toArray(skillTree);
				updateSkillList();
			}

			@Override
			public void onError(SkillTreeResponse response, ApiException exception) 
			{
				
			}
		});
    	//}
    	
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
        
        private ApiSkillGroup[] skillTree, skillTreeTrainedSkills;
        
        private SparseArray<ApiSkill> currentSkills;
                
        private HashMap<CharacterAttribute, Integer> attributeColors = new HashMap<CharacterAttribute, Integer>(5);
        private HashMap<CharacterAttribute, String> attributeLetter = new HashMap<CharacterAttribute, String>(5);
        
		private NumberFormat formatter = NumberFormat.getInstance();
		
		private boolean showAll = false;
        
		private LayoutInflater inflater;
        
        private static final int groupLayoutID = R.layout.char_detail_skills_list_item;
        private static final int childLayoutID = R.layout.char_detail_skills_list_item_subskill;

        public SkillsExpandedListAdapter(Context context, ApiSkillGroup[] skillTree, SparseArray<ApiSkill> currentSkills, int mode) 
        {
            this.context = context;
            this.skillTree = skillTree;
            this.currentSkills = currentSkills;
            this.showAll = (mode == 0);
            
            prepareSkillsets();
            
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            attributeColors.put(CharacterAttribute.INTELLIGENCE, Color.rgb(60, 109, 133));
            attributeColors.put(CharacterAttribute.MEMORY, Color.rgb(140, 104, 158));
            attributeColors.put(CharacterAttribute.CHARISMA, Color.rgb(188, 158, 69));
            attributeColors.put(CharacterAttribute.PERCEPTION, Color.rgb(60, 140, 101));
            attributeColors.put(CharacterAttribute.WILLPOWER, Color.rgb(210, 144, 104));
            
            attributeLetter.put(CharacterAttribute.INTELLIGENCE, "I");
            attributeLetter.put(CharacterAttribute.MEMORY, "M");
            attributeLetter.put(CharacterAttribute.CHARISMA, "C");
            attributeLetter.put(CharacterAttribute.PERCEPTION, "P");
            attributeLetter.put(CharacterAttribute.WILLPOWER, "W");
        }
        
        private void prepareSkillsets()
        {
        	ArrayList<ApiSkillGroup> groupsWithSkillsTrained = new ArrayList<ApiSkillGroup>();
        	for (ApiSkillGroup group : skillTree)
        	{
        		ArrayList<com.beimin.eveapi.eve.skilltree.ApiSkill> trainedSkills = new ArrayList<com.beimin.eveapi.eve.skilltree.ApiSkill>();
        		
        		for (com.beimin.eveapi.eve.skilltree.ApiSkill info : group.getSkills())
        		{
        			if (currentSkills.get(info.getTypeID()) != null) trainedSkills.add(info);
        		}
        		
        		if (!trainedSkills.isEmpty())
        		{	
        			ApiSkillGroup newGroup = new ApiSkillGroup();
        			newGroup.setGroupID(group.getGroupID());
        			newGroup.setGroupName(group.getGroupName());
        			for (com.beimin.eveapi.eve.skilltree.ApiSkill skill : trainedSkills) newGroup.add(skill);
        			
        			groupsWithSkillsTrained.add(newGroup);
        		}
        	}
        	
        	skillTreeTrainedSkills = new ApiSkillGroup[groupsWithSkillsTrained.size()];
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
			ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition].getSkills().toArray()[childPosition];
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) 
		{
			ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			com.beimin.eveapi.eve.skilltree.ApiSkill childSkill = (com.beimin.eveapi.eve.skilltree.ApiSkill) skillTreeType[groupPosition].getSkills().toArray()[childPosition];
			
			return childSkill.getTypeID();
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
		{
			View preparedView;
			
			if (convertView != null) preparedView = convertView;
			else preparedView = inflater.inflate(childLayoutID, parent, false);
			
			com.beimin.eveapi.eve.skilltree.ApiSkill skillInfo = (com.beimin.eveapi.eve.skilltree.ApiSkill) getChild(groupPosition, childPosition);
			prepareChild(skillInfo, preparedView);
			
			final Intent intent = new Intent(context, TypeInfoActivity.class);
			intent.putExtra("typeID", skillInfo.getTypeID());
			
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
		
		protected void prepareChild(com.beimin.eveapi.eve.skilltree.ApiSkill skillInfo, View preparedView)
		{
			TextView skillName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillname);
			TextView spText = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillsptext);
			TextView timeUntilNextLevel = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillTimeRemaining);
			TextView primAttribute = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_subitem_primAttr);
			TextView secAttribute = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_subitem_secAttr);
			
			ImageView skillIcon = (ImageView) preparedView.findViewById(R.id.char_detail_skills_subitem_skillIcon);
			
			SkillLevelIndicator levelIndicator = (SkillLevelIndicator) preparedView.findViewById(R.id.skill_level_indicator);
			
			levelIndicator.reset();
			skillName.setText(skillInfo.getTypeName() + " (" + skillInfo.getRank() + "x)");
			
			primAttribute.setTextColor(attributeColors.get(skillInfo.getPrimaryAttribute()));
			secAttribute.setTextColor(attributeColors.get(skillInfo.getSecondaryAttribute()));
			primAttribute.setText(attributeLetter.get(skillInfo.getPrimaryAttribute()));
			secAttribute.setText(attributeLetter.get(skillInfo.getSecondaryAttribute()));
			
			if (currentSkills.get(skillInfo.getTypeID()) == null) 
			{
				levelIndicator.setVisibility(View.GONE);
				
				skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skills));
				
				skillName.setAlpha(0.45f);
				spText.setAlpha(0.45f);
				
				boolean preReqsMet = true;
				for (ApiRequirement preReq : skillInfo.getRequiredSkills())
				{
					if (currentSkills.get(preReq.getTypeID()) == null || currentSkills.get(preReq.getTypeID()).getLevel() < preReq.getSkillLevel()) preReqsMet = false;
				}
				
				if (preReqsMet) spText.setText("You meet the requirements to train this skill");
				else spText.setText("You do not meet the requirements to train this skill");
			}
			else 
			{
				levelIndicator.setVisibility(View.VISIBLE);
				levelIndicator.provideSkillInfo(currentSkills.get(skillInfo.getTypeID()), false, Color.rgb(75, 75, 75));
		
				ApiSkill currentSkill = currentSkills.get(skillInfo.getTypeID());
				if (currentSkill.getLevel() == 5) skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skill_finished_training));
				else 
				{
					if (currentSkill.getSkillpoints() > baseSPAtLevel[currentSkill.getLevel()] * skillInfo.getRank())
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
				
				spText.setText("SP: " + formatter.format(currentSkill.getSkillpoints()) + " / " + formatter.format(skillInfo.getRank() * 256000));
			}
			
		}
		
		@Override
		public int getChildrenCount(int groupPosition) 
		{
			ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition].getSkills().size();
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
			ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition];
		}

		@Override
		public int getGroupCount() 
		{
			ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType.length;
		}

		@Override
		public long getGroupId(int groupPosition) 
		{
			ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
			return skillTreeType[groupPosition].getGroupID();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
		{
			View preparedView;
			
			if (convertView != null) preparedView = convertView;
			else preparedView = inflater.inflate(groupLayoutID, parent, false);
			
			ApiSkillGroup skillGroup = (ApiSkillGroup) getGroup(groupPosition);
			prepareGroup(skillGroup, preparedView, groupPosition);
			
			return preparedView;
		}
		
		protected void prepareGroup(final ApiSkillGroup skillGroup, View preparedView, int groupPosition)
		{
			TextView groupName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupName);
			TextView skillCount = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupSubText);
			TextView groupSP = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupSkillPoints);			

			groupName.setText(skillGroup.getGroupName());
			
			int totalSkillsCount = getSkillCount(groupPosition); 
			int currentSkillsCount = 0;
			for (com.beimin.eveapi.eve.skilltree.ApiSkill skill : skillGroup.getSkills())
			{
				if (currentSkills.get(skill.getTypeID()) != null) ++currentSkillsCount;
			}
			
			skillCount.setText("Skills: " + currentSkillsCount + " of " + totalSkillsCount);
			
			int groupSPCount = 0;
			for (com.beimin.eveapi.eve.skilltree.ApiSkill skill : skillGroup.getSkills())
			{
				if (currentSkills.get(skill.getTypeID()) != null) groupSPCount += currentSkills.get(skill.getTypeID()).getSkillpoints();
			}
			
			groupSP.setText(formatter.format(groupSPCount) + " SP");
		}
		
		private int getSkillCount(int modifiedPosition)
		{
			if (showAll) return skillTree[modifiedPosition].getSkills().size();
			else
			{
				for (int i = 0; i < skillTree.length; i++)
				{
					if (skillTreeTrainedSkills[modifiedPosition].getGroupID() == skillTree[i].getGroupID())
					{
						return skillTree[i].getSkills().size();
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
		ApiAuthorization apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), getArguments().getInt("characterID"), getArguments().getString("vCode"));
		new com.zdonnell.eve.apilink.character.APICharacter(context, apiAuth).getCharacterSheet(new APIExceptionCallback<CharacterSheetResponse>(parentActivity)
		{
			@Override
			public void onUpdate(CharacterSheetResponse response) 
			{					
				SparseArray<ApiSkill> currentTempSkills = new SparseArray<ApiSkill>(response.getSkills().size());
				for (ApiSkill s : response.getSkills())
				{
					currentTempSkills.put(s.getTypeID(), s);
				}
				currentSkills = currentTempSkills;
				updateSkillList();
			}

			@Override
			public void onError(CharacterSheetResponse response, ApiException exception) 
			{
				
			}
		});
		
		new Eve(context).skillTree(new APIExceptionCallback<SkillTreeResponse>((BaseActivity) context)
		{
			@Override
			public void onUpdate(SkillTreeResponse response) 
			{
				Set<ApiSkillGroup> apiSkillGroups = response.getAll();
				skillTree = new ApiSkillGroup[apiSkillGroups.size()];

				apiSkillGroups.toArray(skillTree);
				updateSkillList();
			}

			@Override
			public void onError(SkillTreeResponse response, ApiException exception) 
			{
				
			}
		});
	}
}
