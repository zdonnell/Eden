package com.zdonnell.eve.helpers;

public class ImageURL {

	private final static int CHAR = 0;
	private final static int CORP = 1;
	private final static int ICON = 2;
	
	private final static String serverUrl = "http://image.eveonline.com/";
	
	private static String[] url = new String[3];
	private static int imageSize[] = new int[3];
	private static String imageExtension[] = new String[3];

	static
	{
		url[CHAR] = serverUrl + "Character/";
		url[CORP] = serverUrl + "Corporation/";
		url[ICON] = serverUrl + "Type/";
		
		imageSize[CHAR] = 512;
		imageSize[CORP] = 256;
		imageSize[ICON] = 64;
		
		imageExtension[CHAR] = ".jpg";
		imageExtension[CORP] = ".png";
		imageExtension[ICON] = ".png";
	}
	
	/**
	 * Converts character ID into it's portrait image URL
	 * 
	 * @param characterID
	 * @return
	 */
	public static String forChar(int characterID)
	{
		return url[CHAR] + characterID + "_" + imageSize[CHAR] + imageExtension[CHAR];
	}
	
	/**
	 * Converts corporation ID into it's icon image URL
	 * 
	 * @param corporationID
	 * @return
	 */
	public static String forCorp(int corporationID)
	{
		return url[CORP] + corporationID + "_" + imageSize[CORP] + imageExtension[CORP];
	}
	
	/**
	 * Converts corporation ID into it's icon image URL
	 * 
	 * @param corporationID
	 * @return
	 */
	public static String forType(int typeID)
	{
		return url[ICON] + typeID + "_" + imageSize[ICON] + imageExtension[ICON];
	}
}
