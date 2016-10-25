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

import cn.ismartv.turetime.TrueTime;
import tv.ismar.app.reporter.EventReporter;
import tv.ismar.app.network.entity.AdElementEntity;

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

    public AdImageDialog(Context context, List<AdElementEntity> adElementEntityList) {
        super(context);
        mContext = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        mAdElementEntityList = adElementEntityList;
        mEventReporter = new EventReporter();

        setWindowProperty();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout frameLayout = new FrameLayout(mContext);
        frameLayout.setLayoutParams(layoutParams);

        imageView = new ImageView(mContext);
        imageView.setLayoutParams(layoutParams);
        frameLayout.addView(imageView);

        button = new Button(mContext);
        button.setGravity(Gravity.RIGHT | Gravity.TOP);
        button.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
        button.setVisibility(View.GONE);
        frameLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mDuration = TrueTime.now().getTime();

        if (sensorTimer == null) {
            sensorTimer = new Timer();
            myTimerTask = new MyTimerTask();
            sensorTimer.schedule(myTimerTask, 1000, 1000);
        }

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

    public Window setWindowProperty() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ((int) (width * 0.53));
        lp.height = ((int) (height * 0.53));
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(0));
        return window;
    }

}
