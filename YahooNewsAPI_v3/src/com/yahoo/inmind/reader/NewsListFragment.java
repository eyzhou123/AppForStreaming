package com.yahoo.inmind.reader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yahoo.inmind.handler.DataHandler;
import com.yahoo.inmind.handler.UIHandler;
import com.yahoo.inmind.i13n.I13NFragment;
import com.yahoo.inmind.model.NewsAdapter;
import com.yahoo.inmind.pluggableview.PluggableAdapterView;
import com.yahoo.inmind.reader.DrawerManager.DrawerItem;

@SuppressLint("ValidFragment")
@SuppressWarnings("rawtypes")
public class NewsListFragment extends I13NFragment implements PluggableAdapterView.PositionChangedListener{
    public static final String ARG_FRAGMENT_NUMBER = "fragment_number";
    protected NewsAdapter mAdapter = null;
    protected SwipeRefreshLayout swipeLayout = null;
    protected String mTitle = null;  
    protected boolean bContainsProfileSource = false;
    protected DrawerItem mItem = null;
    protected boolean bCreated = false;
	private PluggableAdapterView mPv;
	int mLayoutId = R.layout.fragment_news_listview;
    protected int mLastItem = -1;
    protected int mScrollToPos = -1;

	public NewsListFragment() {
    	// Empty constructor required for fragment subclasses
    	super();
    }

	public NewsListFragment(DrawerItem item) {
		super();
    	mItem = item;
    	setLabel(mItem.name);
    }
		
	public NewsAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void onDestroy() {			
		super.onDestroy();
		clearAdapter();
	}
	
	public void clearAdapter()
    {
		if (mItem != null)
			mItem.cancelLoadAsync();
		if (mAdapter == null)
			return;
    	mAdapter.clear();        
    }
            
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
            Bundle savedInstanceState) {
		bCreated = true;
		mAdapter = new NewsAdapter();
        
        if (mItem == null)//recover from the background process pruning
        {
        	((ReaderMainActivity) getActivity()).getDrawerManager().selectItem(0);
        	return null;
        }
        
        View rootView = inflater.inflate(mLayoutId, viewGroup, false);
        mPv = (PluggableAdapterView) rootView.findViewById(R.id.newslist);
        mPv.init(mAdapter, this);
        mPv.setPosChangedListener(this);
                
        mAdapter.setList(mItem.list);
        mAdapter.setRenderer(mItem.renderers);
        
        enableSwipeRefresh(rootView, mPv);
        swipeLayout.setEnabled(true);
        
        setRefreshing(true);
        
        onFocus();
        
        //Scroll to the current Item, if the list has been scrolled.
        int idx = -1;
        if ( (idx = getScrollToPos()) != -1){
        	Message msg = new Message();
        	msg.what = UIHandler.SCROLL_TO_ITEM;
        	msg.obj = mPv;
        	msg.arg1 = idx;
        	App.get().getUIHandler().sendMessage(msg);
        }
        
       	return rootView;
    }
	
	public void setRefreshing(boolean b)
	{
		if (swipeLayout != null)
			swipeLayout.setRefreshing(b);
	}
	
	public void onFocus() {
		mItem.bDirty = false;
		if (mItem.list.size() == 0){//The fragment is newly created
			mItem.list.addAll(mItem.bklist);
			mItem.bklist.clear();
			System.gc();
		}
		else{
			App.get().getUIHandler().sendEmptyMessage(UIHandler.UPDATE_ASYNC_ITEMS);
			setRefreshing(false);
		}
		
		Message msg = new Message();
        msg.what = DataHandler.BEGIN_LOAD_IN_BACKGROUND;
        msg.obj = mItem;
        App.get().getDataHandler().removeMessages(DataHandler.BEGIN_LOAD_IN_BACKGROUND);
        App.get().getDataHandler().sendMessage(msg);
         
        ReaderMainActivity act = (ReaderMainActivity) getActivity();
        if (act == null)
        	act = mItem.getParent().getActivity();
        act.setTitle(mItem.name);
	}
	
	private void enableSwipeRefresh(View rootView, PluggableAdapterView av) {
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setEnabled(false);
        swipeLayout.setOnRefreshListener(new OnRefreshListener(){

			@Override
			public void onRefresh() {
				ReaderMainActivity act = ((ReaderMainActivity)getActivity());
				mItem.bDirty = true;
				act.getDrawerManager().selectItem(mItem.idx);
				log("onRefresh");
			}
        	
        });
        
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        
        av.configureSwipeToRefresh(swipeLayout);
	}

	public void partialFree() {
		mAdapter.partialFree();
	}

	public DrawerItem getItem() {		
		return mItem;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mPv != null)
		{
	    	mPv.onResume();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	    if (mPv != null)
	    {
	    	mPv.onPause();
	    }
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	    if (mPv != null)
	    {
	    	mPv.onDestroyView();
	    	mPv = null;
	    }
	}
	
	public int getLayoutId() {
		return mLayoutId;
	}

	public void setLayoutId(int layoutId) {
		this.mLayoutId = layoutId;
	}
	
	public int getLastItemIdx() {
		return mLastItem;
	}

	public void setLastItemIdx(int mLastItem) {
		this.mLastItem = mLastItem;
	}

	@Override
	public void onPosChanged(int pos) {
		setLastItemIdx(pos);
	}
	
	public int getScrollToPos() {
		return mScrollToPos;
	}

	public void setScrollToPos(int mScrollToPos) {
		this.mScrollToPos = mScrollToPos;
	}
}
