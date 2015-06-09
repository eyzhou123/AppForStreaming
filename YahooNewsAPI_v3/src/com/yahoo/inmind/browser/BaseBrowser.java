package com.yahoo.inmind.browser;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.sbjniapp.VhmsgWrapper;
import com.unity3d.player.UnityPlayer;
import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.i13n.I13N;
import com.yahoo.inmind.reader.BackableActivity;
import com.yahoo.inmind.reader.R;

public class BaseBrowser extends BackableActivity {
	WebView mWv;
	String cookieStr;
	private static UnityPlayer mUnityPlayer;
	private WebViewClient mWebviewClient;
	private WebChromeClient mWebChromeClient;
	public static final String LAUCH_BROWSER_URL = "url";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser);
		WebView wv = (WebView) findViewById(R.id.wv);
		mWv = wv;
		////////////////////////////////////////////////
		
//        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame);
//        mUnityPlayer = new UnityPlayer(this);
//        int glesMode = mUnityPlayer.getSettings().getInt("gles_mode", 1);
//        mUnityPlayer.init(glesMode, false);
//        
//        RelativeLayout layoutRight = (RelativeLayout) inflate.inflate(
//                R.layout.fragment_main, null);
//
//            RelativeLayout.LayoutParams relParam = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT);
//        
      //  RelativeLayout.LayoutParams relParam = new RelativeLayout.LayoutParams(750,640);
           
            //frameLayout.addView(layoutRight);

		
		/////////////////////////////////////////////////
		initExtraFunctions(wv);
		
		Intent intent = getIntent();
		loadUrl(intent.getStringExtra(LAUCH_BROWSER_URL));
		
		Event evt = getEvent();
		evt.uuid = intent.getStringExtra("uuid");
		setEvent(evt);
		
		setConfigCallback((WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
	}
	
	private void loadUrl(String url){
		mWv.loadUrl(url);
		System.gc();
		I13N.get().log(new Event(getEvent()).setAction("load url: " + url));
	}
	
	protected void initExtraFunctions(WebView wv) {
		initWevViewClient(wv);//Prevent WebView from opening a browser
		initProgressBar(wv);//Show progress bar
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(true);
	}
	
	protected void initWevViewClient(WebView wv) {
		mWebviewClient = new WebViewClient()       
        {	
			 @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) 
            {	 
				 loadUrl(url);
            	 return true;
            }
        };
		wv.setWebViewClient(mWebviewClient);
	}

	protected void initProgressBar(WebView wv) {
		final ProgressBar Pbar;
		Pbar = (ProgressBar) findViewById(R.id.pB1);
		mWebChromeClient = new WebChromeClient() {
			
			public void onProgressChanged(WebView view, int progress) 
	        {
				if(progress < 100 && Pbar.getVisibility() == ProgressBar.GONE)
				{
					Pbar.setVisibility(ProgressBar.VISIBLE);                    
				}
		        Pbar.setProgress(progress);
		        if(progress == 100) 
		        {
		            Pbar.setVisibility(ProgressBar.GONE);                   
		        }
	        }
			
		};
		wv.setWebChromeClient(mWebChromeClient);
	}
	
	public void setConfigCallback(WindowManager windowManager) {
	    try {
	        Field field = WebView.class.getDeclaredField("mWebViewCore");
	        field = field.getType().getDeclaredField("mBrowserFrame");
	        field = field.getType().getDeclaredField("sConfigCallback");
	        field.setAccessible(true);
	        Object configCallback = field.get(null);

	        if (null == configCallback) {
	            return;
	        }

	        field = field.getType().getDeclaredField("mWindowManager");
	        field.setAccessible(true);
	        field.set(configCallback, windowManager);
	    } catch(Exception e) {
	    }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (this.getClass() != LoginBrowser.class)
		{
			
		}
		
		if (mWebviewClient != null)
			mWebviewClient = null;
		if (mWebChromeClient != null)
			mWebChromeClient = null;
		
		if (mWv == null)
			return;
		
		mWv.stopLoading();
		mWv.clearCache(true);
		mWv.clearView();
		mWv.freeMemory();

		ViewGroup vg = (ViewGroup)(mWv.getParent());
		vg.removeView(mWv);
		mWv.destroy();
		System.gc();
		mWv = null;
		setConfigCallback(null);
	}

}
