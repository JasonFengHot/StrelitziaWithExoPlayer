package tv.ismar.searchpage.core.initialization;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

import tv.ismar.searchpage.data.table.AppTable;
import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.query.Delete;

/**
 * Created by huaijie on 1/4/16.
 */
public class AppTableInit {
    private static AppTableInit instance;

    private AppTableInit() {

    }

    public static AppTableInit getInstance() {
        if (instance == null) {
            instance = new AppTableInit();
        }
        return instance;
    }

    public void getLocalAppList(final Context context) {
        new Thread() {
            @Override
            public void run() {
                PackageManager packageManager = context.getPackageManager();
                List<PackageInfo> apps = packageManager.getInstalledPackages(0);
                new Delete().from(AppTable.class).execute();
                ActiveAndroid.beginTransaction();
                try {
                    for (PackageInfo packageInfo : apps) {
                        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            //非系统应用
                            AppTable appTable = new AppTable();
                            appTable.app_name = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
                            appTable.app_package = packageInfo.packageName;
                            appTable.version_code = packageInfo.versionCode;
                            appTable.version_name = packageInfo.versionName;
                            appTable.save();
                        } else {
                            //系统应用
                            if (isIndexOfWhiteAppList(packageInfo.packageName)) {
                                AppTable appTable = new AppTable();
                                appTable.app_name = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
                                appTable.app_package = packageInfo.packageName;
                                appTable.version_code = packageInfo.versionCode;
                                appTable.version_name = packageInfo.versionName;
                                appTable.save();
                            }
                        }
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();
                }
            }
        }.start();
    }

    private boolean isIndexOfWhiteAppList(String appPackage) {
        String whiteAppList[] = {
                "com.sharp.childlock",
                "com.sharp.appoint",
                "com.sharp.pinp",
                "com.sharp.camera",
                "com.sharp.tveasycontrol",
                "com.sharp.localmmAni",
                "tv.ismar.daisy",
                "com.boxmate.tv"
        };

        for (String pkg : whiteAppList) {
            if (appPackage.equals(pkg)) {
                return true;
            }
        }
        return false;
    }
}
