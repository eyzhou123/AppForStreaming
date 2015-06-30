package com.yahoo.inmind.reader;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.yahoo.inmind.browser.BaseBrowser;
import com.yahoo.inmind.browser.LoginBrowser;
import com.yahoo.inmind.cache.ImgLruCacher;
import com.yahoo.inmind.handler.DataHandler;
import com.yahoo.inmind.handler.UIHandler;
import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.i13n.I13N;
import com.yahoo.inmind.model.AsyncSource;
import com.yahoo.inmind.model.JsonItem;
import com.yahoo.inmind.model.Renderer;
import com.yahoo.inmind.model.Source;
import com.yahoo.inmind.model.slingstone.SlingstoneRenderer;
import com.yahoo.inmind.model.slingstone.SlingstoneSrc;

public class DrawerManager
{
	protected ArrayList<DrawerItem> mItems = new ArrayList<DrawerItem>();
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ReaderMainActivity mAct = null;
	private String pkgName = this.getClass().getSimpleName();
	
	private static int[] drawer_icons = {R.drawable.ic_launcher, R.drawable.login_square,
        R.drawable.assistant_square, R.drawable.news_square, R.drawable.both_square, R.drawable.camera_square };
	
	public DrawerManager(ReaderMainActivity act)
	{
		//Customize drawer items here
		mAct = act;
		
		//One DrawerItem corresponds to one fragment, and the fragment is created when the item is selected. 
		DrawerItem drawerItem = new DrawerItem();
		
		//The first item is Slingstone news labeled by the user name or the app_name 
		//if no user is present.
		//String user = App.get().getCookieStore().getYahooUserName();
		String user = App.get().getCookieStore().getCurrentUserName();
		drawerItem.name = (user == null? mAct.getString(R.string.app_name) : user);
		
		SlingstoneSrc ss = new SlingstoneSrc(act);
		ss.setDrawerManager(this);//We will use this reference to add user profile features as drawerItems later.
		
		//Register a handler here so Source::generateItemsFromProfile() will be called 
		//on a thread associated with the handler when the user profile is ready.
		ss.registerProfileReadyHandler(App.get().getDataHandler(), DataHandler.PROFILE_READY, ss);
		
		ImgLruCacher cache = new ImgLruCacher(mAct);//Enable cache
		drawerItem.srcs.add(ss.enableCache(cache));
		drawerItem.renderers.add(new SlingstoneRenderer(act).enableCache(cache));	
		addItem(drawerItem);
		
		//The second item is a login/logout button
		drawerItem = new DrawerItem();
		drawerItem.name = (user == null? mAct.getString(R.string.login) : mAct.getString(R.string.logout));
		if (user == null)//login
		{
			drawerItem.intent = new Intent(act, LoginBrowser.class);
			drawerItem.intent.putExtra(BaseBrowser.LAUCH_BROWSER_URL, LoginBrowser.loginUrl);
		}
		addItem(drawerItem);
		
		drawerItem = new DrawerItem();
        drawerItem.name = "Assistant View";
        addItem(drawerItem);

        drawerItem = new DrawerItem();
        drawerItem.name = "News View";
        addItem(drawerItem);

        drawerItem = new DrawerItem();
        drawerItem.name = "Both View";
        addItem(drawerItem);
        
        drawerItem = new DrawerItem();
        drawerItem.name = "WoZ View";
        addItem(drawerItem);
	}
	
	public String getDrawerTitle(int i) {
		return mItems.get(i).name;
	}

	protected void onCreateDrawer(Bundle savedInstanceState) {
		mDrawerList = (ListView) mAct.findViewById(R.id.news_left_drawer);
		mTitle = mDrawerTitle = mAct.getTitle();
	  	mDrawerLayout = (DrawerLayout) mAct.findViewById(R.id.news_drawer_layout);
	  	// set a custom shadow that overlays the main content when the drawer opens
	  	mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	  	mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	  	
	  	ArrayList<String> titles = new ArrayList<String>();
		for (DrawerItem it : mItems)
			titles.add(it.name);
		String [] drawerTitles = titles.toArray(new String[titles.size()]);
			
		// set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(mAct,
                R.layout.drawer_list_item, android.R.id.text1, drawerTitles));

	  	// ActionBarDrawerToggle ties together the the proper interactions
	  	// between the sliding drawer and the action bar app icon
	  	mDrawerToggle = new ActionBarDrawerToggle(
	  			mAct,                  /* host Activity */
	  			mDrawerLayout,         /* DrawerLayout object */
	  			R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
	  			R.string.drawer_open,  /* "open drawer" description for accessibility */
	  			R.string.drawer_close  /* "close drawer" description for accessibility */
	  			) 
	  	{
	  		public void onDrawerClosed(View view) {
	  			mAct.getActionBar().setTitle(mTitle);
	  			mAct.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	  			I13N.get().log(new Event(pkgName, "onDrawerClosed"));
            }

            public void onDrawerOpened(View drawerView) {
            	Log.d("DrawerManager", "Number of drawer items: " + Integer.toString(mDrawerList.getCount()));
                for (int index = 0; index < mDrawerList.getCount(); index++) {
                    View row = mDrawerList.getChildAt(index);
                    ImageView imageView = ((ImageView) row.findViewById(R.id.drawer_icon));
                    imageView.setImageResource(drawer_icons[index]);
                }
                
            	mAct.getActionBar().setTitle(mDrawerTitle);
            	mAct.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            	I13N.get().log(new Event(pkgName, "onDrawerOpened"));
            }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	public void onConfigurationChanged(Configuration newConfig) {
		// Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void syncState() {
		 mDrawerToggle.syncState();
	}

	public void setTitle(CharSequence title) {
		mTitle = title;
	}

	public void updateDrawerUserName() {
		DrawerItem item = mItems.get(0);
		//Get the user name and set as the first item in the drawer
		String user = App.get().getCookieStore().getCurrentUserName();
		item.name = (user == null? mAct.getString(R.string.app_name) : user);
		mItems.set(0, item);
		
		//Set the second item as "Login" or "Logout"
		item = mItems.get(1);
		item.name = (user == null? mAct.getString(R.string.login) : mAct.getString(R.string.logout));
		if (user == null)//login
		{
			item.intent = new Intent(mAct, LoginBrowser.class);
			item.intent.putExtra(BaseBrowser.LAUCH_BROWSER_URL, LoginBrowser.loginUrl);
		}
		else
		{
			item.intent = null;
		}
		mItems.set(1, item);
		
		postItemNamesToDrawer();
		mAct.setTitle(mAct.getCurrentFrag().getItem().name);
	}

	public void postItemNamesToDrawer() {
		if (mDrawerList != null)
		{
			ArrayList<String> titles = new ArrayList<String>();
			for (DrawerItem it : mItems)
				titles.add(it.name);
			String [] drawerTitles = titles.toArray(new String[titles.size()]);
				
			// set up the drawer's list view with items and click listener
	        mDrawerList.setAdapter(new ArrayAdapter<String>(mAct,
	                R.layout.drawer_list_item, android.R.id.text1, drawerTitles));
	        mDrawerList.invalidateViews();        
		}
	}
	
	   /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public final static int DRAWER_DEFAULT = 0;
    public final static int DRAWER_LOGIN = 1;
    public final static int DRAWER_LOGOUT = 2;    
    //This function is used to handle selection event in the drawer
    public void selectItem(int pos) {
    	
    	if (pos >= mItems.size())
    	{
    		Log.e("inmind", this.getClass().getSimpleName() + ".selectItem() pos Out Of Range!");
    		return;
    	}
    	
    	DrawerItem item = mItems.get(pos);
    	item.idx = pos;
    	
    	//This special case is to allow drawer items to launch activities,
    	//such as the Login button.
    	if (item.intent != null)
    	{
    		item.idx = 0;
    		mAct.setCurrentFrag(mItems.get(0).frag);
			mAct.startActivityForResult(item.intent, 0);//e.g. start Login activity
    		showDrawerSelectionAndClose(0);
    		return;
    	}
    	
    	if (item.name.equals(mAct.getString(R.string.logout)))
    	{	
    		item = mItems.get(0);
    		if (item.frag != null)
    			item.frag.onPause();
			mAct.setCurrentFrag(item.frag);
			I13N.get().logImmediately(new Event(this.getClass().getSimpleName(), "Logout"));
    		App.get().getCookieStore().logout();
    		updateDrawerUserName(); 		
    		item.bDirty = true;
    	}
    	
    	// Handle switching views (assistant, news, both)

        if (item.name.equals(mAct.getString(R.string.assistant_view))) {
            ReaderMainActivity.clicked_assistant_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }

        if (item.name.equals(mAct.getString(R.string.news_view))) {
            ReaderMainActivity.clicked_news_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }

        if (item.name.equals(mAct.getString(R.string.both_view))) {
            ReaderMainActivity.clicked_both_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }
        
        if (item.name.equals(mAct.getString(R.string.woz_view))) {
            ReaderMainActivity.woz_view();
            item.idx = 0;
            mAct.setCurrentFrag(mItems.get(0).frag);
            showDrawerSelectionAndClose(0);
            return;
        }
    	
    	Message msg = new Message();
    	msg.obj = item;
    	if (item.bDirty)//Refresh or new fragment.
    	{
    		msg.what = UIHandler.PREPARE_FOR_DOWNLOAD_DATA;
    		App.get().getUIHandler().sendMessage(msg);
    	}
    	else//Don't refresh data, just switch focus to the Fragment
    	{
    		msg.what = UIHandler.SHOW_FRAGMENT;
    		mAct.mUiHandler.sendMessage(msg);
    		
    		Message msgo = new Message();
			msgo.what = UIHandler.FOCUS_FRAGMENT;
			msgo.obj = item.frag;
			mAct.mUiHandler.sendMessage(msgo);
    	}
    	showDrawerSelectionAndClose(item.idx);
    }
    
    // update selected item and title, then close the drawer
	protected void showDrawerSelectionAndClose(int pos) {
		mDrawerList.invalidateViews();
		mDrawerList.setItemChecked(pos, true);		
		setTitle(mItems.get(pos).name);
		mDrawerLayout.closeDrawer(mDrawerList);
	}    
	
	protected boolean isDrawerOpen()
	{
		return mDrawerLayout.isDrawerOpen(mDrawerList);
	}
	
	protected boolean onOptionsItemSelected(MenuItem item)
	{
		return mDrawerToggle.onOptionsItemSelected(item);
	}	
	
	public ReaderMainActivity getActivity()
	{
		return mAct;
	}
	
	public class DrawerItem {
		public String name = null;
		public ArrayList<Source> srcs = new ArrayList<Source>();
		public ArrayList<Renderer> renderers = new ArrayList<Renderer>();
		public ArrayList<JsonItem> list = new ArrayList<JsonItem>();
		public ArrayList<JsonItem> bklist = new ArrayList<JsonItem>();
		public NewsListFragment frag = null;
		public boolean bDirty = true;
		public Intent intent = null;
		public int idx = -1;
		public DrawerManager parent;
		
		//must be executed in background thread
		public void loadSources()
		{	
			for (Source src : srcs)
			{	
				src.fetchData(bklist);
			}
		}
		
		public DrawerManager getParent()
		{
			return parent;
		}
		
		public void loadAsync(Handler UIhandler, int msgWhatAfterCompletion){
			cancelLoadAsync();
			for (Source src : srcs)
			{
				if (src instanceof AsyncSource)
				{
					((AsyncSource) src).loadAsync(UIhandler, list, msgWhatAfterCompletion);
				}
			}
		}
		
		public void cancelLoadAsync(){
			for (Source src : srcs)
			{
				if (src instanceof AsyncSource)
				{
					((AsyncSource) src).cancelLoadAsync();
				}
			}
		}
	}

	public void addItem(DrawerItem item) {
		item.parent = this;
		mItems.add(item);
	}

	public void prepareForExtension()
	{
		//Clean up all extended items
		for (int i = 2; i < mItems.size() ; i++)
		{
			DrawerItem item = mItems.get(i);
			item.cancelLoadAsync();
			if (item.frag != null)
				item.frag.clearAdapter();
		}
		mItems = new ArrayList<DrawerItem>(mItems.subList(0, 6));
	}
	
	public void notifyDatasetChanged() 
	{		
		((BaseAdapter)mDrawerList.getAdapter()).notifyDataSetChanged();
	}
}// public class DrawerManager
