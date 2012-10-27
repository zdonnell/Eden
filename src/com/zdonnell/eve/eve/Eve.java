package com.zdonnell.eve.eve;

import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.BaseRequest;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;

public class Eve extends APIObject {
	
	public static final int TYPE_NAME = 0;

	public static final String[] xmlURLs = new String[1];
	static
	{
		xmlURLs[TYPE_NAME] = baseURL + "eve/TypeName.xml.aspx";
	}
	
	private ResourceManager resourceManager;
	
	public Eve(Context context)
	{
		super.setCredentials(credentials);
		resourceManager = ResourceManager.getInstance(context);
	}
	
	/** 
	 * @param apiCallback
	 * @param typeIDs
	 */
	public void getTypeName(APICallback<String[]> apiCallback, int[] typeIDs) 
	{	
		String typeIDString = "";
		
		/* Comma delineate IDs */
		for (int x = 0; x < typeIDs.length; x++)
		{
			if (x != 0) typeIDString += ",";
			typeIDString += typeIDs[x];
		}
		
		resourceManager.get(new APIRequestWrapper(apiCallback, new TypeNameParser(), null, xmlURLs[TYPE_NAME], true, new BasicNameValuePair("ids", typeIDString)));
	}
	
	private class TypeNameParser extends APIParser<String[]>
	{
		@Override
		public String[] parse(Document document) 
		{			
			NodeList rawTypeNames = document.getElementsByTagName("row");
			String[] typeNames = new String[rawTypeNames.getLength()];

			for (int x = 0; x < rawTypeNames.getLength(); x++)
			{
				Node rawTypeNode = rawTypeNames.item(x);
				NamedNodeMap attributes = rawTypeNode.getAttributes();
								
				typeNames[x] = attributes.getNamedItem("typeName").getTextContent();
			}

			return typeNames;
		}
	}
}