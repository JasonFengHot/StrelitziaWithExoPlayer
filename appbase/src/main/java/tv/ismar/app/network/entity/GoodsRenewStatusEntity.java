package tv.ismar.app.network.entity;

/**
 * Created by huibin on 17-2-16.
 */

public class GoodsRenewStatusEntity extends BaseEntity{
    private Info info;

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public static class  Info{
        private int status;
        private int package_id;

        public int getPackage_id() {
            return package_id;
        }

        public void setPackage_id(int package_id) {
            this.package_id = package_id;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }


    public static class Status {
        public static final int WITHOUT_OPEN = 0;
        public static final int OPEN = 1;
    }
}
