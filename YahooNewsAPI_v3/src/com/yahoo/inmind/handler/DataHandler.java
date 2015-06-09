package com.yahoo.inmind.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.yahoo.inmind.model.ProfiledSource;
import com.yahoo.inmind.reader.App;
import com.yahoo.inmind.reader.DrawerManager.DrawerItem;
import com.yahoo.inmind.reader.NewsListFragment;
import com.yahoo.inmind.reader.R;

public class DataHandler extends Handler {
	
	public static final int INIT_ADAPTER_ON_DATA_THREAD = 0;
	public static final int BEGIN_LOAD_IN_BACKGROUND = 1;
	public static final int PROFILE_READY = 2;
	
	private UIHandler mUiHandler = null;
	
	public DataHandler(Looper looper) {
		super(looper);		
	}
	
	public void registerUiHandler(UIHandler handler)
	{
		mUiHandler = handler;
	}
	
	@Override
	public void handleMessage(Message msg) {			
		super.handleMessage(msg);
		switch (msg.what) {
			case INIT_ADAPTER_ON_DATA_THREAD:
				{
					DrawerItem item = (DrawerItem) msg.obj;	
					if (item.frag == null)//Newly created
					{
						item.frag = new NewsListFragment(item);
					}
					
					Message msgs = new Message();
					msgs.what = UIHandler.SHOW_FRAGMENT;
					msgs.obj = item;
					mUiHandler.sendMessage(msgs);
					
					if (!App.get().isConnected())
					{
						Toast.makeText(App.get(), App.get().getString(R.string.not_connected), 1000).show();
						break;
					}
					
					mUiHandler.sendEmptyMessage(UIHandler.SHOW_LOADING);
					item.loadSources();
					mUiHandler.sendEmptyMessage(UIHandler.SHOW_LOADING_COMPLETE);
					
					Message msgf = new Message();
					msgf.what = UIHandler.FOCUS_FRAGMENT;
					msgf.obj = item.frag;
					mUiHandler.sendMessage(msgf);
				}
	            break;
			case BEGIN_LOAD_IN_BACKGROUND:
				DrawerItem item = (DrawerItem) msg.obj;				
				//Load images from here				
				item.loadAsync(mUiHandler, UIHandler.UPDATE_ASYNC_ITEM);
				mUiHandler.sendEmptyMessage(UIHandler.UPDATE_ASYNC_ITEMS);//in case all drawables are loaded, but no one notifies.
				break;
			case PROFILE_READY:
				ProfiledSource src = (ProfiledSource) msg.obj;
				src.generateItemsFromProfile();
				Message msgo = new Message();
				msgo.what = UIHandler.REFRESH_DRAWER_ITEMS;
				msgo.obj = msg.obj;
				mUiHandler.sendMessage(msgo);
				break;
			default:
		}
	}
};
