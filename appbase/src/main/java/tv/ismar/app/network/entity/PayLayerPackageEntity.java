package tv.ismar.app.network.entity;

import java.util.List;

/** Created by huaijie on 4/12/16. */
public class PayLayerPackageEntity {
    private List<Item_list> item_list;

    private String description;

    private String title;

    private String price;

    private String duration;

    private int pk;

    private String type;

    public List<Item_list> getItem_list() {
        return this.item_list;
    }

    public void setItem_list(List<Item_list> item_list) {
        this.item_list = item_list;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getPk() {
        return this.pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public class Item_list {
        public static final int ISMARTV_CPID = 3;
        public static final int IQIYI_CPID = 2;
        public static final int SEPARATE_CHARGE = 1;

        private int pay_type;
        private int cpid;
        private int item_id;
        private String vertical_url;
        private String cptitle;
        private String title;
        private String content_model;

        public int getPay_type() {
            return pay_type;
        }

        public void setPay_type(int pay_type) {
            this.pay_type = pay_type;
        }

        public int getCpid() {
            return cpid;
        }

        public void setCpid(int cpid) {
            this.cpid = cpid;
        }

        public String getContent_model() {
            return content_model;
        }

        public void setContent_model(String content_model) {
            this.content_model = content_model;
        }

        public int getItem_id() {
            return this.item_id;
        }

        public void setItem_id(int item_id) {
            this.item_id = item_id;
        }

        public String getVertical_url() {
            return this.vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getCptitle() {
            return this.cptitle;
        }

        public void setCptitle(String cptitle) {
            this.cptitle = cptitle;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
