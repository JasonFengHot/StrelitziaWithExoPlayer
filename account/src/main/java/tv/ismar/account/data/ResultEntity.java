package tv.ismar.account.data;


public class ResultEntity {
    private String device_token = "";
    private String domain = "1.1.1.1";
    private String ad_domain = "1.1.1.2";
    private String sn_token = "";
    private String log_domain = "1.1.1.4";
    private String upgrade_domain = "1.1.1.3";
    private String zdevice_token = "";
    private String carnation = "1.1.1.1";


    public String getCarnation() {
        return carnation;
    }

    public void setCarnation(String carnation) {
        this.carnation = carnation;
    }

    public String getZdevice_token() {
        return zdevice_token;
    }

    public void setZdevice_token(String zdevice_token) {
        this.zdevice_token = zdevice_token;
    }

    public String getUpgrade_domain() {
        return upgrade_domain;
    }

    public void setUpgrade_domain(String upgrade_domain) {
        this.upgrade_domain = upgrade_domain;
    }

    public String getSn_Token() {
        return sn_token;
    }

    public void setSn_Token(String sn) {
        this.sn_token = sn;
    }

    public String getLog_Domain() {
        return log_domain;
    }

    public void setLog_Domain(String log) {
        this.log_domain = log;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAd_domain() {
        return ad_domain;
    }

    public void setAd_domain(String ad_domain) {
        this.ad_domain = ad_domain;
    }
}
