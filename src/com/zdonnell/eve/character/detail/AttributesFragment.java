package com.zdonnell.eve.character.detail;

import android.content.Context;
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
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.CharacterSheet.AttributeEnhancer;

public class AttributesFragment extends Fragment {
    
	/**
	 * List of drawable resources to use for the Attribute list icons
	 */
    private static int[] icons = new int[5];
    static
    {
    	icons[CharacterSheet.MEMORY] = R.drawable.memory_icon;
    	icons[CharacterSheet.WILLPOWER] = R.drawable.willpower_icon;
    	icons[CharacterSheet.PERCEPTION] = R.drawable.perception_icon;
    	icons[CharacterSheet.CHARISMA] = R.drawable.charisma_icon;
    	icons[CharacterSheet.INTELLIGENCE] = R.drawable.intelligence_icon;
    }
    
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
    
    private ListView attributesListView;
    
    /**
     * Constructor
     * 
     * @param character the {@link APICharacter} to build the Attribute info from
     */
    public AttributesFragment(APICharacter character) 
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
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_attributes, container, false);
    	
    	attributesListView = (ListView) inflatedView.findViewById(R.id.char_detail_attributes_list);
    	attributesListView.setDivider(context.getResources().getDrawable(R.drawable.divider_grey));
    	attributesListView.setDividerHeight(1);
    	
    	/* Grab the character sheet to get the attribute info */
    	character.getCharacterSheet(new APICallback<CharacterSheet>()
    	{
			@Override
			public void onUpdate(CharacterSheet characterSheet) 
			{
				attributes = characterSheet.getAttributes();
				implants = characterSheet.getAttributeEnhancers();
				
				attributesListView.setAdapter(new AttributesListAdapter(context, R.layout.char_detail_attributes_list_item, new Integer[5]));
			}
    	});
    	
    	return inflatedView;
    }    
    
    /**
     * {@link ArrayAdapter} subclass to populate the Attributes {@link ListView}
     * 
     * @author Zach
     *
     */
    private class AttributesListAdapter extends ArrayAdapter<Integer>
	{	
		/**
		 * ID of the resource to inflate for the entire row
		 */
		int resourceID;
		
		/**
		 * Constructor
		 * 
		 * @param context
		 * @param viewResourceID the ID of the layout resource to use as a row in the Attributes {@link ListView}
		 * @param array Array of Integers used to mark the size of the list, actual data
		 * is pulled from {@link AttributesFragment}
		 */
		public AttributesListAdapter(Context context, int viewResourceID, Integer[] array) 
		{
			super(context, viewResourceID, array);			
			this.resourceID = viewResourceID;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{			
			LinearLayout preparedView; 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			/* Determine if we recycle the old view, or inflate a new one */
			if (convertView == null) preparedView = (LinearLayout) inflater.inflate(resourceID, parent, false);
			else preparedView = (LinearLayout) convertView;
				
			/* Grab references to the views needing update */
			ImageView icon = (ImageView) preparedView.findViewById(R.id.char_detail_attributes_list_item_image);
			TextView attributeValueView = (TextView) preparedView.findViewById(R.id.char_detail_attributes_list_item_value);
			TextView implantName = (TextView) preparedView.findViewById(R.id.char_detail_attributes_list_item_implantname);
						
			int attributeValue = attributes[position];
			
			/* If there is an implant in the current slot, update some things */
			if (implants[position] != null)
			{
				implantName.setText(implants[position].augmentatorName);
				attributeValue += implants[position].augmentatorValue;
			}
			else 
			{
				icon.setAlpha(0.3f);
			}
			
			/* Set views to correct implant info */
			icon.setImageResource(icons[position]);
			attributeValueView.setText(String.valueOf(attributeValue));
			
			return preparedView;
		}
	}
}
