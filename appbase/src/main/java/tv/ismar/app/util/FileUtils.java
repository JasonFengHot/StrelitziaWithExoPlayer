package tv.ismar.app.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by huaijie on 7/31/15.
 */
public class FileUtils {

    public static String getFileByUrl(String httpUrl) {
        try {
            URL url = new URL(httpUrl);
            String file = url.getFile();
            File localFile = new File(file);
            return localFile.getName();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }





    public static Drawable getImageFromAssetsFile(Context context, String fileName) {
        Drawable image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapDrawable.createFromStream(is, "post");
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

}
