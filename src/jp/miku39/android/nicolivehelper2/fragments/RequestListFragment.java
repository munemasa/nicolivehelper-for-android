package jp.miku39.android.nicolivehelper2.fragments;

import java.util.ArrayList;

import jp.miku39.android.nicolivehelper2.R;
import jp.miku39.android.nicolivehelper2.VideoInformation;
import jp.miku39.android.nicolivehelper2.VideoListAdapter;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RequestListFragment extends Fragment {
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

		View v = inflater.inflate(R.layout.fragment_request, container,
				false);
		
		mRequestListView = (ListView)v.findViewById(R.id.lv_requestlist);

    	mListViewAdapter = new VideoListAdapter(getActivity(), R.layout.videoinformation, mRequests);
    	mRequestListView.setAdapter(mListViewAdapter);

		return v;
	}

	public void addRequest( VideoInformation v ){
		mRequests.add(v);
		mListViewAdapter.notifyDataSetChanged();
	}
}
