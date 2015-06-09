package com.yahoo.inmind.model;

import android.graphics.drawable.Drawable;


public class NewsItem extends JsonItem{
	public final static short DIM_UNDEFINED = -1;
	public final static short DIM_PORTRAIT = 0;
	public final static short DIM_LANDSCAPE = 1;
	
	public Integer idx;
	public String title;
	public String uuid;
	public String summary;
	public String imgUrl;
	public String raw_score_map;
	public String publisher;
	public String cap_features;
	public String features_cap;
	public String score;
	public String url;
	public String reason;
	public Drawable drawable;
	public String imgPath;					//For ImgLruCache	
	public Short dimension = DIM_UNDEFINED; //For ImgLruCache
	
	public NewsItem() {
		super();
	}
	
	@Override
	public void partialFree() {
		drawable = null;
	}
}
