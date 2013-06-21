package jp.miku39.android.nicolivehelper2;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jp.miku39.android.nicolivehelper2.fragments.AboutDialogFragment;
import jp.miku39.android.nicolivehelper2.fragments.CommentViewFragment;
import jp.miku39.android.nicolivehelper2.fragments.RequestListFragment;
import jp.miku39.android.nicolivehelper2.fragments.StockListFragment;
import jp.miku39.android.nicolivehelper2.fragments.VideoPlaybackFragment;
import jp.miku39.android.nicolivehelper2.libs.Http;
import jp.miku39.android.nicolivehelper2.libs.Lib;
import jp.miku39.android.nicolivehelper2.libs.SimpleWebProxy;

import org.w3c.dom.Document;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class NicoLiveHelperMainActivity extends Activity implements TabListener {
	final static String TAG = "NicoLiveHelperMainActivity";

	final static int TAB_COMMENT = 0;
	final static int TAB_REQUEST = 1;
	final static int TAB_STOCK = 2;
	final static int TAB_HISTORY = 3;
	final static int MAX_TAB = 4;
	View[] mTabViews = new View[MAX_TAB]; // タブ選択で表示・非表示を切り替える用

	String mLvid;

	CommentServer mCommunicationThread;
	PlayerStatus mPlayerStatus;
	PublishStatus mPublishStatus;

	int mPanes;

	private CommentViewFragment mCommentFragment;
	private RequestListFragment mRequestFragment;
	@SuppressWarnings("unused")
	private StockListFragment mStockFragment;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_nicolivehelper_main);

		mPanes = getResources().getInteger(R.integer.panes);

		Intent intent = getIntent();
		mLvid = intent.getStringExtra("lvid");
		Log.d(TAG, "Request Id=" + mLvid);

		mCommentFragment = (CommentViewFragment) getFragmentManager()
				.findFragmentById(R.id.commentview_fragment);
		mRequestFragment = (RequestListFragment) getFragmentManager()
				.findFragmentById(R.id.requestlist_fragment);
		mStockFragment = (StockListFragment) getFragmentManager()
				.findFragmentById(R.id.stocklist_fragment);

		mTabViews[0] = findViewById(R.id.tab_comment);
		mTabViews[1] = findViewById(R.id.tab_request);
		mTabViews[2] = findViewById(R.id.tab_stock);
		mTabViews[3] = findViewById(R.id.tab_history);

		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		initActionBarTab(bar);
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

		OnFocusChangeListener editTextOnFocusChange = new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// EditTextのフォーカスが外れた場合
				if (hasFocus == false) {
					// 処理を行う
					Lib.hideSoftwareKeyboard(NicoLiveHelperMainActivity.this, v);
				}
			}
		};
		et.setOnFocusChangeListener(editTextOnFocusChange);

		et = (EditText) findViewById(R.id.edit_history);
		et.setOnFocusChangeListener(editTextOnFocusChange);

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

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String s = PlayerStatus.sTitle + "\n";
						addHistory(s);
					}
				});

				// 生主なら主コメ用のgetpublishstatusでトークン取りを
				if (PlayerStatus.sIsOwner) {
					new PublishStatus(PlayerStatus.sLiveId);
				}

				mCommunicationThread = new CommentServer(
						NicoLiveHelperMainActivity.this, PlayerStatus.sAddr,
						PlayerStatus.sPort, PlayerStatus.sThread);
				mCommunicationThread.start();
			}
		});
		th.start();
	}

	private void initActionBarTab(ActionBar bar) {
		Tab tab;

		tab = bar.newTab();
		tab.setText("コメント");
		tab.setTabListener(this);
		bar.addTab(tab);

		tab = bar.newTab();
		tab.setText("リクエスト");
		tab.setTabListener(this);
		bar.addTab(tab);

		tab = bar.newTab();
		tab.setText("ストック");
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
	}
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int n = tab.getPosition();
		showTab(n);
	}
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	/**
	 * タブ選択によってビューの表示・非表示を切り替える
	 * 
	 * @param n
	 *            タブの番号
	 */
	void showTab(int n) {
		if (mPanes == 1) {
			// スマホなどのシングルペインの場合、どれか一つを表示すればOK
			for (int i = 0; i < MAX_TAB; i++) {
				if (mTabViews[i] != null) {
					if (i == n) {
						mTabViews[i].setVisibility(View.VISIBLE);
					} else {
						mTabViews[i].setVisibility(View.INVISIBLE);
					}
				}
			}
		} else if (mPanes == 3) {
			// 3ペインレイアウトなので、消す必要がないのもある
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nicolivehelpermainactivity, menu);
		return true;
	}

	public static void openAboutDialog(Activity activity) {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
		Fragment prev = activity.getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = AboutDialogFragment.newInstance();
		newFragment.show(ft, "dialog");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_startlive:
			startNicoLive();
			return true;
			
		case R.id.menu_open_officialapp:
			openOfficialApp(mLvid);
			return true;

		case R.id.menu_about:
			openAboutDialog(this);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void openOfficialApp(String id) {
		String str = "http://live.nicovideo.jp/watch/" + id;
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
		startActivity(intent);
	}

	private void startNicoLive() {
		if (!PlayerStatus.sIsOwner)
			return;

		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				String conf = "http://watch.live.nicovideo.jp/api/configurestream/"
						+ PlayerStatus.sLiveId
						+ "?key=hq&value=0&version=2&token="
						+ PublishStatus.sToken;
				Document doc = Http.getDocument(conf);
				boolean flg = false;
				if (doc.getElementsByTagName("response_configurestream")
						.item(0).getAttributes().getNamedItem("status")
						.getTextContent().equals("ok")) {
					conf = "http://watch.live.nicovideo.jp/api/configurestream/"
							+ PlayerStatus.sLiveId
							+ "?key=exclude&value=0&version=2&token="
							+ PublishStatus.sToken;
					doc = Http.getDocument(conf);
					if (doc.getElementsByTagName("response_configurestream")
							.item(0).getAttributes().getNamedItem("status")
							.getTextContent().equals("ok")) {
						flg = true;
						// 放送開始時刻を更新
						XPath xpath = XPathFactory.newInstance().newXPath();
						try {
							PlayerStatus.sStartTime = Long.parseLong(xpath
									.evaluate("//start_time", doc));
							PlayerStatus.sEndTime = Long.parseLong(xpath
									.evaluate("//end_time", doc));
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (XPathExpressionException e) {
							e.printStackTrace();
						}
					}
				}
				// TODO ダイアログ表示を消す
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("放送を開始しました", Toast.LENGTH_LONG);
					}
				});
			}
		});
		th.start();
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

	public void sendComment(final String comment, final String mail,
			final String name) {
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				mCommunicationThread.sendComment(comment, mail, name);
			}
		});
		th.start();
	}

	public void addHistory(String s) {
		EditText edit = (EditText) findViewById(R.id.edit_history);
		edit.append(s);
	}

	/**
	 * コメントを表示する
	 * 
	 * @param c
	 *            コメントデータ
	 */
	public void addComment(Comment c) {
		mCommentFragment.addComment(c);
	}

	/**
	 * リクエストを追加する
	 * 
	 * @param v
	 *            動画情報
	 */
	public void addRequest(VideoInformation v) {
		mRequestFragment.addRequest(v);
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
