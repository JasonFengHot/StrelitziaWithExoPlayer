package tv.ismar.app.network.entity;

/**
 * Created by huaijie on 1/22/15.
 */
public class SpeedLogEntity {
    private String speed;
    private String isp;
    private String cdn_id;
    private String cdn_name;
    private String location;


    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getCdn_id() {
        return cdn_id;
    }

    public void setCdn_id(String cdn_id) {
        this.cdn_id = cdn_id;
    }

    public String getCdn_name() {
        return cdn_name;
    }

    public void setCdn_name(String cdn_name) {
        this.cdn_name = cdn_name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
