package com.yahoo.inmind.share;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.yahoo.inmind.i13n.Event;
import com.yahoo.inmind.i13n.I13N;
import com.yahoo.inmind.reader.App;
import com.yahoo.inmind.reader.R;

/**
 * Codes are borrowed from DoublePlay
 * */
public class ShareHelper {
	private static final String FACEBOOK_APP_NAME = "Facebook";
	private static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
    private static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";
    private static final String TUMBLR_PACKAGE_NAME = "com.tumblr";
    private static final String TWITTER_SHARE_URL = "https://twitter.com/intent/tweet?text=%s&url=https://yho.com/mailtwt";
    private static final String FACEBOOK_SHARE_URL = "https://www.facebook.com/dialog/feed?app_id=765758106800669&display=popup&link=%s&redirect_uri=https://facebook.com";
    private static final String TUMBLR_SHARE_URL = "https://play.google.com/store/apps/details?id=com.tumblr";
    
    private ComponentName facebookComponent;
    private ComponentName twitterComponent;
    private ComponentName tumblrComponent;
	private String mPkgName;
    
    public ShareHelper()
    {
    	mPkgName = this.getClass().getSimpleName();
    	getComponentNames();
    }
    
    private boolean isAppOnDevice(String appName) 
    {
        try {
            App.get().getPackageManager().getApplicationInfo(appName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    public void launchBrowserForShare(Context ctx, String url) {
        Uri uri = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        ctx.startActivity(launchBrowser);
    }
    
    private String buildUrlForFacebook(String link) {
        return String.format(FACEBOOK_SHARE_URL, link);
    }
    
    private String buildUrlForTweet(String title) {
        return String.format(TWITTER_SHARE_URL, title);
    }
    
    private void getComponentNames(){
    	PackageManager pm = App.get().getPackageManager();
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> appList = pm.queryIntentActivities(shareIntent, 0);
        for (ResolveInfo info : appList) {
            String appName = info.loadLabel(pm).toString();
            ActivityInfo actInfo = info.activityInfo;
            if (null != actInfo && null != actInfo.applicationInfo
                    && null != actInfo.applicationInfo.packageName) {
                if (appName.equalsIgnoreCase(FACEBOOK_APP_NAME)) {
                    if (null == facebookComponent) {
                        facebookComponent = new ComponentName(actInfo.applicationInfo.packageName, actInfo.name);
                    }
                } else if (appName.equalsIgnoreCase("Twitter")) {
                    if (null == twitterComponent) {
                        twitterComponent = new ComponentName(actInfo.applicationInfo.packageName, actInfo.name);
                    }
                } else if (appName.equalsIgnoreCase("Tumblr")) {
                    if (null == tumblrComponent) {
                        tumblrComponent = new ComponentName(actInfo.applicationInfo.packageName, actInfo.name);
                    }
                } /*else if (actInfo.applicationInfo.packageName.equals(applicationContext.getPackageName())) {
                    // unified mail should always point to myself, even if a similar app (e.g. dogfood app) is
                    // installed
                    if (null == yahooMailComponent) {
                        yahooMailComponent = new ComponentName(applicationContext.getPackageName(), actInfo.name);
                    }
                } else if (appName.equalsIgnoreCase(ymailResourceName)) {
                    if (null == yahooMailComponent) {
                        yahooMailComponent = new ComponentName(actInfo.applicationInfo.packageName,
                                actInfo.name);
                    }
                } else if (appName.equalsIgnoreCase(defaultMailResourceName)) {
                    if (null == defaultMailComponent) {
                        defaultMailComponent = new ComponentName(actInfo.applicationInfo.packageName,
                                actInfo.name);
                    }
                }*/
            }//if (null != actInfo && null != actInfo.applicationInfo
        }//for (ResolveInfo info : appList) {
    }
    
    public enum Type{
    	Facebook, Twitter, Tumblr, More
    };
    
    public void share(Type type, Context ctx/*For launching another activity*/, 
    		String title, String summary, String link, String uuid)
    {
    	String pkgName = null;
    	String browserUrl = null;
    	ComponentName cpName = null;
    	
    	I13N.get().log(new Event(mPkgName, "Share to " + type.toString() + ": " + uuid));
    	
    	switch(type)
    	{
    		case Facebook:
    			pkgName = FACEBOOK_PACKAGE_NAME;
    			browserUrl = buildUrlForFacebook(link);
    			cpName = facebookComponent;
    			break;
    		case Twitter:
    			pkgName = TWITTER_PACKAGE_NAME;
    			browserUrl = buildUrlForTweet(title);
    			cpName = twitterComponent;
    			break;
    		case Tumblr:
    			pkgName = TUMBLR_PACKAGE_NAME;
    			browserUrl = TUMBLR_SHARE_URL;
    			cpName = tumblrComponent;
    			break;
    		
    		case More:
    		default:
    			Intent sendIntent = new Intent();
    			sendIntent.setAction(Intent.ACTION_SEND);
    			sendIntent.putExtra(Intent.EXTRA_TEXT, link + "\n\n" + summary);
    			sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
    			sendIntent.setType("text/plain");
    			ctx.startActivity(Intent.createChooser(sendIntent, App.get().getResources().getText(R.string.send_to)));
    			return;
    	}
    	
    	Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        
        if (!isAppOnDevice(pkgName)) 
        {
        	launchBrowserForShare(ctx, browserUrl);
        	return;
        }
        
        i.setComponent(cpName);
        
        if (null != summary) 
        {
            i.putExtra(Intent.EXTRA_TEXT, link + "\n\n" + summary);
        } 
        else 
        {
            i.putExtra(Intent.EXTRA_TEXT, link);
        }
        
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        ctx.startActivity(i);
    }
   
}
