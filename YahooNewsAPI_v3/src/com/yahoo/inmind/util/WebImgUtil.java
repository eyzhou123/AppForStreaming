package com.yahoo.inmind.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class WebImgUtil {
	private static final int THUMB_HEIGHT = 200;
	private static final int THUMB_WIDTH = 200;
	private Resources mRes; 
	
	public WebImgUtil(Context ctx)
	{
		mRes = ctx.getResources();
	}
	
	public Drawable getDrawableFromUrl(String url){		
		Drawable drawable = decodeSampledBitmapFromUrl(url, THUMB_WIDTH, THUMB_HEIGHT);
		return drawable;
	}
		
	public Drawable drawableFromBmp(Bitmap bmp)
	{
		if (bmp == null || bmp.isRecycled())
			return null;
		return new BitmapDrawable(mRes, bmp);
	}
	
	public Drawable decodeSampledBitmapFromUrl(String url, int reqWidth, int reqHeight) {
		Bitmap bmp = bmpFromUrl(url, reqWidth, reqHeight);
		if (bmp != null)
			return new BitmapDrawable(mRes, bmp);
		else
			return null;
	}

	public Bitmap bmpFromUrl(String url, int reqWidth, int reqHeight) {
		if (reqWidth == 0 || reqHeight == 0)
		{
			reqWidth = THUMB_WIDTH;
			reqHeight = THUMB_HEIGHT;
		}
		try {
			InputStream is = (InputStream) new URL(url).getContent();
		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(is, null, options);
		    
		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		   
		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    
		    is = (InputStream) new URL(url).getContent();
		    return BitmapFactory.decodeStream(is, null, options);
		    
	 	} catch (OutOfMemoryError e){
	 		Log.e("inmind", "WebImgUtil: stop loading images in bkg due to OOM.");
	 		e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Log.e("inmind", "MalformedUrl: " + url);
	 	} catch (FileNotFoundException e) {
	 		//Don't care
	 	} 
		catch (IOException e) {
			e.printStackTrace();
	 	}
		return null;
	}
	
	public boolean writeBmp(Bitmap bmp, String path)
	{
		return writeBmp(bmp, path, false);
	}
	
	public boolean writeBmp(Bitmap bmp, String path, boolean deletOnExit)
	{
		boolean bSuccess = false;
		createParentFolders(path);
		FileOutputStream out = null;
		try {
			File file = new File(path);
			file.createNewFile();
			if (deletOnExit)
				file.deleteOnExit();
			out = new FileOutputStream(file);	//"/sdcard/test.png"
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			bSuccess = true;
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		return bSuccess;
	}

	private void createParentFolders(String path) {
		int idx_slash = path.lastIndexOf('/');
		String folder = null;
		if (idx_slash != -1)
			folder = path.substring(0, idx_slash);
		if (folder != null)
		{
			File folderF = new File(folder);
			if (!folderF.exists())
				folderF.mkdirs();
		}
	}
	
	public Bitmap readBmp(String path)
	{
		try {
			return BitmapFactory.decodeFile(path);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return null;
	}
	
	private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;

	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps 
	        // height and width larger than the requested height and width.
	        
	        while (true) {
	        	int postHeight = halfHeight / inSampleSize;
	        	int postWidth = halfWidth / inSampleSize;
	        	if (postHeight > reqHeight && postWidth > reqWidth)	               
	        		inSampleSize *= 2;
	        	else if (postHeight >= 2048 || postWidth >= 2048)
	        		inSampleSize *= 2;
	        	else
	        		break;
	        }
	    }
	    
	    return inSampleSize;
	}
}
