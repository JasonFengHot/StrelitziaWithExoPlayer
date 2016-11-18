/*
* Copyright (C) 2015 Vincent Mi
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package tv.ismar.searchpage.weight;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ReflectionDrawable extends Drawable {

    private static final String TAG = "ReflectionDrawable";
    //    private final Bitmap mBitmap;
    private final Bitmap targetBitmap;

    private boolean isHorizontal;
    private final int targetBitmapWidth;
    private final int targetBitmapHeight;
    private final Paint mBitmapPaint;


    public ReflectionDrawable(Bitmap mBitmap, boolean isHorizontal) {
        int mBitmapWidth;
        int mBitmapHeight;
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        if (isHorizontal) {
            float bitmapWithReflectionHeight = mBitmapWidth * 355 / (float) 256;
            targetBitmap = Bitmap.createBitmap(mBitmapWidth, (int) bitmapWithReflectionHeight, Bitmap.Config.RGB_565);
            targetBitmapWidth = mBitmapWidth;
            targetBitmapHeight = targetBitmap.getHeight();

            LinearGradient shader = new LinearGradient(0, mBitmapHeight, 0, 2 * mBitmapHeight, 0x00000000, 0xff000000, Shader.TileMode.MIRROR);
            Matrix matrix = new Matrix();
            matrix.preScale(1, -1); // 实现图片的反转
            Bitmap reflectionImage = Bitmap.createBitmap(mBitmap, 0, 0, mBitmapWidth, mBitmapHeight, matrix, false);
            Bitmap bitmapWithReflection = Bitmap.createBitmap(mBitmapWidth, (int) bitmapWithReflectionHeight, Bitmap.Config.RGB_565); // 创建标准的Bitmap对象，宽和原图一致，高是原图的1.5倍
            Paint paint = new Paint();
            PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);


            Canvas canvas = new Canvas(targetBitmap);
            canvas.drawBitmap(mBitmap, 0, 0, null);
            canvas.drawBitmap(reflectionImage, 0, mBitmapHeight, null);
            paint.setShader(shader); // 绘制
            paint.setXfermode(porterDuffXfermode);
            canvas.drawRect(0, mBitmapHeight, mBitmapWidth, 2 * mBitmapHeight, paint);
            paint.reset();
            paint.setColor(Color.BLACK);
            canvas.drawRect(0, 2 * mBitmapHeight, mBitmapWidth, bitmapWithReflection.getHeight(), paint);
//            targetCanvas.drawBitmap(targetBitmap, 0, 0, mBitmapPaint);
        } else {
            targetBitmap = mBitmap;
            targetBitmapWidth = targetBitmap.getWidth();
            targetBitmapHeight = targetBitmap.getHeight();

        }

        mBitmapPaint = new Paint();
        mBitmapPaint.setStyle(Paint.Style.FILL);
        mBitmapPaint.setAntiAlias(true);
//
//    mBitmapWidth = bitmap.getWidth();
//    mBitmapHeight = bitmap.getHeight();
//    mBitmapRect.set(0, 0, mBitmapWidth, mBitmapHeight);
//
//    mBitmapPaint = new Paint();
//    mBitmapPaint.setStyle(Paint.Style.FILL);
//    mBitmapPaint.setAntiAlias(true);
//
//    mBorderPaint = new Paint();
//    mBorderPaint.setStyle(Paint.Style.STROKE);
//    mBorderPaint.setAntiAlias(true);
//    mBorderPaint.setColor(mBorderColor.getColorForState(getState(), DEFAULT_BORDER_COLOR));
//    mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    public static cn.ismartv.imagereflection.ReflectionDrawable fromBitmap(Bitmap bitmap, boolean isHorizontal) {
        if (bitmap != null) {
            return new cn.ismartv.imagereflection.ReflectionDrawable(bitmap, isHorizontal);
        } else {
            return null;
        }
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
//        int w = drawable.getIntrinsicWidth();
//        int h = drawable.getIntrinsicHeight();
//        System.out.println("Drawable转Bitmap");
//        Bitmap.Config config =
//                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//                        : Bitmap.Config.RGB_565;
//        Bitmap bitmap = Bitmap.createBitmap(200,300, config);
//        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, w, h);
//        drawable.draw(canvas);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap;
        int width = Math.max(drawable.getIntrinsicWidth(), 2);
        int height = Math.max(drawable.getIntrinsicHeight(), 2);
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Failed to create bitmap from drawable!");
            bitmap = null;
        }

        return bitmap;
    }

    @Override
    public int getIntrinsicWidth() {
        return targetBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return targetBitmapHeight;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(targetBitmap, 0, 0, mBitmapPaint);
//        if (isHorizontal) {
//            LinearGradient shader = new LinearGradient(0, mBitmapHeight, 0, 2 * mBitmapHeight, 0x00000000, 0xff000000, Shader.TileMode.MIRROR);
//            float bitmapWithReflectionHeight = mBitmapWidth * 355 / (float) 256;
//            Matrix matrix = new Matrix();
//            matrix.preScale(1, -1); // 实现图片的反转
//            Bitmap reflectionImage = Bitmap.createBitmap(mBitmap, 0, 0, mBitmapWidth, mBitmapHeight, matrix, false);
//            Bitmap bitmapWithReflection = Bitmap.createBitmap(mBitmapWidth, (int) bitmapWithReflectionHeight, Bitmap.Config.RGB_565); // 创建标准的Bitmap对象，宽和原图一致，高是原图的1.5倍
//            Paint paint = new Paint();
//            PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
////            Canvas canvas = new Canvas(targetBitmap);
//            canvas.drawBitmap(mBitmap, 0, 0, null);
//            canvas.drawBitmap(reflectionImage, 0, mBitmapHeight, null);
//            paint.setShader(shader); // 绘制
//            paint.setXfermode(porterDuffXfermode);
//            canvas.drawRect(0, mBitmapHeight, mBitmapWidth, 2 * mBitmapHeight, paint);
//            paint.reset();
//            paint.setColor(Color.BLACK);
//            canvas.drawRect(0, 2 * mBitmapHeight, mBitmapWidth, bitmapWithReflection.getHeight(), paint);
////            targetCanvas.drawBitmap(targetBitmap, 0, 0, mBitmapPaint);
//        } else {
//            targetBitmap = mBitmap;
////            targetCanvas.drawBitmap(targetBitmap, 0, 0, mBitmapPaint);
//        }
    }


    @Override
    public void setAlpha(int alpha) {
        mBitmapPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mBitmapPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }


    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    public Bitmap toBitmap() {
        return drawableToBitmap(this);
    }
}
