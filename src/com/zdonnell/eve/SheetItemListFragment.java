package com.zdonnell.eve;

import java.text.ParseException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.QueuedSkill;
import com.zdonnell.eve.dummy.DummyContent;

public class SheetItemListFragment extends Fragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    
    private View rootView;
    
    private ImageService imageService;
    private APICharacter character;
    private ArrayList<QueuedSkill> skillQueue;
    private CharacterSheet characterSheet;
    
    private TextView skillTimeRemaining, skillInTraining;
    
    private ListView listView;

    public interface Callbacks {

        public void onItemSelected(String id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    public SheetItemListFragment() 
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	imageService = new ImageService(inflater.getContext());
    	
    	rootView = inflater.inflate(R.layout.character_sheet, container, false);
    	listView = (ListView) rootView.findViewById(R.id.char_sheet_list);
    	
    	LinearLayout headerText = (LinearLayout) inflater.inflate(R.layout.char_sheet_header_text, container, false);
    	skillTimeRemaining = (TextView) rootView.findViewById(R.id.current_skill_time);
    	skillInTraining = (TextView) rootView.findViewById(R.id.current_skill);
    	
    	listView.addHeaderView(headerText, null, false);
    	
    	listView.setAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                DummyContent.ITEMS));
    	
    	return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState
                .containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    /*@Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
    	listView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
        	listView.setItemChecked(mActivatedPosition, false);
        } else {
        	listView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    /**
     * A call made to load up character information.  Should be called immediately after the fragment has been inflated.
     * @param characterID
     */
	public void setCharacter(APICharacter character) {
		ImageView portrait = (ImageView) rootView.findViewById(R.id.char_sheet_portrait);
		imageService.setPortrait(portrait, character.id(), ImageService.CHAR);
    	
    	character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() {
			@Override
			public void onUpdate(ArrayList<QueuedSkill> skillQueue) 
			{
				long timeUntilSkillFinish = 0;
				try 
				{
					timeUntilSkillFinish = Tools.timeUntilUTCTime(skillQueue.get(0).endTime);
			    	new SkillTimeRemainingCountdown(timeUntilSkillFinish, 1000, skillTimeRemaining).start();
				} 
				catch (ParseException e) { e.printStackTrace();	}
				catch (IndexOutOfBoundsException e) { e.printStackTrace(); }
			}
    	});
    	
    	character.getCharacterSheet(new APICallback<CharacterSheet>() {
			@Override
			public void onUpdate(CharacterSheet rCharacterSheet) 
			{
				characterSheet = rCharacterSheet;
				obtainedCharacterSheet();
			}
    	});
	}
	
	private void obtainedCharacterSheet()
	{
		TextView cloneNameView = (TextView) rootView.findViewById(R.id.current_clone);		
		cloneNameView.setText(characterSheet.getCloneName());
	}
	
	private class SkillTimeRemainingCountdown extends CountDownTimer
	{
		private TextView view;
		
		public SkillTimeRemainingCountdown(long millisInFuture, long countDownInterval, TextView view) {
			super(millisInFuture, countDownInterval);
			this.view = view;
		}

		@Override
		public void onFinish() {
			view.setText(Html.fromHtml("<FONT COLOR='#AAA'>Skill Training Completed</FONT>"));
			
			//skillQueue = character.skillQueue(); /* try to refresh resource */
			boolean foundSkill = false;
			
			/*
			 * When a skill completes we need to check the Queue for the next skill that is to start training.
			 * 
			 * It is possible that the cached skillQueue resource has skills that have already finished, so we
			 * need to iterate through the queue until we find the next skill that has yet to finish.
			 */
			for (int x = 0; x < skillQueue.size(); x++)
			{
				long timeUntilSkillCompletion = 0;
				try { timeUntilSkillCompletion = Tools.timeUntilUTCTime(skillQueue.get(x).endTime); } catch (ParseException e) { e.printStackTrace(); }
				
				if (timeUntilSkillCompletion > 0) 
				{
					new SkillTimeRemainingCountdown(timeUntilSkillCompletion, 1000, view).start();
					foundSkill = true;
					break;
				}
			}
			if (!foundSkill) view.setText(Html.fromHtml("<FONT COLOR='#FF4444'>Skill Queue Empty</FONT>"));
		}

		@Override
		public void onTick(long millisUntilFinished) 
		{
			
			if (millisUntilFinished < 24 * 60 * 60 * 1000) /* 1 Day in millis */
			{
				view.setText(Html.fromHtml("<FONT COLOR='#FFBB33'>" + Tools.millisToEveFormatString(millisUntilFinished) + "</FONT>"));
			}
			else view.setText(Html.fromHtml("<FONT COLOR='#99CC00'>" + Tools.millisToEveFormatString(millisUntilFinished) + "</FONT>"));
		}
	}
}
