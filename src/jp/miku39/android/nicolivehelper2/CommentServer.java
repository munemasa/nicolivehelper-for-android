package jp.miku39.android.nicolivehelper2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.SocketFactory;

import jp.miku39.android.nicolivehelper2.libs.Lib;
import jp.miku39.android.nicolivehelper2.libs.NicoCookie;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class CommentServer extends Thread {
	final static String TAG = "CommentServer";

	private boolean mFinished = false;
	private NicoLiveHelperMainActivity mMainContext;

	private Socket mSocket;
	protected InputStream mInput;
	private OutputStream mOutput;
	private String mHost;
	private int mPort;
	private String mThread;

	private String mTicket;

	private String mPostedComment;
	private String mPostedMail;
	private String mPostKey;
	private int mLastRes;
	private int mRetryCounter;

	/**
	 * 
	 * 
	 * */
	public CommentServer(NicoLiveHelperMainActivity ctx, String host, int port,
			String thread) {
		mHost = host;
		mPort = port;
		mThread = thread;
		mMainContext = ctx;
	}

	// 通信スレッドを終了させる
	public void finish() {
		mFinished = true;
		try {
			mOutput.close();
			mInput.close();
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void showToast(final String message, final int length) {
		mMainContext.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMainContext.showToast(message, length);
			}
		});
	}

	public String getPostKey(int block_no) {
		final String thread = PlayerStatus.sThread;
		final String uri = "http://watch.live.nicovideo.jp/api/getpostkey?thread="
				+ thread + "&block_no=" + block_no;
		Log.d(TAG, uri);

		HttpGet get = new HttpGet(uri);
		get.addHeader("Cookie", NicoCookie.getCookie(uri));
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = client.execute(get);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		int status = response.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_OK) {
			return null;
		}
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		try {
			response.getEntity().writeTo(outstream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		final String s = outstream.toString();
		Log.d(TAG, s);
		Pattern p = Pattern.compile("postkey=(.*)");
		Matcher m = p.matcher(s);
		if (m.find()) {
			final String key = m.group(1);
			Log.d(TAG, "postkey found.");
			return key;
		} else {
			Log.d(TAG, "postkey not found.");
		}
		return null;
	}

	/**
	 * コメントを送信する
	 * 
	 * @param comment
	 *            コメント
	 * @param mail
	 *            コマンド
	 * @param name
	 *            名前
	 */
	public boolean sendComment(String comment, String mail, String name) {
		if (PlayerStatus.sIsOwner) {
			final String uri = "http://watch.live.nicovideo.jp/api/broadcast/"
					+ PlayerStatus.sLiveId;
			HttpPost post = new HttpPost(uri);
			post.addHeader("Cookie", NicoCookie.getCookie(uri));

			List<BasicNameValuePair> pair = new ArrayList<BasicNameValuePair>();
			pair.add(new BasicNameValuePair("body", comment));
			pair.add(new BasicNameValuePair("mail", mail));
			pair.add(new BasicNameValuePair("is_184", "true"));
			if (name != null) {
				pair.add(new BasicNameValuePair("name", name));
			}
			pair.add(new BasicNameValuePair("token", PublishStatus.sToken));
			UrlEncodedFormEntity entity;
			try {
				entity = new UrlEncodedFormEntity(pair, "UTF-8");
				post.setEntity(entity);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				return false;
			}
			DefaultHttpClient client = new DefaultHttpClient();
			try {
				HttpResponse response = client.execute(post);
				int status = response.getStatusLine().getStatusCode();
				if (status != HttpStatus.SC_OK) {
					return false;
				}
				// status=error なら失敗
				String body = Lib.convertStreamToString(response.getEntity()
						.getContent());
				if (body.indexOf("status=error") != -1) {
					showToast("Failed to send the caster comment.",
							Toast.LENGTH_LONG);
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			mPostedComment = comment;
			mPostedMail = mail;

			if (mPostKey == null || mPostKey.length() == 0) {
				int block_no = (mLastRes / 100);
				mPostKey = getPostKey(block_no);
			}

			if (mPostKey == null || mPostKey.length() == 0) {
				Log.d(TAG, "No postkey");
				return false;
			}

			long vpos = ((System.currentTimeMillis() / 1000) - PlayerStatus.sOpenTime) * 100;
			final String str = "<chat thread=\"" + mThread + "\""
					+ " ticket=\"" + mTicket + "\"" + " vpos=\"" + vpos
					+ "\""
					+ " postkey=\""
					+ mPostKey
					+ "\""
					// TODO 184コメントの指定
					+ " mail=\"" + mail + (true ? " 184\"" : "\"")
					+ " user_id=\"" + PlayerStatus.sUserId + "\""
					+ " premium=\"" + PlayerStatus.sIsPremium
					+ "\" locale=\"jp\">" + Lib.escapeHTML(comment)
					+ "</chat>\0";

			try {
				mOutput.write(str.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private void processComment(final Comment comment) {
		// コメント表示
		mMainContext.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMainContext.addComment(comment);
			}
		});
		if (comment.premium != 3) {
			// 主コメじゃない
			if (PlayerStatus.sConnectedTime <= comment.date) {
				Pattern p = Pattern.compile("((sm|nm)\\d+)");
				Matcher m = p.matcher(comment.text);
				if (m.find()) {
					final String vid = m.group(1);
					Log.d(TAG, "Request Video:" + vid);
					Thread th = new Thread(new Runnable() {
						@Override
						public void run() {
							final VideoInformation v = new VideoInformation(
									"http://ext.nicovideo.jp/api/getthumbinfo/"
											+ vid);
							mMainContext.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mMainContext.addRequest(v);
								}
							});
						}
					});
					th.start();
				}
			}
		}
		if (comment.premium == 3) {
			// TODO 動画再生
			if (comment.text.indexOf("/play") == 0) {
				Pattern p = Pattern
						.compile("^/play.*smile:((sm|nm|so)\\d+).*\"(.*)\"");
				Matcher m = p.matcher(comment.text);
				if (m.find()) {
					final String vid = m.group(1);
					final String title = m.group(3);
					mMainContext.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mMainContext.addHistory(vid + " " + title + "\n");

							if (comment.date > PlayerStatus.sConnectedTime) {
								if (vid.indexOf("sm") == 0
										|| vid.indexOf("so") == 0) {
									mMainContext.playbackVideo(vid);
								}
							}
						}
					});
				}
			}
		}
	}

	private void processData(String s) {
		if (s.matches("^<chat\\s+.*>")) {
			final Comment comment = new Comment(s);
			mLastRes = comment.comment_no;
			processComment(comment);
			return;
		}
		Pattern p = Pattern.compile("<chat_result.*status=\"(\\d+)\".*/>");
		Matcher m = p.matcher(s);
		if (m.find()) {
			int result = Integer.parseInt(m.group(1));
			Log.d(TAG, "chat result=" + result);
			switch (result) {
			case 0:
				mRetryCounter = 0;
				mMainContext.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						EditText ev = (EditText) mMainContext
								.findViewById(R.id.inputcomment);
						ev.setText("");
					}
				});
				break; // success
			case 1:
				showToast("Now in comment inhibition.", Toast.LENGTH_LONG);
				break; // 規制中
			case 4:
				// TODO 視聴者コメの送信にバグあり
				resendComment();
				break; // キーが必要
			default:
			}
			return;
		}

	}

	private void resendComment() {
		// TODO 視聴者コメントの送信にバグあり
		if (mRetryCounter >= 3) {
			mRetryCounter = 0;
			Log.d(TAG, "failed to send listener's comment.");
			showToast("Failed to send your comment.", Toast.LENGTH_LONG);
			return;
		}
		Log.d(TAG, "Retransmit a comment.");

		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean b = true;
				do {
					int block_no = (mLastRes / 100) + mRetryCounter;
					mPostKey = getPostKey(block_no);
					mRetryCounter++;
					if (mRetryCounter >= 3) {
						mRetryCounter = 0;
						Log.d(TAG, "failed to send listener's comment.");
						showToast("Failed to send your comment.",
								Toast.LENGTH_LONG);
						b = false;
						break;
					}
				} while (mPostKey == null || mPostKey.length() == 0);
				if (b)
					sendComment(mPostedComment, mPostedMail, null);
			}
		});
		th.start();
	}

	@Override
	public void run() {
		try {
			Log.d(TAG, "Connect to " + mHost + ":" + mPort);
			mSocket = SocketFactory.getDefault().createSocket(mHost, mPort);
			mInput = mSocket.getInputStream();
			mOutput = mSocket.getOutputStream();

			showToast("コメントサーバーに接続しました", Toast.LENGTH_LONG);

			final int lines = -50;
			final String initstr = "<thread thread=\"" + mThread
					+ "\" res_from=\"" + lines + "\" version=\"20061206\"/>\0";

			mOutput.write(initstr.getBytes());
			ArrayList<Byte> line = new ArrayList<Byte>();

			while (!mFinished) {
				int ch = mInput.read();
				if (ch == -1) {
					break;
				}
				if (ch == 0) {
					byte[] b = new byte[line.size()];
					for (int i = 0; i < b.length; i++) {
						b[i] = line.get(i);
					}
					final String s = new String(b, "UTF-8");
					// Log.d(TAG, s);
					processData(s);
					line.clear();
				} else {
					line.add((byte) ch);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!mFinished) {
			showToast("Connection is terminated.", Toast.LENGTH_LONG);
		}

		Log.d(TAG, "Communication thread is finished.");
	}
}
