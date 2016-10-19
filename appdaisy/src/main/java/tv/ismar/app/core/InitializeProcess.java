package tv.ismar.app.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import cn.ismartv.injectdb.library.ActiveAndroid;
import cn.ismartv.injectdb.library.query.Delete;
import cn.ismartv.injectdb.library.query.Select;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.app.R;
import tv.ismar.app.db.location.CdnTable;
import tv.ismar.app.db.location.CityTable;
import tv.ismar.app.db.location.DistrictTable;
import tv.ismar.app.db.location.IspTable;
import tv.ismar.app.db.location.ProvinceTable;
import tv.ismar.app.network.entity.CdnListEntity;
import tv.ismar.app.network.entity.IpLookUpEntity;
import tv.ismar.app.util.Utils;

public class InitializeProcess implements Runnable {
    private static final String TAG = "InitializeProcess";

    private static final int[] PROVINCE_STRING_ARRAY_RES = {
            R.array.china_north,
            R.array.china_east,
            R.array.china_south,
            R.array.china_center,
            R.array.china_southwest,
            R.array.china_northwest,
            R.array.china_northeast
    };

    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String PROVINCE_PY = "province_py";
    public static final String CITY_PY = "city_py";
    public static final String ISP = "isp";
    public static final String IP = "ip";
    public static final String GEO_ID = "geo_id";

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private OkHttpClient mOkHttpClient;
    private final String[] mDistrictArray;
    private final String[] mIspArray;

    Interceptor mHeaderInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request authorised = originalRequest.newBuilder()
                    .addHeader("Accept", "application/json")
                    .build();
            return chain.proceed(authorised);
        }
    };

    public InitializeProcess(Context context) {
        this.mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mDistrictArray = mContext.getResources().getStringArray(R.array.district);
        mIspArray = mContext.getResources().getStringArray(R.array.isp);
        mOkHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .addNetworkInterceptor(mHeaderInterceptor)
                .build();
    }

    @Override
    public void run() {
        initializeDistrict();
        initializeProvince();
        initalizeCity();
        initializeIsp();
        fetchCdnList();
        String city = mSharedPreferences.getString(CITY, "");
        if (Utils.isEmptyText(city)) {
            fetchLocationByIP();
        }

    }


    private void initializeDistrict() {
        if (new Select().from(DistrictTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {

                for (String district : mDistrictArray) {
                    DistrictTable districtTable = new DistrictTable();
                    districtTable.district_id = Utils.getMd5Code(district);
                    districtTable.district_name = district;
                    districtTable.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void initializeProvince() {
        if (new Select().from(ProvinceTable.class).executeSingle() == null) {

            ActiveAndroid.beginTransaction();
            try {
                for (int i = 0; i < mDistrictArray.length; i++) {
                    String[] provinceArray = mContext.getResources().getStringArray(PROVINCE_STRING_ARRAY_RES[i]);
                    for (String province : provinceArray) {
                        ProvinceTable provinceTable = new ProvinceTable();
                        String[] strs = province.split(",");
                        String provinceName = strs[0];
                        String provincePinYin = strs[1];

                        provinceTable.province_name = provinceName;
                        provinceTable.pinyin = provincePinYin;

                        provinceTable.province_id = Utils.getMd5Code(provinceName);
                        provinceTable.district_id = Utils.getMd5Code(mDistrictArray[i]);
                        provinceTable.save();
                    }
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void initalizeCity() {
        if (new Select().from(CityTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                InputStream inputStream = mContext.getResources().getAssets().open("location.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (null != s && !s.equals("")) {
                        String[] strings = s.split("\\,");
                        Long geoId = Long.parseLong(strings[0]);
                        String area = strings[1];
                        String city = strings[2];
                        String province = strings[3];
                        String provinceId = Utils.getMd5Code(province);

                        if (area.equals(city)) {
                            CityTable cityTable = new CityTable();
                            cityTable.geo_id = geoId;
                            cityTable.province_id = provinceId;
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


    private void initializeIsp() {
        if (new Select().from(IspTable.class).executeSingle() == null) {
            ActiveAndroid.beginTransaction();
            try {
                for (String isp : mIspArray) {
                    IspTable ispTable = new IspTable();
                    ispTable.isp_id = Utils.getMd5Code(isp);
                    ispTable.isp_name = isp;
                    ispTable.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }
    }

    private void fetchCdnList() {
        String resultString = null;
        Request request = new Request.Builder()
                .url("http://wx.api.tvxio.com/shipinkefu/getCdninfo?actiontype=getcdnlist")
                .build();
        Call call = mOkHttpClient.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                resultString = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Utils.isEmptyText(resultString)) {
            CdnListEntity cdnListEntity = new Gson().fromJson(resultString, CdnListEntity.class);
            initializeCdnTable(cdnListEntity);
        }
    }

    private void initializeCdnTable(CdnListEntity cdnListEntity) {
        new Delete().from(CdnTable.class).execute();
        ActiveAndroid.beginTransaction();
        try {
            for (CdnListEntity.CdnEntity cdnEntity : cdnListEntity.getCdn_list()) {
                CdnTable cdnTable = new CdnTable();
                cdnTable.cdn_id = cdnEntity.getCdnID();
                cdnTable.cdn_name = cdnEntity.getName();
                cdnTable.cdn_nick = cdnEntity.getNick();
                cdnTable.cdn_flag = cdnEntity.getFlag();
                cdnTable.cdn_ip = cdnEntity.getUrl();
                cdnTable.district_id = getDistrictId(cdnEntity.getNick());
                cdnTable.isp_id = getIspId(cdnEntity.getNick());
                cdnTable.route_trace = cdnEntity.getRoute_trace();
                cdnTable.speed = 0;
                cdnTable.checked = false;
                cdnTable.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    private void fetchLocationByIP() {
        String resultString = null;
        Request request = new Request.Builder()
                .url("http://lily.tvxio.com/iplookup/")
                .build();
        Call call = mOkHttpClient.newCall(request);
        try {
            Response response = call.execute();
            if (response.isSuccessful()) {
                resultString = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Utils.isEmptyText(resultString)) {
            IpLookUpEntity ipLookUpEntity = new Gson().fromJson(resultString, IpLookUpEntity.class);
            initializeLocation(ipLookUpEntity);
        }
    }

    private void initializeLocation(IpLookUpEntity ipLookUpEntity) {
        CityTable cityTable = new Select().from(CityTable.class).where(CityTable.CITY + " = ?", ipLookUpEntity.getCity() == null ? "" : ipLookUpEntity.getCity()).executeSingle();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PROVINCE, ipLookUpEntity.getProv());
        editor.putString(CITY, ipLookUpEntity.getCity());
        editor.putString(ISP, ipLookUpEntity.getIsp());
        editor.putString(IP, ipLookUpEntity.getIp());
        if (cityTable != null) {
            editor.putString(GEO_ID, String.valueOf(cityTable.geo_id));
        }
        ProvinceTable provinceTable = new Select().from(ProvinceTable.class)
                .where(ProvinceTable.PROVINCE_NAME + " = ?", ipLookUpEntity.getProv() == null ? "" : ipLookUpEntity.getProv()).executeSingle();
        if (provinceTable != null) {
            editor.putString(PROVINCE_PY, provinceTable.pinyin);
        }
        editor.apply();
    }

    private String getDistrictId(String cdnNick) {
        for (String district : mDistrictArray) {
            if (cdnNick.contains(district)) {
                return Utils.getMd5Code(district);
            }
        }
        // 第三方节点返回 "0"
        return "0";
    }

    private String getIspId(String cdnNick) {
        for (String isp : mIspArray) {
            if (cdnNick.contains(isp)) {
                return Utils.getMd5Code(isp);
            }
        }
        return Utils.getMd5Code(mIspArray[mIspArray.length - 1]);
    }
}
