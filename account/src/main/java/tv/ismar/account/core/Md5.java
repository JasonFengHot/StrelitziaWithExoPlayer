package tv.ismar.account.core;

import android.util.Log;


import com.blankj.utilcode.util.EncryptUtils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/** Created by huibin on 6/2/16. */
public class Md5 {
    public static String md5(String string) {
        return EncryptUtils.encryptMD5ToString(string).toLowerCase();
    }

    //    public static String md5File(File file) {
    //       return EncryptUtils.encryptMD5File2String(file).toLowerCase();
    //    }

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
}
