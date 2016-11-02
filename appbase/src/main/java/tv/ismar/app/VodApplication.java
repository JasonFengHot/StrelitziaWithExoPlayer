package tv.ismar.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.app.Application;
import tv.ismar.account.HttpParamsInterceptor;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.core.ImageCache;
import tv.ismar.app.core.InitializeProcess;
import tv.ismar.app.core.SimpleRestClient;
import tv.ismar.app.core.VipMark;
import tv.ismar.app.core.advertisement.AdvertisementManager;
import tv.ismar.app.core.client.MessageQueue;
import tv.ismar.app.core.preferences.AccountSharedPrefs;
import tv.ismar.app.core.preferences.AppSharedPrefs;
import tv.ismar.app.db.DBHelper;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.db.LocalFavoriteManager;
import tv.ismar.app.db.LocalHistoryManager;
import tv.ismar.app.entity.ContentModel;
import tv.ismar.app.network.HttpTrafficInterceptor;
import tv.ismar.app.util.NetworkUtils;
import tv.ismar.app.util.SPUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by beaver on 16-8-19.
 */
public class VodApplication extends Application {
    private static HttpTrafficInterceptor mHttpTrafficInterceptor;
    private static HttpParamsInterceptor mHttpParamsInterceptor;
    public static final boolean DEBUG = true;
    private ArrayList<WeakReference<OnLowMemoryListener>> mLowMemoryListeners;
    private static VodApplication appInstance;
    private HistoryManager mModuleHistoryManager;
    private FavoriteManager mModuleFavoriteManager;
    private DBHelper mModuleDBHelper;
    private ImageCache mImageCache;
    public static final String domain = "";
    public static final String ad_domain = "ad_domain";
    public ContentModel[] mContentModel;
    public static final String LOGIN_STATE = "loginstate";
    public static String AUTH_TOKEN = "auth_token";
    public static String MOBILE_NUMBER = "mobile_number";
    public static String DEVICE_TOKEN = "device_token";
    public static String SN_TOKEN = "sntoken";
    public static String DOMAIN = "domain";
    public static String LOG_DOMAIN = "logmain";
    public static String LOCATION_INFO = "location_info";
    public static String LOCATION_PROVINCE = "location_province";
    public static String LOCATION_CITY = "location_city";
    public static String LOCATION_DISTRICT = "location_district";
    public static String BESTTV_AUTH_BIND_FLAG = "besttv_auth_bind_flag";
    private static final int CORE_POOL_SIZE = 5;
    public static String NEWEST_ENTERTAINMENT = "newestentertainment";
    public static String OPENID = "openid";
    public static final String CACHED_LOG = "cached_log";
    private ExecutorService mExecutorService;
    public static String apiDomain = "http://skytest.tvxio.com";
    private static SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String PREFERENCE_FILE_NAME = "Daisy";
    private boolean isFinish = true;
    @Override
    public void onCreate() {
        super.onCreate();
        SPUtils.init(this);
        appInstance = this;
        ActiveAndroid.initialize(this);
        AccountSharedPrefs.initialize(this);
        AppSharedPrefs.initialize(this);
        AdvertisementManager.initialize(this);
        load(this);
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
    }
    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }
    public static void setDevice_Token() {
        SimpleRestClient.device_token = mPreferences.getString(VodApplication.DEVICE_TOKEN, "");
    }
    public VodApplication() {
        mLowMemoryListeners = new ArrayList<WeakReference<OnLowMemoryListener>>();
     //   mActivityPool = new ConcurrentHashMap<String, Activity>();
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
    public void load(Context a) {
        try {
            mPreferences = a.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
            mEditor = mPreferences.edit();
            Set<String> cached_log = mPreferences.getStringSet(CACHED_LOG, null);
            mEditor.remove(CACHED_LOG).commit();
            if (!isFinish) {
                new Thread(mUpLoadLogRunnable).start();
                isFinish = true;
            }
            if (cached_log != null) {
                Iterator<String> it = cached_log.iterator();
                while (it.hasNext()) {
                    MessageQueue.addQueue(it.next());
                }
            }
        } catch (Exception e) {
            System.out.println("load(Activity a)=" + e);
        }
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
     * Return this application {@link HistoryManager}
     *
     * @return The application {@link HistoryManager}
     */
    public static interface OnLowMemoryListener {

        /**
         * Callback to be invoked when the system needs memory.
         */
        public void onLowMemoryReceived();
    }
    /**
     * Add a new listener to registered {@link OnLowMemoryListener}.
     *
     * @param listener The listener to unregister
     * @see OnLowMemoryListener
     */
    public void registerOnLowMemoryListener(OnLowMemoryListener listener) {
        if (listener != null) {
            mLowMemoryListeners.add(new WeakReference<OnLowMemoryListener>(listener));
        }
    }
    /**
     * Remove a previously registered listener
     *
     * @param listener The listener to unregister
     * @see OnLowMemoryListener
     */

    public void unregisterOnLowMemoryListener(OnLowMemoryListener listener) {
        if (listener != null) {
            int i = 0;
            while (i < mLowMemoryListeners.size()) {
                final OnLowMemoryListener l = mLowMemoryListeners.get(i).get();
                if (l == null || l == listener) {
                    mLowMemoryListeners.remove(i);
                } else {
                    i++;
                }
            }
        }
    }
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "GreenDroid thread #" + mCount.getAndIncrement());
        }
    };
    /**
     * Return an ExecutorService (global to the entire application) that may be
     * used by clients when running long tasks in the background.
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
        int i = 0;
        while (i < mLowMemoryListeners.size()) {
            final OnLowMemoryListener listener = mLowMemoryListeners.get(i).get();
            if (listener == null) {
                mLowMemoryListeners.remove(i);
            } else {
                listener.onLowMemoryReceived();
                i++;
            }
        }
    }
    @Override
    public void onTrimMemory(int level) {
        // TODO Auto-generated method stub
        super.onTrimMemory(level);
    }
    private Runnable mUpLoadLogRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (isFinish) {
                try {
                    Thread.sleep(1*60*1000);
//                    synchronized (MessageQueue.async) {
                    // Thread.sleep(900000);


                    ArrayList<String> list = MessageQueue.getQueueList();
                    int i;
                    JSONArray s = new JSONArray();
                    if (list.size() > 0) {
                        for (i = 0; i < list.size(); i++) {
                            JSONObject obj;
                            try {
                                Log.i("qazwsx", "json item==" + list.get(i).toString());
                                obj = new JSONObject(list.get(i).toString());
                                s.put(obj);
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                        if (i == list.size()) {
                            MessageQueue.remove();
//                            NetworkUtils.LogSender(s.toString());
                            Log.i("qazwsx", "json array==" + s.toString());
                            Log.i("qazwsx", "remove");
                        }
                    } else {
                        Log.i("qazwsx", "queue is no elements");
                    }
//                    }

                    //NetworkUtils.LogUpLoad(getApplicationContext());
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }catch (java.lang.IndexOutOfBoundsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Log.i("qazwsx", "Thread is finished!!!");
        }

    };
    public boolean save() {
        return mEditor.commit();
    }
}
