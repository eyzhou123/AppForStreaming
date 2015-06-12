package com.yahoo.inmind.reader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
//import android.util.Log;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;


public class SocketClient extends Thread {
	private Socket mSocket;
	//private CameraPreview mCameraPreview;
	
	private DataListener mDataListener;
	private BufferManager mBufferManager;
	private static final String TAG = "socket";
	//private String mIP = "10.0.0.8";
	//private String mIP = "128.237.223.104";
	private String mIP = "128.237.218.26";
	int width;
	int height;
	private int mPort = 8888;
	
//	public SocketClient(CameraPreview preview, String ip, int port) {
//	    mCameraPreview = preview;
//	    mIP = ip;
//	    mPort = port;
//		start();
//	}
//	
//	public SocketClient(CameraPreview preview) {
//	    mCameraPreview = preview;
//		start();
//	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		try {
			ByteArrayOutputStream byteArray = null;
			mSocket = new Socket();
			Log.d("ERRORCHECK", "creating socket");
			mSocket.connect(new InetSocketAddress(mIP, mPort), 0); // hard-code server address
			BufferedOutputStream outputStream = new BufferedOutputStream(mSocket.getOutputStream());
			BufferedInputStream inputStream = new BufferedInputStream(mSocket.getInputStream());
			
			byte[] buff = new byte[256];
			byte[] imageBuff = null;
			byte[] length_buff = new byte[4];
			int len = 0;
			String msg = null;

			if (byteArray != null)
				byteArray.reset();
			else
				byteArray = new ByteArrayOutputStream();
			
			// read msg
			while ((len = inputStream.read(buff)) != -1) {
				msg = new String(buff, 0, len);
				// JSON analysis
				JsonParser parser = new JsonParser();
				boolean isJSON = true;
				JsonElement element = null;
				try {
					element =  parser.parse(msg);
				}
				catch (JsonParseException e) {
					System.out.println("exception: " + e);
					isJSON = false;
				}
				if (isJSON && element != null) {
					JsonObject obj = element.getAsJsonObject();
					element = obj.get("type");
					if (element != null && element.getAsString().equals("data")) {
						element = obj.get("length");
						int length = element.getAsInt();
						element = obj.get("width");
						width = element.getAsInt();
						element = obj.get("height");
						height = element.getAsInt();
						
						//Log.d("ERRORCHECK", "imageBuff length: " + length);
						imageBuff = new byte[length];
						mBufferManager = new BufferManager(length, width, height);
						mBufferManager.setOnDataListener(mDataListener);
						break;
					}
				}
				else {
					byteArray.write(buff, 0, len);
					break;
				}
			}
            
			if (imageBuff != null) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("state", "ok");
				outputStream.write(jsonObj.toString().getBytes());
				outputStream.flush();
				
				// read image data
					
				while(true) {
					//read image buffer length
					int length_bytes_read = 0;
					int just_read;
					while (length_bytes_read < 4) { // 4 bytes to an int
						just_read = inputStream.read(length_buff, length_bytes_read, 4 - length_bytes_read);
						length_bytes_read += just_read;
					}
					int updated_length = bytesToInt(length_buff);
					imageBuff = new byte[updated_length];
					
					// read image 
					int image_bytes_read = 0;
					while (image_bytes_read < updated_length) {
						just_read = inputStream.read(imageBuff, image_bytes_read, updated_length - image_bytes_read);
						image_bytes_read += just_read;
					}
					
					BitmapFactory.Options opt = new BitmapFactory.Options();
    				opt.inPurgeable = true;
    				opt.inDither = true;
    				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
    				
    				// convert byte array to bitmap
					Bitmap b = BitmapFactory.decodeByteArray(imageBuff, 0, imageBuff.length, opt);
					mDataListener.onDirty(b);
					
				}
				
			}

			if (mBufferManager != null) {
				mBufferManager.close();
			}
			

			
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
	
	public int bytesToInt(byte[] int_bytes) throws IOException {
		return ByteBuffer.wrap(int_bytes).getInt();
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
	
	public void setOnDataListener(DataListener listener) {
		mDataListener = listener;
	}
}