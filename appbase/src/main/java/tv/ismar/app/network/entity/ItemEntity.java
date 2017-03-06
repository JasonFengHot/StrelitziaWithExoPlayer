package tv.ismar.app.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class ItemEntity {

    private Clip clip;
    private String focus;
    @SerializedName("subitem_show")
    private String subitemShow;
    @SerializedName("is_3d")
    private boolean is3d;
    @SerializedName("content_model")
    private String contentModel;
    private String logo;
    @SerializedName("detail_url")
    private String detailUrl;
    private int quality;
    @SerializedName("rating_count")
    private int ratingCount;
    private String source;
    private String vendor;
    @SerializedName("adlet_url")
    private String adletUrl;
    @SerializedName("bean_score")
    private String beanScore;
    @SerializedName("poster_url")
    private String posterUrl;
    private int pk;
    @SerializedName("vertical_url")
    private String verticalUrl;
    private String description;
    private List<String> tags;
    @SerializedName("rating_average")
    private String ratingAverage;
    private ItemEntity[] subitems;
    private boolean finished;
    @SerializedName("live_video")
    private boolean liveVideo;
    @SerializedName("thumb_url")
    private String thumbUrl;
    @SerializedName("counting_count")
    private int countingCount;
    @SerializedName("logo_3d")
    private String logo3d;
    private int episode;
    private String title;
    private String caption;
    private List<Point> points;
    @SerializedName("publish_date")
    private String publishDate;
    @SerializedName("is_complex")
    private boolean isComplex;
    private Attributes attributes;
    @SerializedName("item_pk")
    private int itemPk;
    private String list_url;
    private String item_url;
    private Expense expense;
    private boolean repeat_buy;
    private Preview preview;

    private String subtitle;
    private boolean renew_buy;

    public boolean isRenew_buy() {
        return renew_buy;
    }

    public void setRenew_buy(boolean renew_buy) {
        this.renew_buy = renew_buy;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @SerializedName("start_time")
    private Date startTime;


    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    private List<ItemEntity> items;

    public List<ItemEntity> getItems() {
        return items;
    }

    public void setItems(List<ItemEntity> items) {
        this.items = items;
    }

    public boolean isRepeat_buy() {
        return repeat_buy;
    }

    public void setRepeat_buy(boolean repeat_buy) {
        this.repeat_buy = repeat_buy;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public String getItem_url() {
        return item_url;
    }

    public void setItem_url(String item_url) {
        this.item_url = item_url;
    }

    public String getList_url() {
        return list_url;
    }

    public void setList_url(String list_url) {
        this.list_url = list_url;
    }

    public Clip getClip() {
        return clip;
    }

    public void setClip(Clip clip) {
        this.clip = clip;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getSubitemShow() {
        return subitemShow;
    }

    public void setSubitemShow(String subitemShow) {
        this.subitemShow = subitemShow;
    }

    public boolean getIs3d() {
        return is3d;
    }

    public void setIs3d(boolean is3d) {
        this.is3d = is3d;
    }

    public String getContentModel() {
        return contentModel;
    }

    public void setContentModel(String contentModel) {
        this.contentModel = contentModel;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getAdletUrl() {
        return adletUrl;
    }

    public void setAdletUrl(String adletUrl) {
        this.adletUrl = adletUrl;
    }

    public String getBeanScore() {
        return beanScore;
    }

    public void setBeanScore(String beanScore) {
        this.beanScore = beanScore;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getVerticalUrl() {
        return verticalUrl;
    }

    public void setVerticalUrl(String verticalUrl) {
        this.verticalUrl = verticalUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(String ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public ItemEntity[] getSubitems() {
        return subitems;
    }

    public void setSubitems(ItemEntity[] subitems) {
        this.subitems = subitems;
    }

    public boolean getFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean getLiveVideo() {
        return liveVideo;
    }

    public void setLiveVideo(boolean liveVideo) {
        this.liveVideo = liveVideo;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public int getCountingCount() {
        return countingCount;
    }

    public void setCountingCount(int countingCount) {
        this.countingCount = countingCount;
    }

    public String getLogo3d() {
        return logo3d;
    }

    public void setLogo3d(String logo3d) {
        this.logo3d = logo3d;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public boolean getIsComplex() {
        return isComplex;
    }

    public void setIsComplex(boolean isComplex) {
        this.isComplex = isComplex;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public int getItemPk() {
        return itemPk;
    }

    public void setItemPk(int itemPk) {
        this.itemPk = itemPk;
    }

    public Preview getPreview() {
        return preview;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    public class Clip {

        private String url;
        private int pk;
        private String length;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getPk() {
            return pk;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
        }

    }

    public class Attributes {
        private String classification;

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }

        private String[][] director;
        private String[][] genre;
        private String[][] actor;
        private String[][] guest;
        private String[][] emcee;

        public String[][] getGuest() {
            return guest;
        }

        public void setGuest(String[][] guest) {
            this.guest = guest;
        }

        public String[][] getEmcee() {
            return emcee;
        }

        public void setEmcee(String[][] emcee) {
            this.emcee = emcee;
        }

        @SerializedName("air_date")
        private String airDate;
        private String[] area;

        public String[] getArea() {
            return area;
        }

        public void setArea(String[] area) {
            this.area = area;
        }

        public String[][] getDirector() {
            return director;
        }

        public void setDirector(String[][] director) {
            this.director = director;
        }

        public String[][] getGenre() {
            return genre;
        }

        public void setGenre(String[][] genre) {
            this.genre = genre;
        }

        public String[][] getActor() {
            return actor;
        }

        public void setActor(String[][] actor) {
            this.actor = actor;
        }

        public String getAirDate() {
            return airDate;
        }

        public void setAirDate(String airDate) {
            this.airDate = airDate;
        }

    }


    public class Point {
        private String image;
        private String time;
        private String type;
        private String title;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public class Expense {
        public float price;
        public float subprice;
        public int duration;
        public int cpid;
        public String cpname;
        public String cptitle;
        public int pay_type;
        public String cplogo;
        public boolean sale_subitem;
        public int jump_to;
        public float renew_price;
        public float nominal_price;

        public float getRenew_price() {
            return renew_price;
        }

        public void setRenew_price(float renew_price) {
            this.renew_price = renew_price;
        }

        public float getNominal_price() {
            return nominal_price;
        }

        public void setNominal_price(float nominal_price) {
            this.nominal_price = nominal_price;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public float getSubprice() {
            return subprice;
        }

        public void setSubprice(float subprice) {
            this.subprice = subprice;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getCpid() {
            return cpid;
        }

        public void setCpid(int cpid) {
            this.cpid = cpid;
        }

        public String getCpname() {
            return cpname;
        }

        public void setCpname(String cpname) {
            this.cpname = cpname;
        }

        public String getCptitle() {
            return cptitle;
        }

        public void setCptitle(String cptitle) {
            this.cptitle = cptitle;
        }

        public int getPay_type() {
            return pay_type;
        }

        public void setPay_type(int pay_type) {
            this.pay_type = pay_type;
        }

        public String getCplogo() {
            return cplogo;
        }

        public void setCplogo(String cplogo) {
            this.cplogo = cplogo;
        }

        public boolean isSale_subitem() {
            return sale_subitem;
        }

        public void setSale_subitem(boolean sale_subitem) {
            this.sale_subitem = sale_subitem;
        }

        public int getJump_to() {
            return jump_to;
        }

        public void setJump_to(int jump_to) {
            this.jump_to = jump_to;
        }
    }

    public class Preview {

        private String url;
        private int pk;
        private String length;
        private boolean seek;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getPk() {
            return pk;
        }

        public void setPk(int pk) {
            this.pk = pk;
        }

        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public boolean isSeek() {
            return seek;
        }

        public void setSeek(boolean seek) {
            this.seek = seek;
        }
    }
}
