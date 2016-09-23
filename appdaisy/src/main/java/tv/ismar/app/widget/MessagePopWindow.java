package tv.ismar.app.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import tv.ismar.app.R;

/**
 * Created by huaijie on 9/24/15.
 */
public class MessagePopWindow extends PopupWindow implements View.OnClickListener {
    private Button confirmBtn;
    private Button cancelBtn;
    private TextView firstMessage;
    private TextView secondMessage;
    private ConfirmListener confirmListener;
    private CancelListener cancleListener;
    public boolean isConfirmClick = false;

    private Context mContext;

    public interface CancelListener {
        void cancelClick(View view);
    }

    public interface ConfirmListener {
        void confirmClick(View view);
    }


    public MessagePopWindow(Context context) {
        mContext = context;
        int width = (int) (context.getResources().getDimension(R.dimen.pop_width));
        int height = (int) (context.getResources().getDimension(R.dimen.pop_height));

        setWidth(width);
        setHeight(height);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_message, null);
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
        setContentView(contentView);

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.popwindow_bg));
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
        setHeight((int) (mContext.getResources().getDimension(R.dimen.pop_double_line_height)));
        secondMessage.setVisibility(View.VISIBLE);
        secondMessage.setText(messageId);
    }

    public void setSecondMessage(String message) {
        setHeight((int) (mContext.getResources().getDimension(R.dimen.pop_double_line_height)));
        secondMessage.setVisibility(View.VISIBLE);
        secondMessage.setText(message);
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


    public void showAtLocation(View parent, int gravity, int x, int y, ConfirmListener confirmListener,
                               CancelListener cancleListener) {
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
