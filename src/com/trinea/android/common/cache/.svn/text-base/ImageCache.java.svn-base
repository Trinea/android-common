package com.trinea.android.common.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.trinea.android.common.serviceImpl.RemoveTypeDrawableLarge;
import com.trinea.android.common.serviceImpl.RemoveTypeDrawableSmall;
import com.trinea.android.common.util.ImageUtils;
import com.trinea.java.common.SerializeUtils;
import com.trinea.java.common.StringUtils;
import com.trinea.java.common.entity.CacheObject;
import com.trinea.java.common.service.Cache;
import com.trinea.java.common.service.CacheFullRemoveType;
import com.trinea.java.common.serviceImpl.AutoGetDataCache;
import com.trinea.java.common.serviceImpl.AutoGetDataCache.OnGetDataListener;
import com.trinea.java.common.serviceImpl.RemoveTypeUsedCountSmall;
import com.trinea.java.common.serviceImpl.SimpleCache;

/**
 * <strong>图片缓存</strong>，适用于图片较小，可直接在内存中缓存情况，图片较大情况可使用{@link ImageSDCardCache}。<br/>
 * <ul>
 * 缓存使用
 * <li>使用下面缓存初始化中介绍的几种构造函数之一初始化缓存</li>
 * <li>调用{@link #loadDrawable(String, List, View)}获取当前图片并获取缓存新图片，{@link #loadDrawable(String, View)}仅仅获取当前图片</li>
 * <li>使用{@link #saveCache(String, ImageCache)}保存缓存到文件</li>
 * <li>使用{@link #put(String, CacheObject)}或{@link #put(String, Drawable)}或{@link #putAll(ImageCache)}向缓存中添加元素</li>
 * </ul>
 * <ul>
 * 缓存初始化
 * <li>{@link #ImageCache(OnImageCallListener)}</li>
 * <li>{@link #ImageCache(OnImageCallListener, int)}</li>
 * <li>{@link #ImageCache(OnImageCallListener, int, long)}</li>
 * <li>{@link #ImageCache(OnImageCallListener, int, CacheFullRemoveType)}</li>
 * <li>{@link #ImageCache(OnImageCallListener, int, long, CacheFullRemoveType)}</li>
 * <li>{@link #loadCache(String)}从文件中恢复缓存</li>
 * </ul>
 * 
 * @author Trinea 2012-4-5 下午10:24:52
 */
public class ImageCache implements Serializable, Cache<String, Drawable> {

    private static final long                  serialVersionUID  = 1L;
    private static final String                TAG               = "ImageCache";

    /** 默认缓存大小 **/
    public static final int                    DEFAULT_MAX_SIZE  = 32;

    /** image获取成功的message what **/
    private static final int                   IMAGE_LOADED_WHAT = 1;

    /** 图片缓存 **/
    private AutoGetDataCache<String, Drawable> imageCache;

    /** 图片获取的回调接口 **/
    private OnImageCallListener                listener;

    /**
     * 初始化缓存
     * <ul>
     * <li>缓存最大容量为{@link #DEFAULT_MAX_SIZE}</li>
     * <li>元素不会失效</li>
     * <li>cache满时删除元素类型为{@link RemoveTypeUsedCountSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     */
    public ImageCache(OnImageCallListener listener){
        this(listener, DEFAULT_MAX_SIZE, -1, new RemoveTypeUsedCountSmall<Drawable>());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>元素不会失效</li>
     * <li>cache满时删除元素类型为{@link RemoveTypeUsedCountSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param maxSize 缓存最大容量
     */
    public ImageCache(OnImageCallListener listener, int maxSize){
        this(listener, maxSize, -1, new RemoveTypeUsedCountSmall<Drawable>());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>cache满时删除元素类型为{@link RemoveTypeUsedCountSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param maxSize 缓存最大容量
     * @param validTime 缓存中元素有效时间，小于等于0表示元素不会失效，失效规则见{@link SimpleCache#isExpired(CacheObject)}
     */
    public ImageCache(OnImageCallListener listener, int maxSize, long validTime){
        this(listener, maxSize, validTime, new RemoveTypeUsedCountSmall<Drawable>());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>元素不会失效</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param maxSize 缓存最大容量
     * @param cacheFullRemoveType cache满时删除元素类型，见{@link CacheFullRemoveType}
     */
    public ImageCache(OnImageCallListener listener, int maxSize, CacheFullRemoveType<Drawable> cacheFullRemoveType){
        this(listener, maxSize, -1, cacheFullRemoveType);
    }

    /**
     * 初始化缓存
     * 
     * @param listener 图片获取的回调接口
     * @param maxSize 缓存最大容量
     * @param validTime 缓存中元素有效时间，小于等于0表示元素不会失效，失效规则见{@link SimpleCache#isExpired(CacheObject)}
     * @param cacheFullRemoveType cache满时删除元素类型，见{@link CacheFullRemoveType}
     */
    public ImageCache(OnImageCallListener listener, int maxSize, long validTime,
                      CacheFullRemoveType<Drawable> cacheFullRemoveType){
        if (listener == null) {
            throw new IllegalArgumentException("The onImageCallListener of cache can not be null.");
        }

        this.imageCache = new AutoGetDataCache<String, Drawable>(getOnGetDataListener(), maxSize, validTime,
                                                                 cacheFullRemoveType);
        this.listener = listener;
    }

    /**
     * load图片，规则如下
     * <ul>
     * <li>若imageUrl和listener皆不为空，获取图片（不在缓存中则立即网络下载），执行
     * {@link OnImageCallListener#onImageLoaded(String, Drawable, View)})</li>
     * </ul>
     * 
     * @param imageUrl 图片url
     * @param view 操作图片的view
     * @return 图片是否在缓存中，true表示是
     */
    public boolean loadDrawable(final String imageUrl, final View view) {
        return loadDrawable(imageUrl, null, view);
    }

    /**
     * load图片，规则如下
     * <ul>
     * <li>若imageUrl和listener皆不为空，获取图片（不在缓存中则立即网络下载），执行
     * {@link OnImageCallListener#onImageLoaded(String, Drawable, View)})</li>
     * <li>按照该urlList中的url顺序获取新数据进行缓存</li>
     * </ul>
     * 
     * @param imageUrl 图片url
     * @param urlList 图片url list，按照该list中的url顺序获取新图片进行缓存，为空表示不进行缓存
     * @param view 操作图片的view
     * @return 图片是否在缓存中，true表示是
     */
    public boolean loadDrawable(final String imageUrl, final List<String> urlList, final View view) {
        if (StringUtils.isEmpty(imageUrl) || listener == null) {
            return false;
        }

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        final Handler handler = new Handler() {

            public void handleMessage(Message message) {
                switch (message.what) {
                    case IMAGE_LOADED_WHAT:
                        listener.onImageLoaded(imageUrl, (Drawable)message.obj, view);
                        break;
                }
            }
        };

        // 获取图片并发送图片获取成功的message what
        new Thread("ImageCache load drawable whose imageUrl is " + imageUrl) {

            @Override
            public void run() {
                CacheObject<Drawable> object = imageCache.get(imageUrl, urlList);
                Drawable drawable = (object == null ? null : object.getData());
                handler.sendMessage(handler.obtainMessage(IMAGE_LOADED_WHAT, drawable));
            }
        }.start();
        return imageCache.containsKey(imageUrl);
    }

    /**
     * 向缓存中添加元素, key和value均不允许为空
     * 
     * @param key key
     * @param value 元素
     * @return 为空表示缓存已满无法put，否则为put的value。
     */
    public CacheObject<Drawable> put(String key, CacheObject<Drawable> value) {
        return this.imageCache.put(key, value);
    }

    /**
     * 向缓存中添加元素, key不允许为空
     * <ul>
     * <li>见{@link #put(String, CacheObject)}</li>
     * </ul>
     * 
     * @param key key
     * @param value 元素值
     * @return 为空表示缓存已满无法put，否则为put的value。
     */
    public CacheObject<Drawable> put(String key, Drawable value) {
        return this.imageCache.put(key, value);
    }

    /**
     * 将cache2中的所有元素复制到当前cache，相当于将cache2中的每一个元素{@link #put(String, CacheObject)}到当前cache
     * 
     * @param cache2
     */
    public void putAll(ImageCache cache2) {
        this.imageCache.putAll(cache2.imageCache);
    }

    /**
     * 图片获取的回调接口
     * <ul>
     * <li>实现{@link OnImageCallListener#onImageLoaded(String, Drawable, View)}表示获取到图片后的操作</li>
     * </ul>
     * 
     * @author Trinea 2012-4-5 下午10:31:59
     */
    public interface OnImageCallListener extends Serializable {

        /**
         * 图片获取后的回调接口
         * 
         * @param imageUrl 图片url
         * @param imageDrawable 图片
         * @param view 操作图片的view
         */
        public void onImageLoaded(String imageUrl, Drawable imageDrawable, View view);
    }

    /**
     * 序列化写object
     * <ul>
     * 注意：
     * <li>若{@link SimpleCache#getCacheFullRemoveType()}是{@link RemoveTypeDrawableSmall}或{@link RemoveTypeDrawableLarge}
     * 的实例不会进行序列化，而是在反序列化时默认设置为{@link RemoveTypeDrawableSmall}的实例</li>
     * </ul>
     * 
     * @param out
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeObject(ObjectOutputStream out) {
        try {
            Class removeClass = imageCache.getCacheFullRemoveType().getClass();
            CacheFullRemoveType removeType = (CacheFullRemoveType)(removeClass.newInstance());
            if (removeType instanceof RemoveTypeDrawableSmall || removeType instanceof RemoveTypeDrawableLarge) {
                removeType = new RemoveTypeNull<Byte[]>();
            }
            AutoGetDataCache<String, byte[]> byteCache = new AutoGetDataCache<String, byte[]>(
                                                                                              null,
                                                                                              imageCache.getMaxSize(),
                                                                                              imageCache.getValidTime(),
                                                                                              removeType);
            Set<Map.Entry<String, CacheObject<Drawable>>> entrySet = imageCache.entrySet();
            for (Entry<String, CacheObject<Drawable>> entry : entrySet) {
                if (entry != null) {
                    CacheObject<Drawable> o1 = entry.getValue();
                    if (o1 != null) {
                        CacheObject<byte[]> o2 = new CacheObject<byte[]>();
                        o2.setEnterTime(o1.getEnterTime());
                        o2.setLastUsedTime(o1.getLastUsedTime());
                        o2.setUsedCount(o1.getUsedCount());
                        o2.setPriority(o1.getPriority());
                        o2.setExpired(o1.isExpired());
                        o2.setForever(o1.isForever());
                        o2.setData(ImageUtils.drawableToByte(o1.getData()));
                        byteCache.put(entry.getKey(), o2);
                    }
                }
            }
            PutField putFields = out.putFields();
            putFields.put("imageCache", byteCache);
            putFields.put("listener", listener);
            out.writeFields();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException occurred. ", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("InstantiationException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        }
    }

    /**
     * 序列化读取object
     * 
     * @param in
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void readObject(ObjectInputStream in) {
        try {
            GetField readFields = in.readFields();
            AutoGetDataCache<String, byte[]> byteCache = (AutoGetDataCache<String, byte[]>)readFields.get("imageCache",
                                                                                                          null);
            listener = (OnImageCallListener)readFields.get("listener", null);
            if (byteCache != null) {
                Class removeClass = byteCache.getCacheFullRemoveType().getClass();
                CacheFullRemoveType removeType = (CacheFullRemoveType)(removeClass.newInstance());
                if (removeType instanceof RemoveTypeNull) {
                    removeType = new RemoveTypeDrawableSmall();
                }
                imageCache = new AutoGetDataCache<String, Drawable>(getOnGetDataListener(), byteCache.getMaxSize(),
                                                                    byteCache.getValidTime(), removeType);
                Set<Map.Entry<String, CacheObject<byte[]>>> entrySet = byteCache.entrySet();
                for (Entry<String, CacheObject<byte[]>> entry : entrySet) {
                    if (entry != null) {
                        CacheObject<byte[]> o1 = entry.getValue();
                        if (o1 != null) {
                            CacheObject<Drawable> o2 = new CacheObject<Drawable>();
                            o2.setEnterTime(o1.getEnterTime());
                            o2.setLastUsedTime(o1.getLastUsedTime());
                            o2.setUsedCount(o1.getUsedCount());
                            o2.setPriority(o1.getPriority());
                            o2.setExpired(o1.isExpired());
                            o2.setForever(o1.isForever());
                            o2.setData(ImageUtils.byteToDrawable(o1.getData()));
                            imageCache.put(entry.getKey(), o2);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException occurred. ", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("InstantiationException occurred. ", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ClassNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        }
    }

    /**
     * 从文件中恢复缓存
     * 
     * @param filePath 文件路径
     * @return
     */
    public static ImageCache loadCache(String filePath) {
        return (ImageCache)SerializeUtils.deserialization(filePath);
    }

    /**
     * 保存缓存到文件
     * <ul>
     * 注意：
     * <li>若{@link SimpleCache#getCacheFullRemoveType()}是{@link RemoveTypeDrawableSmall}或{@link RemoveTypeDrawableLarge}
     * 的实例不会进行序列化，而是在反序列化（即{@link #loadCache(String)}）时默认设置为{@link RemoveTypeDrawableSmall}的实例</li>
     * </ul>
     * 
     * @param filePath 文件路径
     * @param cache 缓存
     */
    public static void saveCache(String filePath, ImageCache cache) {
        SerializeUtils.serialization(filePath, cache);
    }

    /**
     * 得到获取新数据的类
     * 
     * @return
     */
    private OnGetDataListener<String, Drawable> getOnGetDataListener() {
        return new OnGetDataListener<String, Drawable>() {

            private static final long serialVersionUID = 1L;

            @Override
            public CacheObject<Drawable> onGetData(String key) {
                Drawable d = null;
                try {
                    d = ImageUtils.getDrawableFromUrl(key);
                } catch (Exception e) {
                    Log.e(TAG, "根据imageUrl获得Drawable异常，imageUrl为：" + key, e);
                }
                return (d == null ? null : new CacheObject<Drawable>(d));
            }
        };
    }

    /**
     * 对于{@link RemoveTypeDrawableSmall}或{@link RemoveTypeDrawableLarge}无法序列化替换
     * 
     * @author Trinea 2012-7-10 下午06:32:33
     */
    public class RemoveTypeNull<T> implements CacheFullRemoveType<T> {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(CacheObject<T> obj1, CacheObject<T> obj2) {
            return 0;
        }

    }

    @Override
    public int getSize() {
        return imageCache.getSize();
    }

    @Override
    public CacheObject<Drawable> get(String key) {
        return imageCache.get(key);
    }

    @Override
    public void putAll(Cache<String, Drawable> cache2) {
        imageCache.putAll(cache2);
    }

    @Override
    public boolean containsKey(String key) {
        return imageCache.containsKey(key);
    }

    @Override
    public CacheObject<Drawable> remove(String key) {
        return imageCache.remove(key);
    }

    @Override
    public void clear() {
        imageCache.clear();
    }

    @Override
    public double getHitRate() {
        return imageCache.getHitRate();
    }

    @Override
    public Set<String> keySet() {
        return imageCache.keySet();
    }

    @Override
    public Set<Entry<String, CacheObject<Drawable>>> entrySet() {
        return imageCache.entrySet();
    }

    @Override
    public Collection<CacheObject<Drawable>> values() {
        return imageCache.values();
    }
}
