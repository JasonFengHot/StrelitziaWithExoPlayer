package tv.ismar.app.core.advertisement;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by huaijie on 7/31/15.
 */
public class AdvertisementDownload implements Runnable {
    private static final String TAG = "AdvertisementDownload";


    private Context context;
    private String downloadPath;
    private String downloadUrl;


    public AdvertisementDownload(Context context, String downloadUrl, String location) {
        this.context = context;
        this.downloadPath = location;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public void run() {
        try {
            Log.d(TAG, "AdvertisementDownload is running: " + downloadUrl);
            OkHttpClient client = new OkHttpClient();
            FileOutputStream fileOutputStream = context.openFileOutput(downloadPath, Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
            Response response;
            Request request = new Request.Builder().url(downloadUrl).build();
            response = client.newCall(request).execute();
            if (response.body() != null) {
                InputStream inputStream = response.body().byteStream();
                byte[] buffer = new byte[1024];
                int byteRead;
                while ((byteRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, byteRead);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
         //   new CallaPlay().bootAdvExcept(AdvertisementLogger.BOOT_ADV_DOWNLOAD_EXCEPTION_CODE, AdvertisementLogger.BOOT_ADV_DOWNLOAD_EXCEPTION_STRING);
            Log.e(TAG, "advertisement download exception!!!");
        }
    }
}
