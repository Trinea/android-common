package cn.trinea.android.common.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import cn.trinea.android.common.dao.impl.ImageSDCardCacheDaoImpl;
import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.service.CacheFullRemoveType;
import cn.trinea.android.common.service.FileNameRule;
import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ImageUtils;
import cn.trinea.android.common.util.SizeUtils;
import cn.trinea.android.common.util.SqliteUtils;
import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.SystemUtils;

/**
 * <strong>Image SDCard Cache</strong><br/>
 * <br/>
 * It applies to images those uesd frequently and their size is big that we cannot store too much in memory, like
 * pictures of twitter or sina weibo. Cache of small images you can consider of {@link ImageMemoryCache}.<br/>
 * <ul>
 * <strong>Setting and Usage</strong>
 * <li>Use one of constructors in sections II to init cache</li>
 * <li>{@link #setOnImageSDCallbackListener(OnImageSDCallbackListener)} set callback interface after image get success</li>
 * <li>{@link #get(String, List, View)} get image asynchronous and preload other images asynchronous according to
 * urlList</li>
 * <li>{@link #get(String, View)} get image asynchronous</li>
 * <li>{@link #initData(Context, String)} or {@link #loadDataFromDb(Context, String)} to init data when app start,
 * {@link #saveDataToDb(Context, String)} to save data when app exit</li>
 * <li>{@link #setFileNameRule(FileNameRule)} set file name rule which be used when saving images, default is
 * {@link FileNameRuleImageUrl}</li>
 * <li>{@link #setCacheFolder(String)} set cache folder path which be used when saving images, default is
 * {@link #DEFAULT_CACHE_FOLDER}</li>
 * <li>{@link #setHttpReadTimeOut(int)} set http read image time out, if less than 0, not set. default is not set</li>
 * <li>{@link #setOpenWaitingQueue(boolean)} set whether open waiting queue, default is true. If true, save all view
 * waiting for image loaded, else only save the newest one</li>
 * <li>{@link PreloadDataCache#setOnGetDataListener(OnGetDataListener)} set how to get image, this cache will get image
 * and preload images by it</li>
 * <li>{@link SimpleCache#setCacheFullRemoveType(CacheFullRemoveType)} set remove type when cache is full</li>
 * <li>other see {@link PreloadDataCache} and {@link SimpleCache}</li>
 * </ul>
 * <ul>
 * <strong>Constructor</strong>
 * <li>{@link #ImageSDCardCache()}</li>
 * <li>{@link #ImageSDCardCache(int)}</li>
 * <li>{@link #ImageSDCardCache(int, int)}</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-4-5
 */
public class ImageSDCardCache extends PreloadDataCache<String, String> {

    private static final long                    serialVersionUID     = 1L;

    private static final String                  TAG                  = "ImageSDCardCache";

    /** callback interface after image get success **/
    private OnImageSDCallbackListener            onImageSDCallbackListener;
    /** cache folder path which be used when saving images, default is {@link #DEFAULT_CACHE_FOLDER} **/
    private String                               cacheFolder          = DEFAULT_CACHE_FOLDER;
    /** file name rule which be used when saving images, default is {@link FileNameRuleImageUrl} **/
    private FileNameRule                         fileNameRule         = new FileNameRuleImageUrl();
    /** http read image time out, if less than 0, not set. default is not set **/
    private int                                  httpReadTimeOut      = -1;
    /**
     * whether open waiting queue, default is true. If true, save all view waiting for image loaded, else only save the
     * newest one
     **/
    private boolean                              isOpenWaitingQueue   = true;

    /** recommend default max cache size according to dalvik max memory **/
    public static final int                      DEFAULT_MAX_SIZE     = getDefaultMaxSize();
    /** cache folder path which be used when saving images **/
    public static final String                   DEFAULT_CACHE_FOLDER = Environment.getExternalStorageDirectory()
                                                                                   .getAbsolutePath()
                                                                        + File.separator
                                                                        + "Trinea"
                                                                        + File.separator
                                                                        + "AndroidCommon"
                                                                        + File.separator + "ImageSDCardCache";

    /** image got success message what **/
    private static final int                     IMAGE_LOADED_WHAT    = 1;
    /** image reloaded success message what **/
    private static final int                     IMAGE_RELOADED_WHAT  = 2;

    /** thread pool whose wait for data got, attention, not the get data thread pool **/
    private transient ExecutorService            threadPool           = Executors.newFixedThreadPool(SystemUtils.DEFAULT_THREAD_POOL_SIZE);
    /**
     * key is image url, value is the newest view which waiting for image loaded, used when {@link #isOpenWaitingQueue}
     * is false
     **/
    private transient Map<String, View>          viewMap;
    /**
     * key is image url, value is view set those waiting for image loaded, used when {@link #isOpenWaitingQueue} is true
     **/
    private transient Map<String, HashSet<View>> viewSetMap;
    private transient Handler                    handler;

    /**
     * get image asynchronous. when get image success, it will pass to
     * {@link OnImageSDCallbackListener#onImageLoaded(String, String, View, boolean)}
     * 
     * @param imageUrl
     * @param view
     * @return whether image already in cache or not
     */
    public boolean get(String imageUrl, View view) {
        return get(imageUrl, null, view);
    }

    /**
     * get image asynchronous and preload other images asynchronous according to urlList
     * 
     * @param imageUrl
     * @param urlList url list, if is null, not preload, else preload forward by
     * {@link PreloadDataCache#preloadDataForward(Object, List, int)}, preload backward by
     * {@link PreloadDataCache#preloadDataBackward(Object, List, int)}
     * @param view
     * @return whether image already in cache or not
     */
    public boolean get(final String imageUrl, final List<String> urlList, final View view) {
        if (StringUtils.isEmpty(imageUrl)) {
            return false;
        }

        /**
         * if already in cache, call onImageSDCallbackListener, else new thread to wait for it
         */
        CacheObject<String> object = getFromCache(imageUrl, urlList);
        if (object != null) {
            String imagePath = object.getData();
            if (!StringUtils.isEmpty(imagePath) && FileUtils.isFileExist(imagePath)) {
                if (onImageSDCallbackListener != null) {
                    onImageSDCallbackListener.onImageLoaded(imageUrl, imagePath, view, true);
                }
                return true;
            } else {
                remove(imageUrl);
            }
        }

        if (isOpenWaitingQueue) {
            synchronized (viewSetMap) {
                HashSet<View> viewSet = viewSetMap.get(imageUrl);
                if (viewSet == null) {
                    viewSet = new HashSet<View>();
                    viewSetMap.put(imageUrl, viewSet);
                }
                viewSet.add(view);
            }
        } else {
            viewMap.put(imageUrl, view);
        }

        if (isExistGettingDataThread(imageUrl)) {
            return false;
        }

        startGetImageThread(IMAGE_LOADED_WHAT, imageUrl, urlList);
        return false;
    }

    /**
     * get cache folder path which be used when saving images, default is {@link #DEFAULT_CACHE_FOLDER}
     * 
     * @return the cacheFolder
     */
    public String getCacheFolder() {
        return cacheFolder;
    }

    /**
     * set cache folder path which be used when saving images, default is {@link #DEFAULT_CACHE_FOLDER}
     * 
     * @param cacheFolder
     */
    public void setCacheFolder(String cacheFolder) {
        if (StringUtils.isEmpty(cacheFolder)) {
            throw new IllegalArgumentException("The cacheFolder of cache can not be null.");
        }

        this.cacheFolder = cacheFolder;
    }

    /**
     * get file name rule which be used when saving images, default is {@link FileNameRuleImageUrl}
     * 
     * @return the fileNameRule
     */
    public FileNameRule getFileNameRule() {
        return fileNameRule;
    }

    /**
     * set file name rule which be used when saving images, default is {@link FileNameRuleImageUrl}
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
     * get callback interface after image get success
     * 
     * @return the onImageSDCallbackListener
     */
    public OnImageSDCallbackListener getOnImageSDCallbackListener() {
        return onImageSDCallbackListener;
    }

    /**
     * set callback interface after image get success
     * 
     * @param onImageSDCallbackListener the onImageSDCallbackListener to set
     */
    public void setOnImageSDCallbackListener(OnImageSDCallbackListener onImageSDCallbackListener) {
        this.onImageSDCallbackListener = onImageSDCallbackListener;
    }

    /**
     * get http read image time out, if less than 0, not set. default is not set
     * 
     * @return the httpReadTimeOut
     */
    public int getHttpReadTimeOut() {
        return httpReadTimeOut;
    }

    /**
     * set http read image time out, if less than 0, not set. default is not set, in mills
     * 
     * @param readTimeOutMillis
     */
    public void setHttpReadTimeOut(int readTimeOutMillis) {
        this.httpReadTimeOut = readTimeOutMillis;
    }

    /**
     * get whether open waiting queue, default is true. If true, save all view waiting for image loaded, else only save
     * the newest one
     * 
     * @return
     */
    public boolean isOpenWaitingQueue() {
        return isOpenWaitingQueue;
    }

    /**
     * set whether open waiting queue, default is true. If true, save all view waiting for image loaded, else only save
     * the newest one
     * 
     * @param isOpenWaitingQueue
     */
    public void setOpenWaitingQueue(boolean isOpenWaitingQueue) {
        this.isOpenWaitingQueue = isOpenWaitingQueue;
    }

    /**
     * <ul>
     * <li>Get data listener is {@link #getDefaultOnGetImageListener()}</li>
     * <li>Callback interface after image get success is null, can set by
     * {@link #setOnImageSDCallbackListener(OnImageSDCallbackListener)}</li>
     * <li>Maximum size of the cache is {@link #DEFAULT_MAX_SIZE}</li>
     * <li>Elements of the cache will not invalid</li>
     * <li>Remove type is {@link RemoveTypeUsedCountSmall} when cache is full</li>
     * </ul>
     * 
     * @see PreloadDataCache#PreloadDataCache()
     */
    public ImageSDCardCache(){
        this(DEFAULT_MAX_SIZE, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * <ul>
     * <li>Get data listener is {@link #getDefaultOnGetImageListener()}</li>
     * <li>Callback interface after image get success is null, can set by
     * {@link #setOnImageSDCallbackListener(OnImageSDCallbackListener)}</li>
     * <li>Elements of the cache will not invalid</li>
     * <li>Remove type is {@link RemoveTypeUsedCountSmall} when cache is full</li>
     * </ul>
     * 
     * @param maxSize maximum size of the cache
     * @see PreloadDataCache#PreloadDataCache(int)
     */
    public ImageSDCardCache(int maxSize){
        this(maxSize, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * <ul>
     * <li>Get data listener is {@link #getDefaultOnGetImageListener()}</li>
     * <li>Callback interface after image get success is null, can set by
     * {@link #setOnImageSDCallbackListener(OnImageSDCallbackListener)}</li>
     * <li>Elements of the cache will not invalid</li>
     * <li>Remove type is {@link RemoveTypeUsedCountSmall} when cache is full</li>
     * </ul>
     * 
     * @param maxSize maximum size of the cache
     * @param threadPoolSize getting data thread pool size
     * @see PreloadDataCache#PreloadDataCache(int, int)
     */
    public ImageSDCardCache(int maxSize, int threadPoolSize){
        super(maxSize, threadPoolSize);

        super.setOnGetDataListener(getDefaultOnGetImageListener());
        super.setCacheFullRemoveType(new RemoveTypeUsedCountSmall<String>());
        this.viewMap = new ConcurrentHashMap<String, View>();
        this.viewSetMap = new HashMap<String, HashSet<View>>();
        this.handler = new MyHandler();
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }

    /**
     * callback interface after image get success
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-4-5
     */
    public interface OnImageSDCallbackListener extends Serializable {

        /**
         * callback function after image get success, run on ui thread
         * 
         * @param imageUrl imageUrl
         * @param imagePath image path
         * @param view view need the image
         * @param isInCache whether already in cache or got realtime
         */
        public void onImageLoaded(String imageUrl, String imagePath, View view, boolean isInCache);
    }

    /**
     * @see ExecutorService#shutdown()
     */
    protected void shutdown() {
        threadPool.shutdown();
        super.shutdown();
    }

    /**
     * @see ExecutorService#shutdownNow()
     */
    public List<Runnable> shutdownNow() {
        threadPool.shutdownNow();
        return super.shutdownNow();
    }

    /**
     * My handler
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-11-20
     */
    private class MyHandler extends Handler {

        public void handleMessage(Message message) {
            switch (message.what) {
                case IMAGE_LOADED_WHAT:
                case IMAGE_RELOADED_WHAT:
                    MessageObject object = (MessageObject)message.obj;
                    if (object == null) {
                        break;
                    }

                    String imageUrl = object.imageUrl;
                    String imagePath = object.imagePath;
                    if (onImageSDCallbackListener != null) {
                        if (isOpenWaitingQueue) {
                            synchronized (viewSetMap) {
                                HashSet<View> viewSet = viewSetMap.get(imageUrl);
                                if (viewSet != null) {
                                    for (View view : viewSet) {
                                        if (view != null) {
                                            onImageSDCallbackListener.onImageLoaded(imageUrl, imagePath, view, false);
                                        }
                                    }
                                }
                            }
                        } else {
                            View view = viewMap.get(imageUrl);
                            if (view != null) {
                                onImageSDCallbackListener.onImageLoaded(imageUrl, imagePath, view, false);
                            }
                        }
                    }

                    if (isOpenWaitingQueue) {
                        synchronized (viewSetMap) {
                            viewSetMap.remove(imageUrl);
                        }
                    } else {
                        viewMap.remove(imageUrl);
                    }
                    break;
            }
        }
    }

    /**
     * message object
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-1-14
     */
    private class MessageObject {

        String imageUrl;
        String imagePath;

        public MessageObject(String imageUrl, String imagePath, List<String> urlList){
            this.imageUrl = imageUrl;
            this.imagePath = imagePath;
        }
    }

    /**
     * start thread to wait for image get
     * 
     * @param messsageWhat
     * @param imageUrl
     * @param urlList url list, if is null, not preload, else preload forward by
     * {@link PreloadDataCache#preloadDataForward(Object, List, int)}, preload backward by
     * {@link PreloadDataCache#preloadDataBackward(Object, List, int)}
     */
    private void startGetImageThread(final int messsageWhat, final String imageUrl, final List<String> urlList) {
        // wait for image be got success and send message
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                CacheObject<String> object = get(imageUrl, urlList);
                String imagePath = (object == null ? null : object.getData());
                // if image file not exist, remove it from cache and reload it
                if (StringUtils.isEmpty(imagePath) || !FileUtils.isFileExist(imagePath)) {
                    remove(imageUrl);
                    if (messsageWhat == IMAGE_LOADED_WHAT) {
                        startGetImageThread(IMAGE_RELOADED_WHAT, imageUrl, urlList);
                    }
                } else {
                    handler.sendMessage(handler.obtainMessage(messsageWhat, new MessageObject(imageUrl, imagePath,
                                                                                              urlList)));
                }
            }
        });
    }

    /**
     * delete file when full remove one
     */
    @Override
    protected CacheObject<String> fullRemoveOne() {
        CacheObject<String> o = super.fullRemoveOne();
        if (o != null) {
            deleteFile(o.getData());
        }
        return o;
    }

    /**
     * delete file when remove
     */
    @Override
    public CacheObject<String> remove(String key) {
        CacheObject<String> o = super.remove(key);
        if (o != null) {
            deleteFile(o.getData());
        }
        return o;
    }

    /**
     * delete file when clear cache
     */
    @Override
    public void clear() {
        for (CacheObject<String> value : values()) {
            if (value != null) {
                deleteFile(value.getData());
            }
        }
        super.clear();
    }

    /**
     * delete unused file in {@link #getCacheFolder()}, you can use it after {@link #loadDataFromDb(Context, String)} at
     * first time
     */
    public void deleteUnusedFiles() {
        int size = getSize();
        final HashSet<String> filePathSet = new HashSet<String>(size > 16 ? size : 16);
        for (CacheObject<String> value : values()) {
            if (value != null) {
                filePathSet.add(value.getData());
            }
        }

        threadPool.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    File file = new File(getCacheFolder());
                    if (file != null && file.exists() && file.isDirectory()) {
                        for (File f : file.listFiles()) {
                            if (f.isFile() && !filePathSet.contains(f.getPath())) {
                                f.delete();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "delete unused files fail.");
                }
            }
        });
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
        ImageSDCardCache.loadDataFromDb(context, this, tag);
        deleteUnusedFiles();
    }

    /**
     * load all data in db whose tag is same to tag to imageSDCardCache. just put, do not affect the original data
     * <ul>
     * <strong>Attentions:</strong>
     * <li>If tag is null or empty, throws exception</li>
     * <li>You should use {@link #saveDataToDb(Context, String)} to save data when app exit</li>
     * </ul>
     * 
     * @param context
     * @param tag tag used to mark this cache when save to and load from db, should be unique and cannot be null or
     * empty
     * @return
     * @see #loadDataFromDb(Context, ImageSDCardCache, String)
     */
    public boolean loadDataFromDb(Context context, String tag) {
        return ImageSDCardCache.loadDataFromDb(context, this, tag);
    }

    /**
     * delete all rows in db whose tag is same to tag at first, and insert all data in imageSDCardCache to db
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
     * empty
     * @return
     * @see #saveDataToDb(Context, ImageSDCardCache, String)
     */
    public boolean saveDataToDb(Context context, String tag) {
        return ImageSDCardCache.saveDataToDb(context, this, tag);
    }

    /**
     * load all data in db whose tag is same to tag to imageSDCardCache. just put, do not affect the original data
     * <ul>
     * <strong>Attentions:</strong>
     * <li>If imageSDCardCache is null, throws exception</li>
     * <li>If tag is null or empty, throws exception</li>
     * <li>You should use {@link #saveDataToDb(Context, ImageSDCardCache, String)} to save data when app exit</li>
     * </ul>
     * 
     * @param context
     * @param imageSDCardCache
     * @param tag tag used to mark this cache when save to and load from db, should be unique and cannot be null or
     * empty
     * @return
     */
    public static boolean loadDataFromDb(Context context, ImageSDCardCache imageSDCardCache, String tag) {
        if (context == null || imageSDCardCache == null) {
            throw new IllegalArgumentException("The context and cache both can not be null.");
        }
        if (StringUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("The tag can not be null or empty.");
        }
        return new ImageSDCardCacheDaoImpl(SqliteUtils.getInstance(context)).putIntoImageSDCardCache(imageSDCardCache,
                                                                                                     tag);
    }

    /**
     * delete all rows in db whose tag is same to tag at first, and insert all data in imageSDCardCache to db
     * <ul>
     * <strong>Attentions:</strong>
     * <li>If imageSDCardCache is null, throws exception</li>
     * <li>If tag is null or empty, throws exception</li>
     * <li>Will delete all rows in db whose tag is same to tag at first</li>
     * <li>You can use {@link #initData(Context, String)} or {@link #loadDataFromDb(Context, ImageSDCardCache, String)}
     * to init data when app start</li>
     * </ul>
     * 
     * @param context
     * @param imageSDCardCache
     * @param tag tag used to mark this cache when save to and load from db, should be unique and cannot be null or
     * empty
     * @return
     */
    public static boolean saveDataToDb(Context context, ImageSDCardCache imageSDCardCache, String tag) {
        if (context == null || imageSDCardCache == null) {
            throw new IllegalArgumentException("The context and cache both can not be null.");
        }
        if (StringUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("The tag can not be null or empty.");
        }
        return new ImageSDCardCacheDaoImpl(SqliteUtils.getInstance(context)).deleteAndInsertImageSDCardCache(imageSDCardCache,
                                                                                                             tag);
    }

    /**
     * delete file
     * 
     * @param path
     * @return
     */
    private boolean deleteFile(String path) {
        if (!StringUtils.isEmpty(path)) {
            if (!FileUtils.deleteFile(path)) {
                Log.e(TAG, new StringBuilder().append("delete file fail, path is ").append(path).toString());
                return false;
            }
        }
        return true;
    }

    /**
     * default get image listener
     * 
     * @return
     */
    public OnGetDataListener<String, String> getDefaultOnGetImageListener() {
        return new OnGetDataListener<String, String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public CacheObject<String> onGetData(String key) {

                String savePath = null;
                try {
                    InputStream stream = ImageUtils.getInputStreamFromUrl(key, httpReadTimeOut);
                    if (stream != null) {
                        savePath = cacheFolder + File.separator + fileNameRule.getFileName(key);
                        try {
                            FileUtils.writeFile(savePath, stream);
                        } catch (Exception e) {
                            if (e.getCause() instanceof FileNotFoundException) {
                                FileUtils.makeFolders(savePath);
                                FileUtils.writeFile(savePath, stream);
                            } else {
                                Log.e(TAG,
                                      new StringBuilder().append("get drawable exception while write to file, imageUrl is: ")
                                                         .append(key).append(", savePath is ").append(savePath)
                                                         .toString(), e);
                                savePath = null;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, new StringBuilder().append("get drawable exception, imageUrl is:").append(key)
                                                  .toString(), e);
                }

                return (StringUtils.isEmpty(savePath) ? null : new CacheObject<String>(savePath));
            }
        };
    }

    /**
     * get recommend default max cache size according to dalvik max memory
     * 
     * @return
     */
    static int getDefaultMaxSize() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory > SizeUtils.GB_2_BYTE) {
            return 256;
        }

        int mb = (int)(maxMemory / SizeUtils.MB_2_BYTE);
        return mb > 8 ? mb : 8;
    }
}
