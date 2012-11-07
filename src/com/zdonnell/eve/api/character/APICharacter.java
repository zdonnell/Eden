package com.zdonnell.eve.api.character;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;
import com.zdonnell.eve.api.character.CharacterInfo.CurrentShipInfo;
import com.zdonnell.eve.api.character.CharacterSheet.AttributeEnhancer;
import com.zdonnell.eve.helpers.Tools;

public class APICharacter extends APIObject {
	
	public static final int SKILL_QUEUE = 0;
	public static final int CHAR_SHEET = 1;
	public static final int CHAR_INFO = 2;
	public static final int WALLET_JOURN = 3;
	public static final int WALLET_TRANS = 4;
	public static final int ASSET_LIST = 5;

	public static final String[] xmlURLs = new String[5];
	static
	{
		xmlURLs[SKILL_QUEUE] = baseURL + "char/SkillQueue.xml.aspx";
		xmlURLs[CHAR_SHEET] = baseURL + "char/CharacterSheet.xml.aspx";
		xmlURLs[CHAR_INFO] = baseURL + "eve/CharacterInfo.xml.aspx";
		xmlURLs[WALLET_JOURN] = baseURL + "char/WalletJournal.xml.aspx";
		xmlURLs[WALLET_TRANS] = baseURL + "char/WalletTransactions.xml.aspx";
		xmlURLs[ASSET_LIST] = baseURL + "char/AssetList.xml.aspx";
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
	
	public void getWalletJournal(APICallback<ArrayList<WalletEntry.Journal>> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new WalletJournalParser(), credentials, xmlURLs[CHAR_INFO], true, new BasicNameValuePair("characterID", String.valueOf(characterID))));		
	}
	
	/*public void getWalletTransactions(APICallback<WalletEntry.Transaction> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new WalletTransactionsParser(), credentials, xmlURLs[CHAR_INFO], true, new BasicNameValuePair("characterID", String.valueOf(characterID))));		
	}*/
	
	public void getAssetsList(APICallback<ArrayList<AssetsEntity>> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new AssetListParser(), credentials, xmlURLs[ASSET_LIST], true, new BasicNameValuePair("characterID", String.valueOf(characterID))));
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
			
			NodeList attributeValuesList = document.getElementsByTagName("attributes").item(0).getChildNodes();
			int[] attributeValues = new int[5];				
			for (int x = 0; x < attributeValuesList.getLength(); x++)
			{
				Node attributeNode = attributeValuesList.item(x);
				attributeValues[x] = Integer.parseInt(attributeNode.getTextContent());
			}
			
			NodeList attributeEnhancersList = document.getElementsByTagName("attributeEnhancers").item(0).getChildNodes();
			AttributeEnhancer[] attributeEnhancers = new AttributeEnhancer[5];			
			for (int x = 0; x < attributeEnhancersList.getLength(); x++)
			{
				Node augmentatorNameNode = attributeEnhancersList.item(x).getFirstChild();
				Node augmentatorValueNode = attributeEnhancersList.item(x).getLastChild();
				
				attributeEnhancers[x] = new AttributeEnhancer(augmentatorNameNode.getTextContent(), Integer.parseInt(augmentatorValueNode.getTextContent()));
			}
			
			characterSheet.setAttributeInfo(attributeEnhancers, attributeValues);
			
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
			
			String location = document.getElementsByTagName("lastKnownLocation").item(0).getTextContent();
			characterInfo.setLocation(location);
			
			String shipName = document.getElementsByTagName("shipName").item(0).getTextContent(); 
			String shipTypeName = document.getElementsByTagName("shipTypeName").item(0).getTextContent();
			int shipTypeID = Integer.parseInt(document.getElementsByTagName("shipTypeID").item(0).getTextContent());
			characterInfo.setCurShipInfo(new CurrentShipInfo(shipName, shipTypeName, shipTypeID));			
			
			return characterInfo;
		}
	}
	
	/**
	 * 
	 * @author Zach
	 *
	 */
	private class WalletJournalParser extends APIParser<WalletEntry.Journal>
	{
		@Override
		public WalletEntry.Journal parse(Document document) {
			
			return null;
		}
	}
	
	/**
	 * 
	 * @author Zach
	 *
	 */
	private class AssetListParser extends APIParser<ArrayList<AssetsEntity>>
	{
		@Override
		public ArrayList<AssetsEntity> parse(Document document) 
		{
			Node rootResultNode = document.getElementsByTagName("result").item(0);						
			return parseAssets(rootResultNode);
		}
		
		private ArrayList<AssetsEntity> parseAssets(Node rowNode)
		{			
			if (rowNode.hasChildNodes())
			{
				NodeList containedAssets = rowNode.getFirstChild().getChildNodes();
				
				ArrayList<AssetsEntity> assetsList = new ArrayList<AssetsEntity>(containedAssets.getLength());
				
				for (int i = 0; i < containedAssets.getLength(); i++)
				{
					Node assetNode = containedAssets.item(i);
					
					AssetsEntity.AssetAttributes attributes = parseAttributes(assetNode);
					AssetsEntity asset = new AssetsEntity(attributes, parseAssets(assetNode));
					
					assetsList.add(asset);
				}
				
				return assetsList;
			}
			
			return null;
		}
		
		private AssetsEntity.AssetAttributes parseAttributes(Node assetNode)
		{
			AssetsEntity.AssetAttributes attributes = new AssetsEntity.AssetAttributes();
			
			NamedNodeMap attributesNodeMap = assetNode.getAttributes();
			attributesNodeMap.getNamedItem("typeID").getTextContent();
			
			return attributes;
		}
	}
}