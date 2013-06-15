package jp.miku39.android.nicolivehelper2;

import jp.miku39.android.nicolivehelper2.fragments.VideoPlaybackFragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NicoLiveHelperMainActivity extends Activity {
	final static String TAG = "NicoLiveHelperMainActivity";
	
	String mLvid;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_nicolivehelper_main);
	    
	    Intent intent = getIntent();
	    mLvid = intent.getStringExtra("lvid");

	    Log.d(TAG,"Request Id="+mLvid);
	    
        if (savedInstanceState == null) {
            // First-time init; create fragment to embed in activity.
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment newFragment = new VideoPlaybackFragment();
            Bundle args = new Bundle();
            args.putString("video_id", "sm17239967");
            newFragment.setArguments(args);

            final String tag = "tag_videoplayback";
            ft.add(R.id.fragment_container, newFragment, tag);
            ft.commit();
        }

	}

}
