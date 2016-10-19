package tv.ismar.app.network.entity;

/**
 * Created by huaijie on 4/11/16.
 */
public class PayLayerEntity {
    private int pk;

    private int pay_type;

    private String cpname;

    private int cpid;

    private Vip vip;

    private Expense_item expense_item;

    private Package pkage;

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPk() {
        return this.pk;
    }

    public Package getPkage() {
        return pkage;
    }

    public void setPkage(Package pkage) {
        this.pkage = pkage;
    }

    public void setPay_type(int pay_type) {
        this.pay_type = pay_type;
    }

    public int getPay_type() {
        return this.pay_type;
    }

    public void setCpname(String cpname) {
        this.cpname = cpname;
    }

    public String getCpname() {
        return this.cpname;
    }

    public void setCpid(int cpid) {
        this.cpid = cpid;
    }

    public int getCpid() {
        return this.cpid;
    }

    public void setVip(Vip vip) {
        this.vip = vip;
    }

    public Vip getVip() {
        return this.vip;
    }

    public void setExpense_item(Expense_item expense_item) {
        this.expense_item = expense_item;
    }

    public Expense_item getExpense_item() {
        return this.expense_item;
    }

    public class Expense_item {
        private String title;

        private String price;

        private String duration;

        private String vertical_url;

        private int pk;

        private String type;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getDuration() {
            return this.duration;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getVertical_url() {
            return this.vertical_url;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public int getPk() {
            return this.pk;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

    }

    public class Package {
        private String title;

        private String price;

        private String duration;

        private String vertical_url;

        private int package_pk;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getDuration() {
            return this.duration;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getVertical_url() {
            return this.vertical_url;
        }

        public void setPackage_pk(int package_pk) {
            this.package_pk = package_pk;
        }

        public int getPackage_pk() {
            return this.package_pk;
        }

    }

    public class Vip {
        private String title;

        private String price;

        private String duration;

        private String vertical_url;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getDuration() {
            return this.duration;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getVertical_url() {
            return this.vertical_url;
        }

    }


}
