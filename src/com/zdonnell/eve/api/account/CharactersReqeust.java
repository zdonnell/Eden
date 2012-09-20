package com.zdonnell.eve.api.account;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.BaseRequest;

public class CharactersReqeust extends BaseRequest 
{
	public final static String URL = "/account/Characters.xml.aspx";
	
	/**
	 * the time that the resource is cached until, based on the last successful
	 * request
	 */
	private String cachedTime;
	
	public CharactersReqeust(APICredentials credentials)
	{
		this.credentials = credentials;
	}
	
	public ArrayList<Character> get()
	{
		List<NameValuePair> POSTData = new ArrayList<NameValuePair>(2);
		POSTData.add(new BasicNameValuePair("keyID", String.valueOf(credentials.keyID)));
		POSTData.add(new BasicNameValuePair("vCode", credentials.verificationCode));
		
		String rawResponse = super.makeHTTPRequest(URL, POSTData);
		Log.d("TEST", rawResponse);
		
		return null;
	}
}
