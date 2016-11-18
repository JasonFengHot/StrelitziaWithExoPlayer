package tv.ismar.searchpage.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtils {
	/**
	 * 返回版本号
	 * 
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {

		try {
			PackageManager mPackageManager = context.getPackageManager();
			PackageInfo packageInfo = mPackageManager.getPackageInfo(
					context.getPackageName(), 0);
			// 获取到版本号
			String versionName = packageInfo.versionName;

			return versionName;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 获取到本地的版本号
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {

		try {
			PackageManager mPackageManager = context.getPackageManager();
			PackageInfo packageInfo = mPackageManager.getPackageInfo(
					context.getPackageName(), 0);
			// 获取到版本号
			int versionCode = packageInfo.versionCode;

			return versionCode;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
