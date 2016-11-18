package tv.ismar.searchpage.core.initialize;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import tv.ismar.searchpage.data.table.CityTable;
import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.query.Select;

/**
 * Created by huaijie on 1/20/16.
 */
public class InitializeProcess implements Runnable {
    private static final String TAG = "InitializeProcess";

    private Context context;

    public InitializeProcess(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        initalizeCity();
    }


    private void initalizeCity() {
        if (new Select().from(CityTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                InputStream inputStream = context.getResources().getAssets().open("location.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (null != s && !s.equals("")) {
                        String[] strings = s.split("\\,");
                        Long geoId = Long.parseLong(strings[0]);
                        String area = strings[1];
                        String city = strings[2];
                        String province = strings[3];

                        if (area.equals(city)) {
                            CityTable cityTable = new CityTable();
                            cityTable.geo_id = geoId;
                            cityTable.province_id = province;
                            cityTable.city = city;
                            cityTable.save();
                        }
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }
}
