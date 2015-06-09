package com.yahoo.inmind.pluggableview;

import android.app.Fragment;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.android.sbjniapp.VhmsgWrapper;
import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.i13n.I13NListView;
import com.yahoo.inmind.model.NewsAdapter;
import com.yahoo.inmind.model.NewsItem;
import com.yahoo.inmind.reader.App;
import com.yahoo.inmind.util.MemUtil;

public class AsyncListView extends I13NListView implements PluggableAdapterView<NewsItem> {
	PositionChangedListener mPosChangedListener;
	
	
	public AsyncListView(Context context) {
		super(context);
		instrument();
	}
	
	public AsyncListView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    instrument();
	}

	public AsyncListView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    instrument();
	}

	@Override
	protected void addItemData(int position, Event evt) {
		if (getAdapter().isEmpty())
			return;
		Object obj = getAdapter().getItem(position);
		if (!(obj instanceof NewsItem))
			return;
		NewsItem item = (NewsItem) obj;
		evt.uuid = item.uuid;
		evt.summary = item.summary;
		
//		evt.url = item.url;
		if (getPosChangedListener() != null)
			getPosChangedListener().onPosChanged(position);
	}

	@Override
	public void init(NewsAdapter adapter, Fragment frag) {
		setAdapter(adapter);
      	setParent(frag);
      	MemUtil.disableListViewCache(this);
      	App.get().getUIHandler().registerAsyncItemReadyListener(this);
	}
	
	@Override
	public void onUpdate(NewsItem item) {
		((BaseAdapter)getAdapter()).notifyDataSetChanged();
		if (!isFocusable())
			invalidateViews();
	}
	
	@Override
	public void configureSwipeToRefresh(final View swipeLayout) {
		setOnScrollListener(new AbsListView.OnScrollListener() {
	        @Override
	        public void onScrollStateChanged(AbsListView absListView, int i) {
	
	        }
	
	        @Override
	        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	                if (firstVisibleItem == 0)
	                	swipeLayout.setEnabled(true);
	                else
	                	swipeLayout.setEnabled(false);
	        }
		});
	}

	@Override
	public void onResume() {
		App.get().getUIHandler().registerAsyncItemReadyListener(this);
	}

	@Override
	public void onPause() {
		App.get().getUIHandler().unregisterAsyncItemReadyListener(this);
	}

	@Override
	public void onDestroyView() {
		App.get().getUIHandler().unregisterAsyncItemReadyListener(this);
	}
	
	
	@Override
	public PositionChangedListener getPosChangedListener() {
		return mPosChangedListener;
	}
	
	@Override
	public void setPosChangedListener(PositionChangedListener mPosChangedListener) {
		this.mPosChangedListener = mPosChangedListener;
	}

	@Override
	public void scrollToIdx(int idx) {
		if (idx >= 0 && idx < getAdapter().getCount())
			setSelection(idx);
		clearAnimation();
	}

}
