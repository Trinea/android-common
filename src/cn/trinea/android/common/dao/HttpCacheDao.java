package cn.trinea.android.common.dao;

import java.util.Map;

import cn.trinea.android.common.entity.HttpResponse;

/**
 * HttpCacheDao
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-04
 */
public interface HttpCacheDao {

    /**
     * insert HttpResponse
     * 
     * @param httpResponse
     * @return
     */
    public long insertHttpResponse(HttpResponse httpResponse);

    /**
     * get HttpResponse by url
     * 
     * @param url
     * @return
     */
    public HttpResponse getHttpResponse(String url);

    /**
     * get HttpResponses by type
     * 
     * @param type
     * @return
     */
    public Map<String, HttpResponse> getHttpResponsesByType(int type);
}
