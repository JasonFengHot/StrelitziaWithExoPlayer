package cn.ismartv.helperpage.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by huaijie on 1/12/15.
 */
public class MessageSubmitButton extends SakuraButton {
    private Context context;

    public MessageSubmitButton(Context context) {
        super(context);
        this.context = context;
    }

    public MessageSubmitButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MessageSubmitButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent intent = new Intent();
        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_LEFT:
//                intent.setAction(HomeActivity.KEYCODE_DPAD_LEFT);
//                context.sendBroadcast(intent);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
//                intent.setAction(HomeActivity.KEYCODE_DPAD_RIGHT);
//                context.sendBroadcast(intent);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
