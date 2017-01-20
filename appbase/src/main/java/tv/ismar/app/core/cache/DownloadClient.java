package tv.ismar.app.core.cache;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import cn.ismartv.injectdb.library.query.Select;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.ismar.app.db.DownloadTable;
import tv.ismar.app.util.FileUtils;
import tv.ismar.app.util.HardwareUtils;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable {
    private static final String TAG = "LH/DownloadClient";

    private String url;
    private File downloadFile;
    private String mServerMD5;
    private String mSaveName;

    private StoreType mStoreType;
    private Context mContext;


    public DownloadClient(Context context, String downloadUrl, String saveName, StoreType storeType) {
        mContext = context;
        url = downloadUrl;
        mServerMD5 = FileUtils.getFileByUrl(downloadUrl).split("\\.")[0];
        mStoreType = storeType;
        mSaveName = saveName;

        switch (mStoreType) {
            case Internal:
                downloadFile = mContext.getFileStreamPath(mSaveName);
                break;
            case External:
                downloadFile = new File(HardwareUtils.getSDCardCachePath(), mSaveName);
                break;
        }
    }


    @Override
    public void run() {
        FileOutputStream fileOutputStream = null;
        switch (mStoreType) {
            case Internal:
                try {
                    fileOutputStream = mContext.openFileOutput(mSaveName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                break;
            case External:
                try {
                    if (!downloadFile.exists()) {
                        downloadFile.getParentFile().mkdirs();
                        downloadFile.createNewFile();
                    }
                    fileOutputStream = new FileOutputStream(downloadFile, false);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                break;
        }
        Log.d(TAG, "DownloadUrl: " + url);

        boolean isDownload = false;
        //database
        DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.DOWNLOAD_PATH + " =? ", downloadFile.getAbsolutePath()).executeSingle();
        InputStream inputStream = null;
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(6, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            long total = response.body().contentLength();
            long current = 0;
            if (response.body() != null) {
                inputStream = response.body().byteStream();
                byte[] buffer = new byte[1024];
                int byteRead;
                while ((byteRead = inputStream.read(buffer)) != -1) {
                    Log.i(TAG, "byteRead:" + byteRead);
                    current += byteRead;
                    fileOutputStream.write(buffer, 0, byteRead);
                }
                isDownload = true;
                fileOutputStream.flush();
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException: " + e.getMessage());
        } finally {
            try {
                if (fileOutputStream != null){
                    fileOutputStream.close();
                }
                if (inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Close stream: " + e.getMessage());
            }
        }
        if(isDownload){
            downloadTable.download_path = downloadFile.getAbsolutePath();
            downloadTable.download_state = DownloadState.complete.name();
            downloadTable.local_md5 = HardwareUtils.getMd5ByFile(downloadFile);
            downloadTable.save();
        }
        Log.d(TAG, "url is: " + url + " mStoreType:" + mStoreType);
        Log.d(TAG, "server md5 is: " + mServerMD5);
        Log.d(TAG, "local md5 is: " + downloadTable.local_md5);
        Log.d(TAG, "download complete!!!");

    }

    public enum StoreType {
        Internal,
        External
    }

    public enum DownloadState {
        run,
        pause,
        complete
    }

    public String getUrl() {
        return url;
    }

    public File getDownloadFile() {
        return downloadFile;
    }

    public String getmServerMD5() {
        return mServerMD5;
    }

    public StoreType getmStoreType() {
        return mStoreType;
    }

    public String getmSaveName() {
        return mSaveName;
    }


}

//                    Request request = new Request.Builder()
//                            .url(url)
//                            .addHeader("RANGE", "bytes=" + localFile.length() + "-")
//                            .build();
//                    response = client.newCall(request).execute();
//                    fileOutputStream = new FileOutputStream(localFile, true);
//
