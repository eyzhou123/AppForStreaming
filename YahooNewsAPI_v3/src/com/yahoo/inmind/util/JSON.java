package com.yahoo.inmind.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSON {
	private static final String YAHOO_COM = "yahoo.com";
	String url = null;
	public String jsonStr = null;
	private JSONParser parser = null;
	
	public JSON(String uri)
	{
		this.url = uri;
		parser = new JSONParser();			
	}
	
	/**
	 * Return the HTTP Get result as String given these parameters.
	 * @param url The url for the Get request.
	 * @param cookies The cookie String to be appended to the request.
	 * @param domain The domain to be set into the cookie if any.
	 * @return String The result of the HTTP Get request.
	 * 
	 * */
	private String getHttpGetResponseString(String url, String cookies, String domain){
		StringBuilder total = new StringBuilder();
		BufferedReader r = null;
		try{
			HttpClient httpclient = new DefaultHttpClient();				
			HttpGet httpget = new HttpGet(url);
			
			HttpContext localContext = new BasicHttpContext();
			
			if(cookies != null)
			{
				//Split the Cookie string into cookies, and put them into the HttpContext
				BasicCookieStore cookieStore = new BasicCookieStore();
				StringTokenizer st = new StringTokenizer(cookies, "[;]");
				while (st.hasMoreElements())
				{	
					String ele = (String) st.nextElement();
					int splitterIdx = ele.indexOf('=');
					
					String key = ele.substring(0, splitterIdx);
					String val = ele.substring(splitterIdx + 1, ele.length());
					
					BasicClientCookie cookie = new BasicClientCookie(key, val);
					if (domain != null)
						cookie.setDomain(domain);					
					cookie.setSecure(false);
					cookie.setVersion(0);
					cookieStore.addCookie(cookie);	
				}
				localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
			}
			//Execute HTTP Post Request			    
			HttpResponse response = httpclient.execute(httpget, localContext);
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
		}catch(Exception e)
		{
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
	
	public void fetch()
	{
		jsonStr = getHttpGetResponseString(url, null, YAHOO_COM);
	}
	
	//Fetch the JSON located at the url
	public void fetch(String cookies)
	{
		jsonStr = getHttpGetResponseString(url, cookies, YAHOO_COM);		
	}
	
	//Get the property named as prop of the JSONObject obj
	public static String getProp(JSONObject obj, String prop)
	{
		if (!prop.contains("/"))
		{
			Object target_obj = obj.get(prop);
			if (target_obj == null)
				return prop + "is null";
			else
				return target_obj.toString();	
		}
		
		StringTokenizer st = new StringTokenizer(prop, "/");
		Object tmpObj = obj;
		while (st.hasMoreElements())
		{
			String childName = (String) st.nextElement();
			tmpObj = ((JSONObject) tmpObj).get(childName);
			if (tmpObj == null)
				return null;			
		}
		
		return tmpObj.toString();
	}
	
	//Get the Object representing the JSONObject/JSONArray specified by the path
	public Object getObjByPath(String path)
	{
		if (jsonStr == null)
			return null;
		
		StringTokenizer st = new StringTokenizer(path, "/");
			
		try {
			
			String json = jsonStr;			
			Object obj = parser.parse(json);
			while (st.hasMoreElements())
			{
				String childName = (String) st.nextElement();
				Object target_obj = ((JSONObject) obj).get(childName);
				if (target_obj == null)
					return null;		
				obj = target_obj;
			}
			return obj;
		} catch (ParseException e) {			
			e.printStackTrace();
		}
		return null;
	}

	
	/** Convert the items in the list to a JSON array of JSON objects, 
	 * 	and remove these objects from the list. Currently only String fields are supported.
	 *  @param list The list as the source of objects to be converted to JSON array.
	 *  @param cnt The number of objects to be converted, and deleted from the list. e.g.
	 *  	cnt = 10 means list[0] to list[9] will be converted and deleted. 
	 * */
	public static String linkedList2Json(ConcurrentLinkedQueue list, int cnt) {
		StringBuilder sb = new StringBuilder();
		//List sub = list..subList(0, cnt);
		LinkedList sub = new LinkedList();
		for (int i = 0 ; i < cnt ; i++)
			sub.add(list.poll());
		Field[] fields = null;
		Class cls = null;
		//create a json array
		sb.append("{\"objects\":[");
		boolean firstObject = true;
		for (Object obj : sub)
		{
			if (firstObject)
			{
				sb.append("{");
				firstObject = false;
			}
			else
			{
				sb.append(", {");
			}
			if (fields == null){
				fields = obj.getClass().getFields();
				cls = obj.getClass();
			}
			//do sth to retrieve each field value -> json property of json object
			//add to json array
			for (int i = 0 ; i < fields.length ; i++)
			{
				Field f = fields[i];
				jsonFromField(sb, obj, i, f);
			}
			sb.append("}");
		}
		sb.append("]}");
		sub.clear();
		return sb.toString();
	}
	
	private static void jsonFromField(StringBuilder sb, Object obj, int i, Field f) {
		if (f.getType().equals(String.class))
		{
			try {
				String key = f.getName();
				String val = String.valueOf(f.get(obj));
				if (i != 0)
				{
					sb.append(", ");
				}
				sb.append("\"");
				sb.append(key);
				sb.append("\"");
				sb.append(":");
				sb.append("\"");
				sb.append(val);
				sb.append("\"");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} 
		else if (f.getType().equals(Date.class))
		{
			try {
				String key = f.getName();
				Date val = (Date) f.get(obj);
				if (i != 0)
				{
					sb.append(", ");
				}
				sb.append("\"");
				sb.append(key);
				sb.append("\"");
				sb.append(":");
				//sb.append("\"");
				sb.append(val.getTime());
				//sb.append("\"");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		else if (f.getType().equals(org.json.JSONObject.class))
		{
			try {
				String key = f.getName();
				org.json.JSONObject jobj = (org.json.JSONObject) f.get(obj);
				if (jobj == null)
					return;
				String val = jobj.toString();
				if (i != 0)
				{
					sb.append(", ");
				}
				sb.append("\"");
				sb.append(key);
				sb.append("\"");
				sb.append(":");
				sb.append(val);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public interface ObjectReceiver<T>{
		public void receiveObject(T obj);
	}
	
	public class ObjectJsonFactory{
		private ObjectReceiver mRec;
		private String mJson;
		private Class mCls;
		private JSONParser mParser;
		
		public ObjectJsonFactory(String json, Class cls)
		{
			mJson = json;
			mCls = cls;
			mParser = new JSONParser();
		}
		
		public void setObjectSink(ObjectReceiver receiver)
		{
			mRec = receiver;
		}
		
		public void generateObjects(String arrayName, Object ...parent)
		{
			if (mRec == null)
				return;
			
			Object rawObj;
			try {
				rawObj = mParser.parse(mJson);
				if (rawObj == null)
					return;
				
				JSONObject obj = (JSONObject) rawObj;
				JSONArray arr = (JSONArray) obj.get(arrayName);
				int cnt = 0;
				if (arr != null)
				{
					for (Object jb : arr.toArray())//each object
					{
						JSONObject jo = (JSONObject) jb;
						mCls.getConstructors()[0].setAccessible(true);
						
						Object w; 
						if (parent != null && parent.length != 0)//Inner class
							w = mCls.getConstructors()[0].newInstance(parent[0]);
						else
							w = mCls.getConstructors()[0].newInstance(null);
						for (Field f : mCls.getDeclaredFields())//each field
						{
							try {
								Object fieldVal = jo.get(f.getName());
								if (fieldVal == null)
									continue;
								if (f.getType() == Integer.class)
									f.set(w, Integer.parseInt(fieldVal.toString()));
								else if (f.getType() == Date.class)
									f.set(w, new Date(Long.parseLong(fieldVal.toString())));
								else if (f.getType() == String.class)
									f.set(w, fieldVal.toString());
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
						mRec.receiveObject(w);
					}
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}
	}
}
