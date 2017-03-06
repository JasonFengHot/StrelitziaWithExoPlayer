package tv.ismar.app.network.entity;

/**
 * Created by huibin on 17-2-21.
 */

public class ChoosewayEntity {
    private  String type;
    private Data agreement;
    private Data qrcode;
    private Data pay;

    public Data getAgreement() {
        return agreement;
    }

    public void setAgreement(Data agreement) {
        this.agreement = agreement;
    }

    public Data getQrcode() {
        return qrcode;
    }

    public void setQrcode(Data qrcode) {
        this.qrcode = qrcode;
    }

    public Data getPay() {
        return pay;
    }

    public void setPay(Data pay) {
        this.pay = pay;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public static class Data{
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
