package tv.ismar.app.network.entity;

import java.util.ArrayList;

/**
 * Created by huaijie on 7/3/15.
 */
public class AccountPlayAuthEntity {

    private ArrayList<PlayAuth> sn_playauth_list;
    private ArrayList<PlayAuth> playauth_list;

    public ArrayList<PlayAuth> getSn_playauth_list() {
        return sn_playauth_list;
    }

    public void setSn_playauth_list(ArrayList<PlayAuth> sn_playauth_list) {
        this.sn_playauth_list = sn_playauth_list;
    }

    public ArrayList<PlayAuth> getPlayauth_list() {
        return playauth_list;
    }

    public void setPlayauth_list(ArrayList<PlayAuth> playauth_list) {
        this.playauth_list = playauth_list;
    }

    public class PlayAuth {
        private String expiry_date;
        private String title;
        private String url;
        private String content_model;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getExpiry_date() {
            return expiry_date;
        }

        public void setExpiry_date(String expiry_date) {
            this.expiry_date = expiry_date;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        public String getContentMode() {
            return content_model;
        }

        public void setContentMode(String contentMode) {
            this.content_model = contentMode;
        }
    }
}
