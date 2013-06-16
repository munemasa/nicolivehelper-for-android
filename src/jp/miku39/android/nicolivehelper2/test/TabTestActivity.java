package jp.miku39.android.nicolivehelper2.test;

import jp.miku39.android.nicolivehelper2.R;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class TabTestActivity extends Activity implements TabListener {
	final static String TAG = "TabTestActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_test);

		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tab_test, menu);
		return true;
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

}
