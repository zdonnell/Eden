package com.zdonnell.eve.api.character;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

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

	public static final String[] xmlURLs = new String[6];
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
	
	public void getWalletTransactions(APICallback<WalletEntry.Transaction[]> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new WalletTransactionParser(), credentials, xmlURLs[WALLET_TRANS], true, new BasicNameValuePair("characterID", String.valueOf(characterID))));		
	}
	
	public void getAssetsList(APICallback<AssetsEntity[]> apiCallback)
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
			
			for (int i = 0; i < attributeEnhancersList.getLength(); i++)
			{
				Node augmentatorNameNode = attributeEnhancersList.item(i).getFirstChild();
				Node augmentatorValueNode = attributeEnhancersList.item(i).getLastChild();
				
				int slot = 0;
				
				/* The attribute enhancer order does not equal that of the attributeValues in the raw API response, correct that */
				if (attributeEnhancersList.item(i).getNodeName().equals("memoryBonus")) slot = 1;
				else if (attributeEnhancersList.item(i).getNodeName().equals("perceptionBonus")) slot = 3;
				else if (attributeEnhancersList.item(i).getNodeName().equals("willpowerBonus")) slot = 4;
				else if (attributeEnhancersList.item(i).getNodeName().equals("intelligenceBonus")) slot = 0;
				else if (attributeEnhancersList.item(i).getNodeName().equals("charismaBonus")) slot = 2;
				
				attributeEnhancers[slot] = new AttributeEnhancer(augmentatorNameNode.getTextContent(), Integer.parseInt(augmentatorValueNode.getTextContent()));
			}
			
			characterSheet.setAttributeInfo(attributeEnhancers, attributeValues);
			
			NodeList skillNodeList = null;
			
			/* Grab the generic rowsets */
			NodeList rowsets = document.getElementsByTagName("rowset");
			for (int i = 0; i < rowsets.getLength(); i++)
			{
				if (rowsets.item(i).getAttributes().item(0).getNodeValue().equals("skills"))
				{
					skillNodeList = rowsets.item(i).getChildNodes();
				}
			}
			
			if (skillNodeList != null)
			{
				SparseArray<Skill> skillList = new SparseArray<Skill>();
				
				for (int i = 0; i < skillNodeList.getLength(); i++)
				{
					Node skill = skillNodeList.item(i);
					
					int typeID = Integer.parseInt(skill.getAttributes().getNamedItem("typeID").getNodeValue());
					int skillPoints = Integer.parseInt(skill.getAttributes().getNamedItem("skillpoints").getNodeValue());
					int level = Integer.parseInt(skill.getAttributes().getNamedItem("level").getNodeValue());
					boolean published = skill.getAttributes().getNamedItem("typeID").getNodeValue().equals("1") ? true : false;
					
					skillList.put(typeID, new Skill(typeID, skillPoints, level, published));
				}
			
				characterSheet.setSkills(skillList);
			}
			
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
	private class WalletTransactionParser extends APIParser<WalletEntry.Transaction[]>
	{
		@Override
		public WalletEntry.Transaction[] parse(Document document) 
		{
			Node rowset = document.getElementsByTagName("rowset").item(0);	
			NodeList transactionNodes = document.getElementsByTagName("row");
			
			ArrayList<WalletEntry.Transaction> transactionsList = new ArrayList<WalletEntry.Transaction>(transactionNodes.getLength());
						
			for (int i = 0; i < transactionNodes.getLength(); ++i)
			{
				Node transactionNode = transactionNodes.item(i);
				NamedNodeMap transactionAttributes = transactionNode.getAttributes();
								
				String dateTime = transactionAttributes.getNamedItem("transactionDateTime").getTextContent();
				long transactionID = Long.valueOf(transactionAttributes.getNamedItem("transactionID").getTextContent());
				int quantity = Integer.valueOf(transactionAttributes.getNamedItem("quantity").getTextContent());
				int typeID = Integer.valueOf(transactionAttributes.getNamedItem("typeID").getTextContent());
				String typeName = transactionAttributes.getNamedItem("typeName").getTextContent();
				double price = Double.valueOf(transactionAttributes.getNamedItem("price").getTextContent());
				String stationName = transactionAttributes.getNamedItem("stationName").getTextContent();
				String transactionTypeString = transactionAttributes.getNamedItem("transactionType").getTextContent();
				int transactionType = transactionTypeString.equals("sell") ? WalletEntry.Transaction.SELL : WalletEntry.Transaction.BUY;
			
				transactionsList.add(new WalletEntry.Transaction(dateTime, transactionID, quantity, typeID, typeName, price, stationName, transactionType));
			}
			
			WalletEntry.Transaction[] transactions = new WalletEntry.Transaction[transactionsList.size()];
			transactionsList.toArray(transactions);
			
			return transactions;
		}
	}
	
	/**
	 * 
	 * @author Zach
	 *
	 */
	private class AssetListParser extends APIParser<AssetsEntity[]>
	{
		@Override
		public AssetsEntity[] parse(Document document) 
		{
			Node rootResultNode = document.getElementsByTagName("result").item(0);						
			return splitIntoLocations(parseAssets(rootResultNode));
		}
		
		/**
		 * Recursively parses a set of assets
		 * 
		 * @param rowNode the root node to start at, assumes that the assets are contained below this root node
		 * but the root node is not an asset itself
		 * @return an {@link ArrayList} of {@link AssetsEntity} objects or null if there are no assets to parse.
		 * @see <a href="http://wiki.eve-id.net/APIv2_Char_AssetList_XML">Character Assets API Documentation</a>
		 */
		private ArrayList<AssetsEntity> parseAssets(Node rowNode)
		{	
			/* if the node has children, this means it contains at least one asset */
			if (rowNode.hasChildNodes())
			{
				/* The list of actual assets as nodes */
				NodeList containedAssets = rowNode.getFirstChild().getChildNodes();
				ArrayList<AssetsEntity> assetsList = new ArrayList<AssetsEntity>(containedAssets.getLength());
				
				for (int i = 0; i < containedAssets.getLength(); i++)
				{
					Node assetNode = containedAssets.item(i);
					AssetsEntity asset = new AssetsEntity.Item(parseAssets(assetNode), parseAttributes(assetNode));
	
					assetsList.add(asset);
				}
				
				return assetsList;
			}
			/* There are no assets to return, return empty assets */
			return null;
		}
		
		/**
		 * Takes the list of root assets, and parses them by location ID
		 * 
		 * @param assets the root {@link ArrayList} of {@link AssetsEntity} objects
		 * @return an {@link AssetsEntity.AssetLocation} Array to be used in an ArrayAdapter for listViews.
		 */
		private AssetsEntity[] splitIntoLocations(ArrayList<AssetsEntity> assets)
		{			
			SparseArray<AssetsEntity> assetsByLocation = new SparseArray<AssetsEntity>();
			
			for (AssetsEntity rootAsset : assets)
			{
				int locationID = rootAsset.attributes().locationID;
				
				/* Only get assets in stations TODO expand this */
				if (locationID > 60000000) 
				{
					/* If this is the first time we have seen this location, set up the AssetLocation */
					if (assetsByLocation.get(locationID) == null)
					{						
						AssetsEntity.Station newLocation = new AssetsEntity.Station(new ArrayList<AssetsEntity>(), locationID);
						assetsByLocation.put(locationID, newLocation);
					}
					
					/* get the appropriate location, grab it's assets list and add the current asset to it */
					assetsByLocation.get(locationID).getContainedAssets().add(rootAsset);	
				}
			}
						
			AssetsEntity[] assetsArray = new AssetsEntity[assetsByLocation.size()];
			for (int x = 0; x < assetsByLocation.size(); x++) assetsArray[x] = assetsByLocation.valueAt(x);
			
			return assetsArray;
		}
		
		/**
		 * Grabs the asset attributes (i.e. locationID, typeID) from a given DOM Node
		 * 
		 * @param assetNode
		 * @return an {@link AssetsEntity.AssetAttributes}
		 */
		private AssetsEntity.AssetAttributes parseAttributes(Node assetNode)
		{
			AssetsEntity.AssetAttributes attributes = new AssetsEntity.AssetAttributes();
			
			NamedNodeMap attributesNodeMap = assetNode.getAttributes();
			
			attributes.typeID = Integer.parseInt(attributesNodeMap.getNamedItem("typeID").getTextContent());
			attributes.quantity = Integer.parseInt(attributesNodeMap.getNamedItem("quantity").getTextContent());
			attributes.flag = Integer.parseInt(attributesNodeMap.getNamedItem("flag").getTextContent());
			
			/* If it's a root asset, locationID will be there */
			if (attributesNodeMap.getNamedItem("locationID") != null)
			{
				attributes.locationID = Integer.parseInt(attributesNodeMap.getNamedItem("locationID").getTextContent());
			}
			
			if (Integer.parseInt(attributesNodeMap.getNamedItem("singleton").getTextContent()) == 0) attributes.singleton = false;
			else attributes.singleton = true;
			
			return attributes;
		}
	}
}