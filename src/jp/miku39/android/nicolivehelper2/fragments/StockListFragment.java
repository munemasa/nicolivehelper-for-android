package jp.miku39.android.nicolivehelper2.fragments;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import jp.miku39.android.nicolivehelper2.NicoLiveHelperMainActivity;
import jp.miku39.android.nicolivehelper2.PlayerStatus;
import jp.miku39.android.nicolivehelper2.R;
import jp.miku39.android.nicolivehelper2.VideoInformation;
import jp.miku39.android.nicolivehelper2.VideoListAdapter;
import jp.miku39.android.nicolivehelper2.libs.Lib;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class StockListFragment extends ListFragment {
	final static String TAG = "StockListFragment";
	final static String sFilename ="stockfile";

	ArrayList<VideoInformation> mStocks = new ArrayList<VideoInformation>();
	VideoListAdapter mListViewAdapter;

	private EditText mEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		readFromFile();
	}
	
	void writeToFile(){
		try {
			OutputStream os = getActivity().openFileOutput( sFilename, Context.MODE_PRIVATE );
			ObjectOutputStream oos;
			oos = new ObjectOutputStream( os );

			for(int i=0;i<mStocks.size();i++){
				oos.writeObject( mStocks.get(i) );
			}
			
			oos.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	void readFromFile(){
		mStocks.clear();
		try {
			InputStream is = getActivity().openFileInput( sFilename );
			ObjectInputStream ois;
			ois = new ObjectInputStream( is );
			try{
				while(true){
					final VideoInformation v = (VideoInformation) ois.readObject();
					if( v==null ) break;
					mStocks.add( v );
				}
			}catch( EOFException e){
			}
			ois.close();
			is.close();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		writeToFile();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		View v = inflater.inflate(R.layout.fragment_stock, container, false);

		mListViewAdapter = new VideoListAdapter(getActivity(),
				R.layout.videoinformation, mStocks);
		setListAdapter(mListViewAdapter);

		Button btn = (Button) v.findViewById(R.id.btn_addstock);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Lib.hideSoftwareKeyboard(getActivity(), v);
				addStock();
			}
		});

		mEditText = (EditText) v.findViewById(R.id.edit_addstock);
		return v;
	}

	public void addStock() {
		final String video_id = mEditText.getEditableText().toString().trim();
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				final VideoInformation v = new VideoInformation(
						"http://ext.nicovideo.jp/api/getthumbinfo/" + video_id);
				if (isAdded()) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if( v.mVideoId==null ){
								((NicoLiveHelperMainActivity)getActivity()).showToast("動画情報の取得に失敗しました", Toast.LENGTH_LONG);
								return;
							}
							addStock(v);
							mEditText.setText("");
						}
					});
				}
			}
		});
		th.start();
	}

	public void addStock(VideoInformation v) {
		mStocks.add(v);
		mListViewAdapter.notifyDataSetChanged();
	}

	void playVideo(int n) {
		if (!PlayerStatus.sIsOwner)
			return;
		VideoInformation v = mStocks.get(n);
		String str = "/play " + v.mVideoId;
		((NicoLiveHelperMainActivity) getActivity()).sendComment(str, "", "");
	}

	void prepareVideo(int n) {
		if (!PlayerStatus.sIsOwner)
			return;
		VideoInformation v = mStocks.get(n);
		String str = "/prepare " + v.mVideoId;
		((NicoLiveHelperMainActivity) getActivity()).sendComment(str, "", "");
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
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
					mStocks.remove(pos);
					mListViewAdapter.notifyDataSetChanged();
					break;
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
