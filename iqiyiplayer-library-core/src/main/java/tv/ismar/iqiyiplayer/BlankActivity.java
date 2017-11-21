package tv.ismar.iqiyiplayer;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * DEBUG CODE, blank activity for test memory leak.
 */
public class BlankActivity extends Activity {

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return super.onKeyDown(keyCode, event);
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            Intent it = new Intent();
            it.setClass(BlankActivity.this, SdkTestActivity.class);
            startActivity(it);
            BlankActivity.this.finish();
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
