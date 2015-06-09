package com.yahoo.inmind.pluggableview;

import com.yahoo.inmind.model.NewsAdapter;

import android.app.Fragment;
import android.view.View;

/**
 * Implement this interface for different kind of AdapterViews (a kind of abstract ListView). 
 * e.g. FlipView and ListView. 
 * */
public interface PluggableAdapterView<T> {
	public void init(NewsAdapter adapter, Fragment frag);
	/**
	 * Refresh the adapter view based on the given updated item. 
	 * @param item The updated item. Null if all views of items should be refreshed.
	 * */
	public void onUpdate(T item);
	/**
	 * Enable swipe to refresh only when the top of the list (item #0) is shown.
	 * @param swipeLayout The swipe layout to be enabled/disabled.
	 * */
	public void configureSwipeToRefresh(final View swipeLayout);
	public void onResume();
	public void onPause();
	public void onDestroyView();
	
	public interface PositionChangedListener{
		public void onPosChanged(int pos);
	}
	
	/**
	 * Used by the implementor to get the listeners, and to call the onPosChanged() of them.
	 * */
	public PositionChangedListener getPosChangedListener();	
	public void setPosChangedListener(PositionChangedListener mPosChangedListener);
	/**
	 * Scroll the list to the position indicated by idx.
	 * @param idx The index to scroll the list to.
	 * */
	public void scrollToIdx(int idx);
}
