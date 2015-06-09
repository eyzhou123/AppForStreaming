package com.yahoo.inmind.model;


public class JsonItem extends ValueObject{
	private String json = null;
	Renderer renderer = null;
	
	public JsonItem()
	{
		json = null;		
	}
	
	public JsonItem(String json)
	{
		this.json = json;
	}
	
	public String getRawString()
	{
		return json;
	}	
}
