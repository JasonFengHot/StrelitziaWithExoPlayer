package tv.ismar.app.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Created by huaijie on 3/12/15. */
public class HardwareUtils {
    private static final String TAG = "HardwareUtils";

    public static String getCachePath(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return new File(Environment.getExternalStorageDirectory(), "/Daisy/").getAbsolutePath();
        } else {
            return context.getCacheDir().getAbsolutePath();
        }
    }

    public static String getSDCardCachePath() {
        return new File(Environment.getExternalStorageDirectory(), "/Daisy/").getAbsolutePath();
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static String getMd5ByFile(File file) {
        String value;
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, length);
            }
            BigInteger bi = new BigInteger(1, messageDigest.digest());
            value = bi.toString(16);
            in.close();
        } catch (Exception e) {
            Log.e("getMd5ByFile", e.getMessage());
            return "";
        }

        int offset = 32 - value.length();
        if (offset > 0) {
            String data = new String();
            for (int i = 0; i < offset; i++) {
                data = data + "0";
            }
            value = data + value;
        }
        return value;
    }

    public static String getMd5ByString(String string) {
        String value;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes());
            value = new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception e) {
            Log.e("getMd5ByFile", e.getMessage());
            return "";
        }
        return value;
    }

    public static String getFileNameWithoutSuffix(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            return fileName.split("\\.")[0];
        } else {
            return fileName;
        }
    }

    public static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                ? true
                : false;
    }

    public static void deleteFiles(String path, ArrayList<String> exceptsArray) {
        File directory = new File(path);
        if (directory.exists()) {
            if (directory.length() > 0) {
                List<String> sub = Arrays.asList(directory.list());
                ArrayList<String> subFiles = new ArrayList<String>(sub);
                ArrayList<String> tmp = new ArrayList<String>(subFiles);
                tmp.retainAll(exceptsArray);
                for (String str : tmp) {
                    Log.d(TAG, "all contain: " + str);
                    subFiles.remove(str);
                }

                for (String str : subFiles) {
                    Log.d(TAG, "will be delete: " + str);
                    File subfile = new File(path + "/" + str);
                    if (subfile.exists()) {
                        subfile.delete();
                    }
                }
            }
        }
    }

    public static String getModelName() {
        return Build.PRODUCT.replace(" ", "_");
    }

    public int getheightPixels(Context context) {
        int H = 0;
        int ver = Build.VERSION.SDK_INT;
        DisplayMetrics dm = new DisplayMetrics();
        android.view.Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        if (ver < 13) {
            H = dm.heightPixels;
        } else if (ver == 13) {
            try {
                Method mt = display.getClass().getMethod("getRealHeight");
                H = (Integer) mt.invoke(display);
            } catch (Exception e) {
                H = dm.heightPixels;
                e.printStackTrace();
            }
        } else if (ver > 13) {
            try {
                Method mt = display.getClass().getMethod("getRawHeight");
                H = (Integer) mt.invoke(display);
            } catch (Exception e) {
                H = dm.heightPixels;
                e.printStackTrace();
            }
        }
        return H;
    }
}
