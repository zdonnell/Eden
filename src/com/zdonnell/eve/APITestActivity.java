package com.zdonnell.eve;

import com.zdonnell.eve.api.account.Account;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class APITestActivity extends Activity {

	Account slick50zd1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apitest);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        slick50zd1 = new Account(1171726, "G87RoqlTiVG7ecrLSLuehJnBl0VjRG11xYppONMOu9GpbHghCqcgqk3n81egdAGm", getApplicationContext());
		//new GetCharacters().execute(slick50zd1);
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
}
