<<<<<<< HEAD
package com.zdonnell.eve.api.account;

import java.util.ArrayList;

import android.content.Context;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.AccountDB;
import com.zdonnell.eve.api.CachedTimeDB;

public class Account extends APIObject {

	CachedTimeDB cacheDB;

	AccountDB accountDB;

	public Account(int keyID, String verificationCode, Context context) {
		super.setCredentials(new APICredentials(keyID, verificationCode));

		cacheDB = new CachedTimeDB(context);
		accountDB = new AccountDB(context);
	}

	/**
	 * Get the list of characters for the current account
	 * 
	 * @return An Array list of {@link EveCharacter} objects
	 */
	public ArrayList<EveCharacter> characters() {
		final String URL = CharactersReqeust.URL;

		boolean isCached = cacheDB.isCached(URL, credentials.keyID);

		if (isCached)
			return accountDB.characters(credentials);
		else {
			CharactersReqeust request = new CharactersReqeust(credentials);
			ArrayList<EveCharacter> characters = request.get();

			cacheDB.setCachedUntil(URL, credentials.keyID, request.cachedTime());

			return characters;
		}
	}

}
=======
package com.zdonnell.eve.api.account;

import java.util.ArrayList;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.APIObject;
import com.zdonnell.eve.api.AccountDB;
import com.zdonnell.eve.api.CachedTimeDB;

public class Account extends APIObject {

	CachedTimeDB cacheDB;
	
	AccountDB accountDB;
	
	public Account(int keyID, String verificationCode) 
	{
		super.setCredentials(new APICredentials(keyID, verificationCode));
	}
	
	/**
	 * Get the list of characters for the current account
	 * 
	 * @return An Array list of {@link Character} objects
	 */
	public ArrayList<Character> characters() 
	{	
		final String URL = CharactersReqeust.URL;
		
		boolean isCached = cacheDB.isCached(URL, credentials.keyID);

		if (isCached) return accountDB.characters(credentials);
		else 
		{			
			CharactersReqeust request = new CharactersReqeust(credentials);
			ArrayList<Character> characters = request.get();
			
			cacheDB.setCachedUntil(URL, credentials.keyID, request.cachedTime());
			
			return characters;
		}
	}
}
>>>>>>> typo, or something.
