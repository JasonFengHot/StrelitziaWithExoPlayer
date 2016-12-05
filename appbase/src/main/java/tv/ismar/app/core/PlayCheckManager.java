package tv.ismar.app.core;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tv.ismar.app.network.SkyService;

/**
 * Created by huibin on 7/4/16.
 */
public class PlayCheckManager {

    private static PlayCheckManager mInstance;
    private SkyService mSkyService;

    public static PlayCheckManager getInstance(SkyService skyService) {
        if (mInstance == null) {
            mInstance = new PlayCheckManager(skyService);
        }
        return mInstance;
    }

    private PlayCheckManager(SkyService skyService) {
        mSkyService = skyService;
    }

    public void check(String item, final Callback callback) {
        mSkyService.playCheck(
                item, null, null).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.errorBody() != null) {
                    callback.onFailure();
                } else {
                    try {
                        handlePlaycheck(response.body().string(), callback);
                    } catch (IOException e) {
                        callback.onFailure();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure();
            }
        });
    }

    public void checkPkg(String pkg, final Callback callback) {
        mSkyService.playCheck(
                null, pkg, null).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.errorBody() != null) {
                    callback.onFailure();
                } else {
                    try {
                        handlePlaycheck(response.body().string(), callback);
                    } catch (IOException e) {
                        callback.onFailure();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure();
            }
        });
    }


    private class PlayCheckEntity {
        private String expiry_date;
        private String iqiyi_code;
        private String user;

        public String getExpiry_date() {
            return expiry_date;
        }

        public void setExpiry_date(String expiry_date) {
            this.expiry_date = expiry_date;
        }

        public String getIqiyi_code() {
            return iqiyi_code;
        }

        public void setIqiyi_code(String iqiyi_code) {
            this.iqiyi_code = iqiyi_code;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }

    private void handlePlaycheck(String info, Callback callback) {
        boolean isBuy = false;
        int remainDay = 0;
        String user = "";
        switch (info) {
            case "0":
                break;
            default:
                PlayCheckEntity playCheckEntity = new Gson().fromJson(info, PlayCheckEntity.class);
                user = playCheckEntity.getUser();
                try {
                    remainDay = Util.daysBetween(Util.getTime(), playCheckEntity.getExpiry_date()) + 1;
                } catch (ParseException e) {
                    callback.onFailure();
                }
                if (remainDay == 0) {
                    isBuy = false;// 过期了。认为没购买
                } else
                    isBuy = true;// 购买了，剩余天数大于0
                break;
        }

        callback.onSuccess(isBuy, remainDay, user);
    }

    public interface Callback {
        void onSuccess(boolean isBuy, int remainDay, String user);

        void onFailure();
    }
}
