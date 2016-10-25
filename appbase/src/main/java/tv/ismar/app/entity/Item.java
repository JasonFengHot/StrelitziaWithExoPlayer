package tv.ismar.app.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Item implements Serializable {
	
	private static final long serialVersionUID = 5414782976396856671L;
    public int subitem_show;
	public float bean_score;
	public String adlet_url;
    public Attribute attributes;
    public String caption;
    public Clip clip;
    public String content_model; //对应频道, 例如: documentary
    public int counting_count;
    public String description;
    public int episode;
    public String focus;
    public boolean is_complex;
    public int pk;
    public String poster_url;
    public String publish_date;
    public int quality;
    public float rating_average;
    public int rating_count;
    public Item[] subitems;
    public String[] tags;
    public String thumb_url;
    public String title;
    public Expense expense;
    public Clip preview;
    public int spinoff_pk;
    public boolean is_3d;
    public String logo;
    public String logo_3d;
    public String vendor;
    public Point[] points;
    public int rated;
    //These field below may be none, when get from non "media-detail" api.
    public String model_name;
    public int item_pk;
    public String item_url;
    public int position;
    public String url;
    public boolean live_video;
    public ArrayList<Item> items;
    public boolean isPreview;
    //used only for daram seria and expense page
    public int remainDay;
    public int offset;
    public int month;
    public String list_url;
    public String section;
    public String detail_url;
    public String subtitle;
    public boolean ispayed;
    public String channel;
    public String slug;
    public String fromPage="";
    public String start_time;
    public boolean repeat_buy;
}
