package tv.ismar.pay;

import android.os.CountDownTimer;
import android.widget.TextView;


public class IsmartCountTimer extends CountDownTimer {

    public static final int TIME_COUNT = 61000;
    private TextView view;
    private int endStrRid;
    private int normalColor, timingColor;

    public IsmartCountTimer(long millisInFuture, long countDownInterval) {
        // TODO Auto-generated constructor stub
        super(millisInFuture, countDownInterval);
        this.view = view;
        this.endStrRid = endStrRid;
    }

    public IsmartCountTimer(TextView view, int endStrRid) {
        super(TIME_COUNT, 1000);
        this.view = view;
        this.endStrRid = endStrRid;
    }

    public IsmartCountTimer(TextView view) {
        super(TIME_COUNT, 1000);
        this.view = view;
        this.endStrRid = R.string.account_connect_error;
    }

    public IsmartCountTimer(TextView tv_varify, int normalColor, int timingColor) {
        this(tv_varify);
        this.normalColor = normalColor;
        this.timingColor = timingColor;
    }

    @Override
    public void onFinish() {
        if (normalColor > 0) {
            // btn.setTextColor(normalColor);
            view.setBackgroundResource(normalColor);
        }
        view.setText("获取验证码");
        view.setEnabled(true);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (timingColor > 0) {
            // view.setTextColor(timingColor);
            view.setBackgroundResource(timingColor);
        }
        view.setEnabled(false);
        view.setText(millisUntilFinished / 1000 + "s");
    }

}
