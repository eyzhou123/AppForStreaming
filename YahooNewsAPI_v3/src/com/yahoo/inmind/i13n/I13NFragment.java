package com.yahoo.inmind.i13n;

import android.app.Fragment;
import android.view.View;

import com.yahoo.inmind.reader.App;

public class I13NFragment extends Fragment{
	
	private String pkgName;
	private Event mEvt;
	private String mLabel;

	public I13NFragment()
	{
		super();
		instrument();
	}
	
	private void instrument()
	{
		pkgName = this.getClass().getSimpleName() + getFormatedLabel();
		mEvt = new Event(pkgName, "");
	}
	
	private String getFormatedLabel() {
		if (mLabel == null)
			return null;
		if (mLabel.equals(App.get().getCookieStore().getCurrentUserName())
				|| mLabel.equals("Reader"))
			return ".Personalized";
		return "." + mLabel;
	}

	@Override
	public void onPause() {
		super.onPause();
		I13N.get().log(new Event(mEvt).setAction("onPause"));
		View selfView = getView();
		if (selfView != null)
			selfView.dispatchWindowVisibilityChanged(View.INVISIBLE);
	}

	@Override
	public void onResume() {
		super.onResume();
		I13N.get().log(new Event(mEvt).setAction("onResume"));
		View selfView = getView();
		if (selfView != null)
			selfView.dispatchWindowVisibilityChanged(View.VISIBLE);
	}
	
	public String getLabel() {
		return mLabel;
	}

	/**
	 * This function is used to discriminate different Fragments in the I13N logs.
	 * @param label The label of this fragment serving as the identifier.
	 * */
	public void setLabel(String label) {
		this.mLabel = label;
		instrument();
	}
	
	protected void log(String action)
	{
		I13N.get().log(new Event(mEvt).setAction(action));
	}
}
