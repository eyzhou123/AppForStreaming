package com.yahoo.inmind.model;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.yahoo.inmind.handler.UIHandler;
import com.yahoo.inmind.reader.App;

public class AsyncSource<T> extends ProfiledSource {
	private ArrayList<AsyncTask> tasks;
	LoadAsyncData mData = null;
	volatile boolean loadAsyncCancelling = false;
	
	//private boolean loadAsyncCancelling = false;
	final int LOAD = 0;
	final int CANCEL = 1;
	
	public AsyncSource(){
		super();
		tasks = new ArrayList<AsyncTask>();
	}
	
	//Tasks used to update the list asynchronously after the list is displayed.
	//The UIHandler should redraw the UI after receiving the msgWhatAfterCompletion message.
	public void loadAsync(Handler uiHandler, ArrayList<T> list, int msgWhatAfterCompletion) 
	{	
		mData = new LoadAsyncData(uiHandler, list, msgWhatAfterCompletion);
		mLoadAsyncHandler.sendEmptyMessage(LOAD);		
	}
	
	public void cancelLoadAsync() {
		loadAsyncCancelling = true;
		App.get().getUIHandler().removeMessages(UIHandler.UPDATE_ASYNC_ITEM);
		App.get().getUIHandler().removeMessages(UIHandler.UPDATE_ASYNC_ITEMS);
//		Log.e("inmind", "loadAsyncCancelling TRUE");
		mLoadAsyncHandler.sendEmptyMessage(CANCEL);
	}
	
	class LoadAsyncData{
		public Handler handler;
		public ArrayList<T> list;
		public int what;
		
		public LoadAsyncData(Handler handler, ArrayList<T> list, int what)
		{
			this.handler = handler;
			this.list = list;
			this.what = what;
		}
	}
	
	private class BackgroundLoadTask extends AsyncTask<T, T, T> {		
		WeakReference<Handler> uiHandler = null;
		int msgWhat = -1;
		
		public BackgroundLoadTask(Handler handler, int what)
		{
			uiHandler = new WeakReference<Handler>(handler);
			msgWhat = what;
		}
		
	    protected T doInBackground(T... objs) {	      
	        T item = objs[0];
//        	if (loadItemInParallel(item) && !loadAsyncCancelling){
//        		Message msg = new Message();
//        		msg.what = msgWhat;
//        		msg.obj = item;
//        		uiHandler.get().sendMessage(msg); //Send msg to notify UI to update       	
//        	}
	    	return null;
	    }	    
	}
	
	/**
	 * Override this method to determine whether an item should be passed to loadItemInParallel() to be processed in parallel.
	 * @param item The item passed to the filter.
	 * @return true if the item should be processed; false if this item should be ignored.
	 * 
	***/
	protected boolean filterItem(T item) {
		return false;
	}
	
	/** This method will be called in the background thread in parallel.
	 * @param item The item in the list to be processed by the background thread.
	 * @return True if the item is loaded into memory; False when the item is not loaded into memory.
	 * */ 
	protected boolean loadItemInParallel(T item) {
		return true;
	}
	
	Handler mLoadAsyncHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what)
			{
				case LOAD:
					if (mData == null)
						break;
					for (int i = 0 ; i < mData.list.size(); i++)
					{
						T item = mData.list.get(i);
						if (filterItem(item))
						{
							if (!launchAsyncTaskInParallel(item, true))
								break;
						}
					}
					break;
				case CANCEL:	
//					Log.e("inmind", "CANCEL received");
					if (tasks.isEmpty())
					{
						loadAsyncCancelling = false;
//						Log.e("inmind", "loadAsyncCancelling false");
						return;
					}
					for (AsyncTask task : tasks)
					{
						if (task != null && !task.isCancelled())
							task.cancel(true);
					}
					tasks.clear();
					loadAsyncCancelling = false;
//					Log.e("inmind", "loadAsyncCancelling false");
					break;
				default:
			}
		}		
	};
	/**	Use thread pool or real threads to back the BackgroundLoadTask.
	 * 	@return True to continue execution; false to discontinue execution due to the cancellation of the task.
	 * 	@param item The item to be processed.
	 * 	@param bThreadPool True to use thread pool. False to run in parallel.
	 * */
	private boolean launchAsyncTaskInParallel(T item, boolean bThreadPool)
	{
		try{
			//Use thread pool, so we jump to the thread pool directly.
			if (bThreadPool)
				throw(new RejectedExecutionException());
			
			//Parallel threads
			BackgroundLoadTask task = new BackgroundLoadTask(mData.handler, mData.what);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
			if (!loadAsyncCancelling)
				tasks.add(task);
			else
			{
				task.cancel(true);
				return false;
			}
		}
		catch(RejectedExecutionException e)
		{
			try{
				//Thread pool
				BackgroundLoadTask task = new BackgroundLoadTask(mData.handler, mData.what);
				task.execute(item);
				if (!loadAsyncCancelling)
					tasks.add(task);
				else
				{
					task.cancel(true);
					return false;
				}
			}catch (RejectedExecutionException ex)
			{
				ex.printStackTrace();
			}
		}
		return true;
	}
	
}
