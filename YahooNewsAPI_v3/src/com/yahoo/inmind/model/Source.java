package com.yahoo.inmind.model;

import java.util.ArrayList;

public class Source {
	protected String url = null;
	protected String json = null;
	Renderer renderer = null;
	
	public Source()
	{
		
	}
	
	/**	Begin downloading data. Parse JSON and fill the list with items.
	 * 	This function will be executed in the background thread.
	 * */
	public void fetchData(ArrayList<JsonItem> list)
	{

	}

	public void cleanCache() {
		
	}
}
