package tv.ismar.app.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.ismar.app.R;

/**
 * Created by liucan on 2016/12/19.
 */

public class NoNetModuleMessagePop extends PopupWindow implements View.OnClickListener{
    private Button confirmBtn;
    private Button cancelBtn;
    private TextView firstMessage;
    private TextView secondMessage;
    private ModuleMessagePopWindow.ConfirmListener confirmListener;
    private ModuleMessagePopWindow.CancelListener cancleListener;
    public boolean isConfirmClick = false;

    private Context mContext;

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }

    int height;

    public NoNetModuleMessagePop(){

    }

    public NoNetModuleMessagePop(Context context) {
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();

        int width = (int) (context.getResources().getDimension(R.dimen.module_pop_width));
        height = (int) (context.getResources().getDimension(R.dimen.module_pop_height));

        setWidth(screenWidth);
        setHeight(screenHeight);

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
//        relativeLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popwindow_bg));
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        relativeLayout.addView(contentView, layoutParams);


        setContentView(relativeLayout);
        setFocusable(true);

    }


    public void setBackgroundRes(int resId) {
        setBackgroundDrawable(mContext.getResources().getDrawable(resId));
    }

    public void setFirstMessage(int messageId) {
        firstMessage.setText(messageId);
    }

    public void setFirstMessage(String message) {
        firstMessage.setText(message);
    }

    public void setSecondMessage(int messageId) {
        height = ((int) (mContext.getResources().getDimension(R.dimen.module_pop_double_line_height)));
        secondMessage.setVisibility(View.VISIBLE);
        secondMessage.setText(messageId);
    }

    public void setConfirmBtn(String text) {
        confirmBtn.setText(text);
    }

    public void setCancelBtn(String text) {
        cancelBtn.setText(text);
    }

    public void setSecondMessage(String message) {
        height = ((int) (mContext.getResources().getDimension(R.dimen.module_pop_double_line_height)));
        secondMessage.setVisibility(View.VISIBLE);
        secondMessage.setText(message);
    }

    public void hideCancelBtn() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                mContext.getResources().getDimensionPixelSize(R.dimen.pop_btn_width),
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        lp.rightMargin = 0;
        confirmBtn.setLayoutParams(lp);
        cancelBtn.setVisibility(View.GONE);
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


    public void showAtLocation(View parent, int gravity, int x, int y, ModuleMessagePopWindow.ConfirmListener confirmListener,
                               ModuleMessagePopWindow.CancelListener cancleListener) {
        if (confirmListener == null) {
            confirmBtn.setVisibility(View.GONE);
        }

        if (cancleListener == null) {
            cancelBtn.setVisibility(View.GONE);
        }
        this.confirmListener = confirmListener;
        this.cancleListener = cancleListener;
        isConfirmClick = false;
        super.showAtLocation(parent, gravity, x, y);
    }
}