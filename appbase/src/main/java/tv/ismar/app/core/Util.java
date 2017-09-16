package tv.ismar.app.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import cn.ismartv.truetime.TrueTime;

public class Util {

    private static final int STRINGMAXLENGTH = 4;

    public static boolean isNetConnected(Context context) {

        boolean ret = false;
        try {
            ConnectivityManager con =
                    (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
            ret = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    public static void setBooleanValue(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences("iamhere", Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBooleanValue(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("iamhere", Context.MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public static void setValue(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences("iamhere", Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getValue(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("iamhere", Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static String getDeviceId(Context context) {
        String deviceId = null;
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId();

        } catch (Exception e) {

            e.printStackTrace();
        }
        return deviceId;
    }

    public static String getMac(Context context) {
        String macSerial = null;
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            macSerial = wm.getConnectionInfo().getMacAddress();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return macSerial;
    }

    /**
     * ��ȡ������UUID
     *
     * @param context
     * @return
     */
    public static String getUUID(Context context) {

        String uniqueId = "";
        for (int i = 0; i < 3; i++) {
            uniqueId = getDeviceId(context);
            if (uniqueId != null) {
                break;
            }
        }

        if (uniqueId == null) {
            uniqueId = getMac(context);
        }

        try {
            UUID deviceUuid = new UUID(uniqueId.hashCode(), uniqueId.hashCode());
            uniqueId = deviceUuid.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uniqueId;
    }

    public static void delCache() {
        // MobileManagementServiceUtil.delCache();
    }

    public static String getAppVer(Context context, String pacName) {
        String ver = "";

        PackageManager pckMan = context.getPackageManager();
        List<PackageInfo> packs = pckMan.getInstalledPackages(0);
        int count = packs.size();

        for (int i = 0; i < count; i++) {
            PackageInfo p = packs.get(i);

            if (p.versionName == null) {
                continue;
            }
            ApplicationInfo appInfo = p.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                continue;
            }
            if (pacName.equals(p.packageName)) {
                ver = p.versionName;
                break;
            }
        }

        return ver;
    }

    public static void installApk(Context context, File file) {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void uninstallApk(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    public static boolean isAppInstalled(Context context, String packageName, String version) {
        if (packageName == null || version == null) {
            return false;
        }
        PackageManager pckMan = context.getPackageManager();
        List<PackageInfo> packs = pckMan.getInstalledPackages(0);
        int count = packs.size();

        for (int i = 0; i < count; i++) {
            PackageInfo p = packs.get(i);

            if (p.versionName == null) {
                continue;
            }
            ApplicationInfo appInfo = p.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                continue;
            }
            if (packageName.equals(p.packageName) && version.equals(p.versionName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        if (packageName == null) {
            return false;
        }
        PackageManager pckMan = context.getPackageManager();
        List<PackageInfo> packs = pckMan.getInstalledPackages(0);
        int count = packs.size();

        for (int i = 0; i < count; i++) {
            PackageInfo p = packs.get(i);

            if (p.versionName == null) {
                continue;
            }
            ApplicationInfo appInfo = p.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                continue;
            }
            if (packageName.equals(p.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap getBitmap(InputStream is, int w, int h) {
        Bitmap bitmap = null;
        try {

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565; // ��ʾ16λλͼ
            // 565����Ӧ��ԭɫռ��λ��
            opt.inInputShareable = true;
            opt.inPurgeable = true; // ����ͼƬ���Ա�����

            Bitmap tmp = BitmapFactory.decodeStream(is, null, opt);
            if (tmp != null) {
                bitmap = Bitmap.createScaledBitmap(tmp, w, h, true);
                tmp.recycle();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return bitmap;
    }

    /**
     * @param x ͼ��Ŀ��
     * @param y ͼ��ĸ߶�
     * @param image ԴͼƬ
     * @param outerRadiusRat Բ�ǵĴ�С
     * @return Բ��ͼƬ
     */
    public static Bitmap createRoundPhoto(int x, int y, Bitmap image, float outerRadiusRat) {
        Drawable imageDrawable = new BitmapDrawable(image);

        // �½�һ���µ����ͼƬ
        Bitmap output = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // �½�һ������
        RectF outerRect = new RectF(0, 0, x, y);

        // ����һ����ɫ��Բ�Ǿ���
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(outerRect, outerRadiusRat, outerRadiusRat, paint);

        // ��ԴͼƬ���Ƶ����Բ�Ǿ�����
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        imageDrawable.setBounds(0, 0, x, y);
        canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
        imageDrawable.draw(canvas);
        canvas.restore();

        return output;
    }

    public static Bitmap getBitmap(String path, String name) {
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            File f = new File(path + name);
            long size = f.length() / 1024;
            int rate = (int) size / 100;
            rate = (int) Math.sqrt(rate);
            rate = (rate > 1) ? rate : 1;
            rate = (rate > 3) ? 3 : rate;
            System.out.println("��ͼ����ϵ��: " + rate);

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565; // ��ʾ16λλͼ
            // 565����Ӧ��ԭɫռ��λ��
            opt.inInputShareable = true;
            opt.inPurgeable = true; // ����ͼƬ���Ա�����
            opt.inSampleSize = rate;
            is = new FileInputStream(path + name);
            bitmap = BitmapFactory.decodeStream(is, null, opt);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    public static String getBlueToothAddr() {
        String m_szBTMAC = "";
        try {
            BluetoothAdapter m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            m_szBTMAC = m_BluetoothAdapter.getAddress();
        } catch (Exception e) {
        }
        return m_szBTMAC;
    }

    // ��ȡ�ֻ����к�
    public static String getSerialNumber(Context context) {
        String uniqueId = "";
        String macAddr = "";
        try {
            macAddr = getBlueToothAddr();
            if (macAddr == null || macAddr.trim().length() <= 0) {
                macAddr = getMac(context);
            }

            for (int i = 0; i < 3; i++) {
                uniqueId = getDeviceId(context);
                if (uniqueId != null && uniqueId.length() > 0) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (uniqueId == null) {
                uniqueId = "";
            }
            if (macAddr == null) {
                macAddr = "";
            }
            UUID deviceUuid = new UUID(uniqueId.hashCode(), macAddr.hashCode());
            uniqueId = "sgm" + deviceUuid.toString().replaceAll("-", "");
        }

        return uniqueId;
    }

    // �жϷ����Ƿ���������
    public static boolean isServiceWorked(Context context, String srvName) {
        ActivityManager myManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<RunningServiceInfo> runningService =
                (ArrayList<RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(srvName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean killProcess(Context context, String name) {

        // ��ȡһ��ActivityManager ����
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        // ��ȡϵͳ�������������еĽ��
        List<RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // ��ϵͳ�������������еĽ�̽��е���������ΪҪɱ�Ľ�̣���Kill��
        for (RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            String processName = appProcessInfo.processName;
            if (processName.equals(name)) {
                if (appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    home.addCategory(Intent.CATEGORY_HOME);
                    context.startActivity(home);
                    System.out.println("foreground process: " + appProcessInfo.processName);
                }

                activityManager.killBackgroundProcesses(processName);
                System.out.println(
                        "Killed -->PID:" + appProcessInfo.pid + "--ProcessName:" + processName);
                return true;
            }
        }
        return false;
    }

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }

    public static String getAppPackageName(Context context) {
        String packageName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            packageName = pi.packageName;
            if (packageName == null || packageName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return packageName;
    }

    public static int getLenOfStr(String s) {
        int num = 0;
        for (int i = 0; i < s.length(); i++) {
            String tmp = s.substring(i, i + 1);
            if (tmp.getBytes().length == 3) {
                num += 2;
            } else if (tmp.getBytes().length == 1) {
                num += 1;
            }
        }

        return num;
    }

    public static boolean isRoot(String cmd) {
        Process process = null;
        DataOutputStream os = null;

        try {

            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();

        } catch (Exception e) {
            return false;

        } finally {

            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }

        return true;
    }

    public static boolean isRoot() {
        boolean bool = false;

        try {
            bool =
                    !((!new File("/system/bin/su").exists())
                            && (!new File("/system/xbin/su").exists()));
        } catch (Exception e) {

        }
        return bool;
    }

    public static String getYearMonthDay() {
        Calendar c = Calendar.getInstance();
        c.setTime(TrueTime.now());
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);

        String year = null, month = null, day = null;

        year = String.valueOf(y);
        if (m < 10) {
            month = "0" + m;
        } else {
            month = "" + m;
        }
        if (d < 10) {
            day = "0" + d;
        } else {
            day = "" + d;
        }

        String strDate = year + "-" + month + "-" + day;
        return strDate;
    }

    public static int getHour() {
        Calendar c = Calendar.getInstance();
        c.setTime(TrueTime.now());
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public static int getMinute() {
        Calendar c = Calendar.getInstance();
        c.setTime(TrueTime.now());
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        int min = c.get(Calendar.MINUTE);
        return min;
    }

    public static String getTime() {

        Calendar c = Calendar.getInstance();
        c.setTime(TrueTime.now());
        c.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        String year = null, month = null, day = null;
        String h = null, min = null, sec = null;
        year = String.valueOf(y);
        if (m < 10) {
            month = "0" + m;
        } else {
            month = "" + m;
        }
        if (d < 10) {
            day = "0" + d;
        } else {
            day = "" + d;
        }
        if (hour < 10) {
            h = "0" + hour;
        } else {
            h = "" + hour;
        }
        if (minute < 10) {
            min = "0" + minute;
        } else {
            min = "" + minute;
        }
        if (second < 10) {
            sec = "0" + second;
        } else {
            sec = "" + second;
        }
        String strDate = year + "-" + month + "-" + day + " " + h + ":" + min + ":" + sec;
        return strDate;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dpToPx(Context context, float dp) {
        float px =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dp,
                        context.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static Point getDisplayPixelSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getDisplayPixelWidth(Context context) {
        Point size = getDisplayPixelSize(context);
        return (size.x);
    }

    public static int getDisplayPixelHeight(Context context) {
        Point size = getDisplayPixelSize(context);
        return (size.y);
    }

    public static String getCutString(String str, int length) {
        String substr;
        int strlength = str.length();
        if (strlength > length) {
            substr = str.substring(0, length) + "..";
        } else {
            substr = str;
        }
        return substr;
    }

    public static int daysBetween(String startdate, String enddate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(startdate));
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        long time1 = cal.getTimeInMillis();
        cal.setTime(sdf.parse(enddate));
        cal.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        long time2 = cal.getTimeInMillis();
        long remain = time2 - time1;
        if (remain <= 0) {
            return -1;
        } else {
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            return Integer.parseInt(String.valueOf(between_days));
        }
    }
}
