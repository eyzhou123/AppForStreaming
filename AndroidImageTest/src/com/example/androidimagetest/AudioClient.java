package com.example.androidimagetest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
//import android.util.Log;



import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;


public class AudioClient extends Thread {
	private Socket mSocket;
	
	private DataListener mDataListener;
	private BufferManager mBufferManager;
	private static final String TAG = "socket";
	//private String mIP = "10.0.0.8";
	//private String mIP = "128.237.223.104";
	private String mIP = "128.237.218.26";
	int width;
	int height;
	private int mPort = 8080;
	AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, 
			AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 1024, 
			AudioTrack.MODE_STREAM);
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		try {
			ByteArrayOutputStream byteArray = null;
			mSocket = new Socket();
			Log.d("ERRORCHECK", "creating audio socket");
			mSocket.connect(new InetSocketAddress(mIP, mPort), 0); // hard-code server address
			BufferedOutputStream outputStream = new BufferedOutputStream(mSocket.getOutputStream());
			BufferedInputStream inputStream = new BufferedInputStream(mSocket.getInputStream());
			
			
			byte[] audio_data = null;
			byte[] buff = new byte[256];
			byte[] imageBuff = null;
			byte[] length_buff = new byte[4];
			int len = 0;
			String msg = null;
			
			

			if (byteArray != null)
				byteArray.reset();
			else
				byteArray = new ByteArrayOutputStream();
			
			try {
				audioTrack.play();
			} catch (IllegalStateException e) {
				Log.d("ERRORCHECK", "Audiotrack play failed");
			}
			
			while(true) {
				//read image buffer length
				int length_bytes_read = 0;
				int just_read;
				while (length_bytes_read < 4) {
					just_read = inputStream.read(length_buff, length_bytes_read, 4 - length_bytes_read);
					length_bytes_read += just_read;
				}
				int updated_length = bytesToInt(length_buff);
				audio_data = new byte[updated_length];
				//Log.d("ERRORCHECK", "will read: " + updated_length + "bytes");
				int audio_bytes_read = 0;
				while (audio_bytes_read < updated_length) {
					just_read = inputStream.read(audio_data, audio_bytes_read, updated_length - audio_bytes_read);
					audio_bytes_read += just_read;
					Log.d("ERRORCHECK", "read: " + just_read + "bytes");
					try {
						Log.d("ERRORCHECK", "audio_data.length = " + audio_data.length);
						audioTrack.write(audio_data, 0, 1024);
						
					} catch(Throwable t){
				        Log.d("ERRORCHECK","Audiotrack write failed");
				    }
				}
				
			    
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.e(TAG, e.toString());
		} 
		finally {
			try {
				audioTrack.flush();
			    audioTrack.stop(); 
				audioTrack.release();
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
