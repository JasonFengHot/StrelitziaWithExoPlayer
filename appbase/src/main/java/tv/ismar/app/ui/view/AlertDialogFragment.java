package tv.ismar.app.ui.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import tv.ismar.app.R;

public class AlertDialogFragment extends DialogFragment {

    public static final int NETWORK_EXCEPTION_DIALOG = 1;
    public static final int ITEM_OFFLINE_DIALOG = 2;

    private DialogInterface.OnClickListener mPositiveListener;
    private DialogInterface.OnClickListener mNegativeListener;

    public static AlertDialogFragment newInstance(int dialogType) {
        AlertDialogFragment f = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogType", dialogType);
        f.setArguments(args);
        return f;
    }

    public void setPositiveListener(DialogInterface.OnClickListener listener) {
        mPositiveListener = listener;
    }

    public void setNegativeListener(DialogInterface.OnClickListener listener) {
        mNegativeListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setStyle(R.style.MyDialog, android.R.style.Theme_Dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int dialogType = getArguments().getInt("dialogType");
        Dialog dialog = null;
        switch (dialogType) {
            case NETWORK_EXCEPTION_DIALOG:
                dialog =
                        new CustomDialog.Builder(getActivity())
                                .setMessage(R.string.vod_get_data_error)
                                .setPositiveButton(R.string.vod_retry, mPositiveListener)
                                .setNegativeButton(R.string.vod_ok, mNegativeListener)
                                .create();
                break;
            case ITEM_OFFLINE_DIALOG:
                dialog =
                        new CustomDialog.Builder(getActivity())
                                .setMessage(R.string.item_offline)
                                .setPositiveButton(R.string.delete_history, mPositiveListener)
                                .setNegativeButton(R.string.vod_cancel, mNegativeListener)
                                .create();
                break;
        }

        return dialog;
    }
}
