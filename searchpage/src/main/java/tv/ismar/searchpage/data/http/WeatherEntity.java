package tv.ismar.searchpage.data.http;

/**
 * Created by <huaijiefeng@gmail.com> on 9/15/14.
 */
public class WeatherEntity {
    private String updated;
    private String region;

    private WeatherDetail today;
    private WeatherDetail tomorrow;


    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public WeatherDetail getToday() {
        return today;
    }

    public void setToday(WeatherDetail today) {
        this.today = today;
    }

    public WeatherDetail getTomorrow() {
        return tomorrow;
    }

    public void setTomorrow(WeatherDetail tomorrow) {
        this.tomorrow = tomorrow;
    }

    public static class WeatherDetail {

        private String condition;
        private String temphigh;
        private String templow;
        private String image_url;


        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getTemphigh() {
            return temphigh;
        }

        public void setTemphigh(String temphigh) {
            this.temphigh = temphigh;
        }

        public String getTemplow() {
            return templow;
        }

        public void setTemplow(String templow) {
            this.templow = templow;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

    }
}
