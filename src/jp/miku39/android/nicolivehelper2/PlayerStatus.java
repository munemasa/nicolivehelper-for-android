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

public class PlayerStatus {
	final static String TAG = "PlayerStatus";

	static public String sStatus;
	static public String sCode;

	static public String sLiveId; // /< 放送ID
	static public String sTitle;
	static public String sOwnerName;
	static public long sBaseTime;
	static public long sOpenTime;
	static public long sEndTime;
	static public long sStartTime;
	static public String sCommunity;
	static public boolean sIsOwner;
	static public String sIsPremium;
	static public String sAddr;
	static public int sPort;
	static public String sThread;
	static public long sConnectedTime;
	static public String sUserId;

	public PlayerStatus(String lv) {
		// http://watch.live.nicovideo.jp/api/getplayerstatus?v=lv0
		final String uri = "http://watch.live.nicovideo.jp/api/getplayerstatus?v="
				+ lv;
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

			sStatus = xpath.evaluate("/getplayerstatus/@status", document);

			sCode = xpath.evaluate("/getplayerstatus/error/code", document);
			if (sCode.equals("notlogin")) {
				return;
			}

			sLiveId = xpath.evaluate("/getplayerstatus/stream/id", document);
			sTitle = xpath.evaluate("/getplayerstatus/stream/title", document);
			sOwnerName = xpath.evaluate("/getplayerstatus/stream/owner_name",
					document);
			try {
				sBaseTime = Long.parseLong(xpath.evaluate(
						"/getplayerstatus/stream/base_time", document));
				sOpenTime = Long.parseLong(xpath.evaluate(
						"/getplayerstatus/stream/open_time", document));
				sEndTime = Long.parseLong(xpath.evaluate(
						"/getplayerstatus/stream/end_time", document));
				sStartTime = Long.parseLong(xpath.evaluate(
						"/getplayerstatus/stream/start_time", document));
				sConnectedTime = Long.parseLong(xpath.evaluate(
						"/getplayerstatus/@time", document));
			} catch (Exception e) {
			}
			sCommunity = xpath.evaluate(
					"/getplayerstatus/stream/default_community", document);
			final String tmp = xpath.evaluate(
					"/getplayerstatus/stream/is_owner", document);
			if (tmp.equals("1")) {
				sIsOwner = true;
			} else {
				sIsOwner = false;
			}

			sIsPremium = xpath.evaluate("/getplayerstatus/user/is_premium",
					document);
			sUserId = xpath.evaluate("/getplayerstatus/user/user_id", document);

			sAddr = xpath.evaluate("/getplayerstatus/ms/addr", document);
			sPort = Integer.parseInt(xpath.evaluate("/getplayerstatus/ms/port",
					document));
			sThread = xpath.evaluate("/getplayerstatus/ms/thread", document);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
