package com.yahoo.inmind.pluggableview;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;

import com.aphidmobile.flip.FlipViewController;
import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.i13n.I13N;
import com.yahoo.inmind.model.NewsAdapter;
import com.yahoo.inmind.model.NewsItem;
import com.yahoo.inmind.reader.App;

class AsyncFlipView extends FlipViewController implements PluggableAdapterView<NewsItem>{
	PositionChangedListener mPosChangedListener;
	ViewFlipListener mOnViewFlipListener;
	private static String pkgName = FlipViewController.class.getSimpleName();

	public AsyncFlipView(Context context) {
		super(context);
	}

	public AsyncFlipView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AsyncFlipView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public AsyncFlipView(Context context, int flipOrientation) {
		super(context, flipOrientation);
	}
	
	@Override
	public void init(NewsAdapter adapter, Fragment frag) {
		setBackgroundColor(Color.BLACK);
		setAnimationBitmapFormat(Bitmap.Config.RGB_565);
		setAdapter(adapter);
		App.get().getUIHandler().registerAsyncItemReadyListener(this);
	}
	
	@Override
	public void onUpdate(NewsItem item) {
		if (getAdapter().isEmpty())
			return;
			
		if (item != null && item.idx != null)
		{
			if (item.idx == 0)
				((BaseAdapter) getAdapter()).notifyDataSetChanged();
			refreshPage(item.idx);
		}
		else//item is deliberately assigned to null, meaning refresh all.
		{
			((BaseAdapter) getAdapter()).notifyDataSetChanged();
			refreshAllPages();
		}
	}	
	
	@Override
	public void configureSwipeToRefresh(final View swipeLayout){
		mOnViewFlipListener = new ViewFlipListener(){
			@Override
			public void onViewFlipped(View view, int position) {
				I13N.get().log(new Event(pkgName, "Flip to item: " + (position + 1) )
					.setUuid(((NewsItem) getAdapter().getItem(position)).uuid) );
				if (position == 0)
					swipeLayout.setEnabled(true);
				else
					swipeLayout.setEnabled(false);
				if (getPosChangedListener() != null)
				{
					getPosChangedListener().onPosChanged(position);
				}
			}
	    	
	    };
		setOnViewFlipListener(mOnViewFlipListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		App.get().getUIHandler().registerAsyncItemReadyListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
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
		{
			setSelection(idx);
			if (mOnViewFlipListener != null)
				mOnViewFlipListener.onViewFlipped(null, idx);
		}
	}	
	
}
