package tv.ismar.app.core;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import java.util.concurrent.ExecutorService;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.R;
import tv.ismar.app.VodApplication;
import tv.ismar.app.db.DBHelper;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.db.HistoryManager;
import tv.ismar.app.ui.MessageDialogFragment;
import tv.ismar.app.widget.LoadingDialog;


public class DaisyUtils {

    private static final String TAG = "DaisyUtils";

    private DaisyUtils() {
    }

    /**
     * Return the current {@link VodApplication}
     *
     * @param context The calling context
     * @return The {@link VodApplication} the given context is linked to.
     */
    public static VodApplication getVodApplication(Context context) {
        return (VodApplication) context.getApplicationContext();
    }

    /**
     * Return the {@link VodApplication} image cache
     *
     * @param context The calling context
     * @return The image cache of the current {@link VodApplication}
     */
    public static ImageCache getImageCache(Context context) {
        return getVodApplication(context).getImageCache();
    }

    /**
     * Return the {@link VodApplication} executors pool.
     *
     * @param context The calling context
     * @return The executors pool of the current {@link VodApplication}
     */
    public static ExecutorService getExecutor(Context context) {
        return getVodApplication(context).getExecutor();
    }

    public static DBHelper getDBHelper(Context context) {
        return getVodApplication(context).getModuleDBHelper();
    }

    public static HistoryManager getHistoryManager(Context context) {
        return getVodApplication(context).getModuleHistoryManager();
    }

    public static FavoriteManager getFavoriteManager(Context context) {
        return getVodApplication(context).getModuleFavoriteManager();
    }

    public static boolean gotoSpecialPage(final Context context, final String contentMode, final String url, final String from) {
        final boolean[] gotoSuccess = {false};
        final LoadingDialog loadingDialog = new LoadingDialog(context, R.style.LoadingDialog);
        loadingDialog.setTvText(context.getResources().getString(R.string.loading_text));
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                loadingDialog.dismiss();
            }
        });
        loadingDialog.show();
        Log.i(TAG, "OfflineCheckManager gotoSpecialPage");
        OfflineCheckManager.getInstance().checkItem(url, new OfflineCheckManager.Callback() {
            @Override
            public void online() {
                loadingDialog.dismiss();
                Log.i(TAG, "online check: " + "online ===> " + url);
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if ("variety".equals(contentMode) || "entertainment".equals(contentMode)) {
                    intent.setAction("tv.ismar.daisy.EntertainmentItem");
                    intent.putExtra("title", "娱乐综艺");
                } else if ("movie".equals(contentMode)) {
                    intent.setAction("tv.ismar.daisy.PFileItem");
                    intent.putExtra("title", "电影");
                } else if ("package".equals(contentMode)) {
                    intent.setAction("tv.ismar.daisy.packageitem");
                    intent.putExtra("title", "礼包详情");
                } else {
                    intent.setAction("tv.ismar.daisy.Item");
                }
                Log.i("LH/","Package get data offline manager");
                intent.putExtra("url", url);
                intent.putExtra("fromPage", from);
                context.startActivity(intent);
                gotoSuccess[0] = true;
            }

            @Override
            public void offline() {
                loadingDialog.dismiss();
                Log.i(TAG, "offline check: " + "offline ===> " + url);
                gotoSuccess[0] = false;
                showNetErrorPopup(context, context.getString(R.string.item_offline));
            }

            @Override
            public void netError() {
                showNetErrorPopup(context, "网络数据异常");
            }
        });
        return gotoSuccess[0];
    }

    private static void showNetErrorPopup(Context context, String message) {
        View rootView = ((Activity) context).getWindow().getDecorView();
        final MessageDialogFragment dialog = new MessageDialogFragment(context, message, null);

        dialog.setButtonText(context.getString(R.string.vod_i_know), null);
        dialog.showAtLocation(rootView, Gravity.CENTER,
                new MessageDialogFragment.ConfirmListener() {
                    @Override
                    public void confirmClick(View view) {
                        dialog.dismiss();
                    }
                }, null);

    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null&&networkInfo.length>0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 从launcher进入详情页，初始化赋值问题
    public static void tempInitStaticVariable(final Activity activity) {
        new Thread() {
            @Override
            public void run() {
                DisplayMetrics metric = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
                SimpleRestClient.densityDpi = metric.densityDpi;
                SimpleRestClient.screenWidth = metric.widthPixels;
                SimpleRestClient.screenHeight = metric.heightPixels;
                PackageManager manager = activity.getPackageManager();
                try {
                    PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
                    SimpleRestClient.appVersion = info.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String apiDomain = IsmartvActivator.getInstance().getApiDomain();
                String ad_domain = IsmartvActivator.getInstance().getAdDomain();
                String log_domain = IsmartvActivator.getInstance().getLogDomain();
                String upgrade_domain = IsmartvActivator.getInstance().getUpgradeDomain();
                if (apiDomain != null && !apiDomain.contains("http")) {
                    apiDomain = "http://" + apiDomain;
                }
                if (ad_domain != null && !ad_domain.contains("http")) {
                    ad_domain = "http://" + ad_domain;
                }
                if (log_domain != null && !log_domain.contains("http")) {
                    log_domain = "http://" + log_domain;
                }
                if (upgrade_domain != null && !upgrade_domain.contains("http")) {
                    upgrade_domain = "http://" + upgrade_domain;
                }
                SimpleRestClient.root_url = apiDomain;
                SimpleRestClient.ad_domain = ad_domain;
                SimpleRestClient.log_domain = log_domain;
                SimpleRestClient.upgrade_domain = upgrade_domain;
                SimpleRestClient.device_token = IsmartvActivator.getInstance().getDeviceToken();
                SimpleRestClient.sn_token = IsmartvActivator.getInstance().getSnToken();
                SimpleRestClient.zuser_token = IsmartvActivator.getInstance().getZUserToken();
                SimpleRestClient.zdevice_token = IsmartvActivator.getInstance().getZDeviceToken();
            }
        }.start();

    }
}
