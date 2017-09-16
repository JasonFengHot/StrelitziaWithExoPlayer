package tv.ismar.account.active;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

/** Created by huibin on 8/26/16. */
public class IsmartvActiveService {
    private static final String DEFAULT_HOST = "http://peachtest.tvxio.com";
    private static final String SIGN_FILE_NAME = "sign";

    private String manufacture;
    private String kind;
    private String version;
    private String location;
    private String sn;
    private Context mContext;
    private String fingerprint;
    private String deviceId;

    //    private SkyService mSkyService;

    public IsmartvActiveService() {
        //        mSkyService = SkyService.ServiceManager.getService();
    }

    private void getLicence() {}

    private String generateKind() {
        return Build.PRODUCT.replaceAll(" ", "_").toLowerCase();
    }

    private int getVersionCode() {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void start() {}

    public static class Builder {}
}
