package tv.ismar.app.network.entity;

import java.util.ArrayList;

/**
 * Created by huaijie on 7/3/15.
 */
public class AccountsOrdersEntity {

    private ArrayList<OrderEntity> order_list;
    private ArrayList<OrderEntity> sn_order_list;

    public ArrayList<OrderEntity> getOrder_list() {
        return order_list;
    }

    public void setOrder_list(ArrayList<OrderEntity> order_list) {
        this.order_list = order_list;
    }

    public ArrayList<OrderEntity> getSn_order_list() {
        return sn_order_list;
    }

    public void setSn_order_list(ArrayList<OrderEntity> sn_order_list) {
        this.sn_order_list = sn_order_list;
    }

    public class OrderEntity {
        private String title;
        private String info;
        private String source;
        private String expiry_date;
        private String total_fee;
        private String image_url;
        private String start_date;
        public String type;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getExpiry_date() {
            return expiry_date;
        }

        public void setExpiry_date(String expiry_date) {
            this.expiry_date = expiry_date;
        }

        public String getTotal_fee() {
            return total_fee;
        }

        public void setTotal_fee(String total_fee) {
            this.total_fee = total_fee;
        }

        public String getThumb_url() {
            return image_url;
        }

        public void setThumb_url(String thumb_url) {
            this.image_url = thumb_url;
        }

        public String getStart_date() {
            return start_date;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }
    }
}
