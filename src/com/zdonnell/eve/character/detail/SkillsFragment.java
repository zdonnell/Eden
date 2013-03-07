package com.zdonnell.eve.character.detail;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.R;
import com.zdonnell.eve.TypeInfoActivity;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.Skill;
import com.zdonnell.eve.api.character.CharacterSheet.AttributeEnhancer;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.eve.SkillInfo;

public class SkillsFragment extends Fragment {
    
	public static final int ALL_SKILLS = 0;
	public static final int TRAINED_SKILLS = 1;
	public static final int TRAINABLE_SKILLS = 2;
    
    private APICharacter character;
        
    /**
     * Array storing attribute values
     */
    private int[] attributes = new int[5];
    
    /**
     * Array of implants / augmentations
     */
    private AttributeEnhancer[] implants = new AttributeEnhancer[5];
        
    private Context context;
    
    private SkillGroup[] skillTree;
    
    private ExpandableListView skillsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_skills, container, false);
    	
    	skillsListView = (ExpandableListView) inflatedView.findViewById(R.id.char_detail_skills_list);
    	
    	character = new APICharacter(new APICredentials(getArguments().getInt("keyID"), getArguments().getString("vCode")), getArguments().getInt("characterID"), context);
    	
    	new Eve(context).getSkillTree(new APICallback<SkillGroup[]>() 
    	{
			@Override
			public void onUpdate(SkillGroup[] newSkillTree) 
			{
				skillTree = newSkillTree;
				updateSkillList();
			}
    	});
    	
    	return inflatedView;
    }   
    
    private void updateSkillList()
    {
    	skillsListView.setAdapter(new SkillsExpandedListAdapter(context, skillTree));
    }
    
    private class SkillsExpandedListAdapter implements ExpandableListAdapter
    {
        private Context context;
        
        private SkillGroup[] skillTree;
        
		LayoutInflater inflater;
        
        private static final int groupLayoutID = R.layout.char_detail_skills_list_item;
        private static final int childLayoutID = R.layout.char_detail_skills_list_item_subskill;

        public SkillsExpandedListAdapter(Context context, SkillGroup[] skillTree) 
        {
            this.context = context;
            this.skillTree = skillTree;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    	
		@Override
		public boolean areAllItemsEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) 
		{
			return skillTree[groupPosition].containedSkills()[childPosition];
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) 
		{
			return skillTree[groupPosition].containedSkills()[childPosition].typeID();
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
		{
			View preparedView;
			
			if (convertView != null) preparedView = convertView;
			else preparedView = inflater.inflate(childLayoutID, parent, false);
			
			SkillInfo skillInfo = (SkillInfo) getChild(groupPosition, childPosition);
			prepareChild(skillInfo, preparedView);
			
			return preparedView;
		}
		
		protected void prepareChild(SkillInfo skillInfo, View preparedView)
		{
			TextView skillName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillname);
			skillName.setText(skillInfo.typeName());
		}

		@Override
		public int getChildrenCount(int groupPosition) 
		{
			return skillTree[groupPosition].containedSkills().length;
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
			return skillTree[groupPosition];
		}

		@Override
		public int getGroupCount() 
		{
			return skillTree.length;
		}

		@Override
		public long getGroupId(int groupPosition) 
		{
			return skillTree[groupPosition].groupID();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
		{
			View preparedView;
			
			if (convertView != null) preparedView = convertView;
			else preparedView = inflater.inflate(groupLayoutID, parent, false);
			
			SkillGroup skillGroup = (SkillGroup) getGroup(groupPosition);
			prepareGroup(skillGroup, preparedView);
			
			return preparedView;
		}
		
		protected void prepareGroup(final SkillGroup skillGroup, View preparedView)
		{
			TextView groupName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupName);			
			groupName.setText(skillGroup.groupName());
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
}
