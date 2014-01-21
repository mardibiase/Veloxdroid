package com.veloxdroid.utils;

import android.content.SharedPreferences;
import android.util.Log;

public class Utils {
		
	public static boolean checkLogin(SharedPreferences settings){
		String s = settings.getString("eMail", "");
		Log.d("checkLogin", s);
		if (s.equalsIgnoreCase("") || s.equalsIgnoreCase("visitatore"))
			return false;
		else
			return true;
	}

}
