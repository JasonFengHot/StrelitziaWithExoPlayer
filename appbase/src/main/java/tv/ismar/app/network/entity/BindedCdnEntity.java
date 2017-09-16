package tv.ismar.app.network.entity;

import java.util.List;

/** Created by huaijie on 2015/4/22. */
public class BindedCdnEntity {
    public static final String NO_RECORD = "104";

    private String retcode;
    private String retmsg;
    private CdnEntity sncdn;

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

    public CdnEntity getSncdn() {
        return sncdn;
    }

    public void setSncdn(CdnEntity sncdn) {
        this.sncdn = sncdn;
    }

    public class CdnEntity {

        private String add_time;
        private String cdnid;
        private List<? extends Object> cdnid1;
        private List cdnid2;
        private String flag;
        private String remark;
        private String sn;
        private String update_time;

        public List<? extends Object> getCdnid1() {
            return cdnid1;
        }

        public void setCdnid1(List<? extends Object> cdnid1) {
            this.cdnid1 = cdnid1;
        }

        public List getCdnid2() {
            return cdnid2;
        }

        public void setCdnid2(List cdnid2) {
            this.cdnid2 = cdnid2;
        }

        public String getAdd_time() {
            return add_time;
        }

        public void setAdd_time(String add_time) {
            this.add_time = add_time;
        }

        public String getCdnid() {
            return cdnid;
        }

        public void setCdnid(String cdnid) {
            this.cdnid = cdnid;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getUpdate_time() {
            return update_time;
        }

        public void setUpdate_time(String update_time) {
            this.update_time = update_time;
        }
    }
}
