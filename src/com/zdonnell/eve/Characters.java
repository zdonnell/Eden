package com.zdonnell.eve;

import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.GridView;

import com.zdonnell.eve.api.account.Account;
import com.zdonnell.eve.api.account.EveCharacter;

public class Characters extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        GridView characterGrid = (GridView) findViewById(R.id.charGrid);
        
        Account slick50zd1 = new Account(892477, "vuywVBKCvhIuYT8xx1dx1YljvxFUj6x8JRrKkVNqGkbROXwchQb3eTrI3rC92u0s", this);
        new GetCharacters().execute(slick50zd1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * 
     * @author Zach
     *
     */
    private class GetCharacters extends AsyncTask<Account, Integer, ArrayList<EveCharacter>>
    {    
    	protected ArrayList<EveCharacter> doInBackground(Account... accounts) 
    	{
            return accounts[0].characters();
        }

        protected void onPostExecute(ArrayList<Character> characters) 
        {
            // TODO stuff
        }
    }
}
