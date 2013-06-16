package jp.miku39.android.nicolivehelper2;

import java.net.CookieHandler;

import jp.miku39.android.nicolivehelper2.libs.NicoCookie;
import jp.miku39.android.nicolivehelper2.test.FragmentTestActivity;
import jp.miku39.android.nicolivehelper2.test.TabTestActivity;
import jp.miku39.android.nicolivehelper2.test.TestTabSwipeActivity;
import jp.miku39.android.nicolivehelper2.test.VideoTestActivity;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;

public class MainActivity extends Activity {
	final static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Button btn = (Button) findViewById(R.id.btn_nicoweb);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						NicoWebActivity.class);
				startActivity(intent);
			}
		});

		btn = (Button) findViewById(R.id.btn_videotest);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						VideoTestActivity.class);
				intent.putExtra("video_id", "sm19695136");
				startActivity(intent);
			}
		});

		btn = (Button) findViewById(R.id.btn_fragmenttest);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						FragmentTestActivity.class);
				startActivity(intent);
			}
		});

		btn = (Button) findViewById(R.id.btn_tabswipte_test);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						TabTestActivity.class);
				startActivity(intent);
			}
		});

		CookieSyncManager.createInstance(this);
		CookieSyncManager.getInstance().startSync();
		CookieManager.getInstance().setAcceptCookie(true);

		Log.d(TAG, "Nico Cookie=" + NicoCookie.getCookie("nicovideo.jp"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CookieSyncManager.getInstance().stopSync();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
