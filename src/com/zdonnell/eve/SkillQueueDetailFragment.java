package com.zdonnell.eve;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.characterdetail.SkillQueueView;
import com.zdonnell.eve.dummy.DummyContent;

public class SkillQueueDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    DummyContent.DummyItem mItem;
        
    private APICharacter character;
    
    public SkillQueueDetailFragment(APICharacter character) 
    {
    	this.character = character;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
       
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	View inflatedView = inflater.inflate(R.layout.char_detail_skillqueue, container);
    	
    	return inflatedView;
    }
}
