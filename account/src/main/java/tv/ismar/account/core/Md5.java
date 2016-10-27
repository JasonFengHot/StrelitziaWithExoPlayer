package tv.ismar.account.core;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by huibin on 6/2/16.
 */
public class Md5 {
    public static String md5(String string) {
        String value = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes());
            value = new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (Exception e) {
            value = "error";
            e.printStackTrace();

        }
        return value;
    }

    public static String md5File(File file) {
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
            Log.e("md5 file: ", e.getMessage());
            return "error";
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
}
