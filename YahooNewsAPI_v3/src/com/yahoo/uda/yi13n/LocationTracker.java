package com.yahoo.uda.yi13n;
/**
 * Borrowed from YI13N
 * */
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.reader.App;

/** 
 * Singleton to encapsulate tracking and data provisioning for location data.  
 * Does not start tracking by default, must call {@link #startListening()} to initiate.
 * 
 * @author timt
 * @since 3.6 
 */

public class LocationTracker {
	private static LocationTracker _instance = null;
	private static LocationManager locationManager = null;
	private static LocationListener locationListener = null;
	
	private static boolean trackingRunning = false;
	private static Location lastLocation = null;
	
	private static final int SLEEP_TIME = 3000;
	/**
	 * This is only true when:
	 * 1. The first time.
	 * 2. The location is changed, and before someone calls annotateLocation().
	 * This flag is originally used to prevent annotateLocation() every time it gets called.
	 * However, I temporarily disabled this flag for analysis. (e.g. SELECT * FROM events WHERE location = ?) 
	 * 
	 * It can be switched on by unmark the first line of annotateLocation().
	 * */
	private static boolean locationDirty = true;
	
	private LocationTracker(){
		startLocationStarterThread();
	}

	
	private void startLocationStarterThread(){
		if (!locationLoggingEnabled()) { return; }
		if(!trackingRunning){
			new Thread(){
				@Override
				public void run(){
					boolean runThread = true;
					while(runThread){
						locationManager = (LocationManager) App.get().getSystemService(Context.LOCATION_SERVICE);
						if(okToTrack() && locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
							startListening();
							runThread = false;
							Log.e("inmind", "**QUIT WAITING**");
						}
						if (runThread)
							Log.e("inmind", "waiting for okToTrack() && locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)");
						if(!trackingRunning){ // only sleep if we're not location tracking
							try{
								Thread.sleep(SLEEP_TIME);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
				}
			}.start();
		}
	}
	/**
	 * Start listening to the respective provider
	 */
  @TargetApi(9)
	protected synchronized void startListening(){
		if (!locationLoggingEnabled()) {
			Log.e ("inmind", "LOCATION LOGGING DISABLED - no listener");
			return;
		}

	    if (Build.VERSION.SDK_INT < 9) {
	      return;
	    }

		if(trackingRunning || !okToTrack()){return;}
		Log.e("inmind", "LOCATION LOGGING ENABLED - start listener");
		trackingRunning = false;
		locationManager = (LocationManager) App.get().getSystemService(Context.LOCATION_SERVICE);
		if(locationManager == null || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			Log.e("inmind", "Oops! locationManager == null || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)");
			return;
		} 

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {	
		    	lastLocation = location;
		    	locationDirty = true;
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		};
		// Kirk Lieb says min Android API Level is now api level 10 as of Oct 2013
		Criteria criteria = new Criteria();
	    locationManager.getBestProvider(criteria, false);
		locationManager.requestSingleUpdate(criteria, locationListener, Looper.getMainLooper());
	}
	
	private Location getLastKnownGPS(){
		if(!hasGPSPermission()){
			return null;
		}
		String locationProvider = LocationManager.GPS_PROVIDER;
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		return lastKnownLocation;
	}
	
	private Location getLastKnownNetworkLocation(){
		if(!okToTrack()){return null;}
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		return lastKnownLocation;
	}
	
	public void annotateLocation(Event e){
		if(okToTrack())//&& locationDirty) 
		{
			locationDirty = false;
			e.annotateLocation();
		}
	} 
	
	private Location freshestLocation(Location a, Location b){
		if(a == null && b != null){return b;}
		if(a != null && b == null){return a;}
		if(a == null && b == null){return null;}
		if(a.getTime() > b.getTime()){
			return a;
		}
		return b; 
	}
	
	/**
	 * Fetch the last provided location data we received
	 * @return An instance of a Location representing the last location known.
	 */	
	public LocationContext getLastLocation(){
		if (!okToTrack()) {
			return new LocationContext(null);
		}
		Location lastGPSLocation = this.getLastKnownGPS();
		Location lastNetworkLocation = this.getLastKnownNetworkLocation();
		Location freshest = freshestLocation(lastGPSLocation, lastNetworkLocation);
		if (freshestLocation(freshest, lastLocation) == null) {
			Log.d("ERRORCHECK", "NULL");
		}
		return new LocationContext(freshestLocation(freshest, lastLocation));
	}
		
	/**
	 * Fetches a pointer to the LocationTracker instance
	 * @return The singleton instance.
	 */
	public static synchronized LocationTracker getInstance(){
		if(_instance == null){
			_instance = new LocationTracker();
		}
		return _instance;
	}

	private static boolean hasNetworkPermission(){
	    int res = App.get().checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION");
	    return (res == PackageManager.PERMISSION_GRANTED) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	private static boolean hasGPSPermission(){	
	    int res = App.get().checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION");
	    return (res == PackageManager.PERMISSION_GRANTED) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	//User's configuration could be used here. Right now we just return true.
	private static boolean locationLoggingEnabled() {
		return false; 
	}
	
	public boolean okToTrack(){
		return  (locationManager != null) && 
				(locationLoggingEnabled()) && 
				(hasNetworkPermission() || hasGPSPermission()) && 
				(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
	}
	public boolean getTrackingRunning(){return trackingRunning;}
}
