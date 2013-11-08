package cn.trinea.android.common.service;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import cn.trinea.android.common.dao.impl.HttpCacheDaoImpl;
import cn.trinea.android.common.entity.HttpRequest;
import cn.trinea.android.common.entity.HttpResponse;
import cn.trinea.android.common.service.impl.SimpleCache;
import cn.trinea.android.common.util.HttpUtils;
import cn.trinea.android.common.util.SqliteUtils;
import cn.trinea.android.common.util.StringUtils;

/**
 * HttpCache
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-1
 */
public class HttpCache extends SimpleCache<String, HttpResponse> {

    /** http memory cache **/
    private Map<String, HttpResponse> cache;

    public HttpCache(){
        cache = new HashMap<String, HttpResponse>();
    }

    /**
     * get httpResponse whose type is type into memory as primary cache to improve performanceo
     * 
     * @param context
     * @param type
     */
    public void initData(Context context, int type) {
        if (context == null) {
            throw new IllegalArgumentException("The context can not be null.");
        }
        this.cache = new HttpCacheDaoImpl(SqliteUtils.getInstance(context)).getHttpResponsesByType(type);
        if (cache == null) {
            cache = new HashMap<String, HttpResponse>();
        }
    }

    public HttpResponse httpGet(HttpRequest request) {
        String url;
        if (request == null || StringUtils.isEmpty(url = request.getUrl())) {
            return null;
        }

        HttpResponse cacheResponse = getFromCache(url);
        return cacheResponse == null ? HttpUtils.httpGet(request) : cacheResponse;
    }

    public HttpResponse httpGet(String httpUrl) {
        HttpResponse cacheResponse = getFromCache(httpUrl);
        return cacheResponse == null ? HttpUtils.httpGet(httpUrl) : cacheResponse;
    }

    public String httpGetString(String httpUrl) {
        HttpResponse cacheResponse = getFromCache(httpUrl);
        return cacheResponse == null ? HttpUtils.httpGetString(httpUrl) : cacheResponse.getResponseBody();
    }

    private HttpResponse getFromCache(String httpUrl) {
        HttpResponse cacheResponse = cache.get(httpUrl);
        if (cacheResponse == null) {
            // get from db
        }
        return (cacheResponse == null || cacheResponse.isExpired()) ? null : cacheResponse;
    }
}
