package jp.miku39.android.nicolivehelper2.fragments;

import jp.miku39.android.nicolivehelper2.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class AboutDialogFragment extends DialogFragment {

    public static AboutDialogFragment newInstance() {
    	AboutDialogFragment f = new AboutDialogFragment();
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getString(R.string.app_name);

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_launcher)
                .setTitle(title)
                .setMessage("Developer Preview Version\nDeveloped by amano.")
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	dismiss();
                        }
                    }
                )
                .create();
    }

}
