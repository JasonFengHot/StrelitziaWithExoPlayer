package tv.ismar.app.core;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.List;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.injectdb.library.query.Select;
import rx.Observer;
import rx.schedulers.Schedulers;
import tv.ismar.app.core.client.SkyService;
import tv.ismar.app.db.DpiTable;
import tv.ismar.app.network.entity.DpiEntity;

/**
 * Created by huibin on 8/11/16.
 */
public class DpiManager {
    private static final String TAG = "DpiManager";
    private static DpiManager ourInstance;


    public static DpiManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new DpiManager();
        }

        return ourInstance;
    }

    private DpiManager() {
        fetchDpi();
    }

    private void fetchDpi() {
        SkyService.Factory.create(SimpleRestClient.root_url).fetchDpi()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<List<DpiEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<DpiEntity> dpiEntities) {
                        new Delete().from(DpiTable.class).execute();
                        ActiveAndroid.beginTransaction();
                        try {
                            for (DpiEntity dpiEntity : dpiEntities) {
                                if (dpiEntity.getApp_name().equals("sky")) {
                                    DpiTable dpiTable = new DpiTable();
                                    dpiTable.pay_type = dpiEntity.getPay_type();
                                    dpiTable.image = dpiEntity.getImage();
                                    dpiTable.cp = dpiEntity.getCp();
                                    dpiTable.name = Integer.parseInt(dpiEntity.getName());
                                    dpiTable.save();
                                }
                            }
                            ActiveAndroid.setTransactionSuccessful();
                        } finally {
                            ActiveAndroid.endTransaction();
                        }
                    }
                });

    }

    public String getImage(Activity activity, int payType, int cpId) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        String name = String.valueOf((int) (height / density));
        DpiTable dpiTable = null;
        try {

            dpiTable = new Select().from(DpiTable.class)
                    .where("pay_type = ?", payType)
                    .where("cp = ?", cpId)
                    .orderBy("abs(" + height + " - name) asc")
                    .executeSingle();
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
        return dpiTable == null ? "test" : dpiTable.image;
    }
}
