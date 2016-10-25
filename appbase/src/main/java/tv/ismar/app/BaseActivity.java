package tv.ismar.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import rx.Observer;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.widget.LoadingDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by beaver on 16-8-19.
 */
public class BaseActivity extends AppCompatActivity {
    public static final String ACTION_CONNECT_ERROR = "tv.ismar.daisy.CONNECT_ERROR";
    private LoadingDialog mLoadingDialog;
    public SkyService mSkyService;
    private  View mRootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSkyService = SkyService.ServiceManager.getService();
        mRootView = getWindow().getDecorView();
    }


    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void showDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, R.style.LoadingDialog);
        }
        if (msg != null) {
            mLoadingDialog.setTvText(msg);
        }
        mLoadingDialog.showDialog();
    }

    public void dismissDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog.setTvText(getString(R.string.loading_text));
        }
    }

    public boolean isDialogShow() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            return true;
        }
        return false;
    }

    public View getRootView() {
        return ((ViewGroup) (getWindow().getDecorView().findViewById(android.R.id.content))).getChildAt(0);
    }

    private  void showNetWorkErrorDialog(Throwable e) {
//        mRootView
    }

    public  abstract class BaseObserver<T> implements Observer<T> {
        @Override
        public void onError(Throwable e) {
            showNetWorkErrorDialog(e);
        }
    }
}
