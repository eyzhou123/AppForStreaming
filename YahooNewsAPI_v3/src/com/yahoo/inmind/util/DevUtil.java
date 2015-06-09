package com.yahoo.inmind.util;

import java.security.MessageDigest;
import java.util.UUID;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import java.util.Formatter;

import com.yahoo.inmind.reader.App;

//This class is borrowed from YI13N's YQLProxy
public class DevUtil {
	private static final String UNKNOWN = "unknown";
	private static final String ERROR_SHA1 = "error_sha1";
	private static final String UTF_8 = "UTF-8";
	private static final String UUID_KEY = "uuid_key";
	private static final String UUID_FILE = "uuid_file";

	private enum UUID_SRC{
		WIFI,
		SERIAL,
		ANDROID_ID,
		UUID
	}

	// this value will change, but init it to wifi for now
	private static UUID_SRC uuid_src = UUID_SRC.WIFI;
	
	
	private static boolean isEmpty(String s){
		return (s == null || s.equals("") || s.equalsIgnoreCase(UNKNOWN));
	}
	
	public static String toSHA1(String s){
		MessageDigest md = null;
		byte[] sha1hash = null;
		try{
			md = MessageDigest.getInstance("SHA-1");
			sha1hash = new byte[40];
			md.update(s.getBytes(UTF_8), 0, s.length());
		}
		catch(Exception e){
			return ERROR_SHA1;
		}
		sha1hash = md.digest();
		Formatter formatter = new Formatter();
        for (byte b : sha1hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
	} 
	
	private static String getAndroidID(){
		return Secure.getString(App.get().getContentResolver(), Secure.ANDROID_ID);
	} 
	/** 
	 * Try to build a unique identifier for the device with a cascading precedence of:
	 * <ol>
	 * 	<li> wifi MAC address
	 *  <li> android.os.Build.SERIAL
	 *  <li> Secure.ANDROID_ID
	 * </ol>
	 * @param yi13n
	 * @return Returns the md5 hash of whatever is non-null and non-empty based on the cascading precedence.
	 */
	
	/**
	 * Add key to log the way that we set the device id.
	 * Always log the UUID also.
	 */
	
	private static String cachedDeviceId;
	@TargetApi(9)
	public static String getDeviceID(){
		if (cachedDeviceId != null)
			return cachedDeviceId;
		WifiManager wifiMan = (WifiManager) App.get().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		String candidate = wifiInf.getMacAddress();
		uuid_src = UUID_SRC.WIFI;
		if(isEmpty(candidate)){
			if(Build.VERSION.SDK_INT >= 9){
				candidate = android.os.Build.SERIAL;
				uuid_src = UUID_SRC.SERIAL;
			} 
			if(isEmpty(candidate)){
				candidate = getAndroidID();
				uuid_src = UUID_SRC.ANDROID_ID;
			}
			if(isEmpty(candidate)){
				candidate = getUUID();
				uuid_src = UUID_SRC.UUID;
			}
		}
		// track the method used to generate the unique identifier
		cachedDeviceId = toSHA1(candidate);
		return cachedDeviceId;
	}
	
	private static String getUUID(){
		String uuid = "";
		SharedPreferences sharedPrefs = App.get().getSharedPreferences(UUID_FILE, Context.MODE_PRIVATE);
		uuid = sharedPrefs.getString(UUID_KEY, "");
		if(isEmpty(uuid)){
			uuid = UUID.randomUUID().toString();
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString(UUID_KEY, uuid);
			editor.commit();
		}
		return uuid;
	}	
}
