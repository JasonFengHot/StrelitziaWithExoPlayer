package tv.ismar.app.network.entity;

import java.util.List;

/** Created by huaijie on 4/12/16. */
public class PayLayerVipEntity {
    private List<Vip_list> vip_list;

    private String type;

    private String cpname;

    private int cpid;

    public List<Vip_list> getVip_list() {
        return this.vip_list;
    }

    public void setVip_list(List<Vip_list> vip_list) {
        this.vip_list = vip_list;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCpname() {
        return this.cpname;
    }

    public void setCpname(String cpname) {
        this.cpname = cpname;
    }

    public int getCpid() {
        return this.cpid;
    }

    public void setCpid(int cpid) {
        this.cpid = cpid;
    }

    public class Vip_list {
        private String description;

        private String duration;

        private int pk;

        private String price;

        private String vertical_url;

        private String title;

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

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getVertical_url() {
            return this.vertical_url;
        }

        public void setVertical_url(String vertical_url) {
            this.vertical_url = vertical_url;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
