package com.zdonnell.eve;

import android.util.SparseArray;

import com.zdonnell.eve.api.character.AssetsEntity;
import com.zdonnell.eve.api.character.CharacterSheet;
import com.zdonnell.eve.api.character.WalletEntry;
import com.zdonnell.eve.staticdata.api.StationInfo;
import com.zdonnell.eve.staticdata.api.TypeInfo;

public class CharacterDetailCache {

	private WalletEntry[] journalEntries = null;
	
	private WalletEntry[] transactions = null;
	
	private CharacterSheet characterSheet = null;
	
	private SparseArray<StationInfo> stationInfo = null;
    
    private SparseArray<TypeInfo> typeInfo = null;
    
    private SparseArray<Float> prices = null;
    
    private AssetsEntity[] assets = null;
    
    public void empty()
    {
    	journalEntries = null;
    	transactions = null;
    	characterSheet = null;
    	stationInfo = null;
        typeInfo = null;
        prices = null;
    }
	
	public void cacheJournalEntries(WalletEntry[] journalEntries) { this.journalEntries = journalEntries; }
	public void cacheTransactions(WalletEntry[] transactions) { this.transactions = transactions; }
	
	public void cacheCharacterSheet(CharacterSheet characterSheet) { this.characterSheet = characterSheet; }
	
	public void cacheStationInfo(SparseArray<StationInfo> stationInfo) { this.stationInfo = stationInfo; }
	public void cacheTypeInfo(SparseArray<TypeInfo> typeInfo) { this.typeInfo = typeInfo; }
	public void cachePrices(SparseArray<Float> prices) { this.prices = prices; }

	public void cacheAssets(AssetsEntity[] assets) { this.assets = assets; }
	
	
	public WalletEntry[] getJournalEntries() { return journalEntries; }
	public WalletEntry[] getTransactions() { return transactions; }
	
	public CharacterSheet getCharacterSheet() { return characterSheet; }
	
	public SparseArray<StationInfo> getStationInfo() { return stationInfo; }
	public SparseArray<TypeInfo> getTypeInfo() { return typeInfo; }
	public SparseArray<Float> getPrices() { return prices; }
	
	public AssetsEntity[] getAssets() { return assets; }
}
