package tv.ismar.app.core.advertisement;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.core.preferences.LogSharedPrefs;
import tv.ismar.app.db.AdvertisementTable;
import tv.ismar.app.entity.LaunchAdvertisementEntity;
import tv.ismar.app.player.CallaPlay;
import tv.ismar.app.util.FileUtils;
import tv.ismar.app.util.HardwareUtils;

/**
 * Created by huaijie on 7/31/15.
 */
public class AdvertisementManager {
    private static final String TAG = "AdvertisementManager";

    public static final String LAUNCH_APP_ADVERTISEMENT = "launch_app";

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private static Context mContext;

    public static void initialize(Context context) {
        mContext = context;
    }

    public void updateAppLaunchAdvertisement(LaunchAdvertisementEntity launchAdvertisementEntity) {
        String type = LAUNCH_APP_ADVERTISEMENT;
        LaunchAdvertisementEntity.AdvertisementData[] advertisementDatas = launchAdvertisementEntity.getAds().getKaishi();
        LogSharedPrefs.setSharedPrefs(LogSharedPrefs.SHARED_PREFS_NAME, new Gson().toJson(advertisementDatas));
        new Delete().from(AdvertisementTable.class).execute();
        for (LaunchAdvertisementEntity.AdvertisementData advertisementData : advertisementDatas) {
            AdvertisementTable advertisementTable = new AdvertisementTable();
            advertisementTable.title = advertisementData.getTitle();
            try {
                advertisementTable.start_date = dateFormat.parse(advertisementData.getStart_date()).getTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateFormat.parse(advertisementData.getEnd_date()));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                advertisementTable.end_date = calendar.getTime().getTime();
                advertisementTable.everyday_time_from = timeFormat.parse(advertisementData.getStart_time()).getTime();
                advertisementTable.everyday_time_to = timeFormat.parse(advertisementData.getEnd_time()).getTime();
            } catch (ParseException e) {
                Log.d(TAG, "updateAppLaunchAdvertisement: " + e.getMessage());
            }

            advertisementTable.url = advertisementData.getMedia_url();
            advertisementTable.location = FileUtils.getFileByUrl(advertisementData.getMedia_url());
            advertisementTable.md5 = advertisementData.getMd5();
            advertisementTable.type = type;
            advertisementTable.media_id = advertisementData.getMedia_id();
            advertisementTable.save();
        }

        List<AdvertisementTable> advertisementTables = new Select().from(AdvertisementTable.class).execute();
        for (int i = 0; i < advertisementTables.size(); i++) {
            String downlaodUrl = advertisementTables.get(i).url;
            File localFile = new File(mContext.getFilesDir() + "/" + advertisementTables.get(i).location);
            String location = advertisementTables.get(i).location;
            String md5Code = advertisementTables.get(i).md5;
            if (!localFile.exists()) {
                //download advertisement
                AdvertisementDownload downloadTask = new AdvertisementDownload(mContext, downlaodUrl, location);
                new Thread(downloadTask).start();

                new CallaPlay().bootAdvDownload(advertisementTables.get(i).title, String.valueOf(advertisementTables.get(i).media_id), advertisementTables.get(i).url);
            } else {
                //compare md5 code
                if (!md5Code.equalsIgnoreCase(HardwareUtils.getMd5ByFile(localFile))) {
                    AdvertisementDownload downloadTask = new AdvertisementDownload(mContext, downlaodUrl, location);
                    new Thread(downloadTask).start();
                   new CallaPlay().bootAdvDownload(advertisementTables.get(i).title, String.valueOf(advertisementTables.get(i).media_id), advertisementTables.get(i).url);
                }
            }
        }
    }


    public String getAppLaunchAdvertisement() {
        Date todayDate = new Date();
        long todayDateTime = todayDate.getTime();
        long todayHour = 0;
        try {
            todayHour = timeFormat.parse(timeFormat.format(todayDate)).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "getAppLaunchAdvertisement: " + e.getMessage());
        }


        List<AdvertisementTable> advertisementTables = new Select().from(AdvertisementTable.class)
                .where("start_date" + " < ?", todayDateTime)
                .where("end_date" + " > ?", todayDateTime)
                .where("everyday_time_from" + " < ?", todayHour)
                .where("everyday_time_to" + " > ?", todayHour)
                .execute();
        if (advertisementTables != null && !advertisementTables.isEmpty()) {
            return "file://" + mContext.getFilesDir() + "/" + advertisementTables.get(0).location;
        } else {
            return "file:///android_asset/poster.png";
        }
    }
}
