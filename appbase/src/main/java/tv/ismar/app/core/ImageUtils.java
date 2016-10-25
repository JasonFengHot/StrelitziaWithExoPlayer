package tv.ismar.app.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

public class ImageUtils {
	public static Bitmap getBitmapFromInputStream(InputStream in, int width, int height) {
		if(in!=null){
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.outWidth = width;
			options.outHeight = height;
            options.inDither = true;
            options.inScaled = true;
            options.inTargetDensity = 160;
            options.inDensity = 160;
			return BitmapFactory.decodeStream(in, null, options);
		} else {
			return null;
		}
	}


}
