package tv.ismar.app.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.utils.AppUtils;

import java.io.IOException;
import java.util.ArrayList;

import tv.ismar.app.R;

/**
 * Created by huibin on 11/17/16.
 */

public class UpdatePopupWindow extends PopupWindow implements View.OnHoverListener {

    private View tmp;

    public UpdatePopupWindow(final Context context, Bundle bundle) {
        super(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();

        int height = (int) (context.getResources().getDimension(R.dimen.app_update_bg_width));
        int width = (int) (context.getResources().getDimension(R.dimen.app_update_bg_height));

        setWidth(screenWidth);
        setHeight(screenHeight);

        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_update, null);

        tmp = contentView.findViewById(R.id.tmp);


        LinearLayout updateMsgLayout = (LinearLayout) contentView.findViewById(R.id.update_msg_layout);

        final String path = bundle.getString("path");

        final ArrayList<String> msgs = bundle.getStringArrayList("msgs");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) (context.getResources().getDimension(R.dimen.app_update_content_margin_left));
        layoutParams.topMargin = (int) (context.getResources().getDimension(R.dimen.app_update_line_margin_));

        for (String msg : msgs) {
            View textLayout = LayoutInflater.from(context).inflate(R.layout.update_msg_text_item, null);
            TextView textView = (TextView) textLayout.findViewById(R.id.update_msg_text);
            textView.setText(msg);
            updateMsgLayout.addView(textLayout);
        }

        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams contentLayoutParams = new RelativeLayout.LayoutParams(width, height);
        contentLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        relativeLayout.addView(contentView, contentLayoutParams);
        setContentView(relativeLayout);
        setBackgroundDrawable(contentView.getResources().getDrawable(R.drawable.pop_bg_drawable));
        setFocusable(true);

        Button updateNow = (Button) contentView.findViewById(R.id.update_now_bt);
        Button updateLater = (Button) contentView.findViewById(R.id.update_later_bt);
        updateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                try {
                    String[] args2 = {"chmod", "604", path};
                    Runtime.getRuntime().exec(args2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AppUtils.installApp(context, path);
            }
        });
        updateLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        updateNow.setOnHoverListener(this);
        updateLater.setOnHoverListener(this);
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                v.requestFocus();
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                tmp.requestFocus();
                break;
        }
        return true;
    }
}
