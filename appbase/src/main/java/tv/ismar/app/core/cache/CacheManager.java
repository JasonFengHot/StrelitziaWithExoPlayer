package tv.ismar.app.core.cache;

import java.io.File;

import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.db.DownloadTable;
import tv.ismar.app.util.FileUtils;
import tv.ismar.app.util.HardwareUtils;

/**
 * Created by huaijie on 8/25/15.
 */
public class CacheManager {
    private static final String TAG = "CacheManager";

    private static CacheManager instance;
    private static String mSavePath;

    public static void initialize(String path) {
        mSavePath = path;
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            instance = new CacheManager();
        }
        return instance;
    }

    public String doRequest(String url, String saveName, DownloadClient.StoreType storeType) {

        File downloadFile = null;
        switch (storeType) {
            case Internal:
                downloadFile = new File(mSavePath, saveName);
                break;
            case External:
                downloadFile = new File(HardwareUtils.getSDCardCachePath(), saveName);
                break;
        }

        DownloadTable downloadTable = new Select().from(DownloadTable.class).where(DownloadTable.DOWNLOAD_PATH + " =? ", downloadFile.getAbsolutePath()).executeSingle();
        if (downloadTable == null) {
            DownloadThreadPool.getInstance().add(new DownloadClient(mSavePath, url, saveName, storeType));
            return url;
        } else {
            String serverMD5 = FileUtils.getFileByUrl(url).split("\\.")[0];
            String localMD5 = downloadTable.local_md5;
            if (serverMD5.equalsIgnoreCase(localMD5)) {
                File file = new File(downloadTable.download_path);
                if (file.exists()) {
                    return "file://" + downloadTable.download_path;
                } else {
                    downloadTable.delete();
                    DownloadThreadPool.getInstance().add(new DownloadClient(mSavePath, url, saveName, storeType));
                    return url;
                }

            } else {
                if (downloadTable.download_state.equals(DownloadClient.DownloadState.run.name())) {
                    //--------
                } else if (downloadTable.download_state.equals(DownloadClient.DownloadState.complete.name())) {
                    downloadTable.delete();
                    DownloadThreadPool.getInstance().add(new DownloadClient(mSavePath, url, saveName, storeType));
                }
                return url;
            }
        }
    }
}
