package jp.miku39.android.nicolivehelper2.fragments;

import java.util.ArrayList;

import jp.miku39.android.nicolivehelper2.NicoLiveHelperMainActivity;
import jp.miku39.android.nicolivehelper2.PlayerStatus;
import jp.miku39.android.nicolivehelper2.R;
import jp.miku39.android.nicolivehelper2.VideoInformation;
import jp.miku39.android.nicolivehelper2.VideoListAdapter;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RequestListFragment extends ListFragment implements
		OnItemClickListener {
	final static String TAG = "RequestListFragment";
	private ListView mRequestListView;

	ArrayList<VideoInformation> mRequests = new ArrayList<VideoInformation>();
	VideoListAdapter mListViewAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		View v = inflater.inflate(R.layout.fragment_request, container, false);

		mListViewAdapter = new VideoListAdapter(getActivity(),
				R.layout.videoinformation, mRequests);
		setListAdapter( mListViewAdapter );
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemClickListener(this);
	}

	public void addRequest(VideoInformation v) {
		mRequests.add(v);
		mListViewAdapter.notifyDataSetChanged();
	}

	void playVideo(int n) {
		if (!PlayerStatus.sIsOwner)
			return;
		VideoInformation v = mRequests.get(n);
		String str = "/play " + v.mVideoId;
		((NicoLiveHelperMainActivity) getActivity()).sendComment(str, "", "");

		mRequests.remove(n);
		mListViewAdapter.notifyDataSetChanged();
	}

	void prepareVideo(int n) {
		if (!PlayerStatus.sIsOwner)
			return;
		VideoInformation v = mRequests.get(n);
		String str = "/prepare " + v.mVideoId;
		((NicoLiveHelperMainActivity) getActivity()).sendComment(str, "", "");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final CharSequence[] items = { "Play", "Prepare", "Delete" };
		final int pos = position;

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Action");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0: // play
					playVideo(pos);
					break;
				case 1: // prepare
					prepareVideo(pos);
					break;
				case 2: // delete
					mRequests.remove(pos);
					mListViewAdapter.notifyDataSetChanged();
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
