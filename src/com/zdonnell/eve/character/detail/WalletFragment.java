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

public class WalletFragment extends Fragment {
    
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
    public WalletFragment(APICharacter character) 
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
    	LinearLayout inflatedView = (LinearLayout) inflater.inflate(R.layout.char_detail_wallet, container, false);
    	
    	return inflatedView;
    }    
}
