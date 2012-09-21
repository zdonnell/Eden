package com.zdonnell.eve.api.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.BaseRequest;

/**
 * 
 * @author Zach
 *
 */
public class CharactersReqeust extends BaseRequest 
{
	public final static String URL = "/account/Characters.xml.aspx";	
	
	/**
	 * Constructor
	 * 
	 * @param credentials the {@link APICredentials} of the account to be queried
	 */
	public CharactersReqeust(APICredentials credentials)
	{
		this.credentials = credentials;
	}
	
	/**
	 * Queries the API server, and retrieves the data.
	 * 
	 * @return an ArrayList of characters found on the account
	 */
	public ArrayList<EveCharacter> get()
	{
		List<NameValuePair> POSTData = new ArrayList<NameValuePair>(2);
		POSTData.add(new BasicNameValuePair("keyID", String.valueOf(credentials.keyID)));
		POSTData.add(new BasicNameValuePair("vCode", credentials.verificationCode));
		
		/* Query the server */
		String rawResponse = super.makeHTTPRequest(URL, POSTData);
		/* Build response into Document Model */
		Document responseDoc = super.buildDocument(rawResponse);
		
		/* Get the nodes for each character */
		NodeList rows = responseDoc.getElementsByTagName("row");
		
		ArrayList<EveCharacter> characters = new ArrayList<EveCharacter>();
		
		/* Loop through the nodes and build character objects to add to the return ArrayList */
		for (int x = 0; x < rows.getLength(); x++)
		{
			Node charNode = rows.item(x);
			NamedNodeMap attributes = charNode.getAttributes();
			
			String name = attributes.getNamedItem("name").getTextContent();
			String corpName = attributes.getNamedItem("corporationName").getTextContent();
			String charID = attributes.getNamedItem("characterID").getTextContent();
			String corpID = attributes.getNamedItem("corporationID").getTextContent();
			
			characters.add(new EveCharacter(name, Integer.parseInt(charID), corpName, Integer.parseInt(corpID)));
		}
		
		cachedTime = responseDoc.getElementsByTagName("cachedUntil").item(0).getTextContent();
		
		return characters;
	}
}
