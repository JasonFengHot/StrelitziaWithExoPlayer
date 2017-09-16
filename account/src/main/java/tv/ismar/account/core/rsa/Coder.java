package tv.ismar.account.core.rsa;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/** Created by Administrator on 2014/10/10. */
public class Coder {
    public static final String ENCODING = "UTF-8";

    /**
     * Base64解码
     *
     * @param key
     * @return
     */
    public static byte[] decryptBASE64(String key) {
        return Base64.decode(key, Base64.DEFAULT);
    }

    /**
     * Base64编码
     *
     * @param sign
     * @return
     */
    public static String encryptBASE64(byte[] sign) {
        return Base64.encodeToString(sign, Base64.DEFAULT);
    }
    // 解密
    public static byte[] UrlSafeBase64_decode(String data) throws UnsupportedEncodingException {
        byte[] b = Base64.decode(data, Base64.URL_SAFE);
        return b;
    }
    // 解密
    public static String UrlSafeBase64_encode(byte[] data) throws UnsupportedEncodingException {
        // byte[] b= Base64.encode(data, Base64.URL_SAFE);
        String b = Base64.encodeToString(data, Base64.URL_SAFE);
        return b;
    }
    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
}
