package tv.ismar.app.network.entity;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by longhai on 16-9-9.
 */
public class ClipEntity {

    public enum Quality {

        QUALITY_LOW(0), QUALITY_ADAPTIVE(1), QUALITY_NORMAL(2), QUALITY_MEDIUM(3),
        QUALITY_HIGH(4), QUALITY_ULTRA(5), QUALITY_BLUERAY(6), QUALITY_4K(7);

        private int value;

        Quality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Quality getQuality(int value) {
            switch (value) {
                case 0:
                    return QUALITY_LOW;
                case 1:
                    return QUALITY_ADAPTIVE;
                case 2:
                    return QUALITY_NORMAL;
                case 3:
                    return QUALITY_MEDIUM;
                case 4:
                    return QUALITY_HIGH;
                case 5:
                    return QUALITY_ULTRA;
                case 6:
                    return QUALITY_BLUERAY;
                case 7:
                    return QUALITY_4K;
                default:
                    return null;
            }
        }

        public static String getString(Quality type) {
            switch (type) {
                case QUALITY_LOW:
                    return "LOW";
                case QUALITY_ADAPTIVE:
                    return "自适应";
                case QUALITY_NORMAL:
                    return "流畅";
                case QUALITY_MEDIUM:
                    return "高清";
                case QUALITY_HIGH:
                    return "超清";
                case QUALITY_ULTRA:
                    return "1080P";
                case QUALITY_BLUERAY:
                    return "蓝光";
                case QUALITY_4K:
                    return "4K";
            }
            return "Error";
        }
    }

    /**
     * 该码率忽略，废除不用
     */
    private String low;
    /**
     * 自适应
     */
    private String adaptive;
    /**
     * 流畅
     */
    private String normal;
    /**
     * 高清
     */
    private String medium;
    /**
     * 超清
     */
    private String high;
    /**
     * 1080P
     */
    private String ultra;
    /**
     * 蓝光
     */
    private String blueray;
    /**
     * 4K
     */
    @SerializedName("4k")
    private String _4k;
    /**
     * 爱奇艺
     */
    private String iqiyi_4_0;
    /**
     * 是否爱奇艺会员
     */
    private boolean is_vip;
    /**
     * 奇艺2.1SDK需要新传入字段
     */
    private boolean is_drm;

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getAdaptive() {
        return adaptive;
    }

    public void setAdaptive(String adaptive) {
        this.adaptive = adaptive;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getUltra() {
        return ultra;
    }

    public void setUltra(String ultra) {
        this.ultra = ultra;
    }

    public String getBlueray() {
        return blueray;
    }

    public void setBlueray(String blueray) {
        this.blueray = blueray;
    }

    public String get_4k() {
        return _4k;
    }

    public void set_4k(String _4k) {
        this._4k = _4k;
    }

    public String getIqiyi_4_0() {
        return iqiyi_4_0;
    }

    public void setIqiyi_4_0(String iqiyi_4_0) {
        this.iqiyi_4_0 = iqiyi_4_0;
    }

    public boolean is_vip() {
        return is_vip;
    }

    public void setIs_vip(boolean is_vip) {
        this.is_vip = is_vip;
    }

    public boolean is_drm() {
        return is_drm;
    }

    public void setIs_drm(boolean is_drm) {
        this.is_drm = is_drm;
    }

    @Override
    public String toString() {
        return "low:" + low
                + "\nadaptive:" + adaptive
                + "\nnormal:" + normal
                + "\nmedium:" + medium
                + "\nhigh:" + high
                + "\nultra:" + ultra
                + "\nblueray:" + blueray
                + "\n_4k:" + _4k
                + "\niqiyi_4_0:" + iqiyi_4_0
                + "\nis_vip:" + is_vip
                + "\nis_drm:" + is_drm;
    }

    public String getHighest() {
        String[] strs = {low, normal, medium, high, ultra, blueray, _4k};
        for (int i = strs.length - 1; i >= 0; i--) {
            if (!TextUtils.isEmpty(strs[i])) {
                return strs[i];
            }
        }
        return "";
    }

    public String getLowest() {
        String[] strs = {low, normal, medium, high, ultra, blueray, _4k};
        for (int i = 0; i < strs.length - 1; i++) {
            if (!TextUtils.isEmpty(strs[i])) {
                return strs[i];
            }
        }
        return "";
    }
}
