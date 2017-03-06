package tv.ismar.app.network.entity;

/**
 * Created by huibin on 17-2-16.
 */

public class PayWhStatusEntity extends BaseEntity {
    private Info info;

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public static class Info {
        private int status;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public enum PayType {
        ALIPAY("alipay");

        PayType(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }
    }

    public static class Status {
        public static final int WITHOUT_OPEN = 0;
        public static final int OPEN = 1;
    }
}
