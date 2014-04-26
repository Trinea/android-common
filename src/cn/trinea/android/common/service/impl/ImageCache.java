package cn.trinea.android.common.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.service.CacheFullRemoveType;
import cn.trinea.android.common.service.FileNameRule;
import cn.trinea.android.common.service.impl.ImageMemoryCache.OnImageCallbackListener;
import cn.trinea.android.common.util.FileUtils;

/**
 * <strong>Image Cache</strong><br/>
 * <br/>
 * It's a cache with primary cache and secondary cache. It's a combination of {@link ImageMemoryCache} and
 * {@link ImageSDCardCache}. It applies to apps those used much images, like sina weibo, twitter, taobao, huaban, weixin
 * and so on.<br/>
 * <ul>
 * <strong>Setting and Usage</strong>
 * <li>Use one of constructors in sections II to init cache</li>
 * <li>{@link ImageMemoryCache#setOnImageCallbackListener(OnImageCallbackListener)} set callback interface after image
 * get success</li>
 * <li>{@link ImageMemoryCache#get(String, List, View)} get image asynchronous and preload other images asynchronous
 * according to urlList</li>
 * <li>{@link ImageMemoryCache#get(String, View)} get image asynchronous</li>
 * <li>{@link #initData(Context, String)} or {@link #loadDataFromDb(Context, String)} to init data when app start,
 * {@link #saveDataToDb(Context, String)} to save data when app exit</li>
 * <li>{@link #setHttpReadTimeOut(int)} set http read image time out, if less than 0, not set. default is not set</li>
 * <li>{@link PreloadDataCache#setContext(Context)} and {@link #setAllowedNetworkTypes(int)} restrict the types of
 * networks over which this data can get.</li>
 * <li>{@link ImageMemoryCache#setOpenWaitingQueue(boolean)} set whether open waiting queue, default is true. If true,
 * save all view waiting for image loaded, else only save the newest one</li>
 * <li>{@link PreloadDataCache#setOnGetDataListener(OnGetDataListener)} set how to get image, this cache will get image
 * and preload images by it</li>
 * <li>{@link SimpleCache#setCacheFullRemoveType(CacheFullRemoveType)} set remove type when primary cache is full</li>
 * <li>{@link #setCacheFullRemoveTypeOfSecondaryCache(CacheFullRemoveType)} set remove type when secondary cache is full
 * </li>
 * </ul>
 * <ul>
 * <strong>Constructor</strong>
 * <li>{@link #ImageCache()}</li>
 * <li>{@link #ImageCache(int)}</li>
 * <li>{@link #ImageCache(int, int)}</li>
 * <li>{@link #ImageCache(int, int, int, int)}</li>
 * </ul>
 * <ul>
 * <strong>Attentions</strong>
 * <li>You should add <strong>android.permission.WRITE_EXTERNAL_STORAGE</strong> in manifest, to store image to sdcard.</li>
 * <li>You should add <strong>android.permission.ACCESS_NETWORK_STATE</strong> in manifest if you get image from
 * network.</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-10-18
 */
public class ImageCache extends ImageMemoryCache {

    private static final long  serialVersionUID     = 1L;
    private ImageSDCardCache   secondaryCache;
    /** image compress size **/
    private int                compressSize         = 1;
    /** compress listener, advanced image compress, will override compressSize **/
    private CompressListener   compressListener;

    /** cache folder path which be used when saving images **/
    public static final String DEFAULT_CACHE_FOLDER = new StringBuilder()
                                                            .append(Environment.getExternalStorageDirectory()
                                                                    .getAbsolutePath()).append(File.separator)
                                                            .append("Trinea").append(File.separator)
                                                            .append("AndroidCommon").append(File.separator)
                                                            .append("ImageCache").toString();

    /**
     * <ul>
     * <li>max size of primary cache is {@link ImageMemoryCache#DEFAULT_MAX_SIZE}, max size of secondary cache is
     * {@link ImageSDCardCache#DEFAULT_MAX_SIZE}</li>
     * <li>thread pool size of primary cache and secondary cache both are
     * {@link PreloadDataCache#DEFAULT_THREAD_POOL_SIZE}</li>
     * </ul>
     * 
     * @see {@link #ImageCache(int, int, int, int)}
     */
    public ImageCache() {
        this(ImageMemoryCache.DEFAULT_MAX_SIZE, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE,
                ImageSDCardCache.DEFAULT_MAX_SIZE, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * <ul>
     * <li>max size of secondary cache is {@link ImageSDCardCache#DEFAULT_MAX_SIZE}</li>
     * <li>thread pool size of primary cache and secondary cache both are
     * {@link PreloadDataCache#DEFAULT_THREAD_POOL_SIZE}</li>
     * </ul>
     * 
     * @param primaryCacheMaxSize
     * @param secondaryCacheMaxSize
     * @see {@link #ImageCache(int, int, int, int)}
     */
    public ImageCache(int primaryCacheMaxSize) {
        this(primaryCacheMaxSize, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE, ImageSDCardCache.DEFAULT_MAX_SIZE,
                PreloadDataCache.DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * thread pool size of primary cache and secondary cache both are {@link PreloadDataCache#DEFAULT_THREAD_POOL_SIZE}
     * 
     * @param primaryCacheMaxSize
     * @param secondaryCacheMaxSize
     * @see {@link #ImageCache(int, int, int, int)}
     */
    public ImageCache(int primaryCacheMaxSize, int secondaryCacheMaxSize) {
        this(primaryCacheMaxSize, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE, secondaryCacheMaxSize,
                PreloadDataCache.DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * <ul>
     * <li>Callback interface after image get success is null, can set by
     * {@link PreloadDataCache#setOnImageCallbackListener(OnImageCallbackListener)}</li>
     * <li>Get data listener of primary cache is {@link #getOnGetImageListenerOfPrimaryCache()}, you can set by
     * {@link #setOnGetImageListenerOfPrimaryCache(OnGetDataListener)}, but not recommended, you may destory secondary
     * cache.</li>
     * <li>Get data listener of secondary cache is {@link #getOnGetImageListenerOfSecondaryCache()}, you can set by
     * {@link #setOnGetImageListenerOfSecondaryCache(OnGetDataListener)}.</li>
     * <li>Elements of the cache will not invalid</li>
     * <li>Remove type of primary cache is {@link RemoveTypeUsedCountSmall} when cache is full</li>
     * </ul>
     * 
     * @param primaryCacheMaxSize maximum size of the primary cache
     * @param primaryCacheThreadPoolSize getting data thread pool size of the primary cache
     * @param secondaryCacheMaxSize maximum size of the secondary cache
     * @param secondaryCacheThreadPoolSize getting data thread pool size of the secondary cache
     */
    public ImageCache(int primaryCacheMaxSize, int primaryCacheThreadPoolSize, int secondaryCacheMaxSize,
            int secondaryCacheThreadPoolSize) {
        super(primaryCacheMaxSize, primaryCacheThreadPoolSize);

        setOnGetDataListener(new OnGetDataListener<String, Bitmap>() {

            private static final long serialVersionUID = 1L;

            @Override
            public CacheObject<Bitmap> onGetData(String key) {
                try {
                    CacheObject<String> object = secondaryCache.get(key);
                    String imagePath = (object == null ? null : object.getData());
                    if (FileUtils.isFileExist(imagePath)) {
                        if (compressListener != null) {
                            compressSize = compressListener.getCompressSize(imagePath);
                        }
                        Bitmap bm;
                        if (compressSize > 1) {
                            BitmapFactory.Options option = new BitmapFactory.Options();
                            option.inSampleSize = compressSize;
                            bm = BitmapFactory.decodeFile(imagePath, option);
                        } else {
                            bm = BitmapFactory.decodeFile(imagePath);
                        }
                        return (bm == null ? null : new CacheObject<Bitmap>(bm));
                    } else {
                        secondaryCache.remove(key);
                    }
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        super.setCheckNetwork(false);
        setCacheFullRemoveType(new RemoveTypeUsedCountSmall<Bitmap>());

        secondaryCache = new ImageSDCardCache(secondaryCacheMaxSize, secondaryCacheThreadPoolSize);
        secondaryCache.setCacheFolder(DEFAULT_CACHE_FOLDER);
        secondaryCache.setFileNameRule(new FileNameRuleImageUrl().setFileExtension(""));
    }

    /**
     * get compressSize
     * 
     * @return the compressSize
     */
    public int getCompressSize() {
        return compressSize;
    }

    /**
     * set image compress scale
     * <ul>
     * <strong>Attentions:</strong>
     * <li>if {@link #setCompressListener(CompressListener)} is set, this function is not work</li>
     * </ul>
     * 
     * @param compressSize the compressSize to set
     * @see {@link #setCompressSize(String)}
     */
    public void setCompressSize(int compressSize) {
        this.compressSize = compressSize;
    }

    /**
     * set compressListener
     * <ul>
     * <strong>Attentions:</strong>
     * <li>if this function is set, the function {@link #setCompressSize(String)} is not work</li>
     * </ul>
     * 
     * @param compressListener
     */
    public void setCompressListener(CompressListener compressListener) {
        this.compressListener = compressListener;
    }

    /**
     * get compressListener
     * 
     * @return compressListener
     */
    public CompressListener getCompressListener() {
        return compressListener;
    }

    /**
     * set image compress scale
     */
    public interface CompressListener {

        /**
         * get image compress scale
         * <ul>
         * <strong>Attentions:</strong>
         * <li>if this function is set, the function {@link #setCompressSize(String)} is not work</li>
         * </ul>
         * 
         * @param imagePath
         * @return return compressSize, If > 1, requests the decoder to subsample the original image, returning a
         *         smaller image to save memory. The sample size is the number of pixels in either dimension that
         *         correspond to a single pixel in the decoded bitmap. For example, inSampleSize == 4 returns an image
         *         that is 1/4 the width/height of the original, and 1/16 the number of pixels. Any value <= 1 is
         *         treated the same as 1. Note: the decoder will try to fulfill this request, but the resulting bitmap
         *         may have different dimensions that precisely what has been requested. Also, powers of 2 are often
         *         faster/easier for the decoder to honor.
         */
        public int getCompressSize(String imagePath);
    }

    /**
     * get http read image time out of secondary cache, if less than 0, not set. default is not set
     * 
     * @return the httpReadTimeOut
     */
    @Override
    public int getHttpReadTimeOut() {
        return secondaryCache.getHttpReadTimeOut();
    }

    /**
     * set http read image time out of secondary cache, if less than 0, not set. default is not set, in mills
     * 
     * @param readTimeOutMillis
     */
    @Override
    public void setHttpReadTimeOut(int readTimeOutMillis) {
        secondaryCache.setHttpReadTimeOut(readTimeOutMillis);
    }

    /**
     * clear both primary cache and secondary cache
     */
    @Override
    public void clear() {
        super.clear();
        secondaryCache.clear();
    }

    @Override
    public void setForwardCacheNumber(int forwardCacheNumber) {
        super.setForwardCacheNumber(forwardCacheNumber);
        secondaryCache.setForwardCacheNumber(forwardCacheNumber);
    }

    @Override
    public void setBackwardCacheNumber(int backwardCacheNumber) {
        super.setForwardCacheNumber(backwardCacheNumber);
        secondaryCache.setForwardCacheNumber(backwardCacheNumber);
    }

    @Override
    public int getAllowedNetworkTypes() {
        return secondaryCache.getAllowedNetworkTypes();
    }

    @Override
    public void setAllowedNetworkTypes(int allowedNetworkTypes) {
        secondaryCache.setAllowedNetworkTypes(allowedNetworkTypes);
    }

    @Override
    public boolean isCheckNetwork() {
        return secondaryCache.isCheckNetwork();
    }

    @Override
    public void setCheckNetwork(boolean isCheckNetwork) {
        secondaryCache.setCheckNetwork(isCheckNetwork);
    }

    @Override
    public boolean checkIsNetworkTypeAllowed() {
        return secondaryCache.checkIsNetworkTypeAllowed();
    }

    @Override
    public Context getContext() {
        return secondaryCache.getContext();
    }

    @Override
    public void setContext(Context context) {
        secondaryCache.setContext(context);
    }

    /**
     * set http request properties
     * <ul>
     * <li>If image is from the different server, setRequestProperty("Connection", "false") is recommended. If image is
     * from the same server, true is recommended, and this is the default value</li>
     * </ul>
     * 
     * @param requestProperties
     */
    public void setRequestProperties(Map<String, String> requestProperties) {
        secondaryCache.setRequestProperties(requestProperties);
    }

    /**
     * get http request properties
     * 
     * @return
     */
    public Map<String, String> getRequestProperties() {
        return secondaryCache.getRequestProperties();
    }

    /**
     * Sets the value of the http request header field
     * 
     * @param field the request header field to be set
     * @param newValue the new value of the specified property
     * @see {@link #setRequestProperties(Map)}
     */
    public void setRequestProperty(String field, String newValue) {
        secondaryCache.setRequestProperty(field, newValue);
    }

    /**
     * get cache folder path which be used when saving images, default is {@link #DEFAULT_CACHE_FOLDER}
     * 
     * @return the cacheFolder
     * @see ImageSDCardCache#getCacheFolder()
     */
    public String getCacheFolder() {
        return secondaryCache.getCacheFolder();
    }

    /**
     * set cache folder path which be used when saving images, default is {@link #DEFAULT_CACHE_FOLDER}
     * 
     * @param cacheFolder
     * @see ImageSDCardCache#setCacheFolder(String)
     */
    public void setCacheFolder(String cacheFolder) {
        secondaryCache.setCacheFolder(cacheFolder);
    }

    /**
     * get file name rule which be used when saving images, default is {@link FileNameRuleImageUrl}
     * 
     * @return the fileNameRule
     * @see ImageSDCardCache#getFileNameRule()
     */
    public FileNameRule getFileNameRule() {
        return secondaryCache.getFileNameRule();
    }

    /**
     * set file name rule which be used when saving images, default is {@link FileNameRuleImageUrl}
     * 
     * @param fileNameRule
     * @see ImageSDCardCache#setFileNameRule(FileNameRule)
     */
    public void setFileNameRule(FileNameRule fileNameRule) {
        secondaryCache.setFileNameRule(fileNameRule);
    }

    /**
     * load all data from db and delete unused file in {@link #getCacheFolder()}
     * <ul>
     * <li>It's a combination of {@link #loadDataFromDb(Context, String)} and {@link #deleteUnusedFiles()}</li>
     * <li>You should use {@link #saveDataToDb(Context, String)} to save data when app exit</li>
     * </ul>
     * 
     * @param context
     * @param tag
     * @see #loadDataFromDb(Context, String)
     * @see #deleteUnusedFiles()
     */
    public void initData(Context context, String tag) {
        loadDataFromDb(context, tag);
        deleteUnusedFiles();
    }

    /**
     * delete unused file in {@link #getCacheFolder()}, you can use it after {@link #loadDataFromDb(Context, String)} at
     * first time
     * 
     * @see {@link ImageSDCardCache#deleteUnusedFiles()}
     */
    public void deleteUnusedFiles() {
        secondaryCache.deleteUnusedFiles();
    }

    /**
     * load all data in db whose tag is same to tag to this cache. just put, do not affect the original data
     * <ul>
     * <strong>Attentions:</strong>
     * <li>If tag is null or empty, throws exception</li>
     * <li>You should use {@link #saveDataToDb(Context, String)} to save data when app exit</li>
     * </ul>
     * 
     * @param context
     * @param tag tag used to mark this cache when save to and load from db, should be unique and cannot be null or
     *        empty
     * @return
     * @see ImageSDCardCache#loadDataFromDb(Context, ImageSDCardCache, String)
     */
    public boolean loadDataFromDb(Context context, String tag) {
        return ImageSDCardCache.loadDataFromDb(context, secondaryCache, tag);
    }

    /**
     * delete all rows in db whose tag is same to tag at first, and insert all data in this cache to db
     * <ul>
     * <strong>Attentions:</strong>
     * <li>If tag is null or empty, throws exception</li>
     * <li>Will delete all rows in db whose tag is same to tag at first</li>
     * <li>You can use {@link #initData(Context, String)} or {@link #loadDataFromDb(Context, String)} to init data when
     * app start</li>
     * </ul>
     * 
     * @param context
     * @param tag tag used to mark this cache when save to and load from db, should be unique and cannot be null or
     *        empty
     * @return
     * @see ImageSDCardCache#saveDataToDb(Context, ImageSDCardCache, String)
     */
    public boolean saveDataToDb(Context context, String tag) {
        return ImageSDCardCache.saveDataToDb(context, secondaryCache, tag);
    }

    /**
     * get image file path
     * 
     * @param imageUrl
     * @return if not in cache return null, else return full path.
     */
    public String getImagePath(String imageUrl) {
        return secondaryCache.getImagePath(imageUrl);
    }

    /**
     * @see ExecutorService#shutdown()
     */
    @Override
    protected void shutdown() {
        secondaryCache.shutdown();
        super.shutdown();
    }

    /**
     * @see ExecutorService#shutdownNow()
     */
    @Override
    public List<Runnable> shutdownNow() {
        secondaryCache.shutdownNow();
        return super.shutdownNow();
    }

    /**
     * get get image listener of primary cache
     * 
     * @return
     * @see {@link PreloadDataCache#getOnGetDataListener()}
     */
    public OnGetDataListener<String, Bitmap> getOnGetImageListenerOfPrimaryCache() {
        return getOnGetDataListener();
    }

    /**
     * set get data listener of primary cache, primary cache will get data and preload data by it
     * 
     * @param onGetImageListener
     * @see {@link PreloadDataCache#setOnGetDataListener(OnGetDataListener)}
     */
    public void setOnGetImageListenerOfPrimaryCache(OnGetDataListener<String, Bitmap> onGetImageListener) {
        this.onGetDataListener = onGetImageListener;
    }

    /**
     * get get image listener of secondary cache
     * 
     * @return
     */
    public OnGetDataListener<String, String> getOnGetImageListenerOfSecondaryCache() {
        return secondaryCache.getOnGetDataListener();
    }

    /**
     * set get data listener of secondary cache, secondary cache will get data and preload data by it
     * 
     * @param onGetImageListener
     */
    public void setOnGetImageListenerOfSecondaryCache(OnGetDataListener<String, String> onGetImageListener) {
        secondaryCache.setOnGetDataListener(onGetImageListener);
    }

    /**
     * get remove type when secondary cache is full
     * 
     * @return
     */
    public CacheFullRemoveType<String> getCacheFullRemoveTypeOfSecondaryCache() {
        return secondaryCache.getCacheFullRemoveType();
    }

    /**
     * set remove type when secondary cache is full
     * 
     * @param cacheFullRemoveType the cacheFullRemoveType to set
     */
    public void setCacheFullRemoveTypeOfSecondaryCache(CacheFullRemoveType<String> cacheFullRemoveType) {
        secondaryCache.setCacheFullRemoveType(cacheFullRemoveType);
    }
}
