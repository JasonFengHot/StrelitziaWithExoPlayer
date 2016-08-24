package tv.ismar.app.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by beaver on 16-8-22.
 */
public class Utils {

    public static boolean isEmptyText(String str) {
        return TextUtils.isEmpty(str) || str.equalsIgnoreCase("null");
    }


    public static int daysBetween(String startdate, String enddate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(startdate));
        long time1 = cal.getTimeInMillis();
        cal.setTime(sdf.parse(enddate));
        long time2 = cal.getTimeInMillis();
        long remain = time2 - time1;
        if (remain <= 0) {
            return -1;
        } else {
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            return Integer.parseInt(String.valueOf(between_days));
        }

    }

    public static String getTime() {

        Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        String year = null, month = null, day = null;
        String h = null, min = null, sec = null;
        year = String.valueOf(y);
        if (m < 10) {
            month = "0" + m;
        } else {
            month = "" + m;
        }
        if (d < 10) {
            day = "0" + d;
        } else {
            day = "" + d;
        }
        if (hour < 10) {
            h = "0" + hour;
        } else {
            h = "" + hour;
        }
        if (minute < 10) {
            min = "0" + minute;
        } else {
            min = "" + minute;
        }
        if (second < 10) {
            sec = "0" + second;
        } else {
            sec = "" + second;
        }
        String strDate = year + "-" + month + "-" + day + " " + h + ":"
                + min + ":" + sec;
        return strDate;
    }
}
