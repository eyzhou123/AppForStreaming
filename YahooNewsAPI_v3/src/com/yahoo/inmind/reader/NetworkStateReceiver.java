package com.yahoo.inmind.reader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("inmind", "Network connectivity change");

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting()) {
            	App.get().setConnected(true);
                Log.i("inmind", "Network " + ni.getTypeName() + " connected");
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            	App.get().setConnected(false);
                Log.d("inmind", "There's no network connectivity");
            }
        }
	}	
}
