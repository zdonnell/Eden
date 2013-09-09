package com.zdonnell.eden.character.detail.skills;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zdonnell.androideveapi.character.sheet.ApiSkill;
import com.zdonnell.androideveapi.character.sheet.CharacterSheetResponse;
import com.zdonnell.androideveapi.core.ApiAuthorization;
import com.zdonnell.androideveapi.eve.skilltree.ApiRequirement;
import com.zdonnell.androideveapi.eve.skilltree.ApiSkillGroup;
import com.zdonnell.androideveapi.eve.skilltree.CharacterAttribute;
import com.zdonnell.androideveapi.eve.skilltree.SkillTreeResponse;
import com.zdonnell.androideveapi.exception.ApiException;
import com.zdonnell.androideveapi.link.ApiExceptionCallback;
import com.zdonnell.androideveapi.link.character.ApiCharacter;
import com.zdonnell.androideveapi.link.eve.ApiEve;
import com.zdonnell.eden.R;
import com.zdonnell.eden.TypeInfoActivity;
import com.zdonnell.eden.character.detail.DetailFragment;
import com.zdonnell.eden.character.detail.SkillLevelIndicator;

/**
 * This Fragment is used to display the Skills List for a character.
 *
 * @author Zach
 */
public class SkillsFragment extends DetailFragment {

    public static final int ALL_SKILLS = 0;
    public static final int TRAINED_SKILLS = 1;
    public static final int TRAINABLE_SKILLS = 2;

    public static final String[] skillOptions = new String[2];
    private static final int[] baseSPAtLevel = new int[6];

    static {
        skillOptions[ALL_SKILLS] = "All Skills";
        skillOptions[TRAINED_SKILLS] = "Trained Skills";

        baseSPAtLevel[0] = 0;
        baseSPAtLevel[1] = 250;
        baseSPAtLevel[2] = 1414;
        baseSPAtLevel[3] = 8000;
        baseSPAtLevel[4] = 45255;
        baseSPAtLevel[5] = 256000;
    }

    /**
     * The unmodified Skill Tree
     */
    private ApiSkillGroup[] skillTree;

    /**
     * The current characters skills, indexed by skill typeID.
     */
    private SparseArray<ApiSkill> charactersSkills;

    /**
     * The main view presenting the skill list.
     */
    private ExpandableListView skillsListView;

    /**
     * The mode to use to display the skills.
     *
     * @see {@link #ALL_SKILLS}
     * @see {@link #TRAINED_SKILLS}
     */
    private int skillsDisplayMode = TRAINED_SKILLS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_skills, container, false);
        skillsListView = (ExpandableListView) inflatedView.findViewById(R.id.char_detail_skills_list);

        skillsDisplayMode = sharedPreferences.getInt("skill_display", TRAINED_SKILLS);

        loadData();

        return inflatedView;
    }

    @Override
    public void loadData() {
        // Load in Character Sheet to get Character Skills
        ApiAuthorization apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), getArguments().getInt("characterID"), getArguments().getString("vCode"));
        new ApiCharacter(context, apiAuth).getCharacterSheet(new ApiExceptionCallback<CharacterSheetResponse>(parentActivity) {
            @Override
            public void onUpdate(CharacterSheetResponse response) {
                SparseArray<ApiSkill> currentTempSkills = new SparseArray<ApiSkill>(response.getSkills().size());
                for (ApiSkill s : response.getSkills()) {
                    currentTempSkills.put(s.getTypeID(), s);
                }
                charactersSkills = currentTempSkills;
                updateSkillList();
            }

            @Override
            public void onError(CharacterSheetResponse response, ApiException exception) {
                // TODO add UI element notifying user of error
            }
        });

        // Load in the Skill Tree
        new ApiEve(context).skillTree(new ApiExceptionCallback<SkillTreeResponse>(parentActivity) {
            @Override
            public void onUpdate(SkillTreeResponse response) {
                Set<ApiSkillGroup> apiSkillGroups = response.getAll();
                skillTree = new ApiSkillGroup[apiSkillGroups.size()];
                apiSkillGroups.toArray(skillTree);

                updateSkillList();
            }

            @Override
            public void onError(SkillTreeResponse response, ApiException exception) {
                // TODO add UI element notifying user of error
            }
        });
    }

    /**
     * This method will tell the ListView to use a new {@link SkillsExpandedListAdapter}.
     */
    private void updateSkillList() {
        if (charactersSkills != null && skillTree != null) {
            skillsListView.setAdapter(new SkillsExpandedListAdapter(context, skillTree, charactersSkills, skillsDisplayMode));
        }
    }

    /**
     * Updates the skill listing display based on the mode provided
     *
     * @param mode {@link #ALL_SKILLS} or {@link #TRAINED_SKILLS}
     */
    public void updateSkillDisplayMode(int mode) {
        skillsDisplayMode = mode;
        sharedPreferences.edit().putInt("skill_display", mode).commit();
        updateSkillList();
    }

    /**
     * Adapter to bind skill data to an {@link ExpandableListView}
     *
     * @author Zach
     */
    private class SkillsExpandedListAdapter implements ExpandableListAdapter {
        private Context context;
        private NumberFormat formatter = NumberFormat.getInstance();
        private LayoutInflater inflater;

        private static final int groupLayoutID = R.layout.char_detail_skills_list_item;
        private static final int childLayoutID = R.layout.char_detail_skills_list_item_subskill;

        /**
         * Map that links an attribute type to it's color representation
         */
        private HashMap<CharacterAttribute, Integer> attributeColors = new HashMap<CharacterAttribute, Integer>(5);

        /**
         * Map that links an attribute type to it's color
         */
        private HashMap<CharacterAttribute, String> attributeLetter = new HashMap<CharacterAttribute, String>(5);

        /**
         * An Array of {@link ApiSkillGroup} representing the entire set of Skills In EVE.
         */
        private ApiSkillGroup[] skillTree;

        /**
         * A Modified version of {@link skillTree} only containing groups that have skills trained
         * according to {@link #characterSkills}
         */
        private ApiSkillGroup[] skillTreeTrainedSkills;

        /**
         * @see {@link SkillsFragment#charactersSkills}
         */
        private SparseArray<ApiSkill> characterSkills;

        /**
         * Flags whether the Adapter should show all skills or only trained skills
         */
        private boolean showAll = false;

        /**
         * Constructor
         *
         * @param context
         * @param skillTree         The master list of skills that the character's skills should be referenced against
         * @param characterSkills   The current character's skills
         * @param skillsDisplayMode {@link SkillsFragment#ALL_SKILLS} or {@link SkillsFragment#TRAINED_SKILLS}
         */
        public SkillsExpandedListAdapter(Context context, ApiSkillGroup[] skillTree, SparseArray<ApiSkill> characterSkills, int skillsDisplayMode) {
            this.context = context;
            this.skillTree = skillTree;
            this.characterSkills = characterSkills;
            this.showAll = (skillsDisplayMode == 0);

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            prepareSkillsets();

            // Fill Attribute Maps
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

        /**
         * Takes the characters current skills and compares them to the Skill Tree to see what groups in the
         * Skill Tree have no skills trained for the given character.  A modified skill tree with reduced groups is created
         * for use with display mode: {@link SkillsFragment#TRAINABLE_SKILLS}
         *
         * @see {@link #skillTreeTrainedSkills}
         */
        private void prepareSkillsets() {
            ArrayList<ApiSkillGroup> groupsWithSkillsTrained = new ArrayList<ApiSkillGroup>();
            for (ApiSkillGroup skillGroup : skillTree) {
                ArrayList<com.zdonnell.androideveapi.eve.skilltree.ApiSkill> trainedSkills = new ArrayList<com.zdonnell.androideveapi.eve.skilltree.ApiSkill>();

                for (com.zdonnell.androideveapi.eve.skilltree.ApiSkill skillInfo : skillGroup.getSkills()) {
                    if (characterSkills.get(skillInfo.getTypeID()) != null)
                        trainedSkills.add(skillInfo);
                }

                if (!trainedSkills.isEmpty()) {
                    ApiSkillGroup newGroup = new ApiSkillGroup();
                    newGroup.setGroupID(skillGroup.getGroupID());
                    newGroup.setGroupName(skillGroup.getGroupName());
                    for (com.zdonnell.androideveapi.eve.skilltree.ApiSkill skill : trainedSkills)
                        newGroup.add(skill);

                    groupsWithSkillsTrained.add(newGroup);
                }
            }

            skillTreeTrainedSkills = new ApiSkillGroup[groupsWithSkillsTrained.size()];
            groupsWithSkillsTrained.toArray(skillTreeTrainedSkills);

            Arrays.sort(skillTreeTrainedSkills, new SkillsSort.SkillGroupAlpha());
        }

        public boolean areAllItemsEnabled() {
            // TODO Auto-generated method stub
            return false;
        }

        public Object getChild(int groupPosition, int childPosition) {
            ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
            com.zdonnell.androideveapi.eve.skilltree.ApiSkill[] groupSkills = new com.zdonnell.androideveapi.eve.skilltree.ApiSkill[skillTreeType[groupPosition].getSkills().size()];
            skillTreeType[groupPosition].getSkills().toArray(groupSkills);

            Arrays.sort(groupSkills, new SkillsSort.SkillInfoAlpha());

            return groupSkills[childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
            com.zdonnell.androideveapi.eve.skilltree.ApiSkill[] groupSkills = new com.zdonnell.androideveapi.eve.skilltree.ApiSkill[skillTreeType[groupPosition].getSkills().size()];
            skillTreeType[groupPosition].getSkills().toArray(groupSkills);

            Arrays.sort(groupSkills, new SkillsSort.SkillInfoAlpha());

            com.zdonnell.androideveapi.eve.skilltree.ApiSkill childSkill = groupSkills[childPosition];

            return childSkill.getTypeID();
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View preparedView;

            if (convertView != null) preparedView = convertView;
            else preparedView = inflater.inflate(childLayoutID, parent, false);

            com.zdonnell.androideveapi.eve.skilltree.ApiSkill skillInfo = (com.zdonnell.androideveapi.eve.skilltree.ApiSkill) getChild(groupPosition, childPosition);
            prepareChild(skillInfo, preparedView);

            final Intent intent = new Intent(context, TypeInfoActivity.class);
            intent.putExtra("typeID", skillInfo.getTypeID());

            preparedView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    startActivity(intent);
                    return true;
                }
            });

            return preparedView;
        }

        protected void prepareChild(com.zdonnell.androideveapi.eve.skilltree.ApiSkill skillInfo, View preparedView) {
            TextView skillName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillname);
            TextView spText = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_skillsptext);
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

            if (characterSkills.get(skillInfo.getTypeID()) == null) {
                levelIndicator.setVisibility(View.GONE);

                skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skills));

                skillName.setAlpha(0.45f);
                spText.setAlpha(0.45f);

                boolean preReqsMet = true;
                for (ApiRequirement preReq : skillInfo.getRequiredSkills()) {
                    if (characterSkills.get(preReq.getTypeID()) == null || characterSkills.get(preReq.getTypeID()).getLevel() < preReq.getSkillLevel())
                        preReqsMet = false;
                }

                if (preReqsMet) spText.setText("You meet the requirements to train this skill");
                else spText.setText("You do not meet the requirements to train this skill");
            } else {
                levelIndicator.setVisibility(View.VISIBLE);
                levelIndicator.provideSkillInfo(characterSkills.get(skillInfo.getTypeID()), false, Color.rgb(75, 75, 75));

                ApiSkill currentSkill = characterSkills.get(skillInfo.getTypeID());
                if (currentSkill.getLevel() == 5)
                    skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skill_finished_training));
                else {
                    if (currentSkill.getSkillpoints() > baseSPAtLevel[currentSkill.getLevel()] * skillInfo.getRank()) {
                        skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skill_in_progress));
                    } else {
                        skillIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.skill_at_midlevel));
                    }
                }

                skillName.setAlpha(1);
                spText.setAlpha(1);

                spText.setText("SP: " + formatter.format(currentSkill.getSkillpoints()) + " / " + formatter.format(skillInfo.getRank() * 256000));
            }

        }

        public int getChildrenCount(int groupPosition) {
            ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
            return skillTreeType[groupPosition].getSkills().size();
        }

        public long getCombinedChildId(long groupId, long childId) {
            return childId;
        }

        public long getCombinedGroupId(long groupId) {
            return groupId;
        }

        public Object getGroup(int groupPosition) {
            ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
            return skillTreeType[groupPosition];
        }

        public int getGroupCount() {
            ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
            return skillTreeType.length;
        }

        public long getGroupId(int groupPosition) {
            ApiSkillGroup[] skillTreeType = showAll ? skillTree : skillTreeTrainedSkills;
            return skillTreeType[groupPosition].getGroupID();
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View preparedView;

            if (convertView != null) preparedView = convertView;
            else preparedView = inflater.inflate(groupLayoutID, parent, false);

            ApiSkillGroup skillGroup = (ApiSkillGroup) getGroup(groupPosition);
            prepareGroup(skillGroup, preparedView, groupPosition);

            return preparedView;
        }

        /**
         * Loads in
         *
         * @param skillGroup
         * @param preparedView
         * @param groupPosition
         */
        protected void prepareGroup(final ApiSkillGroup skillGroup, View preparedView, int groupPosition) {
            TextView groupName = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupName);
            TextView skillCount = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupSubText);
            TextView groupSP = (TextView) preparedView.findViewById(R.id.char_detail_skills_list_item_groupSkillPoints);

            groupName.setText(skillGroup.getGroupName());

            int totalSkillsCount = getSkillCount(groupPosition);
            int currentSkillsCount = 0;
            for (com.zdonnell.androideveapi.eve.skilltree.ApiSkill skill : skillGroup.getSkills()) {
                if (characterSkills.get(skill.getTypeID()) != null) ++currentSkillsCount;
            }

            skillCount.setText("Skills: " + currentSkillsCount + " of " + totalSkillsCount);

            int groupSPCount = 0;
            for (com.zdonnell.androideveapi.eve.skilltree.ApiSkill skill : skillGroup.getSkills()) {
                if (characterSkills.get(skill.getTypeID()) != null)
                    groupSPCount += characterSkills.get(skill.getTypeID()).getSkillpoints();
            }

            groupSP.setText(formatter.format(groupSPCount) + " SP");
        }

        private int getSkillCount(int modifiedPosition) {
            if (showAll) return skillTree[modifiedPosition].getSkills().size();
            else {
                for (int i = 0; i < skillTree.length; i++) {
                    if (skillTreeTrainedSkills[modifiedPosition].getGroupID() == skillTree[i].getGroupID()) {
                        return skillTree[i].getSkills().size();
                    }
                }

                return 0;
            }
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public boolean isEmpty() {
            return skillTree.length == 0;
        }

        public void onGroupCollapsed(int groupPosition) {

        }

        public void onGroupExpanded(int groupPosition) {

        }

        public void registerDataSetObserver(DataSetObserver observer) {

        }

        public void unregisterDataSetObserver(DataSetObserver observer) {

        }
    }
}
