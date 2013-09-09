package com.zdonnell.eden.character.detail.attributes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.androideveapi.character.sheet.ApiAttributeEnhancer;
import com.zdonnell.androideveapi.character.sheet.CharacterSheetResponse;
import com.zdonnell.androideveapi.core.ApiAuth;
import com.zdonnell.androideveapi.core.ApiAuthorization;
import com.zdonnell.androideveapi.exception.ApiException;
import com.zdonnell.androideveapi.link.ApiExceptionCallback;
import com.zdonnell.androideveapi.link.ILoadingActivity;
import com.zdonnell.androideveapi.link.character.ApiCharacter;
import com.zdonnell.eden.R;
import com.zdonnell.eden.character.detail.DetailFragment;

public class AttributesFragment extends DetailFragment {

    public static final int MEMORY = 0;
    public static final int WILLPOWER = 1;
    public static final int PERCEPTION = 2;
    public static final int CHARISMA = 3;
    public static final int INTELLIGENCE = 4;

    /**
     * List of drawable resources to use for the Attribute list icons
     */
    private static int[] icons = new int[5];

    static {
        icons[MEMORY] = R.drawable.memory_icon;
        icons[WILLPOWER] = R.drawable.willpower_icon;
        icons[PERCEPTION] = R.drawable.perception_icon;
        icons[CHARISMA] = R.drawable.charisma_icon;
        icons[INTELLIGENCE] = R.drawable.intelligence_icon;
    }

    private ApiCharacter character;

    /**
     * Array storing attribute values
     */
    private int[] attributes = new int[5];

    /**
     * Array of implants / augmentations
     */
    private ApiAttributeEnhancer[] implants = new ApiAttributeEnhancer[5];

    private Context context;

    private ListView attributesListView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_attributes, container, false);

        ApiAuth<?> apiAuth = new ApiAuthorization(getArguments().getInt("keyID"), getArguments().getInt("characterID"), getArguments().getString("vCode"));
        character = new ApiCharacter(context, apiAuth);

        attributesListView = (ListView) inflatedView.findViewById(R.id.char_detail_attributes_list);
        loadData();

        return inflatedView;
    }

    /**
     * {@link ArrayAdapter} subclass to populate the Attributes {@link ListView}
     *
     * @author Zach
     */
    private class AttributesListAdapter extends ArrayAdapter<Integer> {
        /**
         * ID of the resource to inflate for the entire row
         */
        int resourceID;

        /**
         * Constructor
         *
         * @param context
         * @param viewResourceID the ID of the layout resource to use as a row in the Attributes {@link ListView}
         * @param array          Array of Integers used to mark the size of the list, actual data
         *                       is pulled from {@link AttributesFragment}
         */
        public AttributesListAdapter(Context context, int viewResourceID, Integer[] array) {
            super(context, viewResourceID, array);
            this.resourceID = viewResourceID;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LinearLayout preparedView;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			/* Determine if we recycle the old view, or inflate a new one */
            if (convertView == null)
                preparedView = (LinearLayout) inflater.inflate(resourceID, parent, false);
            else preparedView = (LinearLayout) convertView;
				
			/* Grab references to the views needing update */
            ImageView icon = (ImageView) preparedView.findViewById(R.id.char_detail_attributes_list_item_image);
            TextView attributeValueView = (TextView) preparedView.findViewById(R.id.char_detail_attributes_list_item_value);
            TextView implantName = (TextView) preparedView.findViewById(R.id.char_detail_attributes_list_item_implantname);

            int attributeValue = attributes[position];
			
			/* If there is an implant in the current slot, update some things */
            if (implants[position] != null) {
                implantName.setText(implants[position].getAugmentatorName());
                attributeValue += implants[position].getAugmentatorValue();
            } else {
                icon.setAlpha(0.3f);
                implantName.setAlpha(0.5f);
            }
			
			/* Set views to correct implant info */
            icon.setImageResource(icons[position]);
            attributeValueView.setText(String.valueOf(attributeValue));

            return preparedView;
        }
    }

    @Override
    public void loadData() {
        character.getCharacterSheet(new ApiExceptionCallback<CharacterSheetResponse>((ILoadingActivity) getActivity()) {
            @Override
            public void onUpdate(CharacterSheetResponse response) {
                for (int i = 0; i < 5; i++) implants[i] = null;

                for (ApiAttributeEnhancer enhancer : response.getAttributeEnhancers()) {
                    if (enhancer.getAttribute().equals("intelligence")) implants[0] = enhancer;
                    if (enhancer.getAttribute().equals("memory")) implants[1] = enhancer;
                    if (enhancer.getAttribute().equals("charisma")) implants[2] = enhancer;
                    if (enhancer.getAttribute().equals("perception")) implants[3] = enhancer;
                    if (enhancer.getAttribute().equals("willpower")) implants[4] = enhancer;
                }

                attributes[0] = response.getIntelligence();
                attributes[1] = response.getMemory();
                attributes[2] = response.getCharisma();
                attributes[3] = response.getPerception();
                attributes[4] = response.getWillpower();

                attributesListView.setAdapter(new AttributesListAdapter(context, R.layout.char_detail_attributes_list_item, new Integer[5]));
            }

            @Override
            public void onError(CharacterSheetResponse response, ApiException exception) {

            }
        });
    }
}
