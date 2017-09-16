package tv.ismar.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/** Created by huaijie on 12/2/15. */
public class BitmapDecoder extends Thread {
    private static final int DECODE_SUCCESS = 0x0000;
    private Callback mCallback;
    private Context mContext;
    private int mResId;
    private MessageHandler messageHandler;

    public void decode(Context context, int resId, Callback callback) {
        mCallback = callback;
        mContext = context;
        mResId = resId;
        start();
    }

    @Override
    public void run() {
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ALPHA_8;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            InputStream is = mContext.getResources().openRawResource(mResId);
            byte[] ddd = doRead(is);
            Bitmap bm = BitmapFactory.decodeByteArray(ddd, 0, ddd.length, opt);
            BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), bm);
            messageHandler = new MessageHandler(this);
            Message message = new Message();
            message.obj = bd;
            message.what = DECODE_SUCCESS;
            messageHandler.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] doRead(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        byte[] buffer = new byte[is.available()];
        dis.readFully(buffer);
        dis.close();
        is.close();
        return buffer;
    }

    public void removeAllCallback() {
        if (messageHandler != null) {
            messageHandler.removeMessages(DECODE_SUCCESS);
        }
    }

    public interface Callback {
        void onSuccess(BitmapDrawable bitmapDrawable);
    }

    private static class MessageHandler extends Handler {
        WeakReference<BitmapDecoder> weakReference;

        private MessageHandler(BitmapDecoder bitmapDecoder) {
            super(Looper.getMainLooper());
            weakReference = new WeakReference<BitmapDecoder>(bitmapDecoder);
        }

        @Override
        public void handleMessage(Message msg) {
            BitmapDecoder decoder = weakReference.get();
            if (decoder != null) {
                switch (msg.what) {
                    case DECODE_SUCCESS:
                        BitmapDrawable drawable = (BitmapDrawable) msg.obj;
                        decoder.mCallback.onSuccess(drawable);
                        break;
                }
            }
        }
    }
}
