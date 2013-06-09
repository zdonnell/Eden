package com.zdonnell.eve.apilink.character;

import java.util.Set;

import android.content.Context;

import com.beimin.eveapi.character.sheet.ApiAttributeEnhancer;
import com.beimin.eveapi.character.sheet.ApiSkill;
import com.beimin.eveapi.character.sheet.CharacterSheetParser;
import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.core.ApiPage;
import com.beimin.eveapi.core.ApiPath;
import com.beimin.eveapi.exception.ApiException;
import com.zdonnell.eve.apilink.APIExceptionCallback;
import com.zdonnell.eve.apilink.APITask;
import com.zdonnell.eve.database.AttributesData;
import com.zdonnell.eve.database.CharacterSheetData;
import com.zdonnell.eve.database.SkillsData;

/**
 * AsyncTask to retrieve character sheet information and provide it to the specified callback
 * 
 * @author Zach
 *
 */
public class CharacterSheetTask extends APITask<Void, Void, CharacterSheetResponse>
{	
	final private ApiAuth<?> apiAuth;
	
	public CharacterSheetTask(APIExceptionCallback<CharacterSheetResponse> callback, final ApiAuth<?> apiAuth, final Context context)
	{
		super(callback, context, true, new EveApiInteraction<CharacterSheetResponse>(){

			@Override
			public CharacterSheetResponse perform() throws ApiException
			{
				CharacterSheetParser parser = CharacterSheetParser.getInstance();		
				CharacterSheetResponse response = parser.getResponse(apiAuth);;
		        	
	        	new CharacterSheetData(context).setCharacterSheet(response);
	        	new SkillsData(context).storeSkills((int) response.getCharacterID(), response.getSkills());
	        	new AttributesData(context).setImplants((int) response.getCharacterID(), response.getAttributeEnhancers());
		        	
		        return response;
			}
			
		});
		
		this.apiAuth = apiAuth;
	}
	
	public int requestTypeHash() 
	{
		return ApiPath.CHARACTER.getPath().concat(ApiPage.CHARACTER_SHEET.getPage()).hashCode();
	}

	public CharacterSheetResponse buildResponseFromDatabase() 
	{
		CharacterSheetResponse response = new CharacterSheetData(context).getCharacterSheet(apiAuth.getCharacterID().intValue());
		
		// Get attributes
		Set<ApiAttributeEnhancer> implants = new AttributesData(context).getImplants(apiAuth.getCharacterID().intValue());
		for (ApiAttributeEnhancer enhancer : implants) response.addAttributeEnhancer(enhancer);
		
		// Get skills
		SkillsData skillsData = new SkillsData(context);
		Set<ApiSkill> skills = skillsData.getSkills(apiAuth.getCharacterID().intValue());
		for (ApiSkill s : skills) response.addSkill(s);
		
		return response;
	}
}

