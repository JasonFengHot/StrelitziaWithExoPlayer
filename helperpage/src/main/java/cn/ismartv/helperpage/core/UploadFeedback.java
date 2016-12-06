package cn.ismartv.helperpage.core;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import tv.ismar.app.core.VodUserAgent;
import tv.ismar.app.network.entity.FeedBackEntity;


/**
 * Created by huaijie on 2015/4/23.
 */
public class UploadFeedback implements Runnable {
    private static final String TAG = "UploadFeedback";

    private static final int DEFAULT_TIMEOUT = 10000;
    private static final int SUCCESS = 0;
    private static final int FAILURE = 1;

    private String localeName = Locale.getDefault().toString();
    private String api = "http://iris.tvxio.com/customer/pointlogs/";
    private Callback callback;

    private String snCode;
    private FeedBackEntity feedBackEntity;

    private MessageHandler messageHandler;

    private static UploadFeedback instance;

    private UploadFeedback() {
        messageHandler = new MessageHandler();
    }

    public static UploadFeedback getInstance() {
        if (instance == null) {
            instance = new UploadFeedback();
        }
        return instance;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(api);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("content-type", "text/json");
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setRequestProperty("User-Agent", VodUserAgent.getModelName() + "/" + android.os.Build.ID + " " + snCode);
            connection.setRequestProperty("Accept-Language", localeName);
            OutputStream outputStream = connection.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write("q=" + new Gson().toJson(feedBackEntity));
            writer.flush();
            writer.close();
            int statusCode = connection.getResponseCode();
            if (statusCode == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                if ("OK".equals(sb.toString())) {
                    messageHandler.sendEmptyMessage(SUCCESS);
                } else {
                    messageHandler.sendEmptyMessage(FAILURE);
                }
            } else {
                messageHandler.sendEmptyMessage(FAILURE);
            }
            connection.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }


    }

    public interface Callback {
        void success(String msg);

        void failure(String msg);
    }

    public void excute(FeedBackEntity feedBackEntity, String snCode, Callback callback) {
        this.callback = callback;
        this.snCode = snCode;
        this.feedBackEntity = feedBackEntity;
        new Thread(this).start();
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    callback.success("success");
                    break;
                case FAILURE:
                    callback.failure("failure");
                    break;
            }
        }
    }

}
