package com.zdonnell.eve.eve;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.SparseArray;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;
import com.zdonnell.eve.api.TypeNameDatabase;

public class Eve extends APIObject {
	
	public static final int TYPE_NAME = 0;
	
	private Context context;

	public static final String[] xmlURLs = new String[1];
	static
	{
		xmlURLs[TYPE_NAME] = baseURL + "eve/TypeName.xml.aspx";
	}
	
	private ResourceManager resourceManager ;
	
	public Eve(Context context)
	{
		this.context = context;
		
		super.setCredentials(credentials);
		resourceManager = ResourceManager.getInstance(context);
	}
	
	/** 
	 * @param apiCallback
	 * @param typeIDs
	 */
	public void getTypeName(final APICallback<SparseArray<String>> apiCallback, int[] typeIDs) 
	{	
		final SparseArray<String> typeNames = new SparseArray<String>(typeIDs.length);
		
		typeIDs = checkForStoredTypeNames(typeIDs, typeNames);

		final int sections = (int) Math.ceil(typeIDs.length / 250d);
		
		final boolean[] subSectionsParsed = new boolean[sections];
		
		int i = 0;
		while (i < sections)
		{
			final int currentSection = i;
			
			int remainingIdCound = typeIDs.length - i * 250;
			int upperBounds = remainingIdCound < 250 ? remainingIdCound : 250;
			
			final int[] typeIDsGroup = Arrays.copyOfRange(typeIDs, i * 250, i * 250 + upperBounds);
			
			resourceManager.get(new APIRequestWrapper(new APICallback<String[]>() {

				@Override
				public void onUpdate(String[] updatedData) 
				{
					/* Merge in this batch to the main SparseArray */
					for (int c = 0; c < updatedData.length; c++)
					{
						typeNames.put(typeIDsGroup[c], updatedData[c]);
					}
					
					subSectionsParsed[currentSection] = true;
					
					/* check if remaining type batches have completed */
					boolean sectionsRemaining = false;
					for (int a = 0; a < sections; a++) 
					{
						if (subSectionsParsed[a] == false)
						{
							sectionsRemaining = true;
							break;
						}
					}
					
					/* If all sub batches have completed, return the full list to the initial callback */
					if (!sectionsRemaining) apiCallback.onUpdate(typeNames);
				}
				
			}, new TypeNameParser(), null, xmlURLs[TYPE_NAME], true, new BasicNameValuePair("ids", toCommaDelineatedString(typeIDsGroup))));
			
			++i;
		}
	}
	
	private int[] checkForStoredTypeNames(int[] typeIDs, SparseArray<String> names)
	{
		SparseArray<String> storedNames = new TypeNameDatabase(context).getTypeNames(typeIDs);
				
		ArrayList<Integer> typeIDsList = new ArrayList<Integer>(typeIDs.length);
		for (int id : typeIDs) typeIDsList.add(id);
		
		for (int x = 0; x < storedNames.size(); x++)
		{
			names.put(storedNames.keyAt(x), storedNames.valueAt(x));
			typeIDsList.remove(Integer.valueOf(storedNames.keyAt(x)));
		}
		
		int[] typeIDsToDownload = new int[typeIDsList.size()];
		for (int i = 0; i < typeIDsList.size(); i++)
		{
			typeIDsToDownload[i] = typeIDsList.get(i);
		}
		
		return typeIDsToDownload;
	}
	
	/**
	 * @deprecated use static database for type name lookup
	 */
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
	
	private String toCommaDelineatedString(int[] typeIDs)
	{
		String typeIDString = "";
		
		/* Comma delineate IDs */
		for (int x = 0; x < typeIDs.length; x++)
		{
			if (x != 0) typeIDString += ",";
			typeIDString += typeIDs[x];
		}
		
		return typeIDString;
	}
}
