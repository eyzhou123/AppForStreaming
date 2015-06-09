package com.yahoo.inmind.model.slingstone;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.android.sbjniapp.VhmsgWrapper;
import com.yahoo.inmind.browser.BaseBrowser;
import com.yahoo.inmind.cache.ImgLruCacher;
import com.yahoo.inmind.model.JsonItem;
import com.yahoo.inmind.model.NewsItem;
import com.yahoo.inmind.model.Renderer;
import com.yahoo.inmind.reader.App;
import com.yahoo.inmind.reader.R;
import com.yahoo.inmind.reader.ReaderMainActivity;
import com.yahoo.inmind.share.ShareHelper;
import com.yahoo.inmind.util.WebImgUtil;


public class SlingstoneRenderer extends Renderer {
	public  VhmsgWrapper tempVhmsg = null;
	WebImgUtil mImgUtil;	
	ImgLruCacher mCache;
	
	public SlingstoneRenderer(Context ctx) {
		super(ctx);
		mResId = R.layout.news_list_item_flat;
		mImgUtil = new WebImgUtil(ctx);
	}
	
	@Override
	public boolean isCompatible(JsonItem item) {
		if (item instanceof JsonItem)
			return true;
		return false;
	}
	
	@Override
	public boolean isDirty(View v, JsonItem jsonItem) {

		if (jsonItem == null || v == null || !(jsonItem instanceof NewsItem))
			return true;
		WeakReference<NewsItem> weakRef = (WeakReference<NewsItem>)v.getTag();
		if (weakRef == null || weakRef.get() == null)
			return true;
		NewsItem newItem = (NewsItem) jsonItem;
		NewsItem oldItem = (NewsItem) weakRef.get();
		Drawable d1, d2;
		
		if (mCache == null)
		{
			d1 = oldItem.drawable;
			d2 = newItem.drawable;
			if (d2 == null && d1 == null)
				return false;
			else if (d2 != null && d1 != null)
			{
				if (d1.getIntrinsicHeight() > d1.getIntrinsicWidth() && d2.getIntrinsicHeight() > d2.getIntrinsicWidth()){
					return false;
				}
				if (d1.getIntrinsicHeight() < d1.getIntrinsicWidth() && d2.getIntrinsicHeight() < d2.getIntrinsicWidth()){
					return false;
				}
			}
		}
		else
		{
			return oldItem.dimension != newItem.dimension;
		}
		
		return true;
	}

	//Draw the view of each list item
	@Override
	public View inflate(View view, JsonItem item, ViewGroup vg) {
		if (view != null)//view exists but is dirty
		{
			freeView(view);
		}
		
		NewsItem art = (NewsItem) item;
		
		if (mCache == null)//No cache is used
		{
			Drawable d = art.drawable;
			if (d != null)
			{
				if (d.getIntrinsicWidth() < d.getIntrinsicHeight())
				{				
					view = mInflater.inflate(R.layout.news_list_item, null);				
				}
			}
		}
		else//Use Cache
		{
			switch(art.dimension)
			{	
				case NewsItem.DIM_PORTRAIT:
					view = mInflater.inflate(R.layout.news_list_item, null);	
					break;
				case NewsItem.DIM_LANDSCAPE:
					view = mInflater.inflate(R.layout.news_list_item_flat, null);	
					break;
				case NewsItem.DIM_UNDEFINED:
				default:
			}
		}
		
		//Default view when dimension of the image is not ready
		if (view == null)
			view = mInflater.inflate(R.layout.news_list_item_flat, null);
		return view;
	}

	//Fill every field in the View with data
	@Override
	public void render(View view, JsonItem item, int idx) {
		NewsItem art = (NewsItem) item;
		
		((TextView)view.findViewById(R.id.rank)).setText(String.valueOf(idx + 1));
		if (art.title != null)
			((TextView) view.findViewById(R.id.title)).setText(art.title);
		if (art.score != null)
			((TextView)view.findViewById(R.id.score)).setText("Score: " + (art.score.length() >= 5 ? art.score.substring(0, 5):art.score));
		if (art.summary != null)
		{
			TextView tv = ((TextView)view.findViewById(R.id.summary));
			tv.setText(art.summary);
			addClickListener(art, tv);
		}
		if (art.raw_score_map != null)	
			((TextView)view.findViewById(R.id.feat)).setText(art.raw_score_map);
		if (art.cap_features != null)
		{
			TextView tv = ((TextView)view.findViewById(R.id.feat2));
			tv.setText(art.cap_features);
			addClickListener(art, tv);
		}
		if (art.publisher != null)			
			((TextView)view.findViewById(R.id.publisher)).setText(art.publisher);
		if (art.reason != null)			
			((TextView)view.findViewById(R.id.reason)).setText(art.reason);
		
		//Add click listeners
		ImageView inner_iv = (ImageView) view.findViewById(R.id.img);
		addClickListener(art, inner_iv);
		addClickListener(art, view);//Add clickListener to the container
		
		//Set onclick listener for sharing buttons
		for (int id : new int[]{R.id.btnShareFb, R.id.btnShareTwitter, R.id.btnShareTumblr, R.id.btnShareMore})
		{
			ImageButton btn = ((ImageButton) view.findViewById(id));
			btn.setTag(new WeakReference<NewsItem>(art));
			btn.setOnClickListener(onShareClickListener);	
		}
		
		//Case 1: Use in-memory drawable without cache
		if (art.drawable != null)
		{
			setImageView((ImageView) view.findViewById(R.id.img), 
				art.drawable);
			return;
		}
		
		//Case 2: Use cache
		if (mCache != null)//use cache to retrieve bitmap & drawable
		{
			if (art.imgPath != null)//downloaded, so the Drawable deserved not to be null
			{
				Bitmap cachedBmp = mCache.get(art.imgPath);
				setImageView((ImageView) view.findViewById(R.id.img), 
						mImgUtil.drawableFromBmp(cachedBmp));
				return;
			}
		}
				
		//Case 3: The image is not ready, neither in the persistent storage nor in memory. 
		ImageView iv = (ImageView) view.findViewById(R.id.img);
		iv.setImageDrawable(null);
		iv.setVisibility(View.GONE);
	}

	private void addClickListener(NewsItem art, View container) {
		container.setTag(new WeakReference<NewsItem>(art));
		container.setOnClickListener(mOnItemClickListener);
	}
	
	OnClickListener mOnItemClickListener = new OnClickListener(){

				
		@Override
		public void onClick(View v) {
			String url = ((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).url;
			String imageurl = ((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).imgUrl;
			String uuid = ((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).uuid;
			String summary= ((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).summary;
			String feature_cap=((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).features_cap;
			String cap_features=((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).cap_features;
			String score=((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).score;
			String reason=((NewsItem)((WeakReference<NewsItem>)v.getTag()).get()).reason;
			
			Intent intent = new Intent(getContext(), BaseBrowser.class);
			intent.putExtra("url", url);
			intent.putExtra("uuid", uuid);
			System.out.println("Image url is "+imageurl);
			//intent.putExtra("raw_score_map", raw_score_map);
			String[] summarySplit=summary.split(Pattern.quote("."));
			System.out.println(summarySplit[0]);
			String test=summarySplit[0];
			byte[] utf8Bytes=null;
			try {
				utf8Bytes = test.getBytes("UTF8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   

		    String roundTrip=null;
			try {
				roundTrip = new String(utf8Bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			roundTrip = roundTrip.replaceAll("[^\\x20-\\x7e]", " ");
			roundTrip.replace("\"", "\\\"");
		//	roundTrip.replace("\'", "\\\'");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&cap_feature:dfe "+cap_features+"&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&summary: "+summarySplit[0]+"&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&feature_cap: "+feature_cap+"&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&score: "+score+"&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&reason: "+reason+"&&&&&&&&&&&&&&&&&&&&&&&&");
			tempVhmsg=ReaderMainActivity.vhmsg;
	       // vhmsg.openConnection();
	        String vrExpress="Brad user 1404332904389-10-1  \n<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?> \n<act> \n<participant id=\"Brad\" role=\"actor\" /> \n<fml> \n<turn start=\"take\" end=\"give\" /> \n<affect type=\"neutral\" target=\"addressee\"> \n</affect> \n<culture type=\"neutral\"> \n</culture> \n<personality type=\"neutral\"> \n</personality> \n</fml> \n<bml> \n<speech id=\"sp1\" type=\"application/ssml+xml\">Wow this news sounds interesting."+roundTrip+"\n</speech> "
	        					+"\n</bml> \n</act>";
	        tempVhmsg.send("vrExpress", vrExpress);
	        //vhmsg.closeConnection();
			getContext().startActivity(intent);				
		}
		
	};
	
	@Override
	public void freeView(View view) {//This is very important.
		ImageView iv = (ImageView) view.findViewById(R.id.img);
		iv.setImageDrawable(null);
	}

	public void setImageView(View view, Drawable d) {
		ImageView iv = (ImageView) view.findViewById(R.id.img);
		iv.setImageDrawable(d);
		iv.setScaleType(ScaleType.FIT_CENTER);	
		iv.setMinimumHeight(400);
		iv.setVisibility(View.VISIBLE);
	}
	
	public Renderer enableCache(ImgLruCacher cache) {
		mCache = cache;
		return this;
	}
	
	//The OnClickListeners for sharing buttons
	private OnClickListener onShareClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			WeakReference<NewsItem> itemRef = (WeakReference<NewsItem>) v.getTag();
			NewsItem item = itemRef.get();
			if (item == null)
				return;
			ShareHelper.Type type = ShareHelper.Type.More;
			int id = v.getId();
			if (id == R.id.btnShareFb) {
				type = ShareHelper.Type.Facebook;
			} else if (id == R.id.btnShareTwitter) {
				type = ShareHelper.Type.Twitter;
			} else if (id == R.id.btnShareTumblr) {
				type = ShareHelper.Type.Tumblr;
			} else if (id == R.id.btnShareMore) {
				type = ShareHelper.Type.More;
			}
			App.get().getShareHelper().share(type, getContext(), 
					item.title, item.summary, item.url, item.uuid);
		}
		
	};
}
