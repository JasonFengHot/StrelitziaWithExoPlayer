package tv.ismar.app.core.cache;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.db.DownloadTable;
import tv.ismar.app.util.FileUtils;
import tv.ismar.app.util.HardwareUtils;

/**
 * Created by huaijie on 6/19/15.
 */
public class DownloadClient implements Runnable {
    private static final String TAG = "LH/DownloadClient";

    private String urlStr;
    private File downloadFile;
    private String mServerMD5;
    private String mSaveName;

    private StoreType mStoreType;
    private Context mContext;


    public DownloadClient(Context context, String downloadUrl, String saveName, StoreType storeType) {
        mContext = context;
        urlStr = downloadUrl;
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
        OutputStream outputStream = null;
        switch (mStoreType) {
            case Internal:
                try {
                    outputStream = mContext.openFileOutput(mSaveName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
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
                    outputStream = new FileOutputStream(downloadFile, false);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                break;
        }
        Log.d(TAG, "DownloadUrl: " + urlStr);

        boolean isDownload = false;
        //database
        DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.DOWNLOAD_PATH + " =? ", downloadFile.getAbsolutePath()).executeSingle();
        InputStream input = null;
        try {
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .connectTimeout(6, TimeUnit.SECONDS)
//                    .readTimeout(15, TimeUnit.SECONDS)
//                    .build();
//            Request request = new Request.Builder().url(url).build();
//            Response response = client.newCall(request).execute();
//            long total = response.body().contentLength();
//            long current = 0;
//            if (response.body() != null) {
//                inputStream = response.body().byteStream();
//                byte[] buffer = new byte[1024];
//                int byteRead;
//                while ((byteRead = inputStream.read(buffer)) != -1) {
//                    Log.i(TAG, "byteRead:" + byteRead);
//                    current += byteRead;
//                    fileOutputStream.write(buffer, 0, byteRead);
//                }
//                isDownload = true;
//                fileOutputStream.flush();
//            }
            URL url = new URL(urlStr);
            URLConnection conexion = url.openConnection();
            conexion.connect();
            int lenghtOfFile = conexion.getContentLength();
            Log.d(TAG, "Lenght of file: " + lenghtOfFile);
            input = new BufferedInputStream(url.openStream());
            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                outputStream.write(data, 0, count);
            }
            outputStream.flush();
            isDownload = true;
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException: " + e.getMessage());
        } finally {
            try {
                if (outputStream != null){
                    outputStream.close();
                }
                if (input != null){
                    input.close();
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
        Log.d(TAG, "url is: " + urlStr + " mStoreType:" + mStoreType);
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
        return urlStr;
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
