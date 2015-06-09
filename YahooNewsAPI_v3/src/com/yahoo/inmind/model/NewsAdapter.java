package com.yahoo.inmind.model;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class NewsAdapter extends BaseAdapter {
	ArrayList<JsonItem> mList = null;
	ArrayList<Renderer> mRenderers = null;
	
	public void setList(ArrayList<JsonItem> list)
	{
		mList = list;
	}
	
	public void setRenderer(ArrayList<Renderer> rens)
	{
		mRenderers = rens;
	}
	
	@Override
	public int getCount() {
		if (mList == null)
			return 0;
		return mList.size();
	}

	@Override
	public JsonItem getItem(int i) {
		if (mList == null)
			return null;
		return mList.get(i);
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public int getItemViewType(int arg0) {
		if (mList == null || mList.size() == 0)
			return 3;
		return ((NewsItem)mList.get(arg0)).dimension;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (i >= mList.size())
			return null;
		JsonItem item = mList.get(i);
		Renderer ren = getCompatibleRenderer(item);		
		if (view == null || ren.isDirty(view, item))
		{
			view = ren.inflate(view, item, viewGroup);
		}
		else
			ren.freeView(view);
		ren.render(view, item, i);
		return view;
	}

	private Renderer getCompatibleRenderer(JsonItem item) {
		for (Renderer ren : mRenderers)
		{
			if (ren.isCompatible(item))
				return ren;
		}
		return null;
	}	

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public boolean isEmpty() {
		if (mList == null)
			return true;
		// TODO Auto-generated method stub
		return mList.isEmpty();
	}
	
	public void partialFree(){
		for (JsonItem item : mList)
			item.partialFree();
	}
	
	public void clear() {
		for (JsonItem item : mList)
			item.free();
		mList.clear();
	}

}
