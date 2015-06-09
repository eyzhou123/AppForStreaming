package com.yahoo.inmind.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.yahoo.inmind.reader.App;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;

public class MemUtil {
	private static int mMaxCacheSize = 0;
	
	//Borrowed from ImageCache
	public static int getLruCacheSize(double factor)
	{
		if (mMaxCacheSize == 0) {        
            int mMemClass = ((ActivityManager) App.get().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            mMaxCacheSize = (int) ((1024 * 1024 * mMemClass) * factor);
	    }
		return mMaxCacheSize;
	}
	
	public static double getMemUsage() {
		Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
		Debug.getMemoryInfo(memoryInfo);
		return memoryInfo.getTotalPss() / 1024.0;
	}
	
	public static void disableListViewCache(ListView lv) {
		lv.setAlwaysDrawnWithCacheEnabled(false);
		lv.setDrawingCacheEnabled(false);
		lv.setScrollingCacheEnabled(false);
		lv.setAnimationCacheEnabled(false);
		for (Method m : ViewGroup.class.getDeclaredMethods())
		{
			if (m.getName().equals("setChildrenDrawingCacheEnabled") /*|| m.getName().equals("setChildrenDrawnWithCacheEnabled")*/) {
				m.setAccessible(true);
				try {
					m.invoke(lv, false);
					Log.w("inmind", "setChildrenDrawingCacheEnabled(false) succeeds.");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
