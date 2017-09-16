package tv.ismar.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.client.NetworkUtils;
import tv.ismar.app.db.DBHelper.DBFields;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.network.entity.EventProperty;
import tv.ismar.app.util.Utils;

public class LocalFavoriteManager implements FavoriteManager {

    private ArrayList<Favorite> mFavorites;

    private DBHelper mDBHelper;

    public LocalFavoriteManager(Context context) {
        mDBHelper = VodApplication.getModuleAppContext().getModuleDBHelper();
        mFavorites = mDBHelper.getAllFavorites("no");
    }

    @Override
    public void addFavorite(String title, String url, String content_model, String isnet) {
        if (url == null || title == null) {
            throw new RuntimeException("title and url cannot be null");
        }
        if (mFavorites != null && mFavorites.size() > 0) {
            for (Favorite favorite : mFavorites) {
                if (url.equals(favorite.url) && favorite.id != 0) {
                    favorite.title = title;
                    favorite.content_model = content_model;
                    mDBHelper.updateFavorite(favorite);
                }
            }
        } else {
            ContentValues cv = new ContentValues();
            cv.put(DBFields.FavoriteTable.TITLE, title);
            cv.put(DBFields.FavoriteTable.URL, url);
            cv.put(DBFields.FavoriteTable.CONTENT_MODEL, content_model);
            mDBHelper.insert(cv, DBFields.FavoriteTable.TABLE_NAME, 0);
            mFavorites = mDBHelper.getAllFavorites(isnet);
        }
    }

    @Override
    public Favorite getFavoriteByUrl(String url, String isnet) {
        if (url == null) {
            throw new RuntimeException("url cannot be null");
        }
        Favorite favorite = null;
        if (mFavorites == null) {
            mFavorites = new ArrayList<Favorite>();
        } else {
            for (Favorite f : mFavorites) {
                if (url.equals(f.url)) {
                    favorite = f;
                    break;
                }
            }
        }
        if (favorite == null) {
            favorite = mDBHelper.queryFavoriteByUrl(url, isnet);
            if (favorite != null) {
                mFavorites.add(favorite);
            }
        }
        return favorite;
    }

    @Override
    public ArrayList<Favorite> getAllFavorites(String isent) {
        if (mFavorites == null) {
            mFavorites = new ArrayList<Favorite>();
        }
        mFavorites = mDBHelper.getAllFavorites(isent);
        return mFavorites;
    }

    @Override
    public void deleteFavoriteByUrl(String url, String isnet) {
        if (url == null) {
            throw new RuntimeException("url cannot be null");
        }
        mDBHelper.delete(DBFields.FavoriteTable.TABLE_NAME, url, isnet);
        mFavorites = mDBHelper.getAllFavorites(isnet);
    }

    @Override
    public void deleteAll(String isnet) {
        mDBHelper.delete(DBFields.FavoriteTable.TABLE_NAME, null, isnet);
        mFavorites.clear();
    }

    @Override
    public void addFavorite(Favorite favorite, String isnet) {
        if (favorite == null
                || favorite.url == null
                || favorite.title == null
                || favorite.content_model == null) {
            throw new RuntimeException("favorite or favorite fields cannot be null");
        }
        Favorite f = getFavoriteByUrl(favorite.url, isnet);
        if (f != null) {
            f.title = favorite.title;
            f.content_model = favorite.content_model;
            f.adlet_url = favorite.adlet_url;
            f.quality = favorite.quality;
            f.is_complex = favorite.is_complex;
            f.isnet = isnet;
            f.cpid = favorite.cpid;
            f.cpname = favorite.cpname;
            f.cptitle = favorite.cptitle;
            f.paytype = favorite.paytype;
            mDBHelper.updateFavorite(favorite);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(DBFields.FavoriteTable.TITLE, favorite.title);
            cv.put(DBFields.FavoriteTable.URL, favorite.url);
            cv.put(DBFields.FavoriteTable.CONTENT_MODEL, favorite.content_model);
            cv.put(DBFields.FavoriteTable.ADLET_URL, favorite.adlet_url);
            cv.put(DBFields.FavoriteTable.QUALITY, favorite.quality);
            cv.put(DBFields.FavoriteTable.IS_COMPLEX, favorite.is_complex ? 1 : 0);
            cv.put(DBFields.FavoriteTable.ISNET, isnet);
            cv.put(DBFields.FavoriteTable.CPID, favorite.cpid);
            cv.put(DBFields.FavoriteTable.CPNAME, favorite.cpname);
            cv.put(DBFields.FavoriteTable.CPTITLE, favorite.cptitle);
            cv.put(DBFields.FavoriteTable.PAYTYPE, favorite.paytype);
            long result = mDBHelper.insert(cv, DBFields.FavoriteTable.TABLE_NAME, 0);
            mFavorites = mDBHelper.getAllFavorites(isnet);
            if (result >= 0) {
                new DataUploadTask().execute(favorite);
            }
        }
    }

    class DataUploadTask extends AsyncTask<Favorite, Void, Void> {

        @Override
        protected Void doInBackground(Favorite... params) {
            if (params != null && params.length > 0) {
                Favorite favorite = params[0];
                final String url = favorite.url;
                final String title = favorite.title;
                int id = Utils.getItemPk(url);
                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put(EventProperty.ITEM, id);
                properties.put(EventProperty.TITLE, title);
                properties.put(
                        "userid",
                        TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername())
                                ? IsmartvActivator.getInstance().getDeviceToken()
                                : IsmartvActivator.getInstance().getUsername());

                NetworkUtils.SaveLogToLocal(NetworkUtils.VIDEO_COLLECT, properties);
            }
            return null;
        }
    }
}
