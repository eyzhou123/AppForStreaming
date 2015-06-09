package com.yahoo.inmind.model.slingstone;

import com.yahoo.inmind.model.Profile;

public class UserProfile extends Profile {
	Profile mCapWiki;
	Profile mDecWiki;
	Profile mDecYct;
	Profile mCapYct;
	
	public UserProfile()
	{
		mCapWiki = new Profile();
		mDecWiki = new Profile();
		mDecYct = new Profile();
		mCapYct = new Profile();
	}
}
