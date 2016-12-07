package tv.ismar.helperpage.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
    private static final String TAG = "StringUtilities";
    private static final String NORTH_CHINA_STRING = "北京市, 天津市, 河北省, 山西省,山东省,内蒙古自治区";
    private static final String EAST_CHINA_STRING = "上海市,江苏省,浙江省";
    private static final String SOUTH_CHINA_STRING = "广东省,福建省,海南省,香港特区,澳门特区,台湾省";
    private static final String CENTRAL_CHINA_STRING = "河南省,湖北省,湖南省,安徽省,江西省";
    private static final String WS_CHINA_STRING = "重庆市,云南省,广西,贵州省,四川省";
    private static final String WN_CHINA_STRING = "新疆,甘肃省,陕西省,青海省,西藏,宁夏回族";
    private static final String EN_CHINA_STRING = "辽宁省,黑龙江省,吉林省";

    private static final int NORTH_CHINA_CODE = 1;
    private static final int EAST_CHINA_CODE = 2;
    private static final int SOUTH_CHINA_CODE = 3;
    private static final int CENTRAL_CHINA_CODE = 4;
    private static final int WS_CHINA_CODE = 8;
    private static final int WN_CHINA_CODE = 9;
    private static final int EN_CHINA_CODE = 10;
    public static final int OTHERS_CODE = 6;

    private static final String NORTH_CHINA = "华北";
    private static final String EAST_CHINA = "华东";
    private static final String SOUTH_CHINA = "华南";
    private static final String CENTRAL_CHINA = "华中";
    private static final String WS_CHINA = "西南";
    private static final String WN_CHINA = "西北";
    private static final String EN_CHINA = "东北";


    private static final String CHINA_NET = "电信";
    private static final String CHINA_UNICOM = "联通";
    private static final String CHINA_MOBILE = "移动";
    private static final String CHINA_GREATE_WALL = "长宽";

    private static final int CHINA_NET_CODE = 1;
    private static final int CHINA_UNICOM_CODE = 2;
    private static final int CHINA_MOBILE_CODE = 3;
    private static final int CHINA_GREATE_CODE = 5;


    /**
     * according to province get area
     *
     * @param province
     * @return
     */
    public static String getAreaNameByProvince(String province) {
        if (NORTH_CHINA_STRING.indexOf(province) != -1)
            return NORTH_CHINA;
        else if (EAST_CHINA_STRING.indexOf(province) != -1)
            return EAST_CHINA;
        else if (SOUTH_CHINA_STRING.indexOf(province) != -1)
            return SOUTH_CHINA;
        else if (CENTRAL_CHINA_STRING.indexOf(province) != -1)
            return CENTRAL_CHINA;
        else if (WS_CHINA_STRING.indexOf(province) != -1)
            return WS_CHINA;
        else if (WN_CHINA_STRING.indexOf(province) != -1)
            return WN_CHINA;
        else if (EN_CHINA_STRING.indexOf(province) != -1)
            return EN_CHINA;
        return "未知区域";
    }


    public static int getAreaCodeByProvince(String province) {
        if (NORTH_CHINA_STRING.indexOf(province) != -1)
            return NORTH_CHINA_CODE;
        else if (EAST_CHINA_STRING.indexOf(province) != -1)
            return EAST_CHINA_CODE;
        else if (SOUTH_CHINA_STRING.indexOf(province) != -1)
            return SOUTH_CHINA_CODE;
        else if (CENTRAL_CHINA_STRING.indexOf(province) != -1)
            return CENTRAL_CHINA_CODE;
        else if (WS_CHINA_STRING.indexOf(province) != -1)
            return WS_CHINA_CODE;
        else if (WN_CHINA_STRING.indexOf(province) != -1)
            return WN_CHINA_CODE;
        else if (EN_CHINA_STRING.indexOf(province) != -1)
            return EN_CHINA_CODE;
        return OTHERS_CODE;
    }

    public static int getAreaCodeByNode(String node) {
        if (node.indexOf(NORTH_CHINA) != -1) {
            return NORTH_CHINA_CODE;
        } else if (node.indexOf(EAST_CHINA) != -1)
            return EAST_CHINA_CODE;
        else if (node.indexOf(SOUTH_CHINA) != -1)
            return SOUTH_CHINA_CODE;
        else if (node.indexOf(CENTRAL_CHINA) != -1)
            return CENTRAL_CHINA_CODE;
        else if (node.indexOf(WN_CHINA) != -1)
            return WN_CHINA_CODE;
        else if (node.indexOf(WS_CHINA) != -1)
            return WS_CHINA_CODE;
        else if (node.indexOf(EN_CHINA) != -1)
            return EN_CHINA_CODE;
        return OTHERS_CODE;
    }

    public static int getIspCodeByNode(String node) {
        if (node.indexOf(CHINA_NET) != -1)
            return CHINA_NET_CODE;
        else if (node.indexOf(CHINA_UNICOM) != -1)
            return CHINA_UNICOM_CODE;
        else if (node.indexOf(CHINA_MOBILE) != -1)
            return CHINA_MOBILE_CODE;
        else if (node.indexOf(CHINA_GREATE_WALL) != -1)
            return CHINA_GREATE_CODE;
        else return OTHERS_CODE;
    }

    public static String getMd5Code(String string) {
        String value = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(string.getBytes());
            value = new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value;
    }
}
