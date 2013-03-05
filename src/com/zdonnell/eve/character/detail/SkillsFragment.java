package com.zdonnell.eve.character.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    
    private ListView skillsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	context = inflater.getContext();
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_skills, container, false);
    	
    	skillsListView = (ListView) inflatedView.findViewById(R.id.char_detail_skills_list);
    	
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
    	skillsListView.setAdapter(new SkillsListAdapter(context, R.layout.char_detail_skills_list_item, skillTree, null));
    }
    
    /**
     * {@link ArrayAdapter} subclass to populate the Skill Listing {@link ListView}
     * 
     * @author Zach
     *
     */
    private class SkillsListAdapter extends ArrayAdapter<SkillGroup>
	{	    	
    	/**
		 * ID of the resource to inflate for the entire row
		 */
		int resourceID;
		
		private int displayMode = TRAINED_SKILLS;
		
		/**
		 * Constructor
		 * 
		 * @param context
		 * @param viewResourceID the ID of the layout resource to use as a row in the Attributes {@link ListView}
		 */
		public SkillsListAdapter(Context context, int viewResourceID, SkillGroup[] skillTree, Skill[] characterSkills) 
		{
			super(context, viewResourceID, skillTree);			
			this.resourceID = viewResourceID;
		}
		
		public void setDisplayMode(int displayMode)
		{
			this.displayMode = displayMode;
			notifyDataSetChanged();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{			
			LinearLayout preparedView; 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			/* Determine if we recycle the old view, or inflate a new one */
			if (convertView == null) preparedView = (LinearLayout) inflater.inflate(resourceID, parent, false);
			else preparedView = (LinearLayout) convertView;
				
			SkillGroup skillGroup = getItem(position);
			prepareGroup(skillGroup, preparedView);
			
			return preparedView;
		}
		
		private void prepareGroup(SkillGroup skillGroup, LinearLayout preparedView)
		{
			TextView groupName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupName);
			
			groupName.setText(skillGroup.groupName());
		}
	}
}
