package jp.miku39.android.nicolivehelper2;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoListAdapter extends ArrayAdapter {

	private LayoutInflater mInflater;
	private List<VideoInformation> mItems;

	public VideoListAdapter(Context context, int textViewResourceId,
			List objects) {
		super(context, textViewResourceId, objects);
		mItems = objects;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(R.layout.videoinformation, null);
			// view.setBackgroundResource(android.R.drawable.status_bar_item_background);
		}

		VideoInformation item = (VideoInformation) mItems.get(position);
		TextView text;

		ImageView image = (ImageView) view.findViewById(R.id.video_thumbnail);
		image.setImageBitmap(item.mIcon);

		text = (TextView) view.findViewById(R.id.video_title);
		text.setText(item.mVideoId + " " + item.mTitle);

		text = (TextView) view.findViewById(R.id.video_length);
		text.setText("L:" + item.mLength + "秒");

		text = (TextView) view.findViewById(R.id.video_viewcounter);
		text.setText(" V:" + item.mViewCounter);

		text = (TextView) view.findViewById(R.id.video_commentnum);
		text.setText(" C:" + item.mCommentNum);

		text = (TextView) view.findViewById(R.id.video_mylistcounter);
		text.setText(" M:" + item.mMylistCounter);

		text = (TextView) view.findViewById(R.id.video_posteddate);
		text.setText("" + item.mFirstRetrieve);
		return view;
	}
}
