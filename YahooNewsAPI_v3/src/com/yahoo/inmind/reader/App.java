package com.yahoo.inmind.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;

import com.yahoo.inmind.cache.ImgLruCacher;
import com.yahoo.inmind.handler.DataHandler;
import com.yahoo.inmind.handler.UIHandler;
import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.i13n.I13N;
import com.yahoo.inmind.share.ShareHelper;
import com.yahoo.inmind.util.CookieStore;
import com.yahoo.inmind.util.NetworkUtil;

public class App extends Application{
	public final static String TAG = "inmind";
	static App mApp;
	
	LayoutInflater mInflator;
	CookieStore mCs;
	
	HandlerThread trd = new HandlerThread("JSONRetrievalTrd");
	DataHandler mDataHandler;
	private UIHandler mUiHandler;
    private boolean bIsConnected = true;
    private Config mConfig;
    private Settings mSettings;
	private ShareHelper mShareHelper;
    
	@Override
	public void onCreate() {		
		super.onCreate();
		mApp = this;
		bIsConnected = new NetworkUtil(mApp).hasConnectivity();
		
		ImgLruCacher.purgeDiskCache();
		
		trd.start();
		mDataHandler = new DataHandler(trd.getLooper());
		
		
		mInflator = (LayoutInflater) mApp.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCs = new CookieStore(this);		
		
		mConfig = new Config();
		mConfig.loadConfig();
		
		mSettings = new Settings();	
		mShareHelper = new ShareHelper();
	}

	public ShareHelper getShareHelper(){
		return mShareHelper;
	}
	
	public DataHandler getDataHandler()
	{
		return mDataHandler;
	}
	
	public UIHandler getUIHandler()
	{
		return mUiHandler;
	}
	
	public static App get()
	{
		return mApp;
	}
	
	public CookieStore getCookieStore()
	{
		return mCs;
	}
	
	public boolean isConnected() {
		return bIsConnected;
	}

	public void setConnected(boolean bIsConnected) {
		this.bIsConnected = bIsConnected;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		I13N.get().log(new Event(this.getClass().getSimpleName(), "onConfigurationChanged"));
	}

	public void registerUIHandler(UIHandler uiHandler) {
		mUiHandler = uiHandler;
	}	
	
	public Config getConfig(){
		return mConfig;
	}
	
	public class Config{
		Properties mProp = new Properties();
		
		public String getString(String key)
		{
			return (String) mProp.get(key);
		}
		
		public void loadConfig() {
			AssetManager am = getAssets();
			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();
			try {
				br = new BufferedReader(new InputStreamReader(am.open("config.xml")));
				String line;
				while ( (line = br.readLine()) != null)
				{
					sb.append(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				try {
					if (br != null)
						br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			JSONParser parser = new JSONParser();
			JSONObject jobj;
			try {
				jobj = (JSONObject) parser.parse(sb.toString());
				for (Object key : jobj.keySet())
				{
					mProp.put(key, jobj.get(key));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Settings getSettings(){
		return mSettings;
	}
	
	//Configurable SharedPreferences
	public class Settings{
		
		private SharedPreferences mPref;
	    private String mI13nEnabledKey;
	    private String mLocEnabledKey;
		private String mFlipViewEnabledKey;
	    
	    public Settings(){
	    	mI13nEnabledKey = getResources().getString(R.string.i13nEnabled);
			mLocEnabledKey = getResources().getString(R.string.trackLocEnabled);
			mFlipViewEnabledKey = getResources().getString(R.string.flipViewEnabled);
			mPref = PreferenceManager.getDefaultSharedPreferences(App.this);
	    }
	    
		public boolean getI13NEnabled(){
			return mPref.getBoolean(mI13nEnabledKey, true);
		}
		
		public boolean getLocTrackerEnabled(){
			return mPref.getBoolean(mLocEnabledKey, true);
		}

		public void setI13NEnabled(boolean b) {
			SharedPreferences.Editor editor = mPref.edit();
			editor.putBoolean(mI13nEnabledKey, b);
			editor.commit();
		}

		public boolean isFlipViewEnabled() {
			return mPref.getBoolean(mFlipViewEnabledKey, false);
		}
	}
}
