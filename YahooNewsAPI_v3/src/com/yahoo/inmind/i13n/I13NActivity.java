package com.yahoo.inmind.i13n;

import android.app.Activity;
import android.os.Bundle;

public abstract class I13NActivity extends Activity {
	private I13N mI13n;
	private String pkgName;
	private Event mEvt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mI13n = I13N.get();
		instrument();
	}

	private void instrument()
	{
		pkgName = this.getClass().getSimpleName();
		mEvt = new Event(pkgName, "");
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
