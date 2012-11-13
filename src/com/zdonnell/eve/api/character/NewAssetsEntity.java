package com.zdonnell.eve.api.character;

import java.util.ArrayList;

import com.zdonnell.eve.api.character.AssetsEntity.AssetAttributes;

public abstract class NewAssetsEntity {
	/**
	 * This boolean value represents whether the the AssetsEntity contains assets within it.
	 * If the AssetsEntity is a container, but does not have anything inside, this value will
	 * still be <B>false</B>.
	 */
	private boolean containsAssets = false;
	
	private ArrayList<AssetsEntity> nestedAssets = new ArrayList<AssetsEntity>();
	
	private AssetAttributes attributes;
	
	/**
	 * Raw asset lists are typically parsed recursively, thus if an AssetsEntity has contained
	 * assets, they will be parsed first.  This constructor will allow an AssetsEntity to be provided
	 * it's contained assets.
	 * 
	 * @param attributes the attributes of the AssetsEntity in the form of an {@link AssetAttributes}
	 * @param containedAssets An {@link ArrayList} of {@link AssetsEntity} objects.  Or null, if the Asset does not contain
	 * any assets.
	 */
	public NewAssetsEntity(AssetAttributes attributes, ArrayList<AssetsEntity> containedAssets)
	{
		this.attributes = attributes;
		
		if (containedAssets != null && containedAssets.size() != 0) { 
			this.containsAssets = true;
			this.nestedAssets = containedAssets;
		}
	}
	
	/**
	 * @return whether the AssetsEntity has containedAssets
	 * @see #isContainer
	 */
	public boolean containsAssets() { return containsAssets; }
	
	public ArrayList<AssetsEntity> containedAssets() { return nestedAssets; }
	
	public AssetAttributes attributes() { return attributes; }
	
	
	public static class Station extends NewAssetsEntity
	{
		private int locationID;
		
		public Station(AssetAttributes attributes, ArrayList<AssetsEntity> containedAssets, int locationID) {
			super(attributes, containedAssets);
			this.locationID = locationID;
		}
		
		public int getLocationID() { return locationID; }
	}
	
	public static class Item extends NewAssetsEntity
	{		
		public Item(AssetAttributes attributes, ArrayList<AssetsEntity> containedAssets) {
			super(attributes, containedAssets);
		}
	}
	
	public static class AssetAttributes {
		public int typeID;
		public int flag;
		public int quantity;
	}
}
