package com.zdonnell.eve.api;

public class APICredentials {
	
	public int keyID;
	public String verificationCode;
	
	public APICredentials(int keyID, String verificationCode) {
		this.keyID = keyID;
		this.verificationCode = verificationCode;
	}
	
}
