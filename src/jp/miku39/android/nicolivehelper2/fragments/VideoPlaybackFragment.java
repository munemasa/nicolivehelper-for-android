package jp.miku39.android.nicolivehelper2.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.miku39.android.nicolivehelper2.R;
import jp.miku39.android.nicolivehelper2.libs.Http;
import jp.miku39.android.nicolivehelper2.libs.SimpleWebProxy;
import android.app.Activity;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class VideoPlaybackFragment extends Fragment implements Callback {
	final static String TAG = "VideoPlaybackFragment";

	String mVideoId;
	MediaPlayer mMediaPlayer;

	private SurfaceView mSurface;
	private SurfaceHolder mHolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		try {
			mVideoId = getArguments().getString("video_id");
		} catch (NullPointerException e) {
			mVideoId = "sm17239967";
		}
		Log.d(TAG, "Playback Video Id=" + mVideoId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		View v = inflater.inflate(R.layout.fragment_videoplayback, container,
				false);

		mSurface = (SurfaceView) v.findViewById(R.id.surfaceview_videoplayback);
		mHolder = mSurface.getHolder();
		// TODO 動画サイズは調整が必要
		// 512x384 640x360 640x384
		// xhdpi 1280x720端末で640x360ちょうどいい感じなので
		// DP単位だと 320x180 か。
		final float scale = getActivity().getResources().getDisplayMetrics().density;
		mHolder.setFixedSize((int) (320 * scale), (int) (180 * scale));
		// mHolder.setFixedSize( 640, 360);

		mHolder.addCallback(this);
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		Log.d(TAG, "onDetach");
		super.onDetach();
	}

	void playVideo() {
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				String step1url = "http://ext.nicovideo.jp/thumb_watch/"
						+ mVideoId + "?w=490&h=307";

				String script = Http.getRequest(step1url, null);
				String regex = "\\'thumbPlayKey':\\s\\'(.*)\\'";
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(script);
				String k = "";
				if (m.find()) {
					String s = m.group(1);
					Log.d(TAG, "thumbPlayKey is " + s);
					k = s;
				}

				HashMap<String, String> postdata = new HashMap<String, String>();

				postdata.put("k", k);
				postdata.put("v", mVideoId);
				postdata.put("as", "1");

				String str = Http.postRequest(
						"http://ext.nicovideo.jp/thumb_watch", postdata,
						Http.sCookie);
				Log.d(TAG, str);

				try {
					str = URLDecoder.decode(str, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				String[] values = str.split("&");
				for (String s : values) {
					if (s.indexOf("url=") == 0) {
						// ホスト名から後を取得できればOK
						str = s.substring(4 + 7);
					}
				}
				Log.d(TAG, "Video Data Url=" + str);

				final String video_url = str;
				// MediaPlayerでの再生がCookieをうまく渡せていないのかうまくいかない
				String path = "http://localhost:8081/" + video_url;
				Uri uri = Uri.parse(path);
				mMediaPlayer = MediaPlayer.create(getActivity(), uri);
				if (mMediaPlayer != null) {
					mMediaPlayer.setDisplay(mHolder);
					mMediaPlayer.start();
				}
			}
		});
		th.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		playVideo();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

}
