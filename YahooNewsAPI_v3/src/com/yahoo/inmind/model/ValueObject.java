package com.yahoo.inmind.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import android.util.Log;

public class ValueObject {
	
	public ValueObject()
	{
		
	}
	
	public void setAllFieldsToNull() {
		for (Field f : this.getClass().getFields())
		{
			try {
				if (!Modifier.isFinal(f.getModifiers()))
					f.set(this, null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				Log.e("inmind", "setAllFieldsToNull cannot set " + f.getName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				Log.e("inmind", "setAllFieldsToNull cannot set " + f.getName());
			}
		}
	}
	
	public void deepCopy(Object obj){
		for (Field f : obj.getClass().getFields())
		{
			try {
				if (!Modifier.isFinal(f.getModifiers()))
					f.set(this, f.get(obj));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void free()
	{
		setAllFieldsToNull();
	}

	public void partialFree() {
		
	}
}
