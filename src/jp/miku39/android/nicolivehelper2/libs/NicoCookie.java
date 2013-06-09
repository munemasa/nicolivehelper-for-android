package jp.miku39.android.nicolivehelper2.libs;

import android.util.Log;
import android.webkit.CookieManager;

@SuppressWarnings("unused")
public class NicoCookie {
	static final String TAG = "NicoCookie";

	static public String getCookie(String uri){
		String cookie = CookieManager.getInstance().getCookie(uri);
		if( cookie==null ) cookie = "";
		//Log.d(TAG,cookie);
		return cookie;
	}

}
