package tv.ismar.app.entity;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;

public class SportGame {
    private String start_time;
    private String expiry_date;
    private String name;
    private String imageurl;
    private String url;
    private boolean living;
    private boolean is_complex;

    public boolean isLiving() {
        return living;
    }

    public void setLiving(boolean living) {
        this.living = living;
    }

    public boolean isIs_complex() {
        return is_complex;
    }

    public void setIs_complex(boolean is_complex) {
        this.is_complex = is_complex;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getGameType() {
        if (!TextUtils.isEmpty(start_time) && !TextUtils.isEmpty(expiry_date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            try {
                Date starttime = sdf.parse(start_time);
                Date expirytime = sdf.parse(expiry_date);
                if (TrueTime.now().getTime() < starttime.getTime()) return 5;
                if (TrueTime.now().getTime() > starttime.getTime()
                        && TrueTime.now().getTime() < expirytime.getTime()) return 4;
                if (TrueTime.now().getTime() > expirytime.getTime()) return 6;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 6;
    }
}
