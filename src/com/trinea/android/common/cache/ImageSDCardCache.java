package com.trinea.android.common.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.trinea.android.common.service.FileNameRule;
import com.trinea.android.common.serviceImpl.FileNameRuleCurrentTime;
import com.trinea.android.common.serviceImpl.FileNameRuleCurrentTime.TimeRule;
import com.trinea.android.common.serviceImpl.RemoveTypeFileSmall;
import com.trinea.android.common.util.ImageUtils;
import com.trinea.java.common.FileUtils;
import com.trinea.java.common.SerializeUtils;
import com.trinea.java.common.StringUtils;
import com.trinea.java.common.entity.CacheObject;
import com.trinea.java.common.service.CacheFullRemoveType;
import com.trinea.java.common.serviceImpl.AutoGetDataCache;
import com.trinea.java.common.serviceImpl.AutoGetDataCache.OnGetDataListener;
import com.trinea.java.common.serviceImpl.RemoveTypeNotRemove;
import com.trinea.java.common.serviceImpl.SimpleCache;

/**
 * <strong>图片Sd卡缓存</strong>，适用于图片较大，防止在内存中缓存会占用太多内存情况，图片较小情况可使用{@link ImageCache}。<br/>
 * <ul>
 * 缓存使用
 * <li>使用下面缓存初始化中介绍的几种构造函数之一初始化缓存</li>
 * <li>调用{@link #loadImageFile(String, List, View)}获取当前图片并获取缓存新图片，{@link #loadImageFile(String, View)}仅仅获取当前图片</li>
 * <li>使用{@link #saveCache(String, ImageSDCardCache)}保存缓存到文件</li>
 * <li>使用{@link #put(String, CacheObject)}或{@link #put(String, String)}或{@link #putAll(ImageSDCardCache)}向缓存中添加元素</li>
 * <li>使用{@link #setFileNameRule(FileNameRule)}设置缓存图片保存的文件名规则， {@link #setCacheFolder(String)}设置缓存图片的保存目录</li>
 * </ul>
 * <ul>
 * 缓存初始化
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener)}</li>
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener, int)}</li>
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener, String)}</li>
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener, int, CacheFullRemoveType)}</li>
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener, String, int)}</li>
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener, String, int, CacheFullRemoveType)}</li>
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener, String, int, long)}</li>
 * <li>{@link #ImageSDCardCache(OnImageSDCallListener, String, int, long, CacheFullRemoveType)}</li>
 * <li>{@link #loadCache(String)}从文件中恢复缓存</li>
 * </ul>
 * 
 * @author Trinea 2012-4-5 下午10:24:52
 */
public class ImageSDCardCache implements Serializable {

    private static final long     serialVersionUID     = 1L;
    private static final String   TAG                  = "ImageSDCardCache";

    /** 默认缓存大小 **/
    public static final int       DEFAULT_MAX_SIZE     = 128;
    /** 缓存图片保存的默认目录 **/
    public static final String    DEFAULT_CACHE_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()
                                                         + File.separator + "Trinea" + File.separator + "AndroidCommon"
                                                         + File.separator + "ImageCache";
    /** 缓存图片的保存目录 **/
    private String                cacheFolder;
    /** 缓存图片保存的文件名规则 **/
    private FileNameRule          fileNameRule         = new FileNameRuleCurrentTime(TimeRule.TO_MILLIS);

    /** image获取成功的message what **/
    private static final int      IMAGE_LOADED_WHAT    = 1;

    /** 图片缓存 **/
    private FileSimpleCache       imageCache;

    /** 图片获取的回调接口 **/
    private OnImageSDCallListener listener;

    /**
     * 初始化缓存
     * <ul>
     * <li>缓存图片的保存目录为{@link #DEFAULT_CACHE_FOLDER}</li>
     * <li>缓存最大容量为{@link #DEFAULT_MAX_SIZE}</li>
     * <li>元素不会失效</li>
     * <li>cache满时删除元素类型为{@link RemoveTypeFileSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     */
    public ImageSDCardCache(OnImageSDCallListener listener){
        this(listener, DEFAULT_CACHE_FOLDER, DEFAULT_MAX_SIZE, -1, new RemoveTypeFileSmall());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>缓存图片的保存目录为{@link #DEFAULT_CACHE_FOLDER}</li>
     * <li>元素不会失效</li>
     * <li>cache满时删除元素类型为{@link RemoveTypeFileSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param maxSize 缓存最大容量
     */
    public ImageSDCardCache(OnImageSDCallListener listener, int maxSize){
        this(listener, DEFAULT_CACHE_FOLDER, maxSize, -1, new RemoveTypeFileSmall());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>缓存最大容量为{@link #DEFAULT_MAX_SIZE}</li>
     * <li>元素不会失效</li>
     * <li>cache满时删除元素类型为{@link RemoveTypeFileSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param cacheFolder 图片保存的目录
     */
    public ImageSDCardCache(OnImageSDCallListener listener, String cacheFolder){
        this(listener, cacheFolder, DEFAULT_MAX_SIZE, -1, new RemoveTypeFileSmall());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>元素不会失效</li>
     * <li>cache满时删除元素类型为{@link RemoveTypeFileSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param cacheFolder 图片保存的目录
     * @param maxSize 缓存最大容量
     */
    public ImageSDCardCache(OnImageSDCallListener listener, String cacheFolder, int maxSize){
        this(listener, cacheFolder, maxSize, -1, new RemoveTypeFileSmall());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>缓存图片的保存目录为{@link #DEFAULT_CACHE_FOLDER}</li>
     * <li>元素不会失效</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param maxSize 缓存最大容量
     * @param cacheFullRemoveType cache满时删除元素类型，见{@link CacheFullRemoveType}
     */
    public ImageSDCardCache(OnImageSDCallListener listener, int maxSize, CacheFullRemoveType<String> cacheFullRemoveType){
        this(listener, DEFAULT_CACHE_FOLDER, maxSize, -1, cacheFullRemoveType);
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>cache满时删除元素类型为{@link RemoveTypeFileSmall}</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param cacheFolder 图片保存的目录
     * @param maxSize 缓存最大容量
     * @param validTime 缓存中元素有效时间，小于等于0表示元素不会失效，失效规则见{@link SimpleCache#isExpired(CacheObject)}
     */
    public ImageSDCardCache(OnImageSDCallListener listener, String cacheFolder, int maxSize, long validTime){
        this(listener, cacheFolder, maxSize, validTime, new RemoveTypeFileSmall());
    }

    /**
     * 初始化缓存
     * <ul>
     * <li>元素不会失效</li>
     * </ul>
     * 
     * @param listener 图片获取的回调接口
     * @param cacheFolder 图片保存的目录
     * @param maxSize 缓存最大容量
     * @param cacheFullRemoveType cache满时删除元素类型，见{@link CacheFullRemoveType}
     */
    public ImageSDCardCache(OnImageSDCallListener listener, String cacheFolder, int maxSize,
                            CacheFullRemoveType<String> cacheFullRemoveType){
        this(listener, cacheFolder, maxSize, -1, cacheFullRemoveType);
    }

    /**
     * 初始化缓存
     * 
     * @param listener 图片获取的回调接口
     * @param cacheFolder 图片保存的目录
     * @param maxSize 缓存最大容量
     * @param validTime 缓存中元素有效时间，小于等于0表示元素不会失效，失效规则见{@link SimpleCache#isExpired(CacheObject)}
     * @param cacheFullRemoveType cache满时删除元素类型，见{@link CacheFullRemoveType}
     */
    public ImageSDCardCache(OnImageSDCallListener listener, String cacheFolder, int maxSize, long validTime,
                            CacheFullRemoveType<String> cacheFullRemoveType){
        if (listener == null) {
            throw new IllegalArgumentException("The onImageCallListener of cache can not be null.");
        }
        if (StringUtils.isEmpty(cacheFolder)) {
            throw new IllegalArgumentException("The cacheFolder of cache can not be null.");
        }

        this.listener = listener;
        this.cacheFolder = cacheFolder;
        this.imageCache = new FileSimpleCache(getOnGetDataListener(), maxSize, validTime, cacheFullRemoveType);
    }

    /**
     * load图片，规则如下
     * <ul>
     * <li>若imageUrl和listener皆不为空，获取图片并保存(保存成功后执行 {@link OnImageSDCallListener#onImageLoaded(String, String, View)})</li>
     * </ul>
     * 
     * @param imageUrl 图片url
     * @param view 操作图片的view
     */
    public void loadImageFile(final String imageUrl, final View view) {
        loadImageFile(imageUrl, null, view);
    }

    /**
     * load图片，规则如下
     * <ul>
     * <li>若imageUrl和listener皆不为空，获取图片并保存(保存成功后执行 {@link OnImageSDCallListener#onImageLoaded(String, String, View)})</li>
     * <li>按照该urlList中的url顺序获取新数据进行缓存</li>
     * </ul>
     * 
     * @param imageUrl 图片url
     * @param urlList 图片url list，按照该list中的url顺序获取新图片进行缓存，为空表示不进行缓存
     * @param view 操作图片的view
     */
    public void loadImageFile(final String imageUrl, final List<String> urlList, final View view) {
        if (StringUtils.isEmpty(imageUrl) || listener == null) {
            return;
        }

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        final Handler handler = new Handler() {

            public void handleMessage(Message message) {
                switch (message.what) {
                    case IMAGE_LOADED_WHAT:
                        listener.onImageLoaded(imageUrl, (String)message.obj, view);
                        break;
                }
            }
        };

        // 获取图片并发送图片获取成功的message what
        new Thread("ImageSDCardCache load image whose imageUrl is " + imageUrl) {

            @Override
            public void run() {
                CacheObject<String> object = imageCache.get(imageUrl, urlList);
                String savePath = (object == null ? null : object.getData());
                handler.sendMessage(handler.obtainMessage(IMAGE_LOADED_WHAT, savePath));
            }
        }.start();

        // // 若图片在缓存中存在，则发送图片获取成功的message what，否则开启线程获取
        // CacheObject<String> object = imageCache.get(imageUrl);
        // String imagePath = (object == null ? null : object.getData());
        // if (!StringUtils.isEmpty(imagePath)) {
        // handler.sendMessage(handler.obtainMessage(IMAGE_LOADED_WHAT, imagePath));
        // } else {
        // new Thread("ImageSDCardCache load image whose imageUrl is " + imageUrl) {
        //
        // @Override
        // public void run() {
        // String savePath = null;
        // try {
        // InputStream stream = ImageUtils.getInputStreamFromUrl(imageUrl);
        // savePath = cacheFolder + File.separator + fileNameRule.getFileName(imageUrl);
        // try {
        // FileUtils.writeFile(savePath, stream);
        // } catch (Exception e) {
        // if (e.getCause() instanceof FileNotFoundException) {
        // FileUtils.makeFolder(savePath);
        // FileUtils.writeFile(savePath, stream);
        // } else {
        // Log.e(TAG,
        // "根据imageUrl获得InputStream后写文件异常，imageUrl为：" + imageUrl + "。保存路径为：" + savePath, e);
        // }
        // }
        // imageCache.put(imageUrl, savePath);
        // } catch (Exception e) {
        // Log.e(TAG, "根据imageUrl获得InputStream异常，imageUrl为：" + imageUrl, e);
        // }
        // handler.sendMessage(handler.obtainMessage(IMAGE_LOADED_WHAT, savePath));
        // }
        // }.start();
        // }
    }

    /**
     * 向缓存中添加元素
     * <ul>
     * <li>若元素个数{@link SimpleCache#getSize()}小于最大容量，直接put进入，否则</li>
     * <li>若有效元素个数{@link SimpleCache#getValidSize()}小于元素个数{@link SimpleCache#getSize()}，去除无效元素
     * {@link SimpleCache#removeExpired()}后直接put进入，否则</li>
     * <li>若{@link #cacheFullRemoveType}是{@link RemoveTypeNotRemove}的实例，直接返回null，否则</li>
     * <li>按{@link #cacheFullRemoveType}删除元素后直接put进入</li>
     * </ul>
     * 
     * @param key key
     * @param obj 元素
     * @return
     */
    public void put(String key, CacheObject<String> obj) {
        this.imageCache.put(key, obj);
    }

    /**
     * 向缓存中添加元素
     * <ul>
     * <li>见{@link #put(String, CacheObject)}</li>
     * </ul>
     * 
     * @param key key
     * @param value 元素值
     * @return
     */
    public void put(String key, String value) {
        this.imageCache.put(key, value);
    }

    /**
     * 将cache2中的所有元素复制到当前cache，相当于将cache2中的每一个元素{@link #put(String, CacheObject)}到当前cache
     * 
     * @param cache2
     */
    public void putAll(ImageSDCardCache cache2) {
        this.imageCache.putAll(cache2.imageCache);
    }

    /**
     * 得到缓存图片的保存目录
     * 
     * @return the cacheFolder
     */
    public String getCacheFolder() {
        return cacheFolder;
    }

    /**
     * 设置缓存图片的保存目录
     * 
     * @param cacheFolder
     */
    public void setCacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
    }

    /**
     * 得到缓存图片保存的文件名规则
     * 
     * @return the fileNameRule
     */
    public FileNameRule getFileNameRule() {
        return fileNameRule;
    }

    /**
     * 设置缓存图片保存的文件名规则，使用{@link FileNameRule#getFileName(Object)}设置文件名
     * 
     * @param fileNameRule
     */
    public void setFileNameRule(FileNameRule fileNameRule) {
        if (fileNameRule == null) {
            throw new IllegalArgumentException("The fileNameRule of cache can not be null.");
        }
        this.fileNameRule = fileNameRule;
    }

    /**
     * 图片获取的回调接口
     * <ul>
     * <li>实现{@link OnImageSDCallListener#onImageLoaded(String, String, View)}表示获取到图片后的操作</li>
     * </ul>
     * 
     * @author Trinea 2012-4-5 下午10:31:59
     */
    public interface OnImageSDCallListener extends Serializable {

        /**
         * 图片获取后的回调接口
         * 
         * @param imageUrl 图片url
         * @param imagePath 图片sd卡路径
         * @param view 操作图片的view
         */
        public void onImageLoaded(String imageUrl, String imagePath, View view);
    }

    /**
     * 文件特殊缓存
     * <ul>
     * <li>删除缓存中文件路径同时，删除其路径对应的文件</li>
     * </ul>
     * 
     * @author Trinea 2012-6-30 下午09:42:00
     */
    public class FileSimpleCache extends AutoGetDataCache<String, String> {

        private static final long serialVersionUID = 1L;

        public FileSimpleCache(OnGetDataListener<String, String> onGetDataListener, int maxSize, long validTime,
                               CacheFullRemoveType<String> cacheFullRemoveType){
            super(onGetDataListener, maxSize, validTime, cacheFullRemoveType);
        }

        @Override
        protected CacheObject<String> fullRemoveOne() {
            CacheObject<String> o = super.fullRemoveOne();
            if (o != null) {
                deleteFile(o.getData());
            }
            return o;
        }

        @Override
        public CacheObject<String> remove(String key) {
            CacheObject<String> o = super.remove(key);
            if (o != null) {
                deleteFile(o.getData());
            }
            return o;
        }

        @Override
        public void clear() {
            for (Entry<String, CacheObject<String>> entry : entrySet()) {
                if (entry != null && entry.getValue() != null) {
                    deleteFile(entry.getValue().getData());
                }
            }
            cache.clear();
        }

        private boolean deleteFile(String path) {
            if (!StringUtils.isEmpty(path)) {
                if (!FileUtils.deleteFile(path)) {
                    Log.e(TAG, "删除文件失败，路径为：" + path);
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 从文件中恢复缓存
     * 
     * @param filePath 文件路径
     * @return
     */
    public static ImageSDCardCache loadCache(String filePath) {
        return (ImageSDCardCache)SerializeUtils.deserialization(filePath);
    }

    /**
     * 保存缓存到文件
     * 
     * @param filePath 文件路径
     * @param cache 缓存
     */
    public static void saveCache(String filePath, ImageSDCardCache cache) {
        SerializeUtils.serialization(filePath, cache);
    }

    /**
     * 得到获取新数据的类
     * 
     * @return
     */
    private OnGetDataListener<String, String> getOnGetDataListener() {
        return new OnGetDataListener<String, String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public CacheObject<String> onGetData(String key) {

                String savePath = null;
                try {
                    InputStream stream = ImageUtils.getInputStreamFromUrl(key);
                    savePath = cacheFolder + File.separator + fileNameRule.getFileName(key);
                    try {
                        FileUtils.writeFile(savePath, stream);
                    } catch (Exception e) {
                        if (e.getCause() instanceof FileNotFoundException) {
                            FileUtils.makeFolder(savePath);
                            FileUtils.writeFile(savePath, stream);
                        } else {
                            savePath = null;
                            Log.e(TAG, "根据imageUrl获得InputStream后写文件异常，imageUrl为：" + key + "。保存路径为：" + savePath, e);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "根据imageUrl获得InputStream异常，imageUrl为：" + key, e);
                }

                return (StringUtils.isEmpty(savePath) ? null : new CacheObject<String>(savePath));
            }
        };
    }
}
