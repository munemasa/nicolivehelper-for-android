package jp.miku39.android.nicolivehelper2.fragments;

import jp.miku39.android.nicolivehelper2.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class InputDialogFragment extends DialogFragment {
	final static String TAG = "InputDialogFragment";

	String mCaption;
	String mText;

	public interface Callback {
		public void onReturnValue(String str);
	}

	public static InputDialogFragment newInstance(String caption, String text) {
		InputDialogFragment f = new InputDialogFragment();
		Bundle args = new Bundle();
		args.putString("caption", caption);
		args.putString("text", text);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCaption = getArguments().getString("caption");
		mText = getArguments().getString("text");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final EditText et = new EditText(getActivity());

		return new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.ic_launcher)
				.setTitle(mCaption)
				.setMessage(mText)
				.setView(et)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String str = et.getEditableText().toString();
								Log.d(TAG,str);
								((Callback)getActivity()).onReturnValue(str);
								dismiss();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dismiss();
							}
						}).create();
	}

}
