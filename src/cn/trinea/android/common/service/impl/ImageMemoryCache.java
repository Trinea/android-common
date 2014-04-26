package cn.trinea.android.common.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.entity.FailedReason;
import cn.trinea.android.common.entity.FailedReason.FailedType;
import cn.trinea.android.common.service.CacheFullRemoveType;
import cn.trinea.android.common.util.ImageUtils;
import cn.trinea.android.common.util.SizeUtils;
import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.SystemUtils;

/**
 * <strong>Image Memory Cache</strong><br/>
 * <br/>
 * It applies to images those uesd frequently, like users avatar of twitter or sina weibo. Cache of big image you can
 * consider of {@link ImageSDCardCache}.<br/>
 * <ul>
 * <strong>Setting and Usage</strong>
 * <li>Use one of constructors in sections II to init cache</li>
 * <li>{@link #setOnImageCallbackListener(OnImageCallbackListener)} set callback interface when getting image</li>
 * <li>{@link #get(String, List, View)} get image asynchronous and preload other images asynchronous according to
 * urlList</li>
 * <li>{@link #get(String, View)} get image asynchronous</li>
 * <li>{@link #setHttpReadTimeOut(int)} set http read image time out, if less than 0, not set. default is not set</li>
 * <li>{@link PreloadDataCache#setContext(Context)} and {@link PreloadDataCache#setAllowedNetworkTypes(int)} restrict
 * the types of networks over which this data can get.</li>
 * <li>{@link #setOpenWaitingQueue(boolean)} set whether open waiting queue, default is true. If true, save all view
 * waiting for image loaded, else only save the newest one</li>
 * <li>{@link PreloadDataCache#setOnGetDataListener(OnGetDataListener)} set how to get image, this cache will get image
 * and preload images by it</li>
 * <li>{@link SimpleCache#setCacheFullRemoveType(CacheFullRemoveType)} set remove type when cache is full</li>
 * <li>other see {@link PreloadDataCache} and {@link SimpleCache}</li>
 * </ul>
 * <ul>
 * <strong>Constructor</strong>
 * <li>{@link #ImageMemoryCache()}</li>
 * <li>{@link #ImageMemoryCache(int)}</li>
 * <li>{@link #ImageMemoryCache(int, int)}</li>
 * </ul>
 * <ul>
 * <strong>Attentions</strong>
 * <li>You should add <strong>android.permission.ACCESS_NETWORK_STATE</strong> in manifest if you get image from
 * network.</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-4-5
 */
public class ImageMemoryCache extends PreloadDataCache<String, Bitmap> {

    private static final long                    serialVersionUID       = 1L;

    private static final String                  TAG                    = "ImageCache";

    /** callback interface when getting image **/
    private OnImageCallbackListener              onImageCallbackListener;
    /** http read image time out, if less than 0, not set. default is not set **/
    private int                                  httpReadTimeOut        = -1;
    /**
     * whether open waiting queue, default is true. If true, save all view waiting for image loaded, else only save the
     * newest one
     **/
    private boolean                              isOpenWaitingQueue     = true;
    /** http request properties **/
    private Map<String, String>                  requestProperties      = null;

    /** recommend default max cache size according to dalvik max memory **/
    public static final int                      DEFAULT_MAX_SIZE       = getDefaultMaxSize();
    /** message what for get image successfully **/
    private static final int                     WHAT_GET_IMAGE_SUCCESS = 1;
    /** message what for get image failed **/
    private static final int                     WHAT_GET_IMAGE_FAILED  = 2;

    /** thread pool whose wait for data got, attention, not the get data thread pool **/
    private transient ExecutorService            threadPool             = Executors
                                                                                .newFixedThreadPool(SystemUtils.DEFAULT_THREAD_POOL_SIZE);
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
     * {@link OnImageCallbackListener#onGetSuccess(String, Bitmap, View, boolean)}
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
     *        {@link PreloadDataCache#preloadDataForward(Object, List, int)}, preload backward by
     *        {@link PreloadDataCache#preloadDataBackward(Object, List, int)}
     * @param view
     * @return whether image already in cache or not
     */
    public boolean get(final String imageUrl, final List<String> urlList, final View view) {
        if (onImageCallbackListener != null) {
            onImageCallbackListener.onPreGet(imageUrl, view);
        }

        if (StringUtils.isEmpty(imageUrl)) {
            if (onImageCallbackListener != null) {
                onImageCallbackListener.onGetNotInCache(imageUrl, view);
            }
            return false;
        }

        /**
         * if already in cache, call onImageSDCallbackListener, else new thread to wait for it
         */
        CacheObject<Bitmap> object = getFromCache(imageUrl, urlList);
        if (object != null) {
            Bitmap bitmap = object.getData();
            if (bitmap != null) {
                onGetSuccess(imageUrl, bitmap, view, true);
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

        if (onImageCallbackListener != null) {
            onImageCallbackListener.onGetNotInCache(imageUrl, view);
        }
        if (isExistGettingDataThread(imageUrl)) {
            return false;
        }

        startGetImageThread(imageUrl, urlList);
        return false;
    }

    /**
     * get callback interface when getting image
     * 
     * @return the onImageCallbackListener
     */
    public OnImageCallbackListener getOnImageCallbackListener() {
        return onImageCallbackListener;
    }

    /**
     * set callback interface when getting image
     * 
     * @param onImageCallbackListener
     */
    public void setOnImageCallbackListener(OnImageCallbackListener onImageCallbackListener) {
        this.onImageCallbackListener = onImageCallbackListener;
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
     * set http request properties
     * <ul>
     * <li>If image is from the different server, setRequestProperty("Connection", "false") is recommended. If image is
     * from the same server, true is recommended, and this is the default value</li>
     * </ul>
     * 
     * @param requestProperties
     */
    public void setRequestProperties(Map<String, String> requestProperties) {
        this.requestProperties = requestProperties;
    }

    /**
     * get http request properties
     * 
     * @return
     */
    public Map<String, String> getRequestProperties() {
        return requestProperties;
    }

    /**
     * Sets the value of the http request header field
     * 
     * @param field the request header field to be set
     * @param newValue the new value of the specified property
     * @see {@link #setRequestProperties(Map)}
     */
    public void setRequestProperty(String field, String newValue) {
        if (StringUtils.isEmpty(field)) {
            return;
        }

        if (requestProperties == null) {
            requestProperties = new HashMap<String, String>();
        }
        requestProperties.put(field, newValue);
    }

    /**
     * <ul>
     * <li>Get data listener is {@link #getDefaultOnGetImageListener()}</li>
     * <li>callback interface when getting image is null, can set by
     * {@link #setOnImageCallbackListener(OnImageCallbackListener)}</li>
     * <li>Maximum size of the cache is {@link #DEFAULT_MAX_SIZE}</li>
     * <li>Elements of the cache will not invalid</li>
     * <li>Remove type is {@link RemoveTypeUsedCountSmall} when cache is full</li>
     * </ul>
     * 
     * @see PreloadDataCache#PreloadDataCache()
     */
    public ImageMemoryCache() {
        this(DEFAULT_MAX_SIZE, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * <ul>
     * <li>Get data listener is {@link #getDefaultOnGetImageListener()}</li>
     * <li>callback interface when getting image is null, can set by
     * {@link #setOnImageCallbackListener(OnImageCallbackListener)}</li>
     * <li>Elements of the cache will not invalid</li>
     * <li>Remove type is {@link RemoveTypeUsedCountSmall} when cache is full</li>
     * </ul>
     * 
     * @param maxSize maximum size of the cache
     * @see PreloadDataCache#PreloadDataCache(int)
     */
    public ImageMemoryCache(int maxSize) {
        this(maxSize, PreloadDataCache.DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * <ul>
     * <li>Get data listener is {@link #getDefaultOnGetImageListener()}</li>
     * <li>callback interface when getting image is null, can set by
     * {@link #setOnImageCallbackListener(OnImageCallbackListener)}</li>
     * <li>Elements of the cache will not invalid</li>
     * <li>Remove type is {@link RemoveTypeUsedCountSmall} when cache is full</li>
     * </ul>
     * 
     * @param maxSize maximum size of the cache
     * @param threadPoolSize getting data thread pool size
     * @see PreloadDataCache#PreloadDataCache(int, int)
     */
    public ImageMemoryCache(int maxSize, int threadPoolSize) {
        super(maxSize, threadPoolSize);

        super.setOnGetDataListener(getDefaultOnGetImageListener());
        super.setCacheFullRemoveType(new RemoveTypeUsedCountSmall<Bitmap>());
        this.viewMap = new ConcurrentHashMap<String, View>();
        this.viewSetMap = new HashMap<String, HashSet<View>>();
        this.handler = new MyHandler();
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
    }

    /**
     * callback interface when getting image
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-4-5
     */
    public interface OnImageCallbackListener {

        /**
         * callback function before get image, run on ui thread
         * 
         * @param imageUrl imageUrl
         * @param view view need the image
         */
        public void onPreGet(String imageUrl, View view);

        /**
         * callback function when get image but image not in cache, run on ui thread.<br/>
         * Will be called after {@link #onPreGet(String, View)}, before
         * {@link #onGetSuccess(String, String, View, boolean)} and
         * {@link #onGetFailed(String, String, View, FailedReason)}
         * 
         * @param imageUrl imageUrl
         * @param view view need the image
         */
        public void onGetNotInCache(String imageUrl, View view);

        /**
         * callback function after get image successfully, run on ui thread
         * 
         * @param imageUrl imageUrl
         * @param loadedImage loaded image bitmap
         * @param view view need the image
         * @param isInCache whether already in cache or got realtime
         */
        public void onGetSuccess(String imageUrl, Bitmap loadedImage, View view, boolean isInCache);

        /**
         * callback function after get image failed, run on ui thread
         * 
         * @param imageUrl imageUrl
         * @param loadedImage loaded image bitmap
         * @param view view need the image
         * @param failedReason failed reason for get image
         */
        public void onGetFailed(String imageUrl, Bitmap loadedImage, View view, FailedReason failedReason);
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
                case WHAT_GET_IMAGE_SUCCESS:
                case WHAT_GET_IMAGE_FAILED:
                    MessageObject object = (MessageObject)message.obj;
                    if (object == null) {
                        break;
                    }

                    String imageUrl = object.imageUrl;
                    Bitmap bitmap = object.bitmap;
                    if (onImageCallbackListener != null) {
                        if (isOpenWaitingQueue) {
                            synchronized (viewSetMap) {
                                HashSet<View> viewSet = viewSetMap.get(imageUrl);
                                if (viewSet != null) {
                                    for (View view : viewSet) {
                                        if (view != null) {
                                            if (WHAT_GET_IMAGE_SUCCESS == message.what) {
                                                onGetSuccess(imageUrl, bitmap, view, false);
                                            } else {
                                                onImageCallbackListener.onGetFailed(imageUrl, bitmap, view,
                                                        object.failedReason);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            View view = viewMap.get(imageUrl);
                            if (view != null) {
                                if (WHAT_GET_IMAGE_SUCCESS == message.what) {
                                    onGetSuccess(imageUrl, bitmap, view, false);
                                } else {
                                    onImageCallbackListener.onGetFailed(imageUrl, bitmap, view, object.failedReason);
                                }
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
    };

    private void onGetSuccess(String imageUrl, Bitmap loadedImage, View view, boolean isInCache) {
        if (onImageCallbackListener == null) {
            return;
        }

        try {
            onImageCallbackListener.onGetSuccess(imageUrl, loadedImage, view, isInCache);
        } catch (OutOfMemoryError e) {
            onImageCallbackListener.onGetFailed(imageUrl, loadedImage, view, new FailedReason(
                    FailedType.ERROR_OUT_OF_MEMORY, e));
        }
    }

    /**
     * message object
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-1-14
     */
    private class MessageObject {

        String       imageUrl;
        Bitmap       bitmap;
        FailedReason failedReason;

        public MessageObject(String imageUrl, Bitmap bitmap) {
            this.imageUrl = imageUrl;
            this.bitmap = bitmap;
        }

        public MessageObject(String imageUrl, Bitmap bitmap, FailedReason failedReason) {
            this.imageUrl = imageUrl;
            this.bitmap = bitmap;
            this.failedReason = failedReason;
        }

    }

    /**
     * start thread to wait for image get
     * 
     * @param imageUrl
     * @param urlList url list, if is null, not preload, else preload forward by
     *        {@link PreloadDataCache#preloadDataForward(Object, List, int)}, preload backward by
     *        {@link PreloadDataCache#preloadDataBackward(Object, List, int)}
     */
    private void startGetImageThread(final String imageUrl, final List<String> urlList) {
        // wait for image be got success and send message
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    CacheObject<Bitmap> object = get(imageUrl, urlList);
                    Bitmap bitmap = (object == null ? null : object.getData());
                    if (bitmap == null) {
                        // if bitmap is null, remove it
                        remove(imageUrl);
                        String failedException = "get image from network or save image to sdcard error. please make sure you have added permission android.permission.WRITE_EXTERNAL_STORAGE and android.permission.ACCESS_NETWORK_STATE";
                        FailedReason failedReason = new FailedReason(FailedType.ERROR_IO, failedException);
                        handler.sendMessage(handler.obtainMessage(WHAT_GET_IMAGE_FAILED, new MessageObject(imageUrl,
                                bitmap, failedReason)));
                    } else {
                        handler.sendMessage(handler.obtainMessage(WHAT_GET_IMAGE_SUCCESS, new MessageObject(imageUrl,
                                bitmap)));
                    }
                } catch (OutOfMemoryError e) {
                    MessageObject msg = new MessageObject(imageUrl, null, new FailedReason(
                            FailedType.ERROR_OUT_OF_MEMORY, e));
                    handler.sendMessage(handler.obtainMessage(WHAT_GET_IMAGE_FAILED, msg));
                }
            }
        });
    }

    /**
     * default get image from network listener
     * 
     * @return
     */
    public OnGetDataListener<String, Bitmap> getDefaultOnGetImageListener() {
        return new OnGetDataListener<String, Bitmap>() {

            private static final long serialVersionUID = 1L;

            @Override
            public CacheObject<Bitmap> onGetData(String key) {
                Bitmap d = null;
                try {
                    d = ImageUtils.getBitmapFromUrl(key, httpReadTimeOut, requestProperties);
                } catch (Exception e) {
                    Log.e(TAG, "get image exception, imageUrl is:" + key, e);
                }
                return (d == null ? null : new CacheObject<Bitmap>(d));
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
            return 512;
        }

        int mb = (int)(maxMemory / SizeUtils.MB_2_BYTE);
        return mb > 16 ? mb * 2 : 16;
    }
}
