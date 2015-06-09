package com.yahoo.inmind.model.slingstone;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;

import com.yahoo.inmind.cache.ImgLruCacher;
import com.yahoo.inmind.i13n.I13N;
import com.yahoo.inmind.i13n.UUIDEvent;
import com.yahoo.inmind.model.AsyncSource;
import com.yahoo.inmind.model.JsonItem;
import com.yahoo.inmind.model.NewsItem;
import com.yahoo.inmind.reader.App;
import com.yahoo.inmind.reader.DrawerManager;
import com.yahoo.inmind.reader.DrawerManager.DrawerItem;
import com.yahoo.inmind.util.JSON;
import com.yahoo.inmind.util.WebImgUtil;

public class SlingstoneSrc extends AsyncSource<NewsItem> {
	
	//Constants of JSON paths for properties of user profile
	private static final String YAHOO_USER_PROFILE_PATH = "yahoo-coke:*/yahoo-coke:debug-scoring/feature-response/result";
	private static final String USER_GENDER = "USER_GENDER";
	private static final String USER_AGE = "USER_AGE";
	private static final String POSITIVE_DEC_YCT = "POSITIVE_DEC YCT";
	private static final String POSITIVE_DEC_WIKIID = "POSITIVE_DEC WIKIID";
	private static final String CAP_YCT_ID = "CAP_YCT_ID";
	private static final String CAP_ENTITY_WIKI = "CAP_ENTITY_WIKI";
	
	//Constants of JSON paths for properties of news content
	private static final String YAHOO_COKE_STREAM_ELEMENTS = "yahoo-coke:stream/elements";
	private static final String TITLE = "title";
	private static final String UUID = "uuid";
	private static final String EXPLAIN_REASON = "explain/reason";
	private static final String SNIPPET_URL = "snippet/url";
	private static final String SCORE = "score";
	private static final String CAP_FEATURES = "cap_features";
	private static final String RAW_SCORE_MAP = "raw_score_map";
	private static final String PUBLISHER = "publisher";
	private static final String SNIPPET_IMAGE_ORIGINAL_URL = "snippet/image/original/url";
	private static final String SNIPPET_SUMMARY = "snippet/summary";
	
	//Constants for constructing the url for retrieving the JSON
	private static final String p13nProto = "http";
	private static final String p13nHost = "any-ts.cpu.yahoo.com:4080";
	private static final String p13nPath = "/score/v9/homerun/en-US/unified/ga";
	private static final String p13nParam = "debug=true&today.region=remove&Cookie=cookiejar&snippet=true&cap_summary=true&snippet_count=170";
	private static final String p13nurl = p13nProto + "://" + p13nHost + p13nPath + "?" + p13nParam;
	
	private static final String SS_FAKE_USER_PROFILE_PARAM_NAME = "profile";
	
	private Context mCtx;
	private DrawerManager mDm;
	private ImgLruCacher mCache;
	private WebImgUtil mImgUtil;
	
	public SlingstoneSrc(Context ctx) {
		super();
		this.mCtx = ctx;
		url = p13nurl;
		String configUrl = App.get().getConfig().getString("SS_URL");
		if (configUrl != null)
			url = configUrl;
		mImgUtil = new WebImgUtil(ctx);
	}
	
	public SlingstoneSrc enableCache(ImgLruCacher cache)
	{
		mCache = cache;
		return this;
	}
	
	//++Source
	//This function must be executed on the thread other than the UI thread.
	@Override
	public void fetchData(final ArrayList<JsonItem> list) {
		//Read cookies if available
		//String identStr = App.get().getCookieStore().getCookies();
		String identStr = App.get().getCookieStore().getInMindProfStr();
		
		JSON json = new JSON(url);
		if (identStr == null)
		{
			json.fetch();
			mProfile = null;
		}
		else
		{	
			//Unmark below to use Yahoo's profile
			//json.fetch(identStr);//identStr is a String of cookie(s)
			
			//Use the 'fake' user profile of Slingstone here.
			StringBuilder sb = new StringBuilder(json.getUrl());
			sb.append("&");
			sb.append(SS_FAKE_USER_PROFILE_PARAM_NAME);
			sb.append("=");
			try {
				sb.append(URLEncoder.encode(identStr, "UTF-8").replaceAll("%5C", ""));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			json.setUrl(sb.toString());
			json.fetch();
			
			generateUserProfile(json);
		}
		JSONArray elements = (JSONArray) json.getObjByPath(YAHOO_COKE_STREAM_ELEMENTS);	
		for (int i = 0 ; elements != null && i < elements.size() ; i++)
		{
			JSONObject jobj = (JSONObject) elements.get(i);
			
			NewsItem arti = new NewsItem();
			arti.idx = new Integer(i);
			arti.title = JSON.getProp(jobj, TITLE);			
			arti.uuid = JSON.getProp(jobj, UUID);
			arti.raw_score_map = replaceAll(JSON.getProp(jobj, RAW_SCORE_MAP), "[{}]", "");
			arti.publisher = JSON.getProp(jobj, PUBLISHER);
			arti.cap_features = replaceAll(JSON.getProp(jobj, CAP_FEATURES), "[{}]", "");			
			arti.summary = JSON.getProp(jobj, SNIPPET_SUMMARY);
			System.out.println("Summary: "+arti.summary);
			arti.imgUrl = JSON.getProp(jobj, SNIPPET_IMAGE_ORIGINAL_URL);
			arti.score = JSON.getProp(jobj, SCORE);
			arti.url = JSON.getProp(jobj, SNIPPET_URL);	
			arti.reason = JSON.getProp(jobj, EXPLAIN_REASON);	
			
			list.add(arti);
		}
		super.fetchData(list);//Call super at the end to signal the completion of user profile
		trackUnreadUUIDs(list);
	}

	/**
	 * Send all UUID to the I13N server for tracking unread behaviors.
	 * */
	private void trackUnreadUUIDs(ArrayList<JsonItem> list) {
		//1. to JSON
		StringBuilder sb = new StringBuilder("{\"UUIDs\":[");
		int cnt = 0;
		for (JsonItem ji : list)
		{
			if (cnt != 0)
				sb.append(", ");
			
			//{"2":"o23k12p3-123o1k-234234-234sdg234"},
			//{"3":"er34123d-123fkg-351235-ads231f23"}
			sb.append("{");
			NewsItem ni = (NewsItem) ji;
			sb.append("\"uuid\":\"");
			sb.append(ni.uuid);
			sb.append("\", \"idx\":");
			sb.append(String.valueOf(ni.idx));
			sb.append("}");
			cnt++;
		}
		sb.append("]}");
		
		//2. Send to I13N
		I13N.get().log(new UUIDEvent(App.get().getCookieStore().getCurrentUserName(),
				 sb.toString()));
	}

	public String replaceAll(String str, String pat, String rep)
	{
		if (str == null)
			return null;
		return str.replaceAll(pat, rep);
	}
	
	//Personal data begins here
	private void generateUserProfile(JSON json) {
		JSONObject jobjUser = (JSONObject) json.getObjByPath(YAHOO_USER_PROFILE_PATH);
	
		if (jobjUser != null)
		{	
			UserProfile profile = new UserProfile();
			fillProfile(jobjUser, CAP_ENTITY_WIKI, profile.mCapWiki);
			fillProfile(jobjUser, CAP_YCT_ID, profile.mCapYct);
			fillProfile(jobjUser, POSITIVE_DEC_WIKIID, profile.mDecWiki);
			fillProfile(jobjUser, POSITIVE_DEC_YCT, profile.mDecYct);
			Object tmp = JSON.getProp(jobjUser, USER_AGE);
			if (tmp != null)
				profile.put(USER_AGE, (String) tmp);
			tmp = JSON.getProp(jobjUser, USER_GENDER);
			if (tmp != null)
				profile.put(USER_GENDER, (String) tmp);
			mProfile = profile;
//			for (Entry<String, String> ent : mProfile.mCapWiki.entrySet())						
//				Log.i("inmind", "key:" + ent.getKey() + ", val: " + ent.getValue());
		}
		else
			mProfile = null;
	}
	
	//Data Thread
	@Override
	public void generateItemsFromProfile() {
		mDm.prepareForExtension();
		if (mProfile != null)
		{
			UserProfile profile = (UserProfile) mProfile;
			//Here we simply add CAP_ENTITIES_WIKI as filters into the drawer.
			for (Entry<String, String> ent : profile.mDecWiki.entrySet())
			{
				DrawerItem item = mDm.new DrawerItem();
				item.name = ent.getKey() + ":" + ent.getValue();
				item.srcs.add(new SlingstoneSrc(mCtx).enableCache(mCache).filter("&filter=(CAP_ENTITY_WIKI=" + ent.getKey() + ")"));
				item.renderers.add(new SlingstoneRenderer(mCtx).enableCache(mCache));
				mDm.addItem(item);
			}
		}
	}
	
	//UI Thread
	@Override
	public void showExtendedOptions() {
		mDm.postItemNamesToDrawer();		
	}
	//--Source
	
	//++AsyncSource
	@Override
	/**
	 * @return True pass it to loadItemInParallel(); False otherwise.
	 * */
	protected boolean filterItem(NewsItem art) {
		if (art == null || 
		   (art.drawable != null || art.imgPath != null) || //image is already loaded
		    art.imgUrl == null)		//image has no url
		{
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean loadItemInParallel(NewsItem art) {
		if (mCache == null)//Do not use cache, download images & load into memory directly
		{
			art.drawable = mImgUtil.getDrawableFromUrl(art.imgUrl);
			if (art.drawable != null && 
					art.drawable.getIntrinsicWidth() >= art.drawable.getIntrinsicHeight())
			{
				art.dimension = NewsItem.DIM_LANDSCAPE;
			}
			else
				art.dimension = NewsItem.DIM_PORTRAIT;
			return true;//tells the caller that refresh is necessary, since we really load images into memory
		}
		
		//1. Download the image
		Bitmap bmp = mImgUtil.bmpFromUrl(art.imgUrl, 0, 0);
		if (bmp == null)
			return false;
		if (bmp.getWidth() >= bmp.getHeight())
			art.dimension = NewsItem.DIM_LANDSCAPE;
		else
			art.dimension = NewsItem.DIM_PORTRAIT;
		
		String bmpExportPath = ImgLruCacher.IMG_CACHE_PATH + art.uuid;
		//2. Cache to disk
		if (mImgUtil.writeBmp(bmp, bmpExportPath))
			art.imgPath = bmpExportPath;
		
		//3. Cache to memory when cache is still ample
		if (mCache.isAmple())
		{
			mCache.put(art.imgPath, bmp);
		}
		else
		{
			bmp.recycle();
		}
		return true;
	}
	//--AsyncSource
	
	public void setDrawerManager(DrawerManager drawerManager) {
		mDm = drawerManager;	
	}
}
