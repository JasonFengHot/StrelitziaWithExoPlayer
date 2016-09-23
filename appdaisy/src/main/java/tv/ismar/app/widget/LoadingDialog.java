package tv.ismar.app.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tv.ismar.app.R;

public class LoadingDialog extends Dialog implements DialogInterface.OnDismissListener {
    private Context mContext;
    private TextView tipTextView;
    private ImageView dialog_back_img;
    private AnimationDrawable animationDrawable;

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
        setContentView(R.layout.layout_loading);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        tipTextView = (TextView) findViewById(R.id.tipTextView);// 提示文字

        dialog_back_img = (ImageView) findViewById(R.id.dialog_back_img);
        dialog_back_img.setBackgroundResource(R.drawable.module_loading);
        animationDrawable = (AnimationDrawable) dialog_back_img.getBackground();
        setOnDismissListener(this);
    }

    public void showDialog() {
        if (!this.isShowing()) {
            show();
            if (animationDrawable != null && !animationDrawable.isRunning()) {
                animationDrawable.start();
            }
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (animationDrawable != null && animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
    }
}
