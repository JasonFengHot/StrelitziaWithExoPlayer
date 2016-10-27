package tv.ismar.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.account.HttpParamsInterceptor;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.db.DBHelper;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.db.LocalFavoriteManager;
import tv.ismar.app.db.LocalHistoryManager;
import tv.ismar.app.network.HttpTrafficInterceptor;
import tv.ismar.app.update.UpdateService;
import tv.ismar.app.util.NetworkUtils;
import tv.ismar.app.util.SPUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by beaver on 16-8-19.
 */
public class VodApplication extends Application {
    private static final String TAG = VodApplication.class.getSimpleName();
    private static HttpTrafficInterceptor mHttpTrafficInterceptor;
    private static HttpParamsInterceptor mHttpParamsInterceptor;
    public static final boolean DEBUG = true;

    private static VodApplication appInstance;
    private HistoryManager mModuleHistoryManager;
    private FavoriteManager mModuleFavoriteManager;
    private DBHelper mModuleDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        SPUtils.init(this);
        appInstance = this;
        ActiveAndroid.initialize(this);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Picasso picasso = new Picasso.Builder(this).executor(executorService).build();
        Picasso.setSingletonInstance(picasso);
        IsmartvActivator.initialize(this);
        mHttpTrafficInterceptor = new HttpTrafficInterceptor(this);
        mHttpTrafficInterceptor.setTrafficType(HttpTrafficInterceptor.TrafficType.UNLIMITED);
        mHttpParamsInterceptor = new HttpParamsInterceptor.Builder()
                .build();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("DroidSansFallback.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        if (NetworkUtils.isConnected(this)) {
            new Thread(new InitializeProcess(this)).start();
        }
        checkUpgrade();
    }

    private void checkUpgrade() {
        Intent intent = new Intent();
        intent.setClass(this, UpdateService.class);
        startService(intent);
    }

    public static VodApplication get(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

    public static HttpTrafficInterceptor getHttpTrafficInterceptor() {
        return mHttpTrafficInterceptor;
    }

    public static HttpParamsInterceptor getHttpParamsInterceptor() {
        return mHttpParamsInterceptor;
    }

    public static VodApplication getModuleAppContext() {
        return appInstance;
    }

    /**
     * Return this application {@link DBHelper}
     *
     * @return The application {@link DBHelper}
     */
    public DBHelper getModuleDBHelper() {
        if (mModuleDBHelper == null) {
            mModuleDBHelper = new DBHelper(this);
        }
        return mModuleDBHelper;
    }

    /**
     * Return this application {@link HistoryManager}
     *
     * @return The application {@link HistoryManager}
     */
    public HistoryManager getModuleHistoryManager() {
        if (mModuleHistoryManager == null) {
            mModuleHistoryManager = new LocalHistoryManager(this);
        }
        return mModuleHistoryManager;
    }

    public FavoriteManager getModuleFavoriteManager() {
        if (mModuleFavoriteManager == null) {
            mModuleFavoriteManager = new LocalFavoriteManager(this);
        }
        return mModuleFavoriteManager;
    }
}
