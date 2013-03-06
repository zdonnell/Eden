package com.zdonnell.eve.eve;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.zdonnell.eve.api.APICallback;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.ResourceManager;
import com.zdonnell.eve.api.ResourceManager.APIRequestWrapper;
import com.zdonnell.eve.character.detail.SkillGroup;
import com.zdonnell.eve.staticdata.api.StationInfo;

public class Eve extends APIObject {
	
	public static final int SKILL_TREE = 0;
	public static final int CONQ_STATIONS = 1;
	
	private Context context;

	public static final String[] xmlURLs = new String[2];
	static
	{
		xmlURLs[SKILL_TREE] = baseURL + "eve/SkillTree.xml.aspx";
		xmlURLs[CONQ_STATIONS] = baseURL + "eve/ConquerableStationList.xml.aspx";
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
	
	public void getSkillTree(final APICallback<SkillGroup[]> apiCallback)
	{
		resourceManager.get(new APIRequestWrapper(apiCallback, new SkillTreeParser(), null, xmlURLs[SKILL_TREE], true));
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
	
	private class SkillTreeParser extends APIParser<SkillGroup[]>
	{
		@Override
		public SkillGroup[] parse(Document document) 
		{
			Node resultNode = document.getElementsByTagName("result").item(0);
			NodeList groupNodes = resultNode.getFirstChild().getChildNodes();
			
			SparseArray<ArrayList<SkillInfo>> assembledSkillInfo = new SparseArray<ArrayList<SkillInfo>>();
			SparseArray<String> groupNames = new SparseArray<String>();
			
			for (int i = 0; i < groupNodes.getLength(); ++i)
			{
				Node groupNode = groupNodes.item(i);
				
				/* get the group info */
				String groupName = groupNode.getAttributes().getNamedItem("groupName").getTextContent();
				int groupID = Integer.parseInt(groupNode.getAttributes().getNamedItem("groupID").getTextContent());
				
				groupNames.put(groupID, groupName);
				if (assembledSkillInfo.get(groupID) == null) assembledSkillInfo.put(groupID, new ArrayList<SkillInfo>());
				
				/* get the list of contained skills */
				NodeList containedSkillsNodeList = groupNode.getFirstChild().getChildNodes();
				SkillInfo[] containedSkills = new SkillInfo[containedSkillsNodeList.getLength()];
								
				for (int j = 0; j < containedSkillsNodeList.getLength(); ++j)
				{
					Node skillNode = containedSkillsNodeList.item(j);
					assembledSkillInfo.get(groupID).add(parseSkillNode(skillNode));
				}				
			}
			
			SkillGroup[] skillTree = new SkillGroup[assembledSkillInfo.size()];
			
			for (int a = 0; a < assembledSkillInfo.size(); ++a)
			{
				int groupID = assembledSkillInfo.keyAt(a);
				ArrayList<SkillInfo> containedSkillsList = assembledSkillInfo.valueAt(a);
				
				SkillInfo[] containedSkills = new SkillInfo[containedSkillsList.size()];
				containedSkillsList.toArray(containedSkills);
				
				skillTree[a] = new SkillGroup(groupID, groupNames.get(groupID), containedSkills);
			}
			
			return skillTree;
		}
		
		private SkillInfo parseSkillNode(Node skillNode)
		{			
			/* grab node attributes */
			boolean published = skillNode.getAttributes().getNamedItem("published").getTextContent().equals("1") ? true : false;
			int	typeID = Integer.parseInt(skillNode.getAttributes().getNamedItem("typeID").getTextContent());
			String typeName = skillNode.getAttributes().getNamedItem("typeName").getTextContent();
			
			/* grab basic info */
			String description = skillNode.getChildNodes().item(0).getTextContent();
			int rank = Integer.parseInt(skillNode.getChildNodes().item(1).getTextContent());
			
			/* get required skills */
			NodeList requiredSkillsNodeList = skillNode.getChildNodes().item(2).getChildNodes();
			ArrayList<SkillInfo.SkillPreReq> requiredSkills = new ArrayList<SkillInfo.SkillPreReq>(requiredSkillsNodeList.getLength());
			for (int i = 0; i < requiredSkillsNodeList.getLength(); ++i)
			{
				Node skillReqNode = requiredSkillsNodeList.item(i);
				int skillLevel = Integer.parseInt(skillReqNode.getAttributes().getNamedItem("skillLevel").getTextContent()); 
				int skillTypeID = Integer.parseInt(skillReqNode.getAttributes().getNamedItem("typeID").getTextContent()); 
				
				requiredSkills.add(new SkillInfo.SkillPreReq(skillLevel, skillTypeID));
			}

			/* get training attribute types */
			NodeList attributeTypeNodes = skillNode.getChildNodes().item(3).getChildNodes();
			String primaryAttribute = null, secondaryAttribute = null;
			if (attributeTypeNodes.item(0) != null) primaryAttribute = attributeTypeNodes.item(0).getTextContent();
			if (attributeTypeNodes.item(1) != null) secondaryAttribute = attributeTypeNodes.item(1).getTextContent();
			
			return new SkillInfo(published, typeID, typeName, description, rank, primaryAttribute, secondaryAttribute, requiredSkills);
		}
	}
}
