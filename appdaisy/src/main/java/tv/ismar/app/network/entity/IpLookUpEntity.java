package tv.ismar.app.network.entity;

public class IpLookUpEntity {

    private String city;
    private String isp;
    private String tel;
    private String ip;
    private String ip_start;
    private String prov;
    private String ip_end;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp_start() {
        return ip_start;
    }

    public void setIp_start(String ip_start) {
        this.ip_start = ip_start;
    }

    public String getProv() {
        return prov;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public String getIp_end() {
        return ip_end;
    }

    public void setIp_end(String ip_end) {
        this.ip_end = ip_end;
    }

}
