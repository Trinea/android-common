package com.trinea.android.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.trinea.java.common.ArrayUtils;

/**
 * 图片工具类
 * <ul>
 * Bitmap、byte数组、drawable之间转换
 * </ul>
 * 
 * @author Trinea 2012-6-27 下午01:38:03
 */
public class ImageUtils {

    /**
     * Bitmap转换为byte数组
     * 
     * @param b
     * @return
     */
    public static byte[] bitmapToByte(Bitmap b) {
        if (b == null) {
            return null;
        }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    /**
     * byte数组转换为Bitmap
     * 
     * @param b
     * @return
     */
    public static Bitmap byteToBitmap(byte[] b) {
        return ArrayUtils.isEmpty(b) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    /**
     * Drawable转换为Bitmap
     * 
     * @param d
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable d) {
        return d == null ? null : ((BitmapDrawable)d).getBitmap();
    }

    /**
     * Bitmap转换为Drawable
     * 
     * @param b
     * @return
     */
    public static Drawable bitmapToDrawable(Bitmap b) {
        return b == null ? null : new BitmapDrawable(b);
    }

    /**
     * Drawable转换为byte数组
     * 
     * @param d
     * @return
     */
    public static byte[] drawableToByte(Drawable d) {
        return bitmapToByte(drawableToBitmap(d));
    }

    /**
     * byte数组转换为Drawable
     * 
     * @param b
     * @return
     */
    public static Drawable byteToDrawable(byte[] b) {
        return bitmapToDrawable(byteToBitmap(b));
    }

    /**
     * 根据imageUrl获得InputStream，需要自己手动关闭InputStream
     * 
     * @param imageUrl 图片url
     * @return
     */
    public static InputStream getInputStreamFromUrl(String imageUrl) {
        InputStream stream = null;
        try {
            URL url = new URL(imageUrl);
            stream = (InputStream)url.getContent();
        } catch (MalformedURLException e) {
            closeInputStream(stream);
            throw new RuntimeException("MalformedURLException occurred. ", e);
        } catch (IOException e) {
            closeInputStream(stream);
            throw new RuntimeException("IOException occurred. ", e);
        }
        return stream;
    }

    /**
     * 根据imageUrl获得Drawable
     * 
     * @param imageUrl 图片url
     * @return
     */
    public static Drawable getDrawableFromUrl(String imageUrl) {
        InputStream stream = getInputStreamFromUrl(imageUrl);
        Drawable d = Drawable.createFromStream(stream, "src");
        closeInputStream(stream);
        return d;
    }

    /**
     * 根据imageUrl获得Bitmap
     * 
     * @param imageUrl 图片url
     * @return
     */
    public static Bitmap getBitmapFromUrl(String imageUrl) {
        InputStream stream = getInputStreamFromUrl(imageUrl);
        Bitmap b = BitmapFactory.decodeStream(stream);
        closeInputStream(stream);
        return b;
    }

    /**
     * 关闭InputStream
     * 
     * @param s
     */
    private static void closeInputStream(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                throw new RuntimeException("IOException occurred. ", e);
            }
        }
    }
}
