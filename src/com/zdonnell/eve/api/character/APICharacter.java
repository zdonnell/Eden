package com.zdonnell.eve.api.character;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;

import com.zdonnell.eve.Tools;
import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;
import com.zdonnell.eve.api.character.CharacterInfo.CurrentShipInfo;

public class APICharacter extends APIObject {
	
	public static final int SKILL_QUEUE = 0;
	public static final int CHAR_SHEET = 1;
	public static final int CHAR_INFO = 2;

	public static final String[] xmlURLs = new String[3];
	static
	{
		xmlURLs[SKILL_QUEUE] = baseURL + "char/SkillQueue.xml.aspx";
		xmlURLs[CHAR_SHEET] = baseURL + "char/CharacterSheet.xml.aspx";
		xmlURLs[CHAR_INFO] = baseURL + "eve/CharacterInfo.xml.aspx";
	}
	
	private ResourceManager resourceManager;
	
	private int characterID;

	public APICharacter(APICredentials credentials, int characterID, Context context) 
	{	
		this.characterID = characterID;
		super.setCredentials(credentials);	
		resourceManager = ResourceManager.getInstance(context);
	}

	public int id() { return characterID; }
	
	/**
	 * 
	 * 
	 * @param apiCallback
	 */
	public void getSkillQueue(APICallback<ArrayList<QueuedSkill>> apiCallback) 
	{			
		resourceManager.get(new APIRequestWrapper(apiCallback, new SkillQueueParser(), credentials, xmlURLs[SKILL_QUEUE], true, new BasicNameValuePair("characterID", String.valueOf(characterID))));		
	}
	
	public void getCharacterSheet(APICallback<CharacterSheet> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new CharacterSheetParser(), credentials, xmlURLs[CHAR_SHEET], true, new BasicNameValuePair("characterID", String.valueOf(characterID))));		
	}
	
	public void getCharacterInfo(APICallback<CharacterInfo> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new CharacterInfoParser(), credentials, xmlURLs[CHAR_INFO], true, new BasicNameValuePair("characterID", String.valueOf(characterID))));		
	}
	
	private class SkillQueueParser extends APIParser<ArrayList<QueuedSkill>>
	{
		@Override
		public ArrayList<QueuedSkill> parse(Document document) {
			
			NodeList skillNodes = document.getElementsByTagName("row");
			ArrayList<QueuedSkill> skillQueue = new ArrayList<QueuedSkill>();

			for (int x = 0; x < skillNodes.getLength(); x++) {
				Node skillNode = skillNodes.item(x);
				NamedNodeMap skillAttributes = skillNode.getAttributes();

				int skillID = Integer.parseInt(skillAttributes.getNamedItem("typeID").getTextContent());
				int skillLevel = Integer.parseInt(skillAttributes.getNamedItem("level").getTextContent());
				int startSP = Integer.parseInt(skillAttributes.getNamedItem("startSP").getTextContent());
				int endSP = Integer.parseInt(skillAttributes.getNamedItem("endSP").getTextContent());
				String startTime = skillAttributes.getNamedItem("startTime").getTextContent();
				String endTime = skillAttributes.getNamedItem("endTime").getTextContent();

				long millisUntilCompletion = Tools.timeUntilUTCTime(endTime);
				
				if (millisUntilCompletion > 0) skillQueue.add(new QueuedSkill(skillID, skillLevel, startSP, endSP, startTime, endTime));
			}
			
			return skillQueue;
		}
	}
	
	/**
	 * TODO Implement other character sheet fields
	 * 
	 * @author Zach
	 *
	 */
	private class CharacterSheetParser extends APIParser<CharacterSheet>
	{
		@Override
		public CharacterSheet parse(Document document) {
			
			CharacterSheet characterSheet = new CharacterSheet(characterID);
			
			String cloneName = document.getElementsByTagName("cloneName").item(0).getTextContent();
			int cloneSkillPoints = Integer.parseInt(document.getElementsByTagName("cloneSkillPoints").item(0).getTextContent());
			characterSheet.setCloneInfo(cloneName, cloneSkillPoints);
			
			double walletBalance = Double.parseDouble(document.getElementsByTagName("balance").item(0).getTextContent());
			characterSheet.setWalletBalance(walletBalance);
			
			return characterSheet;
		}
	}
	
	/**
	 * TODO Implement other character info fields
	 * 
	 * @author Zach
	 *
	 */
	private class CharacterInfoParser extends APIParser<CharacterInfo>
	{
		@Override
		public CharacterInfo parse(Document document) {
			
			CharacterInfo characterInfo = new CharacterInfo(characterID);
			
			double secStatus = Double.parseDouble(document.getElementsByTagName("securityStatus").item(0).getTextContent());
			characterInfo.setSecStatus(secStatus);
			
			int SP = Integer.parseInt(document.getElementsByTagName("skillPoints").item(0).getTextContent());
			characterInfo.setSP(SP);
			
			String shipName = document.getElementsByTagName("shipName").item(0).getTextContent(); 
			String shipTypeName = document.getElementsByTagName("shipTypeName").item(0).getTextContent();
			int shipTypeID = Integer.parseInt(document.getElementsByTagName("shipTypeID").item(0).getTextContent());
			characterInfo.setCurShipInfo(new CurrentShipInfo(shipName, shipTypeName, shipTypeID));			
			
			return characterInfo;
		}
	}
}