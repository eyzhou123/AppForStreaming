package com.yahoo.inmind.handler;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;

import com.yahoo.inmind.model.ProfiledSource;
import com.yahoo.inmind.pluggableview.PluggableAdapterView;
import com.yahoo.inmind.reader.App;
import com.yahoo.inmind.reader.DrawerManager.DrawerItem;
import com.yahoo.inmind.reader.NewsListFragment;
import com.yahoo.inmind.reader.ReaderMainActivity;

@SuppressWarnings("rawtypes")
public class UIHandler extends Handler
{
	public static final int SHOW_LOADING = 100;
	public static final int SHOW_LOADING_COMPLETE = 101;
	public static final int UPDATE_ASYNC_ITEM = 102;
	public static final int SHOW_FRAGMENT = 103;
	public static final int FOCUS_FRAGMENT = 104;
	public static final int REFRESH_DRAWER_ITEMS = 105;
	public static final int UPDATE_ASYNC_ITEMS = 106;
	public static final int SCROLL_TO_ITEM = 107;
	public static final int PREPARE_FOR_DOWNLOAD_DATA = 108;
	public static final int RE_ENABLE_SWITCH_VIEW = 109;
	
	
	ReaderMainActivity mAct = null;
	ArrayList<PluggableAdapterView> mItemReadyListeners = new ArrayList<PluggableAdapterView>();
	
	public UIHandler(ReaderMainActivity readerActivity) {
		mAct = readerActivity;
	}

	@Override
	public void handleMessage(Message msg) {			
		super.handleMessage(msg);
		DrawerItem item;
		switch(msg.what){				
			case SHOW_LOADING:
				mAct.getCurrentFrag().setRefreshing(true);
				break;
			case SHOW_LOADING_COMPLETE:
				mAct.getCurrentFrag().setRefreshing(false);
				break;
			case SHOW_FRAGMENT:
				item = (DrawerItem) msg.obj;
				if (item.frag != null)
					mAct.enableFragment(item.frag);
				break;
			case UPDATE_ASYNC_ITEMS:
				msg.obj = null;
			case UPDATE_ASYNC_ITEM:
				for (PluggableAdapterView listener : mItemReadyListeners)
				{
					listener.onUpdate(msg.obj);
				}
				break;			
			case FOCUS_FRAGMENT:
				((NewsListFragment)msg.obj).onFocus();
				break;
			case REFRESH_DRAWER_ITEMS:
				ProfiledSource src = (ProfiledSource) msg.obj;
				src.showExtendedOptions();
				break;
			case SCROLL_TO_ITEM:
//				PluggableAdapterView pv = (PluggableAdapterView) msg.obj;
//				pv.scrollToIdx(msg.arg1);//the index
				break;
			case PREPARE_FOR_DOWNLOAD_DATA:
				item = (DrawerItem) msg.obj;
	    		if (item.frag != null)//refresh
	    		{
	    			item.frag.clearAdapter();
	    		}
	    		msg = new Message();
	    		msg.obj = item;
	    		msg.what = DataHandler.INIT_ADAPTER_ON_DATA_THREAD;
	    		App.get().getDataHandler().sendMessage(msg); 
	    		break;
			case RE_ENABLE_SWITCH_VIEW:
				MenuItem menuItem = (MenuItem)msg.obj;
				menuItem.setEnabled(true);
			default:
		}
	}
	
	/**
	 * Register to be notified when the AsyncSource, if any, finishes loading every item.
	 * The listener's onUpdate() will be called given the item given in AsyncSource::loadItemInParallel()
	 * on a UI thread, so UI refresh could be done in the AsyncItemReadyListener's onUpdate().
	 * 
	 * */
	public void registerAsyncItemReadyListener(PluggableAdapterView l)
	{
		if (!mItemReadyListeners.contains(l))
			mItemReadyListeners.add(l);
	}
	
	public void unregisterAsyncItemReadyListener(PluggableAdapterView l)
	{
		mItemReadyListeners.remove(l);
	}
}
