package tv.ismar.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import tv.ismar.account.HttpParamsInterceptor;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.core.ImageCache;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.db.DBHelper;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.db.LocalFavoriteManager;
import tv.ismar.app.db.LocalHistoryManager;
import tv.ismar.app.entity.ContentModel;
import tv.ismar.app.network.HttpCacheInterceptor;
import tv.ismar.app.util.SPUtils;

/** Created by beaver on 16-8-19. */
public class VodApplication extends Application {
    public static final boolean DEBUG = true;
    public static final String CACHED_LOG = "cached_log";
    private static final int CORE_POOL_SIZE = 5;
    private static final ThreadFactory sThreadFactory =
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "GreenDroid thread #" + mCount.getAndIncrement());
                }
            };
    private static final String TAG = VodApplication.class.getSimpleName();
    public static String DEVICE_TOKEN = "device_token";
    private static HttpParamsInterceptor mHttpParamsInterceptor;
    private static VodApplication appInstance;
    private static SharedPreferences mPreferences;
    public ContentModel[] mContentModel;
    protected String userAgent;
    private HttpCacheInterceptor mHttpCacheInterceptor;
    private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
    private HistoryManager mModuleHistoryManager;
    private FavoriteManager mModuleFavoriteManager;
    private DBHelper mModuleDBHelper;
    private ImageCache mImageCache;
    private ExecutorService mExecutorService;
    private SharedPreferences.Editor mEditor;

    public VodApplication() {}

    public static void setDevice_Token() {
        SimpleRestClient.device_token = mPreferences.getString(VodApplication.DEVICE_TOKEN, "");
    }

    public static VodApplication get(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

    public static HttpParamsInterceptor getHttpParamsInterceptor() {
        return mHttpParamsInterceptor;
    }

    public static VodApplication getModuleAppContext() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        SPUtils.init(this);
        appInstance = this;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Picasso picasso = new Picasso.Builder(this).executor(executorService).build();
        Picasso.setSingletonInstance(picasso);
        IsmartvActivator.initialize(this);
        mHttpParamsInterceptor = new HttpParamsInterceptor.Builder().build();
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
    }

    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    public HttpCacheInterceptor getCacheInterceptor() {
        if (mHttpCacheInterceptor == null) {
            mHttpCacheInterceptor = new HttpCacheInterceptor(this);
        }
        return mHttpCacheInterceptor;
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

    public float getRate(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        float rate = (float) densityDpi / (float) 160;
        return rate;
    }

    /**
     * Return an ExecutorService (global to the entire application) that may be used by clients when
     * running long tasks in the background.
     *
     * @return An ExecutorService to used when processing long running tasks
     */
    public ExecutorService getExecutor() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(CORE_POOL_SIZE, sThreadFactory);
        }
        return mExecutorService;
    }

    /**
     * Return this application {@link ImageCache}.
     *
     * @return The application {@link ImageCache}
     */
    public ImageCache getImageCache() {
        if (mImageCache == null) {
            mImageCache = new ImageCache(this);
        }
        return mImageCache;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    public boolean save() {
        return mEditor.commit();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initLogger() {
        Logger.init("VOD_APPLICATION") // default PRETTYLOGGER or use just init()
                .methodCount(10) // default 2
                .logLevel(LogLevel.FULL) // default LogLevel.FULL
                .methodOffset(2); // default 0
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

    /**
     * Return this application {@link HistoryManager}
     *
     * @return The application {@link HistoryManager}
     */
    public interface OnLowMemoryListener {

        /** Callback to be invoked when the system needs memory. */
        void onLowMemoryReceived();
    }
}
