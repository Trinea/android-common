package cn.trinea.android.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.AsyncTask;
import cn.trinea.android.common.dao.HttpCacheDao;
import cn.trinea.android.common.dao.impl.HttpCacheDaoImpl;
import cn.trinea.android.common.entity.HttpRequest;
import cn.trinea.android.common.entity.HttpResponse;
import cn.trinea.android.common.service.impl.ImageCache;
import cn.trinea.android.common.util.ArrayUtils;
import cn.trinea.android.common.util.HttpUtils;
import cn.trinea.android.common.util.SqliteUtils;
import cn.trinea.android.common.util.StringUtils;

/**
 * <strong>Http Cache</strong><br/>
 * <br/>
 * It applies to get and cache api data from server, like json or xml and so on. It applies to apps like weixin, weibo,
 * twitter, taobao and so on. If want to cache image, please use {@link ImageCache}<br/>
 * <ul>
 * <strong>Constructor</strong>
 * <li>{@link #HttpCache(Context)} to init cache</li>
 * </ul>
 * <ul>
 * <strong>Get data asynchronous</strong>
 * <li>{@link #httpGet(HttpRequest, HttpCacheListener)}</li>
 * <li>{@link #httpGet(String, HttpCacheListener)}</li>
 * </ul>
 * <ul>
 * <strong>Get data synchronous</strong>
 * <li>{@link #httpGet(HttpRequest)}</li>
 * <li>{@link #httpGet(String)}</li>
 * <li>{@link #httpGetString(HttpRequest)}</li>
 * <li>{@link #httpGetString(String)}</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-1
 */
public class HttpCache {

    private Context                   context;

    /** http memory cache **/
    private Map<String, HttpResponse> cache;
    /** dao to get data from http db cache **/
    private HttpCacheDao              httpCacheDaoImpl;
    private int                       type = -1;

    public HttpCache(Context context){
        if (context == null) {
            throw new IllegalArgumentException("The context can not be null.");
        }
        this.context = context;
        cache = new ConcurrentHashMap<String, HttpResponse>();
        httpCacheDaoImpl = new HttpCacheDaoImpl(SqliteUtils.getInstance(context));
    }

    /**
     * waiting to be perfect^_^
     * 
     * @param context
     * @param type get httpResponse whose type is type into memory as primary cache to improve performance
     */
    private HttpCache(Context context, int type){
        this(context);
        this.type = type;
        initData(type);
    }

    /**
     * get httpResponse whose type is type into memory as primary cache to improve performance
     * 
     * @param type
     */
    private void initData(int type) {
        this.cache = httpCacheDaoImpl.getHttpResponsesByType(type);
        if (cache == null) {
            cache = new HashMap<String, HttpResponse>();
        }
    }

    /**
     * http get
     * <ul>
     * <strong>Attentions:</strong>
     * <li>Don't call this on the ui thread, it may costs some times. Becaust if not in cache, it get from network
     * synchronous.</li>
     * <li>If you want get data asynchronous, use {@link HttpCache#httpGet(HttpRequest, HttpCacheListener)}</li>
     * </ul>
     * 
     * @param httpRequest
     * @return the response of the url, if null represents http error
     */
    public HttpResponse httpGet(HttpRequest request) {
        String url;
        if (request == null || StringUtils.isEmpty(url = request.getUrl())) {
            return null;
        }

        HttpResponse cacheResponse = getFromCache(url);
        return cacheResponse == null ? putIntoCache(HttpUtils.httpGet(url)) : cacheResponse;
    }

    /**
     * http get
     * <ul>
     * <li>It gets data from cache or network asynchronous.</li>
     * <li>If you want get data synchronous, use {@link HttpCache#httpGet(HttpRequest)} or
     * {@link HttpCache#httpGetString(HttpRequest)}</li>
     * </ul>
     * 
     * @param httpUrl
     * @param listener listener which can do something before or after HttpGet. this can be null if you not want to do
     * something
     */
    public void httpGet(String httpUrl, HttpCacheListener listener) {
        new HttpCacheStringAsyncTask(listener).execute(httpUrl);
    }

    /**
     * http get
     * <ul>
     * <li>It gets data from cache or network asynchronous.</li>
     * <li>If you want get data synchronous, use {@link HttpCache#httpGet(HttpRequest)} or
     * {@link HttpCache#httpGetString(HttpRequest)}</li>
     * </ul>
     * 
     * @param request
     * @param listener listener which can do something before or after HttpGet. this can be null if you not want to do
     * something
     */
    public void httpGet(HttpRequest request, HttpCacheListener listener) {
        new HttpCacheRequestAsyncTask(listener).execute(request);
    }

    /**
     * http get
     * <ul>
     * <strong>Attentions:</strong>
     * <li>Don't call this on the ui thread, it may costs some times. Becaust if not in cache, it get from network
     * synchronous.</li>
     * <li>If you want get data asynchronous, use {@link HttpCache#httpGet(HttpRequest, HttpCacheListener)}</li>
     * </ul>
     * 
     * @param httpUrl
     * @return the response of the url, if null represents http error
     */
    public HttpResponse httpGet(String httpUrl) {
        return httpGet(new HttpRequest(httpUrl));
    }

    /**
     * http get
     * <ul>
     * <strong>Attentions:</strong>
     * <li>Don't call this on the ui thread, it may costs some times. Becaust if not in cache, it get from network
     * synchronous.</li>
     * <li>If you want get data asynchronous, use {@link HttpCache#httpGet(String, HttpCacheListener)}</li>
     * </ul>
     * 
     * @param httpUrl
     * @return the response body of the url, if null represents http error
     */
    public String httpGetString(String httpUrl) {
        HttpResponse cacheResponse = httpGet(new HttpRequest(httpUrl));
        return cacheResponse == null ? null : cacheResponse.getResponseBody();
    }

    /**
     * http get
     * <ul>
     * <strong>Attentions:</strong>
     * <li>Don't call this on the ui thread, it may costs some times. Becaust if not in cache, it get from network
     * synchronous.</li>
     * <li>If you want get data asynchronous, use {@link HttpCache#httpGet(HttpRequest, HttpCacheListener)}</li>
     * </ul>
     * 
     * @param httpRequest
     * @return the response body of the url, if null represents http error
     */
    public HttpResponse httpGetString(HttpRequest httpRequest) {
        return httpGet(httpRequest);
    }

    /**
     * HttpCacheListener, can do something before or after HttpGet
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-15
     */
    public abstract class HttpCacheListener {

        /**
         * Runs on the UI thread before httpGet.<br/>
         * <ul>
         * <li>this can be null if you not want to do something</li>
         * </ul>
         */
        protected void onPreExecute() {
        }

        /**
         * Runs on the UI thread after httpGet. The httpResponse is returned by httpGet.
         * <ul>
         * <li>this can be null if you not want to do something</li>
         * </ul>
         * 
         * @param httpResponse get by the url
         * @param isInCache the data responsed to the url whether is in cache
         */
        protected void onPostExecute(HttpResponse httpResponse, boolean isInCache) {
        }

    }

    /**
     * get type, waiting to be perfect^_^
     * 
     * @return the type
     */
    private int getType() {
        return type;
    }

    /**
     * put response into cache
     * <ul>
     * <li>put response to db, if {@link HttpResponse#getType()} == {@link HttpCache#getType()}, also put into memory
     * cache</li>
     * </ul>
     * 
     * @param httpResponse
     * @return if insert into db error, return null, otherwise return HttpResponse
     */
    private HttpResponse putIntoCache(HttpResponse httpResponse) {
        String url;
        if (httpResponse == null || (url = httpResponse.getUrl()) == null) {
            return null;
        }

        if (type != -1 && type == httpResponse.getType()) {
            cache.put(url, httpResponse);
        }
        return (httpCacheDaoImpl.insertHttpResponse(httpResponse) == -1) ? null : httpResponse;
    }

    /**
     * get from memory cache first, if not exist in memory cache, get from db
     * 
     * @param httpUrl
     * @return <ul>
     * <li>if neither exit in memory cache nor db, return null</li>
     * <li>if is expired, return null, otherwise return cache response</li>
     * </ul>
     */
    private HttpResponse getFromCache(String httpUrl) {
        if (StringUtils.isEmpty(httpUrl)) {
            return null;
        }

        HttpResponse cacheResponse = cache.get(httpUrl);
        if (cacheResponse == null) {
            cacheResponse = httpCacheDaoImpl.getHttpResponse(httpUrl);
        }
        return (cacheResponse == null || cacheResponse.isExpired()) ? null : cacheResponse.setInCache(true);
    }

    /**
     * AsyncTask to get data by String url
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-15
     */
    private class HttpCacheStringAsyncTask extends AsyncTask<String, Void, HttpResponse> {

        private HttpCacheListener listener;

        public HttpCacheStringAsyncTask(HttpCacheListener listener){
            this.listener = listener;
        }

        protected HttpResponse doInBackground(String... url) {
            if (ArrayUtils.isEmpty(url)) {
                return null;
            }
            return httpGet(url[0]);
        }

        protected void onPreExecute() {
            if (listener != null) {
                listener.onPreExecute();
            }
        }

        protected void onPostExecute(HttpResponse httpResponse) {
            if (listener != null) {
                listener.onPostExecute(httpResponse, httpResponse == null ? false : httpResponse.isInCache());
            }
        }
    }

    /**
     * AsyncTask to get data by HttpRequest
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-15
     */
    private class HttpCacheRequestAsyncTask extends AsyncTask<HttpRequest, Void, HttpResponse> {

        private HttpCacheListener listener;

        public HttpCacheRequestAsyncTask(HttpCacheListener listener){
            this.listener = listener;
        }

        protected HttpResponse doInBackground(HttpRequest... httpRequest) {
            if (ArrayUtils.isEmpty(httpRequest)) {
                return null;
            }
            return httpGet(httpRequest[0]);
        }

        protected void onPreExecute() {
            if (listener != null) {
                listener.onPreExecute();
            }
        }

        protected void onPostExecute(HttpResponse httpResponse) {
            if (listener != null) {
                listener.onPostExecute(httpResponse, httpResponse == null ? false : httpResponse.isInCache());
            }
        }
    }
}
