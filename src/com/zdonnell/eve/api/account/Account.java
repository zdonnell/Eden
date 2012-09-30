package com.zdonnell.eve.api.account;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.BaseRequest;
import com.zdonnell.eve.api.ResourceManager;

public class Account extends APIObject {
	
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
	public ArrayList<EveCharacter> characters() 
	{	
		final String resourceSpecificURL = "account/Characters.xml.aspx";
		String fullURL = BaseRequest.baseURL + resourceSpecificURL;
		
		Document resourceDoc = resourceManager.getResource(credentials, fullURL, true);		
		NodeList characterNodes = resourceDoc.getElementsByTagName("row");
		
		ArrayList<EveCharacter> characters = new ArrayList<EveCharacter>();

		for (int x = 0; x < characterNodes.getLength(); x++) {
			Node characterNode = characterNodes.item(x);
			NamedNodeMap charAttributes = characterNode.getAttributes();

			String name = charAttributes.getNamedItem("name").getTextContent();
			String corpName = charAttributes.getNamedItem("corporationName").getTextContent();
			String charID = charAttributes.getNamedItem("characterID").getTextContent();
			String corpID = charAttributes.getNamedItem("corporationID").getTextContent();

			characters.add(new EveCharacter(name, Integer.parseInt(charID), corpName, Integer.parseInt(corpID)));
		}
		
		return characters;
	}
}
