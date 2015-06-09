package com.yahoo.uda.yi13n;
/**
 * Borrowed from YI13N
 * */
import android.location.Location;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

public class LocationContext {
	// Keys used for storage as well as sending to YQL in the _loc object for the event
	private static final String LATITUDE = "lat";
	private static final String LONGITUDE = "lon";
	private static final String TIMESTAMP = "ts";
	private static final String HORIZONTAL_ACCURACY = "horacc";
	private static final String ALTITUDE = "altitude";
	private static final String SPEED = "speed";
	private static final String DIR_ANGLE = "dir_angle";
	
	// Actual values we're storing
	private double latitude = 0.0f;
	private double longitude = 0.0f;
	private long timestamp = 0L;
	private float horizontalAccuracy = 0.0f;
	private double altitude = 0.0f;
	private float speed = 0.0f;
	private float dir_angle = 0.0f;
	
	private LocationContext(double lat, double lng, long ts, float horizAccuracy, double alt, float sp, float dir){
		this.latitude = lat;
		this.longitude = lng;
		this.timestamp = ts;
		this.horizontalAccuracy = horizAccuracy;
		this.altitude = alt;
		this.speed = sp;
		this.dir_angle = dir;
	}
	
	public LocationContext(Location loc){
		this(loc.getLatitude(), loc.getLongitude(), loc.getTime() / 1000L, loc.getAccuracy(), 
				loc.getAltitude(), loc.getSpeed(), loc.getBearing());
	}
	
	public static LocationContext makeLocationContext(JSONObject o){
		double _lat = 0.0, _lng = 0.0, _alt = 0.0;
		long _ts = 0L;
		float _ha = 0.0f, _speed = 0.0f, _dir_angle = 0.0f;
		try{
			_lat = o.getDouble(LATITUDE);
			_lng = o.getDouble(LONGITUDE);
			_ts = o.getLong(TIMESTAMP);
			_ha = o.getLong(HORIZONTAL_ACCURACY);
			_alt = o.getDouble(ALTITUDE);
			_speed = o.getLong(SPEED);
			_dir_angle = o.getLong(DIR_ANGLE);
		}catch(JSONException e){
			e.printStackTrace();
		}
		return new LocationContext(_lat, _lng, _ts, _ha, _alt, _speed, _dir_angle);
	}
	
	public JSONObject toJSON(){
		JSONObject o = new JSONObject();
		try{
			o.put(LATITUDE, latitude);
			o.put(LONGITUDE, longitude);
			o.put(TIMESTAMP, timestamp);
			o.put(HORIZONTAL_ACCURACY, horizontalAccuracy);
			o.put(ALTITUDE, altitude);
			o.put(SPEED, speed);
			o.put(DIR_ANGLE, dir_angle);
		}catch(Exception e){
			e.printStackTrace();
		}
		return o;
	}
}
