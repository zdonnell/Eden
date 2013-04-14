package com.zdonnell.eve.api.account;

/**
 * A wrapper for brief character data
 * 
 * @author Zach
 * 
 */
public class EveCharacter {

	public final String name;
	public final int charID;
	public final String corpName;
	public final int corpID;
	public final int keyID;
	public final String vCode;

	public EveCharacter(String name, int charID, String corpName, int corpID, int keyID, String vCode) {
		this.name = name;
		this.charID = charID;
		this.corpName = corpName;
		this.corpID = corpID;
		this.keyID = keyID;
		this.vCode = vCode;
	}
}
