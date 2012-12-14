package com.zdonnell.eve;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.ImageService;
import com.zdonnell.eve.api.character.APICharacter;
import com.zdonnell.eve.api.character.CharacterInfo;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.QueuedSkill;
import com.zdonnell.eve.eve.Eve;
import com.zdonnell.eve.helpers.Tools;

public class CharacterSheetFragment extends Fragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    
    public final static int SKILLS = 0;
    public final static int SKILL_QUEUE = 1;
    public final static int ATTRIBUTES = 2;
    public final static int WALLET = 3;
    public final static int ASSETS = 4;
    
    /**
     * Static array of text strings for the character sheet list
     */
    public final static String[] sheetItems = new String[5];
    static 
    {
    	sheetItems[SKILLS] = "Skills";
    	sheetItems[SKILL_QUEUE] = "Skill Queue";
    	sheetItems[ATTRIBUTES] = "Attributes";
    	sheetItems[WALLET] = "Wallet";
    	sheetItems[ASSETS] = "Assets";
    }
    
    /**
     * Static array to hold drawable id's for the sheet item images
     */
    public final static int[] sheetItemImageIDs = new int[5];
    static
    {
    	sheetItemImageIDs[SKILLS] = R.drawable.skills;
    	sheetItemImageIDs[SKILL_QUEUE] = R.drawable.skillqueue;
    	sheetItemImageIDs[ATTRIBUTES] = R.drawable.attributes;
    	sheetItemImageIDs[WALLET] = R.drawable.wallet;
    	sheetItemImageIDs[ASSETS] = R.drawable.assets;
    }
    
    /**
     * Static array of the fragment classes to jump to when a sheet item is pressed
     */
    private final static Class<?>[] detailClass = new Class<?>[5];
    static 
    {
    	detailClass[SKILLS] = CorporationsFragment.class;
    	detailClass[SKILL_QUEUE] = CorporationsFragment.class;
    	detailClass[ATTRIBUTES] = CorporationsFragment.class;
    	detailClass[WALLET] = CorporationsFragment.class;
    	detailClass[ASSETS] = CorporationsFragment.class;
    }
    
    private final static SheetItem[] items = new SheetItem[5];
    static
    {
    	for (int x = 0; x < 5; x++)
    	{
    		items[x] = new SheetItem(sheetItems[x], sheetItemImageIDs[x], detailClass[x]);
    	}
    }

    private TextView[] subTexts = new TextView[5];
    
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    
    private View rootView;
    
    private Context context;
    
    private boolean updatedView;
    
    private ImageService imageService;
    private APICharacter character;
    private ArrayList<QueuedSkill> skillQueue;
    private CharacterSheet characterSheet;
    private CharacterInfo characterInfo;
    
    private static HashMap<Integer, String> skillLevelMap = new HashMap<Integer, String>();
    
    static {
    	skillLevelMap.put(1, "I");
    	skillLevelMap.put(2, "II");
    	skillLevelMap.put(3, "III");
    	skillLevelMap.put(4, "IV");
    	skillLevelMap.put(5, "V");
    }
    
    private TextView skillTimeRemaining, skillInTraining;
    
    private ListView listView;

    public interface Callbacks {

        public void onItemSelected(int id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };

    public CharacterSheetFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	context = inflater.getContext();
    	imageService = ImageService.getInstance(context);
    	
    	rootView = inflater.inflate(R.layout.character_sheet, container, false);
    	listView = (ListView) rootView.findViewById(R.id.char_sheet_list);
    	
    	LinearLayout headerText = (LinearLayout) inflater.inflate(R.layout.char_sheet_header_text, container, false);
    	skillTimeRemaining = (TextView) rootView.findViewById(R.id.current_skill_time);
    	skillInTraining = (TextView) rootView.findViewById(R.id.current_skill);
    	
    	listView.addHeaderView(headerText, null, false);
    	
    	listView.setAdapter(new CharacterSheetAdapater(context, R.layout.character_sheet_item, items));
    	listView.setDivider(context.getResources().getDrawable(R.drawable.divider_grey));
    	listView.setDividerHeight(1);
    	    	
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
		final ImageView portrait = (ImageView) rootView.findViewById(R.id.char_sheet_portrait);
		imageService.getPortraits(new ImageService.IconObtainedCallback() 
		{
			@Override
			public void iconsObtained(SparseArray<Bitmap> bitmaps) 
			{
				portrait.setImageBitmap(bitmaps.valueAt(0));
			}
		}, character.id());
		
		final TextView currentSkillView = (TextView) rootView.findViewById(R.id.current_skill);
    	
    	character.getSkillQueue(new APICallback<ArrayList<QueuedSkill>>() 
    	{
			@Override
			public void onUpdate(ArrayList<QueuedSkill> pSkillQueue) 
			{
				skillQueue = pSkillQueue;
				
				if (!skillQueue.isEmpty()) 
				{
					long timeUntilSkillFinish = 0;
					try 
					{
						timeUntilSkillFinish = Tools.timeUntilUTCTime(skillQueue.get(0).endTime);
						new SkillTimeRemainingCountdown(timeUntilSkillFinish, 1000, skillTimeRemaining).start();
					} 
					catch (IndexOutOfBoundsException e) { e.printStackTrace(); }
					
					final int skillLevel = skillQueue.get(0).skillLevel;
					new Eve(context).getTypeName(new APICallback<SparseArray<String>>() 
					{
						@Override
						public void onUpdate(SparseArray<String> typeName) {
							currentSkillView.setText(typeName.valueAt(0) + " " + skillLevelMap.get(skillLevel));						
						}
						
					}, new int[] { skillQueue.get(0).skillID });
				}
				else
				{
					currentSkillView.setText(Html.fromHtml("<FONT COLOR=#FF4444>No Skill in Training</FONT>"));
					skillTimeRemaining.setVisibility(View.INVISIBLE);
				}
			}
    	});
    	
    	character.getCharacterSheet(new APICallback<CharacterSheet>() 
    	{
			@Override
			public void onUpdate(CharacterSheet rCharacterSheet) 
			{
				characterSheet = rCharacterSheet;
				obtainedCharacterInfoSheet();
			}
    	});
    	
    	character.getCharacterInfo(new APICallback<CharacterInfo>() 
    	{
			@Override
			public void onUpdate(CharacterInfo pCharacterInfo) 
			{
				characterInfo = pCharacterInfo;
				obtainedCharacterInfoSheet();
			}
    	});
	}
	
	private class CharacterSheetAdapater extends ArrayAdapter<SheetItem>
	{
		SheetItem[] items;
		int resourceID;
		
		public CharacterSheetAdapater(Context context, int viewResourceID, SheetItem[] items) 
		{
			super(context, viewResourceID, items);
			this.items = items;
			this.resourceID = viewResourceID;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			LinearLayout preparedView; 
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if (convertView != null) preparedView = (LinearLayout) convertView;
			else preparedView = (LinearLayout) inflater.inflate(resourceID, parent, false);
			
			subTexts[position] = (TextView) preparedView.findViewById(R.id.character_sheet_item_subtext);
			
			TextView text = (TextView) preparedView.findViewById(R.id.character_sheet_item_text);
			text.setText(items[position].text);
						
			setSubTexts();
			
			ImageView image = (ImageView) preparedView.findViewById(R.id.character_sheet_item_image);
			image.setImageResource(items[position].imageID);
			
			preparedView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					 mCallbacks.onItemSelected(position);
				}
			});
			
			return preparedView;
		}
		
	}
	
	/**
	 * Method handles most of the dynamic data display.  Due to information
	 * needing to be obtained from the {@link CharacterSheet} and {@link CharacterInfo}
	 * resources, no action will be taken until both are displayed.
	 */
	private void obtainedCharacterInfoSheet()
	{
		if (characterInfo != null && characterSheet != null)
		{
			NumberFormat formatter = NumberFormat.getInstance();
			
			TextView characterSPView = (TextView) rootView.findViewById(R.id.current_sp);
			characterSPView.setText(formatter.format(characterInfo.getSP()) + " SP");
						
			TextView cloneNameView = (TextView) rootView.findViewById(R.id.current_clone);
			if (characterInfo.getSP() > characterSheet.getCloneSkillPoints())
			{
				cloneNameView.setText(Html.fromHtml("<FONT COLOR='#FF4444'>" + characterSheet.getCloneName() + "</FONT>"));
			}
			else cloneNameView.setText(Html.fromHtml("<FONT COLOR='#99CC00'>" + characterSheet.getCloneName() + "</FONT>"));
		
			TextView currentShipName = (TextView) rootView.findViewById(R.id.character_sheet_ship_name);
			TextView currentLocation = (TextView) rootView.findViewById(R.id.character_sheet_location);
			final ImageView currentShipIcon = (ImageView) rootView.findViewById(R.id.current_ship_icon);

			currentShipName.setText(characterInfo.getCurShipInfo().getName() + " (" + characterInfo.getCurShipInfo().getTypeName() + ")");
			currentLocation.setText(characterInfo.getLocation());
		
			final int shipTypeID = characterInfo.getCurShipInfo().getTypeID();
			imageService.getTypes( new ImageService.IconObtainedCallback() {
				
				@Override
				public void iconsObtained(SparseArray<Bitmap> bitmaps) {
					currentShipIcon.setImageBitmap(bitmaps.get(shipTypeID));
					
				}
			}, shipTypeID);
			
			setSubTexts();
		}
	}
	
	/**
	 * 
	 */
	private void setSubTexts()
	{
		NumberFormat formatter = NumberFormat.getInstance();
		
		/* Skills */
		
		
		/* SkillQueue */
		if (subTexts[SKILL_QUEUE] != null && skillQueue != null) 
		{
			if (skillQueue.isEmpty()) subTexts[SKILL_QUEUE].setText("Skill Queue Empty");
			else
			{
				QueuedSkill lastSkill = skillQueue.get(skillQueue.size() - 1);
				long timeUntilQueueFinished = Tools.timeUntilUTCTime(lastSkill.endTime);
				String formattedTimeRemaining = Tools.millisToEveFormatString(timeUntilQueueFinished);
				
				subTexts[SKILL_QUEUE].setText(skillQueue.size() + " Skill(s) in Queue (" + formattedTimeRemaining + ")");
			}
		}
		
		/* Attributes */
		
		
		/* Wallet */
		if (subTexts[WALLET] != null && characterSheet != null) 
		{
			subTexts[WALLET].setText("Balance: " + formatter.format(characterSheet.getWalletBalance()) + " ISK");
		}

		/* Assets */
		
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
				long timeUntilSkillCompletion = Tools.timeUntilUTCTime(skillQueue.get(x).endTime); 
				
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
	
	public static class SheetItem
	{
		String text;
		int imageID;
		Class<?> fragmentClass;
		
		public SheetItem(String text, int imageID, Class<?> fragmentClass)
		{
			this.text = text;
			this.imageID = imageID;
			this.fragmentClass = fragmentClass;
		}
	}
}
