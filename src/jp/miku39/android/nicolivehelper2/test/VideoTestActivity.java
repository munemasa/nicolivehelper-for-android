package jp.miku39.android.nicolivehelper2.test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.miku39.android.nicolivehelper2.R;
import jp.miku39.android.nicolivehelper2.libs.Http;
import jp.miku39.android.nicolivehelper2.libs.SimpleWebProxy;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class VideoTestActivity extends Activity implements Callback {
	final static String TAG = "VideoTestActivity";

	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private MediaPlayer mMediaPlayer;

	private String mVideoId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test_video_layout);

		Intent intent = getIntent();
		mVideoId = intent.getStringExtra("video_id");
		Log.d(TAG, "Video ID="+mVideoId);

	    surfaceView = (SurfaceView) findViewById(R.id.surfaceview_videoplayback);
	    holder = surfaceView.getHolder();
    	//holder.setFixedSize(640, 380);
	    holder.addCallback(this);

	    Thread th = new Thread( new Runnable() {
			@Override
			public void run() {
				SimpleWebProxy.runServer(8081);
			}
		});
	    th.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		SimpleWebProxy.terminate();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
	}

	void playVideo(){
		Thread th = new Thread( new Runnable() {
			@Override
			public void run() {
//				String tmp = Http.getRequest("http://localhost:8081/path/to", null);
				
				// 	,'thumbPlayKey': '1370792171.0.YnCbN_9OYWU9VRyMuXIBI4cnl1E.aHR0cDovL21pa3UzOS5qcC9-YW1hbm8vdGVzdDIuaHRtbA==..'
				String step1url = "http://ext.nicovideo.jp/thumb_watch/"+mVideoId+"?w=490&h=307";

				String script = Http.getRequest(step1url,null);
				String regex = "\\'thumbPlayKey':\\s\\'(.*)\\'";
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(script);
				String k = "";
				if( m.find() ){
					String s = m.group(1);
					Log.d(TAG,"thumbPlayKey is "+s);
					k = s;
				}

				HashMap<String,String> postdata = new HashMap<String,String>();

				postdata.put("k", k);
				postdata.put("v",mVideoId);
				postdata.put("as", "1");

				String str = Http.postRequest("http://ext.nicovideo.jp/thumb_watch", postdata, Http.sCookie);
				Log.d(TAG, str);

				try {
					str = URLDecoder.decode(str, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] values = str.split("&");
				for( String s: values ){
					if( s.indexOf("url=")==0 ){
						// ホスト名から後を取得できればOK
						str = s.substring(4 + 7);
					}
				}
				Log.d(TAG,"Video Data Url="+str);
//				String video = Http.getRequest(str, Http.sCookie); // これは成功する

				final String video_url = str; 
				runOnUiThread( new Runnable() {
					@Override
					public void run() {
						// MediaPlayerでの再生がCookieをうまく渡せていないのかうまくいかない
//						String path = "http://miku39.jp/~amano/smile.mp4";
//						String path = "file:///mnt/sdcard/external_sd/smile.mp4";
//						String path = "file:///mnt/sdcard/external_sd/videotest.mp4";
//						String path = video_url;
						String path = "http://localhost:8081/"+video_url;
						Uri uri = Uri.parse(path);
						mMediaPlayer = MediaPlayer.create(VideoTestActivity.this, uri);
						mMediaPlayer.setDisplay(holder);

						holder.setFixedSize( mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
						mMediaPlayer.start();
					}
				});
			}
		});
		th.start();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		playVideo();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	    if(mMediaPlayer != null){
	        mMediaPlayer.release();
	        mMediaPlayer = null;
	    }		
	}

}
