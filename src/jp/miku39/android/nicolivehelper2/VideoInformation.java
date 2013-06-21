package jp.miku39.android.nicolivehelper2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class VideoInformation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5477290732693666628L;

	final transient static String TAG = "VideoInformation";

	public int mCommentNo;
	public String mVideoId;
	public String mTitle;
	public String mDescription;
	public String mThumbnailUrl;
	public String mFirstRetrieve;
	public long mLength;
	public String mLengthStr;
	public int mViewCounter;
	public int mCommentNum;
	public int mMylistCounter;
	public String[] mTags;
	public boolean mNoLivePlay;

	public transient Bitmap mIcon;

	public VideoInformation(String uri) {
		mCommentNo = 0;
		Log.d(TAG, "accessing " + uri);

		try {
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = docBuilder.parse(uri);
			NodeList root = doc.getElementsByTagName("thumb");
			Node node = root.item(0).getFirstChild();
			String name, value;
			do {
				name = node.getNodeName();
				value = node.getTextContent();
				// Log.d(TAG,name+"="+value);

				if (name.equals("video_id")) {
					mVideoId = value;
				} else if (name.equals("title")) {
					mTitle = value;
				} else if (name.equals("description")) {
					mDescription = value;
				} else if (name.equals("thumbnail_url")) {
					mThumbnailUrl = value;
				} else if (name.equals("first_retrieve")) {
					try {
						// 2011-07-03T00:03:07+09:00
						MessageFormat mf = new MessageFormat(
								"{0,number,integer}-{1,number,integer}-{2,number,integer}T{3,number,integer}:{4,number,integer}:{5,number,integer}");
						Object[] result = mf.parse(value);
						long y = (Long) result[0];
						long mon = (Long) result[1];
						long d = (Long) result[2];
						long h = (Long) result[3];
						long min = (Long) result[4];
						long s = (Long) result[5];
						mFirstRetrieve = "" + y + "/"
								+ (mon < 10 ? "0" + mon : mon) + "/"
								+ (d < 10 ? "0" + d : d);
						mFirstRetrieve += " " + h + ":"
								+ (min < 10 ? "0" + min : min) + ":"
								+ (s < 10 ? "0" + s : s);
					} catch (Exception e) {
						mFirstRetrieve = "1970/1/1 0:00:00";
						e.printStackTrace();
					}
				} else if (name.equals("length")) {
					try {
						MessageFormat mf = new MessageFormat(
								"{0,number,integer}:{1,number,integer}");
						Object[] result = mf.parse(value);
						long min = (Long) result[0];
						long sec = (Long) result[1];
						mLength = min * 60 + sec;
						mLengthStr = value;
					} catch (Exception e) {
						mLength = 0;
						mLengthStr = "0:00";
					}
				} else if (name.equals("view_counter")) {
					mViewCounter = Integer.parseInt(value);
				} else if (name.equals("comment_num")) {
					mCommentNum = Integer.parseInt(value);
				} else if (name.equals("mylist_counter")) {
					mMylistCounter = Integer.parseInt(value);
				} else if (name.equals("tags")) {
					NamedNodeMap attrs = node.getAttributes();
					if (attrs.getNamedItem("domain").getTextContent()
							.equals("jp")) {
						int sz = node.getChildNodes().getLength();
						mTags = new String[sz];
						for (int i = 0; i < sz; i++) {
							mTags[i] = node.getChildNodes().item(i)
									.getTextContent();
						}
					}
				} else if (name.equals("no_live_play")) {
					if (Integer.parseInt(value) != 0) {
						mNoLivePlay = true;
					} else {
						mNoLivePlay = false;
					}
				}

				node = node.getNextSibling();
			} while (node != null);
			loadThumbnail();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadThumbnail() {
		HttpGet get = new HttpGet(mThumbnailUrl);
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK) {
				return;
			}
			mIcon = BitmapFactory.decodeStream(response.getEntity()
					.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		mIcon.compress(Bitmap.CompressFormat.PNG, 100, oos);
	}

	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		mIcon = BitmapFactory.decodeStream(ois);
	}

}
