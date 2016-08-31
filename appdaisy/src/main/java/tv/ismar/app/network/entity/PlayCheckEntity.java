package tv.ismar.app.network.entity;

public class PlayCheckEntity {
    private String expiry_date;
    private String iqiyi_code;
    private int remainDay;

    public int getRemainDay() {
        return remainDay;
    }

    public void setRemainDay(int remainDay) {
        this.remainDay = remainDay;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getIqiyi_code() {
        return iqiyi_code;
    }

    public void setIqiyi_code(String iqiyi_code) {
        this.iqiyi_code = iqiyi_code;
    }
}