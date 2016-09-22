package tv.ismar.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.VodApplication;
import tv.ismar.app.db.DBHelper.DBFields;
import tv.ismar.app.entity.DBQuality;
import tv.ismar.app.entity.History;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.util.Utils;

public class LocalHistoryManager implements HistoryManager {

    /**
     * store history records.
     */
    private ArrayList<History> mHistories;

    private DBHelper mDBHelper;

    public LocalHistoryManager(Context context) {
        mDBHelper = VodApplication.getAppContext().getDBHelper();
        mHistories = mDBHelper.getAllHistories("no");
    }

    private int mTotalEntriesLimit = 50;

    @Override
    public void addHistory(String title, String url, long currentPosition, String isnet) {
        if (url == null || title == null) {
            throw new RuntimeException("url or title can not be null");
        }

        long currentTimeMillis = System.currentTimeMillis();
        if (mHistories != null && mHistories.size() > 0) {
            for (History history : mHistories) {
                if (url.equals(history.url) && history.id != 0) {
                    history.last_position = currentPosition;
                    history.last_played_time = currentTimeMillis;
                    mDBHelper.updateHistory(history);
                    break;
                }
            }
        } else {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.DBFields.HistroyTable.TITLE, title);
            cv.put(DBHelper.DBFields.HistroyTable.URL, url);
            cv.put(DBHelper.DBFields.HistroyTable.LAST_PLAY_TIME, currentTimeMillis);
            cv.put(DBHelper.DBFields.HistroyTable.LAST_POSITION, currentPosition);
            mDBHelper.insert(cv, DBHelper.DBFields.HistroyTable.TABLE_NAME, mTotalEntriesLimit);
            mHistories = mDBHelper.getAllHistories(isnet);
        }
    }

    @Override
    public History getHistoryByUrl(String url, String isnet) {
        if (url == null) {
            throw new RuntimeException("url cannot be null");
        }
        History history = null;
        if (mHistories == null) {
            mHistories = new ArrayList<History>();
        } else {
            for (History h : mHistories) {
                if (url.equals(h.url)) {
                    history = h;
                    break;
                }
            }
        }
        if (history == null) {
            history = mDBHelper.queryHistoryByUrl(url, isnet);
            if (history != null) {
                mHistories.add(history);
            }
        }
        return history;
    }

    @Override
    public ArrayList<History> getAllHistories(String isnet) {
        if (mHistories == null) {
            mHistories = new ArrayList<History>();
        }
        mHistories = mDBHelper.getAllHistories(isnet);
        return mHistories;
    }

    @Override
    public void addHistory(History history, String isnet) {
        if (history == null || history.title == null || history.content_model == null || history.url == null) {
            throw new RuntimeException("history or history's field should not be null");
        }

        long currentTimeMillis = System.currentTimeMillis();
        History h = getHistoryByUrl(history.url, isnet);
        if (h != null) {
            h.last_position = history.last_position;
            h.last_played_time = currentTimeMillis;
            h.title = history.title;
            h.adlet_url = history.adlet_url;
            h.content_model = history.content_model;
            h.quality = history.quality;
            h.last_quality = history.last_quality;
            h.is_complex = history.is_complex;
            h.is_continue = history.is_continue;
            h.sub_url = history.sub_url;
            h.isnet = isnet;
            h.price = history.price;
            h.cpid = history.cpid;
            h.cpname = history.cpname;
            h.cptitle = history.cptitle;
            h.paytype = history.paytype;
            mDBHelper.updateHistory(h);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(DBFields.HistroyTable.TITLE, history.title);
            cv.put(DBFields.HistroyTable.URL, history.url);
            cv.put(DBFields.HistroyTable.LAST_PLAY_TIME, currentTimeMillis);
            cv.put(DBFields.HistroyTable.LAST_POSITION, history.last_position);
            cv.put(DBFields.HistroyTable.ADLET_URL, history.adlet_url);
            cv.put(DBFields.HistroyTable.CONTENT_MODEL, history.content_model);
            cv.put(DBFields.HistroyTable.QUALITY, history.quality);
            cv.put(DBFields.HistroyTable.LAST_QUALITY, history.last_quality);
            cv.put(DBFields.HistroyTable.IS_COMPLEX, history.is_complex ? 1 : 0);
            cv.put(DBFields.HistroyTable.IS_CONTINUE, history.is_continue ? 1 : 0);
            cv.put(DBFields.HistroyTable.SUB_URL, history.sub_url);
            cv.put(DBFields.HistroyTable.PRICE, history.price);
            cv.put(DBFields.HistroyTable.ISNET, isnet);
            cv.put(DBFields.FavoriteTable.CPID, history.cpid);
            cv.put(DBFields.FavoriteTable.CPNAME, history.cpname);
            cv.put(DBFields.FavoriteTable.CPTITLE, history.cptitle);
            cv.put(DBFields.FavoriteTable.PAYTYPE, history.paytype);
            long result = mDBHelper.insert(cv, DBFields.HistroyTable.TABLE_NAME, mTotalEntriesLimit);
            mHistories = mDBHelper.getAllHistories(isnet);
            if (result >= 0) {
                new DataCollectionTask().execute(history);
            }
        }
    }

    @Override
    public void deleteHistory(String url, String isnet) {
        if (url == null) {
            throw new RuntimeException("url should not be null");
        }
        int rowsAffected = mDBHelper.deleteHistory(DBFields.HistroyTable.TABLE_NAME, url, isnet);
        Log.d("LocalHistoryManager", rowsAffected + "records delete");
        mHistories = mDBHelper.getAllHistories(isnet);
    }

    @Override
    public void deleteAll(String isnet) {
        mDBHelper.deleteHistory(DBFields.HistroyTable.TABLE_NAME, null, isnet);
        mHistories.clear();
    }

    @Override
    public void addOrUpdateQuality(DBQuality quality) {
        DBQuality tempQuality = mDBHelper.queryQualtiy();
        if (tempQuality != null) {
            tempQuality.url = quality.url;
            tempQuality.quality = quality.quality;
            mDBHelper.updateQualtiy(tempQuality);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(DBFields.QualityTable.URL, quality.url);
            cv.put(DBFields.QualityTable.QUALITY, quality.quality);
            mDBHelper.insert(cv, DBFields.QualityTable.TABLE_NAME, 1);
        }

    }

    @Override
    public DBQuality getQuality() {
        DBQuality quality = mDBHelper.queryQualtiy();
        return quality;
    }

    class DataCollectionTask extends AsyncTask<History, Void, Void> {

        @Override
        protected Void doInBackground(History... params) {
            if (params != null && params.length > 0) {
                History history = params[0];
                HashMap<String, Object> properties = new HashMap<String, Object>();
                int item_id = Utils.getItemPk(history.url);
                properties.put(EventProperty.ITEM, item_id);
                if (history.sub_url != null) {
                    int sub_id = Utils.getItemPk(history.sub_url);
                    properties.put(EventProperty.SUBITEM, sub_id);
                }
                properties.put(EventProperty.TITLE, history.title);
                properties.put(EventProperty.POSITION, history.last_position);
                properties.put("userid", IsmartvActivator.getInstance().getDeviceToken());

                // TODO 上传历史记录至服务器
//                NetworkUtils.SaveLogToLocal(NetworkUtils.VIDEO_HISTORY, properties);
            }
            return null;
        }

    }

}
