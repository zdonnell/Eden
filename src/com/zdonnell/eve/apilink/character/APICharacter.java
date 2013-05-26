package com.zdonnell.eve.apilink.character;

import android.content.Context;
import android.os.AsyncTask;

import com.beimin.eveapi.character.sheet.CharacterSheetResponse;
import com.beimin.eveapi.character.skill.queue.SkillQueueResponse;
import com.beimin.eveapi.core.ApiAuth;
import com.beimin.eveapi.eve.character.CharacterInfoResponse;
import com.beimin.eveapi.shared.wallet.journal.WalletJournalResponse;
import com.zdonnell.eve.apilink.APIExceptionCallback;

public class APICharacter 
{
	private Context context;
	private ApiAuth<?> apiAuth;
	
	public APICharacter(Context context, ApiAuth<?> apiAuth)
	{
		this.context = context;
		this.apiAuth = apiAuth;
	}
	
	public ApiAuth<?> getApiAuth()
	{
		return apiAuth;
	}
	
	public void getCharacterSheet(APIExceptionCallback<CharacterSheetResponse> callback)
	{
		new CharacterSheetTask(callback, apiAuth, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
	}
	
	public void getCharacterInfo(APIExceptionCallback<CharacterInfoResponse> callback)
	{
		new CharacterInfoTask(callback, apiAuth, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
	}
	
	public void getSkillQueue(APIExceptionCallback<SkillQueueResponse> callback)
	{
		new SkillQueueTask(callback, apiAuth, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
	}
	
	public void getWalletJournal(APIExceptionCallback<WalletJournalResponse> callback)
	{
		new WalletJournalTask(callback, apiAuth, context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
}
