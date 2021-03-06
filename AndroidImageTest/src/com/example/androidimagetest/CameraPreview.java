package com.example.androidimagetest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    public static Camera mCamera;
    private static final String TAG = "camera";
    private Size mPreviewSize;
    private byte[] mImageData;
    public static LinkedList<byte[]> mQueue = new LinkedList<byte[]>();
    private static final int MAX_BUFFER = 5;
    private byte[] mLastFrame = null;
    private int mFrameLength;
    private int width = 320;
    private int height = 240;
    private byte[] jdata;

    @SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Parameters params = mCamera.getParameters();
        List<Size> sizes = params.getSupportedPreviewSizes();
        for (Size s : sizes) {
        	Log.i(TAG, "preview size = " + s.width + ", " + s.height);
        }
        
        for (int i: params.getSupportedPreviewFormats()) { 
        	Log.i(TAG, "preview formats supported are = " + i);
        }
        
        //params.setPreviewSize(640, 480); // set preview size. smaller is better
        params.setPreviewSize(width, height);
//        params.setPreviewSize(240, 320);
//        mCamera.setDisplayOrientation(90);
        params.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(params);
        
        mPreviewSize = mCamera.getParameters().getPreviewSize();
        Log.i(TAG, "preview size = " + mPreviewSize.width + ", " + mPreviewSize.height);
        
        int format = mCamera.getParameters().getPreviewFormat();
        mFrameLength = mPreviewSize.width * mPreviewSize.height * ImageFormat.getBitsPerPixel(format) / 8;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
        	Parameters parameters = mCamera.getParameters();

            mCamera.setDisplayOrientation(90);

            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
          return;
        }
        
        try {
            mCamera.stopPreview();
            resetBuff();
            
        } catch (Exception e){

        }

        try {
        	
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    public void setCamera(Camera camera) {
    	mCamera = camera;
    }
    
    public byte[] getImageBuffer() {
        synchronized (mQueue) {
			if (mQueue.size() > 0) {
				mLastFrame = mQueue.poll();
			}
    	}
        if (mQueue.size() == 0) {
        	//Log.d("ERRORCHECK", "QUEUE IS EMPTY");
        }
        return mLastFrame;
    }
    
    private void resetBuff() {
        synchronized (mQueue) {
        	mQueue.clear();
        	mLastFrame = null;
    	}
    }
    
    public int getPreviewLength() {
        return mFrameLength;
    }
    
    public int getPreviewWidth() {
    	return mPreviewSize.width;
    }
    
    public int getPreviewHeight() {
    	return mPreviewSize.height;
    }
    
    public void onPause() {
    	if (mCamera != null) {
    		mCamera.setPreviewCallback(null);
    		mCamera.stopPreview();
    	}
    	resetBuff();
    }
    
    private Camera.PreviewCallback mPreviewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
        	
        	if (camera.getParameters().getPreviewFormat() == ImageFormat.NV21) {// NV21
                // Convert to JPG
                YuvImage yuvimage = new YuvImage(data,
                        ImageFormat.NV21, width, height, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, width, height), 50, baos);
                jdata = baos.toByteArray();
//                Log.d("ERRORCHECK", "jdata size = " + jdata.length);
//                Log.d("ERRORCHECK", "data size = " + data.length);
            }
        	
        	synchronized (mQueue) {
    			if (mQueue.size() == MAX_BUFFER) {
    				mQueue.poll();
    			}
    			mQueue.add(jdata);
        	}
        }
    };
    
    private void saveYUV(byte[] byteArray) {

        YuvImage im = new YuvImage(byteArray, ImageFormat.NV21, mPreviewSize.width, mPreviewSize.height, null);
        Rect r = new Rect(0, 0, mPreviewSize.width, mPreviewSize.height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        im.compressToJpeg(r, 100, baos);

        try {
            FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/yuv.jpg");
            output.write(baos.toByteArray());
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
    
    private void saveRAW(byte[] byteArray) {
        try {
            FileOutputStream file = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/test.yuv"));
            try {
                file.write(mImageData);
                file.flush();
                file.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
