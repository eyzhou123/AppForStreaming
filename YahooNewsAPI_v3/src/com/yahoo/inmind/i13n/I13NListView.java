package com.yahoo.inmind.i13n;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.ListView;

public class I13NListView extends ListView {
	String pkgName = null;
	Event evt = null;
	
	public I13NListView(Context context) {
		super(context);
		instrument();
	}
	
	public I13NListView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    instrument();
	}

	public I13NListView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    instrument();
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		if (changedView != this)
			return;
		switch(visibility)
		{
			case View.VISIBLE:
				I13N.get().log(evt.setAction("onVisibilityChanged(VISIBLE)"));
				logLargestItem();
				break;
			case View.INVISIBLE:
				I13N.get().log(evt.setAction("onVisibilityChanged(INVISIBLE)"));
				break;
			case View.GONE:
				I13N.get().log(evt.setAction("onVisibilityChanged(GONE)"));
				break;
		}
		super.onVisibilityChanged(changedView, visibility);
	}

	protected void instrument()
	{
		pkgName = this.getClass().getSimpleName();
		evt = new Event(pkgName, "");
		getViewTreeObserver().addOnScrollChangedListener(new OnScrollChangedListener()
        {
			@Override
			public void onScrollChanged() {
				logLargestItem();
			}
	
        });
	}
	
	private void logLargestItem() {
		int pos_beg = getFirstVisiblePosition();
		int pos_end = getLastVisiblePosition();
		int cnt = getCount();
		
		if (pos_beg < cnt)
		{
			int largestArea = -1, largestIdx = 0;
			for (int i = 0 ; i < (pos_end - pos_beg + 1) ; i++)
			{
				View v = getChildAt(i);
				if (v == null)
					continue;
				Rect r = new Rect();
				v.getGlobalVisibleRect(r);
				int area = r.bottom - (r.top > 0 ? r.top : 0);
				if (largestArea == -1 || largestArea < area)
				{
					largestArea = area;
					largestIdx = i;
				}
			}
			if (getVisibility() == View.VISIBLE)
			{
				addItemData(pos_beg + largestIdx, evt);
				I13N.get().logLatestDif(
					evt.setAction("item with the largest area:" + (pos_beg + largestIdx + 1)), 
					this);
			}
			else
				I13N.get().clearPreviousEvts(this);
		}
	}
	
	/**
	 * Override this function to customize the event to be logged when a specific item occupies the largest area on screen.
	 * @param position The position of the item in the adapter.
	 * @param evt The event to be customized and logged.
	 * */
	protected void addItemData(int position, Event evt) {
				
	}	
	
	public void setParent(Object obj) {
		if (!pkgName.equals(this.getClass().getSimpleName()))
			return;
		pkgName = obj.getClass().getName() + "." + pkgName;
		evt = new Event(pkgName, "");
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		setVisibility(visibility);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		
	}
	
	
}
