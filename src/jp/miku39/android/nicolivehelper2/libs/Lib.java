package jp.miku39.android.nicolivehelper2.libs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Lib {
	final static String sPrefsName = "prefs";

	
	/**
	 * 現在時刻を取得.
	 * @return UNIX時間を返す（秒）
	 */
	public static final int getNowTime(){
		Date d = new Date();
		return (int) (d.getTime()/1000);
	}

	/**
	 * 文字列をHTMLエスケープする
	 * @param s 文字列
	 * @return HTMLエスケープした文字列を返す
	 */
	public static final String escapeHTML(String s) {
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case 'à':
				sb.append("&agrave;");
				break;
			case 'À':
				sb.append("&Agrave;");
				break;
			case 'â':
				sb.append("&acirc;");
				break;
			case 'Â':
				sb.append("&Acirc;");
				break;
			case 'ä':
				sb.append("&auml;");
				break;
			case 'Ä':
				sb.append("&Auml;");
				break;
			case 'å':
				sb.append("&aring;");
				break;
			case 'Å':
				sb.append("&Aring;");
				break;
			case 'æ':
				sb.append("&aelig;");
				break;
			case 'Æ':
				sb.append("&AElig;");
				break;
			case 'ç':
				sb.append("&ccedil;");
				break;
			case 'Ç':
				sb.append("&Ccedil;");
				break;
			case 'é':
				sb.append("&eacute;");
				break;
			case 'É':
				sb.append("&Eacute;");
				break;
			case 'è':
				sb.append("&egrave;");
				break;
			case 'È':
				sb.append("&Egrave;");
				break;
			case 'ê':
				sb.append("&ecirc;");
				break;
			case 'Ê':
				sb.append("&Ecirc;");
				break;
			case 'ë':
				sb.append("&euml;");
				break;
			case 'Ë':
				sb.append("&Euml;");
				break;
			case 'ï':
				sb.append("&iuml;");
				break;
			case 'Ï':
				sb.append("&Iuml;");
				break;
			case 'ô':
				sb.append("&ocirc;");
				break;
			case 'Ô':
				sb.append("&Ocirc;");
				break;
			case 'ö':
				sb.append("&ouml;");
				break;
			case 'Ö':
				sb.append("&Ouml;");
				break;
			case 'ø':
				sb.append("&oslash;");
				break;
			case 'Ø':
				sb.append("&Oslash;");
				break;
			case 'ß':
				sb.append("&szlig;");
				break;
			case 'ù':
				sb.append("&ugrave;");
				break;
			case 'Ù':
				sb.append("&Ugrave;");
				break;
			case 'û':
				sb.append("&ucirc;");
				break;
			case 'Û':
				sb.append("&Ucirc;");
				break;
			case 'ü':
				sb.append("&uuml;");
				break;
			case 'Ü':
				sb.append("&Uuml;");
				break;
			case '®':
				sb.append("&reg;");
				break;
			case '©':
				sb.append("&copy;");
				break;
			case '€':
				sb.append("&euro;");
				break;
			// be carefull with this one (non-breaking whitee space)
			// case ' ': sb.append("&nbsp;");break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public static final String convertStreamToString(InputStream is)
			throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		return sb.toString();
	}

	public static void hideSoftwareKeyboard(Context ctx, View v){
		InputMethodManager inputMethodManager = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public static void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String getStringValue(Context ctx, String key){
		return ctx.getSharedPreferences(sPrefsName, Context.MODE_PRIVATE).getString(key, "");
	}

	public static void setStringValue(Context ctx, String key, String value){
		Editor edit = ctx.getSharedPreferences(sPrefsName, Context.MODE_PRIVATE).edit();
		edit.putString(key, value);
		edit.commit();
	}

	/**
	 * 設定から整数値を読み込む.
	 * @param ctx
	 * @param key
	 * @return 正の整数値を返す。値がkeyに保存されていなければ負の値を返す
	 */
	public static Integer getIntegerValue(Context ctx, String key){
		return ctx.getSharedPreferences(sPrefsName, Context.MODE_PRIVATE).getInt(key, -1);
	}

	public static void setIntegerValue(Context ctx, String key, Integer value){
		Editor edit = ctx.getSharedPreferences(sPrefsName, Context.MODE_PRIVATE).edit();
		edit.putInt(key, value);
		edit.commit();
	}

	public static Long getLongValue(Context ctx, String key){
		return ctx.getSharedPreferences(sPrefsName, Context.MODE_PRIVATE).getLong(key, -1);
	}

	public static void setLongValue(Context ctx, String key, long value) {
		Editor edit = ctx.getSharedPreferences(sPrefsName, Context.MODE_PRIVATE).edit();
		edit.putLong(key, value);
		edit.commit();
	}

	public static void deleteFile(Context ctx, String name){
		ctx.deleteFile(name);
	}

	public static void writeObject(Context ctx, String name, Object obj) {
		try {
			OutputStream os = ctx.openFileOutput( name, Context.MODE_PRIVATE );
			ObjectOutputStream oos;
			oos = new ObjectOutputStream( os );
			oos.writeObject( obj );
			oos.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Object readObject(Context ctx, String name) {
		Object obj = null;
		try {
			InputStream is = ctx.openFileInput( name );
			ObjectInputStream ois;
			ois = new ObjectInputStream( is );
			obj = ois.readObject();
			ois.close();
			is.close();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
