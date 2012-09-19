package com.zdonnell.eve;

import java.util.ArrayList;

/**
 * 
 * @author Zach
 * @version 1.0
 *
 */
public interface API {	
	
	/**
	 * Adds the account credentials to the database
	 * 
	 * @param keyID
	 * @param verificationCode
	 */
	public void addAccount(int keyID, String verificationCode);
		
}
