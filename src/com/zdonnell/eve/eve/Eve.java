package com.zdonnell.eve.eve;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.SparseArray;

import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;
import com.zdonnell.eve.apilink.APICallback;
import com.zdonnell.eve.staticdata.api.StationInfo;

public class Eve extends APIObject {
	
	public static final int SKILL_TREE = 0;
	public static final int CONQ_STATIONS = 1;
	public static final int REF_TYPES = 2;
	
	private Context context;

	public static final String[] xmlURLs = new String[3];
	static
	{
		xmlURLs[SKILL_TREE] = baseURL + "eve/SkillTree.xml.aspx";
		xmlURLs[CONQ_STATIONS] = baseURL + "eve/ConquerableStationList.xml.aspx";
		xmlURLs[REF_TYPES] = baseURL + "/eve/RefTypes.xml.aspx";
	}
	
	private ResourceManager resourceManager ;
	
	public Eve(Context context)
	{
		this.context = context;
		
		super.setCredentials(credentials);
		resourceManager = ResourceManager.getInstance(context);
	}
	
	public void getConquerableStations(final APICallback<SparseArray<StationInfo>> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new ConquerableStationsParser(), null, xmlURLs[CONQ_STATIONS], true));
	}
	
	public void getRefTypes(final APICallback<SparseArray<String>> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new RefTypesParser(), null, xmlURLs[REF_TYPES], true));
	}
	
	private class ConquerableStationsParser extends APIParser<SparseArray<StationInfo>>
	{
		@Override
		public SparseArray<StationInfo> parse(Document document) 
		{
			SparseArray<StationInfo> stationInformation = new SparseArray<StationInfo>();
	
			NodeList stationList = document.getElementsByTagName("row");
			
			for (int i = 0; i < stationList.getLength(); ++i)
			{
				StationInfo stationInfo = new StationInfo();
				
				Node station = stationList.item(i);
				String stationIDString = station.getAttributes().getNamedItem("stationID").getTextContent();
				String stationTypeIDString = station.getAttributes().getNamedItem("stationTypeID").getTextContent();
				String stationName = station.getAttributes().getNamedItem("stationName").getTextContent();
				
				stationInfo.stationID = Integer.parseInt(stationIDString);
				stationInfo.stationTypeID = Integer.parseInt(stationTypeIDString);
				stationInfo.stationName = stationName;
				
				stationInformation.put(stationInfo.stationID, stationInfo);
			}
			
			return stationInformation;
		}
	}
	
	private class RefTypesParser extends APIParser<SparseArray<String>>
	{
		@Override
		public SparseArray<String> parse(Document document) 
		{
	
			NodeList refList = document.getElementsByTagName("row");
			SparseArray<String> refTypes = new SparseArray<String>(refList.getLength());

			for (int i = 0; i < refList.getLength(); ++i)
			{				
				Node station = refList.item(i);
				
				int refTypeID = Integer.valueOf(station.getAttributes().getNamedItem("refTypeID").getTextContent());
				String refTypeName = station.getAttributes().getNamedItem("refTypeName").getTextContent();
				
				refTypes.put(refTypeID, refTypeName);
			}
			
			return refTypes;
		}
	}
}
