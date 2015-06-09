package com.yahoo.inmind.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class Profile {
	HashMap<String, String> mAttrs; //less important attributes
	
	public Profile()
	{
		mAttrs = new HashMap<String, String>();
	}
	
	public void put(String key, String val)
	{
		
		mAttrs.put(key, val);
	}
	
	public Set<String> keySet()
	{
		return mAttrs.keySet();
	}
	
	public Collection<String> values()
	{
		return mAttrs.values();
	}
	
	public String getVal(String key)
	{
		return mAttrs.get(key);
	}
	
	public Set<Entry<String, String>> entrySet()
	{
		return mAttrs.entrySet();
	}
}
