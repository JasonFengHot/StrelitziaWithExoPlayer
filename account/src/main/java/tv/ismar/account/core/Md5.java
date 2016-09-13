package tv.ismar.account.core;

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
            e.printStackTrace();

        }
        return value;
    }
}
