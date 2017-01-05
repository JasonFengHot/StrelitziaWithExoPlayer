package tv.ismar.app.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.app.R;

/**
 * Created by liucan on 2017/1/5.
 */

public class NoNetConnectDialog extends Dialog implements View.OnClickListener{
    private Button confirmBtn;
    private Button cancelBtn;
    private TextView firstMessage;
    private TextView secondMessage;
    private ModuleMessagePopWindow.ConfirmListener confirmListener;
    private ModuleMessagePopWindow.CancelListener cancleListener;
    public boolean isConfirmClick = false;
    private Context mContext;
    private int height;

    public NoNetConnectDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        int width = (int) (context.getResources().getDimension(R.dimen.module_pop_width));
        height = (int) (context.getResources().getDimension(R.dimen.module_pop_height));

        View contentView = LayoutInflater.from(context).inflate(R.layout.module_popup_message, null);
        contentView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.pop_bg_drawable));
        confirmBtn = (Button) contentView.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) contentView.findViewById(R.id.cancel_btn);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnHoverListener(new View.OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        cancelBtn.setOnHoverListener(new View.OnHoverListener() {

            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    v.requestFocus();
                }
                return false;
            }
        });
        firstMessage = (TextView) contentView.findViewById(R.id.first_text_info);
        secondMessage = (TextView) contentView.findViewById(R.id.pop_second_text);
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        relativeLayout.addView(contentView, layoutParams);
        setContentView(relativeLayout,layoutParams);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.confirm_btn) {
            if (confirmListener != null) {
                isConfirmClick = true;
                confirmListener.confirmClick(v);

            }

        } else if (i == R.id.cancel_btn) {
            if (cancleListener != null) {
                isConfirmClick = false;
                cancleListener.cancelClick(v);
            }

        }
    }

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }

    public void setConfirmBtn(String text) {
        confirmBtn.setText(text);
    }

    public void setCancelBtn(String text) {
        cancelBtn.setText(text);
    }
    public void setFirstMessage(String message) {
        firstMessage.setText(message);
    }

    @Override
    public void onBackPressed() {

    }
    public void keyListen(ModuleMessagePopWindow.ConfirmListener confirmListener, ModuleMessagePopWindow.CancelListener cancleListener){
        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        isConfirmClick = false;
    }
}
