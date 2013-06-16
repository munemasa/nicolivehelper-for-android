package jp.miku39.android.nicolivehelper2;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import jp.miku39.android.nicolivehelper2.libs.NicoCookie;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;

import android.util.Log;

public class PublishStatus {
	final static String TAG = "PublishStatus";

	protected static String sToken = "";
	protected static Long sStartTime = 0L;
	protected static Long sEndTime = 0L;
	protected static int sExclude = 0;

	public PublishStatus(String lv) {
		final String uri = "http://watch.live.nicovideo.jp/api/getpublishstatus?v="
				+ lv + "&version=2";
		HttpGet get = new HttpGet(uri);
		get.addHeader("Cookie", NicoCookie.getCookie(uri));
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				return;
			}
			// client.getCookieStore().getCookies();
			parse(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parse(HttpResponse response) {
		try {
			InputStream is = response.getEntity().getContent();
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = docBuilder.parse(is);
			XPath xpath = XPathFactory.newInstance().newXPath();

			sToken = xpath.evaluate("/getpublishstatus/stream/token", document);
			sStartTime = Long.parseLong(xpath.evaluate(
					"/getpublishstatus/stream/start_time", document));
			sEndTime = Long.parseLong(xpath.evaluate(
					"/getpublishstatus/stream/end_time", document));
			sExclude = Integer.parseInt(xpath.evaluate(
					"/getpublishstatus/stream/token", document));

			Log.d(TAG, "Token=" + sToken);
			Log.d(TAG, "Exclude=" + sExclude);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
