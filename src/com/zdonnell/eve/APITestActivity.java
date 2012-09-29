package com.zdonnell.eve;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;

import com.zdonnell.eve.api.account.Account;
import com.zdonnell.eve.api.account.EveCharacter;

public class APITestActivity extends Activity {

	Account slick50zd1;
	CharacterDB charDB;
	Spinner charSpinner;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apitest);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        charDB = new CharacterDB(getApplicationContext());
        charSpinner = (Spinner) findViewById(R.id.charSpinner);
        
        slick50zd1 = new Account(1171726, "G87RoqlTiVG7ecrLSLuehJnBl0VjRG11xYppONMOu9GpbHghCqcgqk3n81egdAGm", getApplicationContext());
		new GetCharacters().execute(slick50zd1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_apitest, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class GetCharacters extends	AsyncTask<Account, Integer, ArrayList<EveCharacter>> 
	{	
		protected ArrayList<EveCharacter> doInBackground(Account... accounts) { return accounts[0].characters(); }

		protected void onPostExecute(ArrayList<EveCharacter> characters) 
		{
			//for (EveCharacter character : characters) charSpinner.set;
		}
	}
}
