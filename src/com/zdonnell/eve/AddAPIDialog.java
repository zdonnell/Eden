package com.zdonnell.eve;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.zdonnell.eve.api.APICredentials;
import com.zdonnell.eve.api.account.EveCharacter;

public class AddAPIDialog extends DialogFragment 
{
	APICredentials[] keys;
	
	SparseArray<ArrayList<EveCharacter>> characters = new SparseArray<ArrayList<EveCharacter>>();
	
	CharacterDB charDB;
	
	boolean refreshRequired = false;
	
	public AddAPIDialog()
	{
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{		
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View root = inflater.inflate(R.layout.characters_add_characters, null);
		
		builder.setView(root)
			.setNeutralButton("Get Characters", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int id) 
				{
					/*builder.setNeutralButton("Add Characters", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int id) 
						{
							
						}
					});*/
				}
			});
				
	    return builder.create();
	}
}