package com.example.androidimagetest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class SocketClientAndroid extends Thread {
	private Socket mSocket;
	private CameraPreview mCameraPreview;
	private static final String TAG = "socket";
	private String mIP = "128.237.223.104";
	private int mPort = 8880;
	
	public SocketClientAndroid(CameraPreview preview, String ip, int port) {
	    mCameraPreview = preview;
	    mIP = ip;
	    mPort = port;
		start();
	}
	
	public SocketClientAndroid(CameraPreview preview) {
	    mCameraPreview = preview;
		start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		try {
			mSocket = new Socket();
			mSocket.connect(new InetSocketAddress(mIP, mPort), 10000); // hard-code server address
			BufferedOutputStream outputStream = new BufferedOutputStream(mSocket.getOutputStream());
			BufferedInputStream inputStream = new BufferedInputStream(mSocket.getInputStream());
			
			JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("type", "data");
            jsonObj.addProperty("length", mCameraPreview.getPreviewLength());
            jsonObj.addProperty("width", mCameraPreview.getPreviewWidth());
            jsonObj.addProperty("height", mCameraPreview.getPreviewHeight());
            
			byte[] buff = new byte[256];
			int len = 0;
            String msg = null;
            outputStream.write(jsonObj.toString().getBytes());
            outputStream.flush();
                        
            while ((len = inputStream.read(buff)) != -1) {
                msg = new String(buff, 0, len);
                
                // JSON analysis
                JsonParser parser = new JsonParser();
                boolean isJSON = true;
                JsonElement element = null;
                try {
                    element =  parser.parse(msg);
                    if (element == null) {
                    	Log.d("ERROR CHECK", "null message");
                    } else {
                    	Log.d("ERROR CHECK", "non-null message");
                    }
                }
                catch (JsonParseException e) {
                    Log.e(TAG, "exception: " + e);
                    isJSON = false;
                }
                if (isJSON && element != null) {
                    JsonObject obj = element.getAsJsonObject();
                    element = obj.get("state");
                    if (element != null && element.getAsString().equals("ok")) {
                        // send data
                        while (true) {
                        	Log.d("ERRORCHECK", "sending " + mCameraPreview.getImageBuffer().length);
                            outputStream.write(mCameraPreview.getImageBuffer());
                            outputStream.flush();
                            
                            if (Thread.currentThread().isInterrupted())
                                break;
                        }
                        
                        break;
                    }
                }
                else {
                    break;
                }
            }

			outputStream.close();
			inputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.e(TAG, e.toString());
		} 
		finally {
			try {
				mSocket.close();
				mSocket = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
