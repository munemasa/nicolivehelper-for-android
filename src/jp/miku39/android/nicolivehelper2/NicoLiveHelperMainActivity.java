package jp.miku39.android.nicolivehelper2;

import java.util.Date;

import jp.miku39.android.nicolivehelper2.fragments.VideoPlaybackFragment;
import jp.miku39.android.nicolivehelper2.libs.Lib;
import jp.miku39.android.nicolivehelper2.libs.SimpleWebProxy;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class NicoLiveHelperMainActivity extends Activity implements TabListener {
	final static String TAG = "NicoLiveHelperMainActivity";

	String mLvid;

	CommentServer mCommunicationThread;
	PlayerStatus mPlayerStatus;
	PublishStatus mPublishStatus;

	TableLayout mCommentTable;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_nicolivehelper_main);

		Intent intent = getIntent();
		mLvid = intent.getStringExtra("lvid");
		Log.d(TAG, "Request Id=" + mLvid);

		mCommentTable = (TableLayout) findViewById(R.id.commenttable);

		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		initTab(bar);
		startThreads();

		// ソフトキーボードでSend押したときの処理
		EditText et = (EditText) findViewById(R.id.inputcomment);
		et.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					Lib.hideSoftwareKeyboard(NicoLiveHelperMainActivity.this, v);
					sendComment();
					handled = true;
				}
				return handled;
			}
		});

		// 送信ボタン
		Button btn = (Button) findViewById(R.id.btn_send);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Lib.hideSoftwareKeyboard(NicoLiveHelperMainActivity.this, v);
				sendComment();
			}
		});
	}

	private void startThreads() {
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
				mCommunicationThread = new CommentServer(
						NicoLiveHelperMainActivity.this, PlayerStatus.sAddr,
						PlayerStatus.sPort, PlayerStatus.sThread);
				mCommunicationThread.start();
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

		mCommunicationThread.finish();

		SimpleWebProxy.terminate();
	}

	@Override
	public void onBackPressed() {
		if (!PlayerStatus.sStatus.equals("ok")) {
			finish();
			return;
		}
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
		// タイトルを設定
		alertDialogBuilder.setTitle("Caution !");
		// メッセージを設定
		alertDialogBuilder.setMessage("生放送から切断しますか？");
		// アイコンを設定
		alertDialogBuilder.setIcon(R.drawable.ic_launcher);
		// Positiveボタンとリスナを設定
		alertDialogBuilder.setPositiveButton(getString(android.R.string.yes),
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		// Negativeボタンとリスナを設定
		alertDialogBuilder.setNegativeButton(getString(android.R.string.no),
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		// ダイアログを表示
		alertDialogBuilder.create().show();
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

	/**
	 * コメントを送信する
	 */
	private void sendComment() {
		EditText et = (EditText) findViewById(R.id.inputcomment);
		final String comment = et.getEditableText().toString();
		// TODO コマンドなど
		final String mail = "184";
		final String name = "";
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				mCommunicationThread.sendComment(comment, mail, name);
			}
		});
		th.start();
	}

	/**
	 * コメントを表示する
	 * 
	 * @param c
	 *            コメントデータ
	 */
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
		final String datestr = d.getHours() + ":" + (m < 10 ? "0" + m : m)
				+ ":" + (s < 10 ? "0" + s : s);
		v.setText(datestr);

		mCommentTable.addView(view, 0);
		try {
			mCommentTable.removeViewAt(100);
		} catch (Exception e) {
			// 存在チェックせずに削除してるだけなので何もしなくてok
		}
	}

	/**
	 * 動画を再生する.
	 * 
	 * @param video_id
	 *            再生する動画ID
	 */
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

	/**
	 * トーストメッセージを表示する
	 * 
	 * @param msg
	 *            表示するメッセージ
	 * @param length
	 *            表示時間
	 */
	public void showToast(String msg, int length) {
		Toast.makeText(this, msg, length).show();
	}

}
