package tv.ismar.app.network.entity;


import java.util.ArrayList;
import java.util.Random;

/**
 * Created by huaijie on 2015/4/9.
 */
public class CdnListEntity {
    private ArrayList<CdnEntity> cdn_list;
    private String retcode;
    private String retmsg;

    public ArrayList<CdnEntity> getCdn_list() {
        return cdn_list;
    }

    public void setCdn_list(ArrayList<CdnEntity> cdn_list) {
        this.cdn_list = cdn_list;
    }

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    public class CdnEntity {
        private int cdnID;
        private int flag;
        private String name;
        private int route_trace;
        private String url;


        public int getCdnID() {
            return cdnID;
        }

        public void setCdnID(int cdnID) {
            this.cdnID = cdnID;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public int getRoute_trace() {
            return route_trace;
        }

        public void setRoute_trace(int route_trace) {
            this.route_trace = route_trace;
        }

        public String getName() {
            return name.replace("|", "-").split("-")[0];
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            String[] urls = url.replace("|", "-").split("-");
            return urls[new Random().nextInt(urls.length)];
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getNick() {
            return name.replace("|", "-").split("-")[1];
        }
    }

}
