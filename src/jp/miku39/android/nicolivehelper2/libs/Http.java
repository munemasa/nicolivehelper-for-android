package jp.miku39.android.nicolivehelper2.libs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import android.util.Log;

public class Http {
	final static String TAG = "Http";

	public static String sCookie = ""; // /< 最後に行った通信で送られてきたクッキーを保存する

	static public String postRequest(String uri, Map<String, String> form,
			String cookie) {
		Log.d(TAG, "POST " + uri);
		HttpPost post = new HttpPost(uri);

		if (cookie != null) {
			post.addHeader("Cookie", cookie);
		}

		List<BasicNameValuePair> pair = new ArrayList<BasicNameValuePair>();
		for (String key : form.keySet()) {
			String value = form.get(key);
			pair.add(new BasicNameValuePair(key, value));
		}

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pair, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		post.setEntity(entity);

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				Log.d(TAG, "HTTP POST Status=" + status);
				return null;
			}

			Header[] headers = response.getHeaders("Set-Cookie");
			for (Header header : headers) {
				String tmp = header.getValue();
				sCookie = tmp.substring(0, tmp.indexOf(";"));
				Log.d(TAG, "Cookie: " + sCookie);
			}

			String body = Lib.convertStreamToString(response.getEntity()
					.getContent());
			return body;

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static public String getRequest(String uri, String cookie) {
		Log.d(TAG, "GET " + uri);
		HttpGet get = new HttpGet(uri);
		if (cookie != null) {
			get.addHeader("Cookie", cookie);
		}
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				Log.d(TAG, "HTTP GET Status=" + status);
				return null;
			}

			Header[] headers = response.getHeaders("Set-Cookie");
			for (Header header : headers) {
				String tmp = header.getValue();
				sCookie = tmp.substring(0, tmp.indexOf(";"));
				Log.d(TAG, "Cookie: " + sCookie);
			}

			String body = Lib.convertStreamToString(response.getEntity()
					.getContent());
			return body;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document getDocument(String uri) {
		HttpGet get = new HttpGet(uri);
		get.addHeader("Cookie", NicoCookie.getCookie(uri));
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				return null;
			}
			InputStream is = response.getEntity().getContent();
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = docBuilder.parse(is);
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
