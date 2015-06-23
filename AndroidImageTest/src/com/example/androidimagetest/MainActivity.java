package com.example.androidimagetest;

import java.io.File;
import java.util.LinkedList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;


public class MainActivity extends Activity implements DataListener {
	private LinkedList<Bitmap> mQueue = new LinkedList<Bitmap>();
	private static final int MAX_BUFFER = 15;
	private Bitmap mLastFrame;
	
	private ImageView mImageView;
	private Handler handler;
	
	public static File cache_dir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
	    setContentView(R.layout.surface_layout);
	    
	    SocketClient socketclient = new SocketClient();
	    socketclient.setOnDataListener(this);
	    
	    AudioClient audioclient = new AudioClient();
	    
	    
	    
	    cache_dir = getCacheDir();
	   
	    handler = new Handler();
	    
	    mImageView = (ImageView) findViewById(R.id.image_view);
	    
	    socketclient.start();
	    audioclient.start();
	    
	    
	}

	private void paint() {
		//Canvas tempCanvas = new Canvas();
		
	    //Draw the image bitmap into the canvas
	    //tempCanvas.drawBitmap(mLastFrame, 0, 0, null);
		synchronized (mQueue) {
        	if (mQueue.size() > 0) {
        		mLastFrame = mQueue.poll();
        	}	
        }
	    handler.post(new Runnable() {

			@Override
			public void run() {
				//mImageView.setImageDrawable(new BitmapDrawable(getResources(), mLastFrame));
				mImageView.setImageBitmap(mLastFrame);
			}
	    	
	    });
	    //Attach the canvas to the ImageView
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onDirty(Bitmap bufferedImage) {
		synchronized(mQueue) {
			if (mQueue.size() == MAX_BUFFER) {
        		mLastFrame = mQueue.poll();
        	}
			mQueue.add(bufferedImage);
		}
		paint();
	}

}
