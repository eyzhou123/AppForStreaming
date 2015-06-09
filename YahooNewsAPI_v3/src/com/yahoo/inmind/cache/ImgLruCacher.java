package com.yahoo.inmind.cache;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.yahoo.inmind.util.WebImgUtil;

public class ImgLruCacher {
	private static final int CACHE_SIZE = 8*1024*1024;
	public static final String IMG_CACHE_PATH = "/sdcard/InMind/Cache/";
	
	Context mCtx;
	WebImgUtil mImg;
	
	public ImgLruCacher(Context ct)
	{
		mCtx = ct;
		if (mCtx != null)
			mImg = new WebImgUtil(mCtx);
	}

	public static void purgeDiskCache(){
		File folder = new File(IMG_CACHE_PATH);
		if (!folder.exists())
			return;
		for (File file : folder.listFiles())
		{
			file.delete();
		}
	}
	
	@SuppressLint("NewApi")
	public LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(CACHE_SIZE)
	{

		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getByteCount();
		}

		@Override
		protected Bitmap create(String key) {
			Bitmap bmp = mImg.readBmp(key);
//			if (bmp != null)
//				Log.w("inmind", "create bmp ok: " + key);
//			else
//				Log.w("inmind", "create bmp failed: " + key);
			return bmp;
		}

		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			oldValue.recycle();
//			Log.w("inmind", "recycled bmp ok: " + key);
			super.entryRemoved(evicted, key, oldValue, newValue);
		}
		
	};
	
	public Bitmap get(String path)
	{
		if (mCache == null)
			return null;
		return mCache.get(path);
	}

	public boolean isAmple() {

		return mCache.size() < CACHE_SIZE * 0.8;
	}

	public void put(String path, Bitmap bmp) {
		mCache.put(path, bmp);
	}

	public int size() {
		return mCache.size();
	}
}
