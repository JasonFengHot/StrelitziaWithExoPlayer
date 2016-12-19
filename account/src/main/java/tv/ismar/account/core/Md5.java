package tv.ismar.account.core;

import com.blankj.utilcode.utils.EncryptUtils;

import java.io.File;

/**
 * Created by huibin on 6/2/16.
 */
public class Md5 {
    public static String md5(String string) {
        return EncryptUtils.encryptMD5ToString(string).toLowerCase();
    }

    public static String md5File(File file) {
       return EncryptUtils.encryptMD5File2String(file).toLowerCase();
    }
}
