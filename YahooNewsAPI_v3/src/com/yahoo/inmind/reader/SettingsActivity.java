package com.yahoo.inmind.reader;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.i13n.I13N;

public class SettingsActivity extends PreferenceActivity {
	boolean bI13nOriginallyEnabled = false;
	private I13N mI13n;
	private String pkgName;
	private Event mEvt;
	
	@Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mI13n = I13N.get();
		instrument();
		
        getFragmentManager().beginTransaction().replace(android.R.id.content, new ReaderPreferenceFragment()).commit();
        ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		bI13nOriginallyEnabled = App.get().getSettings().getI13NEnabled();
    }

	private void instrument()
	{
		pkgName = this.getClass().getSimpleName();
		mEvt = new Event(pkgName, "");
	}
	
    @SuppressLint("NewApi")
	public static class ReaderPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.news_settings);
        }
    }
    
    //Enable the back button in the action bar
  	@Override
  	public boolean onOptionsItemSelected(MenuItem menuItem)
  	{       
  		if(((String) menuItem.getTitle()).equals(getResources().getString(R.string.app_name)))
  			onBackPressed();
  	    return true;
  	}

	@Override
	protected void onDestroy()
	{
		if (bI13nOriginallyEnabled != App.get().getSettings().getI13NEnabled())
		{
			if (App.get().getSettings().getI13NEnabled())
			{
				I13N.get().log(mEvt.setAction("I13N enabled"));
			}
			else
			{
				App.get().getSettings().setI13NEnabled(true);
				I13N.get().logImmediately(mEvt.setAction("I13N disabled"));
				App.get().getSettings().setI13NEnabled(false);
			}
		}
		super.onDestroy();
	}
  	
	@Override
	protected void onResume() {
		super.onResume();
		if (mI13n != null)
		{
			I13N.get().log(new Event(mEvt.setAction("onResume")));
			I13N.get().cancelFlushDelayed();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mI13n != null)
		{
			I13N.get().log(new Event(mEvt.setAction("onPause")));
			I13N.get().flushDelayed();
		}
	}

	public Event getEvent() {
		return mEvt;
	}

	public void setEvent(Event evt) {
		this.mEvt = evt;
	}	
}
