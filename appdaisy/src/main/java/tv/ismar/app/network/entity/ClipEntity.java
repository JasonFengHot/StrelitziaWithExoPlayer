package tv.ismar.app.network.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by longhai on 16-9-9.
 */
public class ClipEntity {

    public enum Quality {

        QUALITY_LOW, QUALITY_ADAPTIVE, QUALITY_NORMAL, QUALITY_MEDIUM,
        QUALITY_HIGH, QUALITY_ULTRA, QUALITY_BLUERAY, QUALITY_4K;

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
                + "\nis_vip:" + is_vip;
    }
}
