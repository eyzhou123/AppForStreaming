package com.yahoo.inmind.reader;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

import com.yahoo.inmind.i13n.I13NActivity;

@SuppressLint("NewApi")
public class BackableActivity extends I13NActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);	
		//Enable the back button in the action bar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	//Enable the back button in the action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{       
		if(((String) menuItem.getTitle()).equals(getResources().getString(R.string.app_name)))
			onBackPressed();
	    return true;
	}
}
