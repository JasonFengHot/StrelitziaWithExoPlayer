package tv.ismar.helperpage.core;
import cn.ismartv.truetime.TrueTime;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import tv.ismar.helperpage.utils.DeviceUtils;
import cn.ismartv.injectdb.library.query.Select;
import cn.ismartv.truetime.TrueTime;
import tv.ismar.app.db.location.CdnTable;

/**
 * Created by huaijie on 12/26/14.
 */
public class HttpDownloadTask extends AsyncTask<List<Integer>, String, Long> {
    private static final String TAG = HttpDownloadTask.class.getSimpleName();

    /**
     * 常量声明
     */
    private static final int CONNECT_TIME_OUT = 5000;
    private static final String DEFAULT_DOWNLOAD_CACHE_NAME = "download.cache";

    /**
     *
     */
    private final File defaultCacheFile;
    private final Context mContext;

    private OnCompleteListener completeListener;

    public interface OnCompleteListener {
        /**
         * 单个节点测速完成
         */
        public void onSingleComplete(String cndId, String nodeName, String speed);

        /**
         * 所有节点测速完成
         */
        public void onAllComplete();

        public void onCancel();
    }


    public HttpDownloadTask(Context context) {
        this.mContext = context;
        defaultCacheFile = new File(DeviceUtils.getAppCacheDirectory(mContext), DEFAULT_DOWNLOAD_CACHE_NAME);
    }

    @Override
    protected Long doInBackground(List<Integer>... params) {
        List<Integer> cdnIDCollection = params[0];

        Log.d(TAG, "node cache size is ---> " + cdnIDCollection.size());
        for (Integer cdnID : cdnIDCollection) {

            /**
             * 获取数据库信息
             */
            CdnTable cacheTable = new Select().from(CdnTable.class).where("cdn_id=?", cdnID).executeSingle();


            /**
             * 测速下载
             */
            if (!isCancelled()) {
                try {
                    /**
                     * 开始测速时间
                     */
                    Timer timer = new Timer();
                    timer.start();
                    long startTime = TrueTime.now().getTime();

                    URL url = new URL("http://" + cacheTable.cdn_ip + "/cdn/speedtest.ts");
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(CONNECT_TIME_OUT);
                    /**
                     *获得输入流
                     */
                    InputStream inputStream = connection.getInputStream();

                    /**
                     * 创建文件输出流
                     */
                    FileOutputStream fileOutputStream = new FileOutputStream(defaultCacheFile);

                    byte[] buffer = new byte[100];
                    int byteRead = 0;
                    int byteSum = 0;
                    while ((byteRead = inputStream.read(buffer)) != -1 && timer.timer < Timer.TIME_OVER && !isCancelled()) {
                        byteSum += byteRead;
                        fileOutputStream.write(buffer, 0, byteRead);
                    }

                    /**
                     * 结束测速时间
                     */
                    long stopTime = TrueTime.now().getTime();
                    /**
                     * 计算测试速度
                     */
                    int speed = calculateSpeed(byteSum, startTime, stopTime);


                    /**
                     * 保存测速信息
                     */
                    cacheTable.speed = speed;
                    cacheTable.save();


                    publishProgress(String.valueOf(cacheTable.cdn_id), cacheTable.cdn_name, String.valueOf(speed));

                    Log.d(TAG, cacheTable.cdn_nick + " speed is ---> " + speed);

                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();

                } catch (MalformedURLException e) {
                    Log.e(TAG, "MalformedURLException ---> " + e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, "IOException ---> " + e.getMessage());
                }
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Long aLong) {
        /**
         * 所有节点测速完成
         */
        try {
            completeListener.onAllComplete();
        } catch (NullPointerException e) {
            Log.e(TAG, "Please set HttpDownload Listener!!!");
        }


    }

    @Override
    protected void onProgressUpdate(String... values) {
        /**
         * 单个节点测速完成回调
         */

        try {
            completeListener.onSingleComplete(values[0], values[1], values[2]);
        } catch (NullPointerException e) {
            Log.e(TAG, "Please set HttpDownload Listener!!!");
        }

    }

    @Override
    protected void onCancelled(Long aLong) {
        completeListener.onCancel();
    }

    @Override
    protected void onCancelled() {
        completeListener.onCancel();
    }

    /**
     * 计算测试速度
     *
     * @param dataByte
     * @param startTime
     * @param stopTime
     * @return 返回单位为 KB / S
     */
    private final int calculateSpeed(long dataByte, long startTime, long stopTime) {
        return (int) (((float) dataByte) / ((float) (stopTime - startTime)) * (1024f / 1000f));
    }


    public void setCompleteListener(OnCompleteListener listener) {
        this.completeListener = listener;
    }


}
