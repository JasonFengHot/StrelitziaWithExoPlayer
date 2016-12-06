package tv.ismar.player.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.reporter.EventReporter;
import tv.ismar.app.network.entity.AdElementEntity;
import tv.ismar.player.R;

/**
 * Created by Beaver on 2016/6/16.
 */
public class AdImageDialog extends Dialog {

    private static final String TAG = "LH/AdImageDialog";

    private Context mContext;
    private int width;
    private int height;
    private long mDuration;
    private EventReporter mEventReporter;
    private List<AdElementEntity> mAdElementEntityList;
    private int mCurrentAdIndex = 0;
    private ImageView imageView;
    private Button button;

    public AdImageDialog(Context context, int theme, List<AdElementEntity> adElementEntityList) {
        super(context, theme);
        mContext = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        mAdElementEntityList = adElementEntityList;
        mEventReporter = new EventReporter();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_pause_ad);
        imageView = (ImageView) findViewById(R.id.player_pause_image);
        button = (Button) findViewById(R.id.player_pause_close);
        button.setVisibility(View.GONE);

        mDuration = TrueTime.now().getTime();

        if (sensorTimer == null) {
            sensorTimer = new Timer();
            myTimerTask = new MyTimerTask();
            sensorTimer.schedule(myTimerTask, 1000, 1000);
        }

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ((int) (width * 0.53));
        lp.height = ((int) (height * 0.53));
        lp.gravity = Gravity.CENTER;
        button.requestFocus();
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    // 定时器
    private Timer sensorTimer;
    private MyTimerTask myTimerTask;

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (imageView != null && button != null) {
                        AdElementEntity element = mAdElementEntityList.get(mCurrentAdIndex);
                        mEventReporter.pause_ad_download(element.getTitle(), element.getMedia_id(), element.getMedia_url(), "bestv");

                        Picasso.with(mContext).load(element.getMedia_url())
                                .into(imageView, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        button.setVisibility(View.VISIBLE);
                                        button.requestFocus();
                                    }

                                    @Override
                                    public void onError() {
                                    }
                                });

                        if (mCurrentAdIndex == mAdElementEntityList.size() - 1) {
                            mCurrentAdIndex = 0;
                        } else {
                            mCurrentAdIndex++;
                        }
                    }
                }
            });
        }

    }

    private void cancelTimer() {
        if (myTimerTask != null) {
            myTimerTask.cancel();
            myTimerTask = null;
        }
        if (sensorTimer != null) {
            sensorTimer.cancel();
            sensorTimer = null;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                dismiss();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void dismiss() {
        cancelTimer();
        super.dismiss();
        mDuration = TrueTime.now().getTime() - mDuration;
        mEventReporter.pause_ad_play(
                mAdElementEntityList.get(mCurrentAdIndex).getTitle(),
                mAdElementEntityList.get(mCurrentAdIndex).getMedia_id(),
                mAdElementEntityList.get(mCurrentAdIndex).getMedia_url(),
                mDuration, "bestv");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
