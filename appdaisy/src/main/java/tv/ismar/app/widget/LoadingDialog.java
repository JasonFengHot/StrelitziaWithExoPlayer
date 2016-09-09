package tv.ismar.app.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.app.R;

public class LoadingDialog extends Dialog {
    private final RotateAnimation rotate;
    private Context mContext;
    private TextView tipTextView;
    private ImageView img2;

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
        setContentView(R.layout.layout_loading);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        tipTextView = (TextView) findViewById(R.id.tipTextView);// 提示文字
        img2 = (ImageView) findViewById(R.id.dialog_back_img2);
        // 加载动画
        rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(2000);
        rotate.setRepeatCount(-1);
    }

    public void showDialog() {
        if (!this.isShowing()) {
            if (rotate != null) {
                img2.startAnimation(rotate);
            }
            show();
        }
    }

    public void setTvText(String text) {
        if (tipTextView != null) {
            tipTextView.setVisibility(View.VISIBLE);
            tipTextView.setText(text);
        }
    }

    public void setProgress(int progress) {
        tipTextView.setVisibility(View.VISIBLE);
        tipTextView.setText(progress + "%");
    }

    public void hideTipTv() {
        tipTextView.setVisibility(View.GONE);
    }

}
