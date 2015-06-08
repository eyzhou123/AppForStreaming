package com.example.androidimagetest;

import android.graphics.Bitmap;



public interface DataListener {
	public void onDirty(Bitmap bufferedImage);
}
