
package com.yahoo.inmind.i13n;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.yahoo.inmind.reader.App;
import com.yahoo.inmind.util.Crypto;
import com.yahoo.inmind.util.DevUtil;
import com.yahoo.inmind.util.JSON;
import com.yahoo.uda.yi13n.LocationTracker;

public class I13N {
	private static final String PUBLIC_KEY = "public.key";
	private static final int IDLE_FLUSH_TIMEOUT_MILLIS = 800;
	private static final int EVENT_QUEUE_MAX_COUNT = 50;
	private static final int LOG_LATEST_DELAY_MILLISECONDS = 300;
	
	private static String RSA_KEY_PATH = "/sdcard/";
	private static String I13N_SERVER_URL = "http://blondbeyond.corp.ne1.yahoo.com:4080/i13n";
	private static String UUIDTRACKER_SERVER_URL = "http://blondbeyond.corp.ne1.yahoo.com:4080/ut";
//	private final static String I13N_SERVER_URL = "http://10.73.212.57:4080/i13n";
//	private final static String I13N_SERVER_URL = "http://192.168.1.128:4080/i13n";
	
	static I13N mI13n = null;
	ConcurrentLinkedQueue<Event> list = null;
	Handler logHandler = null;
	Handler transportHandler = null;
	HandlerThread logTrd = new HandlerThread("logHandlerThread");
	HandlerThread transportTrd = new HandlerThread("transportLogHandlerThread");
	LocationTracker mLocTracker;
	
	public static String mSessionId = "No Registered Activity";
	
	private I13N()
	{
		prepareKeys();
		list = new ConcurrentLinkedQueue<Event>();
		logTrd.start();
		logHandler = new LogHandler(logTrd.getLooper());
		transportTrd.start();
		transportHandler = new TransportHandler(transportTrd.getLooper());
		mLocTracker = LocationTracker.getInstance();  
		readConfigParams();
	}

	private void prepareKeys() {
		try {
			FileInputStream fis = App.get().openFileInput(PUBLIC_KEY);
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException e)
		{
			try {
				FileOutputStream outputFile = App.get().openFileOutput(PUBLIC_KEY, App.get().MODE_PRIVATE);
				BufferedOutputStream bos = new BufferedOutputStream(outputFile);
				BufferedInputStream bis = new BufferedInputStream(App.get().getAssets().open(PUBLIC_KEY));
				byte buf[] = new byte[256];
				int read;
				while ((read = bis.read(buf, 0, 256)) != -1)
				{
					bos.write(buf, 0, read);
				}
				bos.flush();
				bos.close();
				bis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		RSA_KEY_PATH = App.get().getFilesDir().getAbsolutePath(); //"/data/data/com.yahoo.inmind.reader/files"; //
	}

	private void readConfigParams() {
		String configUrl = App.get().getConfig().getString("I13N_URL");
		if (configUrl != null)
			I13N_SERVER_URL = configUrl;
		
		configUrl = null;
		configUrl = App.get().getConfig().getString("UUIDTRACKER_URL");
		if (configUrl != null)
			UUIDTRACKER_SERVER_URL = configUrl;
	}
	
	public static I13N get(){
		if (mI13n == null)
			mI13n = new I13N();
		return mI13n;
	}
	
	public void log(Event evt)
	{
		Message msg = new Message();
		msg.what = LogHandler.LOG_EVENT;
		msg.obj = evt;
		logHandler.sendMessage(msg);
	}
	
	/**	
	 *  Log the latest event when there's no event after LOG_LATEST_DELAY_MILLISECONDS.
	 *  @param evt Event to be logged.
	 *  @param obj The object requesting to log.
	 * */	
	public void logLatest(Event evt, Object obj)
	{
		logLatestInternal(evt, obj, 0);
	}
	
	/**	
	 *  Log the latest event when there's no event after LOG_LATEST_DELAY_MILLISECONDS.
	 *  If the latest event is the same as its previous one, ignore it.
	 *  @param evt Event to be logged.
	 *  @param obj The object requesting to log.
	 * */	
	public void logLatestDif(Event evt, Object obj)
	{
		logLatestInternal(evt, obj, 1);
	}
	
	public void clearPreviousEvts(Object obj)
	{
		logHandler.removeMessages(LogHandler.LOG_LATEST_BASE + BatchIDManager.getBatchID(obj));
	}
	
	private void logLatestInternal(Event evt, Object obj, int iDuplicate) {
		Message msg = new Message();
		msg.what = LogHandler.LOG_LATEST_BASE + BatchIDManager.getBatchID(obj);
		msg.obj = evt;
		msg.arg1 = iDuplicate;//0: can log the same event 1: can't
		logHandler.removeMessages(msg.what);
		logHandler.sendMessageDelayed(msg, LOG_LATEST_DELAY_MILLISECONDS);
	}
	
	public void logImmediately(Event evt)
	{
		logInternal(evt);
	}
	
	private void logInternal(Event evt)
	{
		if (!App.get().getSettings().getI13NEnabled())
			return;
		
		finalizeEventInfo(evt);
		push(evt);
	}

	private void finalizeEventInfo(Event evt) {
		//userid
		evt.userid = App.get().getCookieStore().getCurrentUserName();
		if (evt.userid == null)
		{
			//Generate a random user id
			evt.userid = getSessionId();
		}
		
		//orientation
		if (App.get().getResources().getConfiguration().orientation
				== Configuration.ORIENTATION_LANDSCAPE)
			evt.orientation = "Landscape";
		else
			evt.orientation = "Portrait";
		
		evt.devid = DevUtil.getDeviceID();
		
		if (mLocTracker != null && App.get().getSettings().getLocTrackerEnabled())
			mLocTracker.annotateLocation(evt);
		else
			evt.location = null;
	}

	/**
	 * Send the logs to the server directly after IDLE_FLUSH_TIMEOUT_MILLIS milliseconds.
	 * */
	public void flushDelayed(){
		transportHandler.removeMessages(TransportHandler.UPLOAD_LOG_DELAYED);
		transportHandler.sendEmptyMessageDelayed(TransportHandler.UPLOAD_LOG_DELAYED, IDLE_FLUSH_TIMEOUT_MILLIS);	
	}
	
	public void cancelFlushDelayed(){
		transportHandler.removeMessages(TransportHandler.UPLOAD_LOG_DELAYED);
	}
	
	private void push(Event evt) {
		list.add(new Event(evt));
		if (list.size() >= EVENT_QUEUE_MAX_COUNT)
		{
			transportHandler.removeMessages(TransportHandler.UPLOAD_LOG);
			transportHandler.sendEmptyMessage(TransportHandler.UPLOAD_LOG);
		}
	}
	
	public class LogHandler extends Handler
	{
		public static final int LOG_EVENT = 0;
		public final static int LOG_LATEST_BASE = 100;
		private Event lastEvt;
		public LogHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Event evt = (Event) msg.obj;
			if (msg.what >= LOG_LATEST_BASE)//LOG_LATEST
			{	
				int logDifferent = msg.arg1;
				//Only log the different events
				//if (logDifferent == 1 && list.size() != 0 && list.peekLast().equals(evt))
				if (logDifferent == 1 && lastEvt != null && lastEvt.equals(evt))
					return;
				lastEvt = new Event(evt);
				logInternal(evt);
				Log.w("inmind", "event: " + evt.pkgName + ", " + evt.action);
				return;
			}
			
			switch(msg.what)
			{
				case LOG_EVENT:
					logInternal(evt);
					Log.w("inmind", "event: " + evt.pkgName + ", " + evt.action);
					break;
				default:
			}
		}
	}
	
	public class TransportHandler extends Handler
	{
		public static final int UPLOAD_LOG = 0;
		public static final int UPLOAD_LOG_DELAYED = 1;
		public static final int UPLOAD_UUID_EVENT_LOG = 2;
		public final static int LOG_LATEST_BASE = 100;
		Crypto cry;
		
		public TransportHandler(Looper looper) {
			super(looper);
			cry = new Crypto();
			//RSA_KEY_PATH = "/data/data/com/yahoo.inmind.test/files";
			cry.readPublicKey(RSA_KEY_PATH + "/public.key");
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
				case UPLOAD_LOG_DELAYED:
					//Wait for all messages to be pushed
					for (int i = 0 ; i < BatchIDManager.list.size() ; i++){
						while (logHandler.hasMessages(LOG_LATEST_BASE + i)){
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					logInternal(new Event("App", "PushToBackground"));
				case UPLOAD_LOG:
					{
						//Determine the size to be flushed to the server
						int flushSize = 0;
						if (list.size() >= EVENT_QUEUE_MAX_COUNT)
							flushSize = EVENT_QUEUE_MAX_COUNT;
						else
							flushSize = list.size();
						
						String payload = JSON.linkedList2Json(list, flushSize);
											
						if (payload == null){
							return;
						}
						Log.w(App.TAG, payload);
						
						//Key
						SecretKeySpec sks = cry.genAESKey();
						byte[] key = cry.encryptRSA(sks.getEncoded());
						String keyBase64 = Base64.encodeToString(key, Base64.DEFAULT);
						
						//Payload
						String payloadEncBase64 = cry.encryptAES(payload, sks);					
						
						String response = getHttpPostResponseString(I13N_SERVER_URL, payloadEncBase64, keyBase64);
						Log.w(App.TAG, "response: " + response);
						break;
					}
				case UPLOAD_UUID_EVENT_LOG:
					{
						UUIDEvent evt = (UUIDEvent) msg.obj;
						//Key
						SecretKeySpec sks = cry.genAESKey();
						byte[] key = cry.encryptRSA(sks.getEncoded());
						String keyBase64 = Base64.encodeToString(key, Base64.DEFAULT);
						
						//Payload
						String payloadEncBase64 = cry.encryptAES(evt.uuids, sks);					
						
						//UserID
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("userid", evt.userid);
						map.put("time", String.valueOf(evt.time.getTime()));
						
						String response = getHttpPostResponseString(UUIDTRACKER_SERVER_URL, payloadEncBase64, keyBase64, map);
						Log.w(App.TAG, "(u)response: " + response);
						break;
					}
				default:
			}
		}
		
		private String getHttpPostResponseString(String url, String payload, String key, HashMap<String, String> ...maps){
			StringBuilder total = new StringBuilder();
			BufferedReader r = null;
			
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();	
			params.add(new BasicNameValuePair("payload", payload));
			params.add(new BasicNameValuePair("key", key));
			//Add additional parameters
			if (maps != null && maps.length != 0)
			{	
				for (Entry<String, String> ent : maps[0].entrySet())
					params.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
			}
		
			try{
				HttpClient httpclient = new DefaultHttpClient();				

				HttpPost post = new HttpPost(url);
				post.setEntity(new UrlEncodedFormEntity(params));

				//Execute HTTP Post Request			    
				HttpResponse response = httpclient.execute(post);
			    r = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = null;
				while ((line = r.readLine()) != null) {
					total.append(line);				
				}
				
			}catch (ClientProtocolException e) {
				e.printStackTrace();
			}catch(OutOfMemoryError e){
				//e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					if (r != null)
						r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
					
			return total.toString();
		}
	}
	
	/**
	 * This function will be called every time the ReaderMainActivity is created.
	 * @param act The activity with its hash code to be used as the session ID.
	 * */
	public void registerSession(Activity act){
		mSessionId = String.valueOf(act.hashCode());
	}
	
	public String getSessionId() {
		return mSessionId;
	}
	
	public static class BatchIDManager {
		static ArrayList<String> list = new ArrayList<String>();;
		
		public static int getBatchID(Object obj){
			String key = obj.toString();
			int idx = list.indexOf(key);
			if (idx != -1)
				return idx;
			list.add(key);
			return list.size() - 1;
		}
	}

	//For logging UUIDs
	public void log(UUIDEvent uuidEvent) {
		if (!App.get().getSettings().getI13NEnabled())
			return;
		Message msg = new Message();
		msg.what = TransportHandler.UPLOAD_UUID_EVENT_LOG;
		msg.obj = uuidEvent;
		transportHandler.sendMessage(msg);
	}	
}
