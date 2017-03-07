package tv.ismar.app.network.entity;

/**
 * Created by huibin on 17-2-16.
 */

public class AgreementEntity extends BaseEntity {
    private Info[] info;

    public Info[] getInfo() {
        return info;
    }

    public void setInfo(Info[] info) {
        this.info = info;
    }

    public static class Info {
        private String title;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public enum RenewalType {

        ALIPAY_RENEWAL("renew");

        private String value;

        RenewalType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
