package com.zdonnell.eve.api.account;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;

public class Account extends APIObject {
	
	public static final int CHARS = 0;

	public static final String[] xmlURLs = new String[1];
	static
	{
		xmlURLs[CHARS] = baseURL + "account/Characters.xml.aspx";
	}
	
	private ResourceManager resourceManager;

	public Account(int keyID, String verificationCode, Context context) 
	{	
		super.setCredentials(new APICredentials(keyID, verificationCode));	
		resourceManager = ResourceManager.getInstance(context);
	}

	/**
	 * Get the list of characters for the current account
	 * 
	 * @return An Array list of {@link EveCharacter} objects
	 */
	public void characters(APICallback<ArrayList<EveCharacter>> apiCallback) 
	{			
		resourceManager.get(new APIRequestWrapper(apiCallback, new CharactersParser(), credentials, xmlURLs[CHARS], true));		
	}
	
	private class CharactersParser extends APIParser<ArrayList<EveCharacter>>
	{
		@Override
		public ArrayList<EveCharacter> parse(Document document) 
		{
			NodeList characterNodes = document.getElementsByTagName("row");
			ArrayList<EveCharacter> characters = new ArrayList<EveCharacter>();

			for (int x = 0; x < characterNodes.getLength(); x++) {
				Node characterNode = characterNodes.item(x);
				NamedNodeMap charAttributes = characterNode.getAttributes();

				String name = charAttributes.getNamedItem("name").getTextContent();
				String corpName = charAttributes.getNamedItem("corporationName").getTextContent();
				String charID = charAttributes.getNamedItem("characterID").getTextContent();
				String corpID = charAttributes.getNamedItem("corporationID").getTextContent();

				characters.add(new EveCharacter(name, Integer.parseInt(charID), corpName, Integer.parseInt(corpID), Account.this.credentials.keyID, Account.this.credentials.verificationCode));
			}
			
			return characters;
		}
		
	}
}
