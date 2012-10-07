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
		
	private ResourceManager resourceManager;
	
	public Eve(Context context)
	{
		super.setCredentials(credentials);
		resourceManager = ResourceManager.getInstance(context);
	}
	
	/**
	 * Obtains the status of the TQ Server
	 * 
	 * @return An array storing the status values, where index 0 = Online/Offline
	 * and index 1 = Online Players
	 */
	public void getTypeName(APICallback<String[]> apiCallback, int[] typeIDs) 
	{
		final String resourceSpecificURL = "eve/TypeName.xml.aspx";
		String fullURL = BaseRequest.baseURL + resourceSpecificURL;
		
		String typeIDString = "";
				
		for (int x = 0; x < typeIDs.length; x++)
		{
			if (x != 0) typeIDString += ",";
			typeIDString += typeIDs[x];
		}
		
		resourceManager.requestResource(new APIRequestWrapper(apiCallback, new TypeNameParser(), null, fullURL, true, new BasicNameValuePair("ids", typeIDString)));
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
				
				if (attributes == null) Log.d("ATTRIBUTES", "IS NULL");
				
				typeNames[x] = attributes.getNamedItem("typeName").getTextContent();
			}

			return typeNames;
		}
	}
}
