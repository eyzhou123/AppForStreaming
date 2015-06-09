package com.yahoo.inmind.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
	Context mCtx;
	
	public NetworkUtil(Context ctx)
	{
		mCtx = ctx;
	}
	
	public boolean hasConnectivity(){
		ConnectivityManager connectivityManager = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager == null){return false;}
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		
        if (info==null || !info.isConnected()) {
                return false;
        }
        return true;
		
	}
}
