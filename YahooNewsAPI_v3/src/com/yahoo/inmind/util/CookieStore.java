package com.yahoo.inmind.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class CookieStore {
	enum AccountType{
		YAHOO,
		INMIND
	};
	
	AccountType mType = AccountType.INMIND;
	
	private static final String COOKIE = "cookie";
	Context mCtx;
	String mCookies;
	String mCachedUserName;	
	
	public CookieStore(Context ctx)
	{
		this.mCtx = ctx;
	}
	
	public void saveLoginCookies(String url) {
		CookieSyncManager.getInstance().sync();
		String cookieStr = CookieManager.getInstance().getCookie(url);
		Log.e("inmind", "saveLoginCookies: getCookie: " + cookieStr);//FIXME: remove after debug
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(COOKIE, cookieStr); // value to store
		editor.commit();
	}

	public String getCookies() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		return prefs.getString(COOKIE, null);
	}
	
	public String getCurrentUserName(){
		if (mCachedUserName != null)
			return mCachedUserName;
		switch(mType){
			case YAHOO:
				return getYahooUserName();
			case INMIND:
				return getInMindUserName();
			default:
				return getYahooUserName();
		}
	}
	
	public String getYahooUserName(){
		if (mCachedUserName != null)
			return mCachedUserName;
		return getUserName(Pattern.compile("userid%3D([a-zA-Z0-9_-]+)%26sign"));
	}
	
	public String getInMindUserName(){
		if (mCachedUserName != null)
			return mCachedUserName;
		return getUserName(Pattern.compile(" id=([^;]+)(;|$)"));
	}
	
	public String getUserName(Pattern pat){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		String cookiestr = prefs.getString(COOKIE, null);
		if (cookiestr == null)
			return null;
//		Log.e("inmind", "getUserName(), cookieStr: " + cookiestr);
		Matcher mat = pat.matcher(cookiestr);
		if (mat.find())
		{
			mCachedUserName = mat.group(1);
			return mCachedUserName;
		}
		return null;
	}
	
	public void logout() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(COOKIE);
		editor.commit();
		mCachedUserName = null;
	}

	public String getInMindProfStr() {
		String out = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		String cookieStr = prefs.getString(COOKIE, null);
		if (cookieStr == null)
			return null;
		Pattern pat = Pattern.compile(" profile=\"([^;]+)\"(;|$)");
		Matcher mat = pat.matcher(cookieStr);
		if (mat.find())
		{
			out = mat.group(1);
		}
//		Log.e("inmind", "getInMindProfStr(): " + out);
		return out;
	}
}
