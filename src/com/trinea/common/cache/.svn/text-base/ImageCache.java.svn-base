package com.trinea.common.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.trinea.java.common.StringUtils;
import com.trinea.java.common.entity.CacheObject;
import com.trinea.java.common.serviceImpl.RemoveTypeEnterTimeFirst;
import com.trinea.java.common.serviceImpl.SimpleCache;

/**
 * 图片缓存
 * <ul>
 * 使用方法如下
 * <li>调用{@link #ImageCache()}或是{@link #ImageCache(int)}新建缓存</li>
 * <li>实现{@link ImageCallback}再调用{@link #loadDrawable(String, int, View, ImageCallback)}获取图片，关于具体获取图片的方式可以参考
 * {@link #loadDrawable(String, int, View, ImageCallback)}的注释</li>
 * </ul>
 * <ul>
 * 
 * @author Trinea 2012-4-5 下午10:24:52
 */
public class ImageCache {

    /** 默认缓存大小 **/
    public static final int               MAX_CACHE_SIZE          = 50;

    /** image不在缓存中的message what **/
    private static final int              IMAGE_NOT_IN_CACHE_WHAT = 0;
    /** image获取成功的message what **/
    private static final int              IMAGE_LOADED_WHAT       = 1;

    /** 图片缓存 **/
    private SimpleCache<String, Drawable> imageCache;

    /**
     * 图片缓存，缓存大小为{@link #MAX_CACHE_SIZE}
     */
    public ImageCache(){
        imageCache = new SimpleCache<String, Drawable>(MAX_CACHE_SIZE, -1, new RemoveTypeEnterTimeFirst<Drawable>());
    }

    /**
     * 图片缓存，若maxCacheSize小于等于0，则采取默认值{@link #MAX_CACHE_SIZE}
     * 
     * @param maxCacheSize 缓存大小
     */
    public ImageCache(int maxCacheSize){
        imageCache = new SimpleCache<String, Drawable>(maxCacheSize <= 0 ? MAX_CACHE_SIZE : maxCacheSize, -1,
                                                       new RemoveTypeEnterTimeFirst<Drawable>());
    }

    /**
     * load图片
     * <ul>
     * <li>若imageUrl为空或imageCallback为空，返回null</li>
     * <li>若图片在缓存中, 执行{@link ImageCallback#imageLoaded(String, Drawable, View)}并返回图片</li>
     * <li>否则执行 {@link ImageCallback#imageNotInCache(int, View)}并开启线程获取图片(获取成功后执行
     * {@link ImageCallback#imageLoaded(String, Drawable, View)})并返回null</li>
     * </ul>
     * 
     * @param imageUrl 图片url
     * @param defaultResourceId 默认资源id，在{@link ImageCallback#imageNotInCache(int, View)}中可以使用
     * @param view 操作图片的view
     * @param imageCallback 图片callback，{@link ImageCallback}的实例
     * @return 若图片在缓存中返回图片，否则开启线程获取图片并返回null
     */
    public Drawable loadDrawable(final String imageUrl, int defaultResourceId, final View view,
                                 final ImageCallback imageCallback) {
        if (StringUtils.isEmpty(imageUrl) || imageCallback == null) {
            return null;
        }

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        final Handler handler = new Handler() {

            public void handleMessage(Message message) {
                switch (message.what) {
                    case IMAGE_NOT_IN_CACHE_WHAT:
                        imageCallback.imageNotInCache((Integer)message.obj, view);
                        break;
                    case IMAGE_LOADED_WHAT:
                        imageCallback.imageLoaded(imageUrl, (Drawable)message.obj, view);
                        break;
                }
            }
        };

        // 若图片在缓存中存在，则发送图片获取成功的message what，否则开启线程获取
        CacheObject<Drawable> object = imageCache.get(imageUrl);
        Drawable drawable = (object == null ? null : object.getData());
        if (drawable != null) {
            handler.sendMessage(handler.obtainMessage(IMAGE_LOADED_WHAT, drawable));
            return drawable;
        }

        // 若图片不在缓存中，发送不在缓存中的message what
        handler.sendMessage(handler.obtainMessage(IMAGE_NOT_IN_CACHE_WHAT, (Integer)defaultResourceId));
        new Thread("ImageCache loadDrawable whose imageUrl is " + imageUrl) {

            @Override
            public void run() {
                Drawable drawable = loadImageFromUrl(imageUrl);
                imageCache.put(imageUrl, drawable);
                handler.sendMessage(handler.obtainMessage(IMAGE_LOADED_WHAT, drawable));
            }
        }.start();
        return null;
    }

    /**
     * 根据imageUrl获得图片
     * 
     * @param imageUrl 图片url
     * @return
     */
    public static Drawable loadImageFromUrl(String imageUrl) {
        InputStream stream = null;
        try {
            URL url = new URL(imageUrl);
            stream = (InputStream)url.getContent();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Drawable.createFromStream(stream, "src");
    }

    /**
     * 图片获取的回调函数
     * <ul>
     * <li>可以实现{@link ImageCallback#imageNotInCache(int, View)}表示图片不在缓存中时的操作</li>
     * <li>可以实现{@link ImageCallback#imageLoaded(String, Drawable, View)}表示从缓存中或是另启线程获取到图片后的操作</li>
     * </ul>
     * 
     * @author Trinea 2012-4-5 下午10:31:59
     */
    public interface ImageCallback {

        /**
         * 图片不在缓存中的回调函数
         * 
         * @param resourceId 资源id
         * @param view 操作图片的view
         */
        public void imageNotInCache(int resourceId, View view);

        /**
         * 图片获取成功的回调函数
         * 
         * @param imageUrl 图片url
         * @param imageDrawable 图片
         * @param view 操作图片的view
         */
        public void imageLoaded(String imageUrl, Drawable imageDrawable, View view);
    }
}
