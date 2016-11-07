package tv.ismar.app;

import android.support.v4.app.Fragment;

/**
 * Created by huibin on 11/7/16.
 */

public class BaseFragment extends Fragment {
    public OnViewLoadCallback mViewLoadCallback;

    public interface OnViewLoadCallback {
        void loadComplete();
    }

    public void setViewLoadCallback(OnViewLoadCallback viewLoadCallback) {
        mViewLoadCallback = viewLoadCallback;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
