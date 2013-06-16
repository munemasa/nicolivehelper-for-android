package jp.miku39.android.nicolivehelper2;

import java.util.Date;

import jp.miku39.android.nicolivehelper2.fragments.VideoPlaybackFragment;
import jp.miku39.android.nicolivehelper2.libs.SimpleWebProxy;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NicoLiveHelperMainActivity extends Activity implements TabListener {
	final static String TAG = "NicoLiveHelperMainActivity";

	String mLvid;

	CommentServer mCommentReceiveThread;
	PlayerStatus mPlayerStatus;
	PublishStatus mPublishStatus;

	TableLayout mCommentTable;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nicolivehelper_main);

		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		initTab(bar);

		Intent intent = getIntent();
		mLvid = intent.getStringExtra("lvid");

		mCommentTable = (TableLayout) findViewById(R.id.commenttable);

		Log.d(TAG, "Request Id=" + mLvid);

		Thread th;
		th = new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleWebProxy.runServer(8081);
			}
		});
		th.start();

		th = new Thread(new Runnable() {
			@Override
			public void run() {
				new PlayerStatus(mLvid);
				mCommentReceiveThread = new CommentServer(
						NicoLiveHelperMainActivity.this, PlayerStatus.sAddr,
						PlayerStatus.sPort, PlayerStatus.sThread);
				mCommentReceiveThread.start();
			}
		});
		th.start();
	}

	private void initTab(ActionBar bar) {
		Tab tab;
		tab = bar.newTab();
		tab.setText("リクエスト");
		tab.setTabListener(this);
		bar.addTab(tab);

		tab = bar.newTab();
		tab.setText("ストック");
		tab.setTabListener(this);
		bar.addTab(tab);

		tab = bar.newTab();
		tab.setText("コメント");
		tab.setTabListener(this);
		bar.addTab(tab);

		tab = bar.newTab();
		tab.setText("再生履歴");
		tab.setTabListener(this);
		bar.addTab(tab);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mCommentReceiveThread.finish();

		SimpleWebProxy.terminate();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onTabReselected");
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onTabSelected");
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onTabUnselected");
	}

	@SuppressWarnings("deprecation")
	public void addComment(Comment c) {
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.commentrow, null);
		TextView v;
		v = (TextView) view.findViewById(R.id.comment_no);
		v.setText(c.comment_no.toString());
		v = (TextView) view.findViewById(R.id.comment_text);
		if (c.premium == 2 || c.premium == 3) {
			v.setBackgroundColor(0xffffeeee);
		}

		if (c.premium == 3) {
			v.setText(c.text.replaceAll("<.*?>", ""));
		} else {
			v.setText(c.text);
		}
		v = (TextView) view.findViewById(R.id.comment_date);
		final Date d = new Date(c.date * 1000);
		int m = d.getMinutes();
		int s = d.getSeconds();
		final String datestr = d.getMonth() + "/" + d.getDate() + " "
				+ d.getHours() + ":" + (m < 10 ? "0" + m : m) + ":"
				+ (s < 10 ? "0" + s : s);
		v.setText(datestr);

		mCommentTable.addView(view, 0);
		try {
			mCommentTable.removeViewAt(100);
		} catch (Exception e) {
			// 存在チェックせずに削除してるだけなので何もしなくてok
		}
	}

	public void playbackVideo(String video_id) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		Fragment newFragment = new VideoPlaybackFragment();
		Bundle args = new Bundle();
		args.putString("video_id", video_id);
		newFragment.setArguments(args);

		final String tag = "tag_videoplayback";
		ft.replace(R.id.videoplayback_fragment_container, newFragment, tag);
		ft.commit();
	}

	public void showToast(String msg, int length) {
		Toast.makeText(this, msg, length).show();
	}

}
