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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class MainActivity extends Activity implements DataListener {
	private LinkedList<Bitmap> mQueue = new LinkedList<Bitmap>();
	private static final int MAX_BUFFER = 15;
	private Bitmap mLastFrame;
	
	private ImageView mImageView;
	private Handler handler;
	
	public static File cache_dir;
	
	private CameraPreview mPreview;
    private CameraManager mCameraManager;
    private boolean mIsOn = true;
    private SocketClientAndroid mThread;
    private Button mButton;
    public static String mIP = "128.237.223.104";
//    public static String mIP = "10.0.0.8";
    private int mPort = 8880;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
	    setContentView(R.layout.surface_layout);
	    
	    SocketClient socketclient = new SocketClient();
	    socketclient.setOnDataListener(this);
	    
	    AudioClient audioclient = new AudioClient();
////
	    mCameraManager = new CameraManager(this);
	    mPreview = new CameraPreview(this, mCameraManager.getCamera());
	    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
	    if (mIP == null) {
  		  mThread = new SocketClientAndroid(mPreview);
  	  	}
  	  	else {
  	  		mThread = new SocketClientAndroid(mPreview, mIP, 8880);
  	  	}
	    
////	    
	    cache_dir = getCacheDir();
	   
	    handler = new Handler();
	    
	    mImageView = (ImageView) findViewById(R.id.image_view);
	    
	    
	    
	    socketclient.start();
	    audioclient.start();
	    
	}

//	@Override
//	protected void onStart(){
//		super.onStart();
//		if (mIP == null) {
//	  		  mThread = new SocketClientAndroid(mPreview);
//  	  	}
//  	  	else {
//  	  		mThread = new SocketClientAndroid(mPreview, mIP, 8880);
//  	  	}
//	}

	// This paints the server's webcam stream in an android imageview
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
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mCameraManager.onResume();
		mPreview.setCamera(mCameraManager.getCamera());
	}
	
//	@Override
//	protected void onStop() {
//		super.onStop();
//		Log.d("ERRORCHECK", "Closing android socket client");
////		if (CameraPreview.mCamera != null) {
////			CameraPreview.mCamera.stopPreview();
////			CameraPreview.mCamera.release();
////			CameraPreview.mCamera = null;
////	    }
//		mThread.close();
//		mThread = null;
//	}
	
	
	private void closeSocketClient() {
		if (mThread == null)
			return;
		
		mThread.interrupt();
        try {
			mThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mThread = null;
	}

}
