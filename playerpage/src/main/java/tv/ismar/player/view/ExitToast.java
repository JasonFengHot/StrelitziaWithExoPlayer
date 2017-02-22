package tv.ismar.player.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import tv.ismar.player.R;

/**
 * Created by beaver on 16-12-15.
 */

public class ExitToast {

    private static ExitToast exitToast;
    private Toast toast;
    private SpannableString msp = null;
    private static int mDuration = 0;
    private Handler mHandler;

    private ExitToast() {
        mHandler = new Handler();

        msp = new SpannableString("再次点击“返回”，退出播放");
        msp.setSpan(new ForegroundColorSpan(Color.parseColor("#ff9933")), 5, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static ExitToast createToastConfig() {
        if (exitToast == null) {
            exitToast = new ExitToast();
        }
        return exitToast;
    }

    public void show(Context context, int duration) {
        View layout = LayoutInflater.from(context).inflate(R.layout.exit_toast, null);
        TextView text = (TextView) layout.findViewById(R.id.exit_text);
        text.setText(msp);
        toast = new Toast(context);
        toast.setView(layout);

        mDuration = duration;
        mHandler.post(showRunnable);
    }

    public void dismiss() {
        mDuration = 0;
        if (toast != null) {
            toast.cancel();
            mHandler.removeCallbacks(showRunnable);
            toast = null;
        }
    }

    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i("LH/", "duration:" + mDuration);
            if (mDuration != 0) {
                toast.show();
            } else {
                dismiss();
                return;
            }

            mHandler.postDelayed(showRunnable, 1000);
            mDuration -= 1000;

        }
    };

}
