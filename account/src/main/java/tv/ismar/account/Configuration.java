package tv.ismar.account;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

/**
 * Created by huibin on 1/13/2017.
 */

public class Configuration {

    private SharedPreferences sharedPreferences;
    private TelephonyManager telephonyManager;
    private PackageManager packageManager;
    private String savePath;

    public Configuration(SharedPreferences sharedPreferences, TelephonyManager telephonyManager, PackageManager packageManager, String savePath) {
        this.sharedPreferences = sharedPreferences;
        this.telephonyManager = telephonyManager;
        this.packageManager = packageManager;
        this.savePath = savePath;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public TelephonyManager getTelephonyManager() {
        return telephonyManager;
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public String getSavePath() {
        return savePath;
    }
}
