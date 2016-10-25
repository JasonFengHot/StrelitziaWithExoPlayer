package tv.ismar.app.entity;

/**
 * Created by huaijie on 7/30/15.
 */
public class LaunchAdvertisementEntity {
    private String retcode;
    private String retmsg;
    private Advertisement ads;

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

    public Advertisement getAds() {
        return ads;
    }

    public void setAds(Advertisement ads) {
        this.ads = ads;
    }

    public class Advertisement {
        private AdvertisementData[] kaishi;

        public AdvertisementData[] getKaishi() {
            return kaishi;
        }

        public void setKaishi(AdvertisementData[] kaishi) {
            this.kaishi = kaishi;
        }
    }

    public class AdvertisementData {
        private String start_date;
        private String start_time;
        private String end_date;
        private String end_time;
        private String media_id;
        private String description;
        private String retmsg;
        private String title;
        private String retcode;
        private String end;
        private String start;
        private String report_url;
        private String duration;
        private String media_type;
        private String serial;
        private String md5;
        private String media_url;


        public String getStart_date() {
            return start_date;
        }

        public void setStart_date(String start_date) {
            this.start_date = start_date;
        }

        public String getStart_time() {
            return start_time;
        }

        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }

        public String getEnd_date() {
            return end_date;
        }

        public void setEnd_date(String end_date) {
            this.end_date = end_date;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }

        public String getMedia_id() {
            return media_id;
        }

        public void setMedia_id(String media_id) {
            this.media_id = media_id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRetmsg() {
            return retmsg;
        }

        public void setRetmsg(String retmsg) {
            this.retmsg = retmsg;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRetcode() {
            return retcode;
        }

        public void setRetcode(String retcode) {
            this.retcode = retcode;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getReport_url() {
            return report_url;
        }

        public void setReport_url(String report_url) {
            this.report_url = report_url;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getMedia_type() {
            return media_type;
        }

        public void setMedia_type(String media_type) {
            this.media_type = media_type;
        }

        public String getSerial() {
            return serial;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public String getMedia_url() {
            return media_url;
        }

        public void setMedia_url(String media_url) {
            this.media_url = media_url;
        }
    }


}
