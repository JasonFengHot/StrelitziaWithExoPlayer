package tv.ismar.app.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by mac on 16/3/22.
 */
public class AppConfigHelper {

    private static AppConfigHelper mInstance;
    private Properties sysProperties;
    private static Context mContext;
    private static String FILE_NAME = "configure/setup.properties";

    public static final String KEY_PLATFORM = "platform";
    public static final String KEY_API_VERSION = "api_version";

    private AppConfigHelper() throws IOException, IllegalAccessException {
        if (mContext == null) {
            throw new IllegalAccessException();
        }
        sysProperties = new Properties();
        InputStream is = mContext.getAssets().open(FILE_NAME);
        sysProperties.load(is);
    }

    public static void init(Context context) {
        mContext = context;
    }

    public static AppConfigHelper getInstance() throws IOException, IllegalAccessException {
        if (mInstance == null) {
            synchronized (AppConfigHelper.class) {
                if (mInstance == null) {
                    mInstance = new AppConfigHelper();
                }
            }
        }
        return mInstance;
    }

    public static String getPlatform() throws IOException, IllegalAccessException {
        return getInstance().sysProperties.getProperty(KEY_PLATFORM);
    }

    public static String getApiVersion() throws IOException, IllegalAccessException {
        return getInstance().sysProperties.getProperty(KEY_API_VERSION);
    }

}
