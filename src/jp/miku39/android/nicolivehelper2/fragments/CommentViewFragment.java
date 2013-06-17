package jp.miku39.android.nicolivehelper2.fragments;

import java.util.Date;

import jp.miku39.android.nicolivehelper2.Comment;
import jp.miku39.android.nicolivehelper2.R;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

public class CommentViewFragment extends Fragment {
	final static String TAG = "CommentViewFragment";

	private TableLayout mCommentTable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		View v = inflater.inflate(R.layout.fragment_commentview, container,
				false);
		
		mCommentTable = (TableLayout)v.findViewById(R.id.commenttable);
		
		return v;
	}

	/**
	 * コメントを表示する
	 * 
	 * @param c
	 *            コメントデータ
	 */
	@SuppressWarnings("deprecation")
	public void addComment(Comment c) {
		final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

}
