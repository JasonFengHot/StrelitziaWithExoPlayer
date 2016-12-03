package tv.ismar.app.core.cache;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tv.ismar.app.db.DownloadTable;


/**
 * Created by huaijie on 6/23/15.
 */
public class DownloadThreadPool {
    private static final String TAG = "DownloadThreadPool";
    private static DownloadThreadPool instance;

    private ExecutorService executorService;

    private DownloadThreadPool() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    public static DownloadThreadPool getInstance() {
        if (instance == null) {
            instance = new DownloadThreadPool();
        }
        return instance;
    }

    public void add(DownloadClient client) {
        Log.i(TAG, "DownloadThreadPool add invoke...");
        //database
        DownloadTable downloadTable = new DownloadTable();
        downloadTable.file_name = client.getmSaveName();
        downloadTable.download_path = client.getDownloadFile().getAbsolutePath();
        downloadTable.url = client.getUrl();
        downloadTable.server_md5 = client.getmServerMD5();
        downloadTable.local_md5 = "";
        downloadTable.download_state = DownloadClient.DownloadState.run.name();
        downloadTable.save();

        executorService.execute(client);
    }
}
