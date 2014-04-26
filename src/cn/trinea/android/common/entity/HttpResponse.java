package cn.trinea.android.common.entity;

import java.util.HashMap;
import java.util.Map;

import cn.trinea.android.common.constant.HttpConstants;
import cn.trinea.android.common.service.HttpCache;
import cn.trinea.android.common.util.HttpUtils;
import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.TimeUtils;

/**
 * <strong>HttpResponse</strong><br/>
 * <ul>
 * <strong>Constructor</strong>
 * <li>{@link HttpResponse#HttpResponse()}</li>
 * <li>{@link HttpResponse#HttpResponse(String)}</li>
 * </ul>
 * <ul>
 * <strong>Get</strong>
 * <li>{@link #getResponseBody()}</li>
 * <li>{@link #getUrl()}</li>
 * <li>{@link #getExpiredTime()} expires time</li>
 * <li>{@link #getExpiresHeader()}</li>
 * </ul>
 * <ul>
 * <strong>Setting</strong>
 * <li>{@link #setUrl(String)}</li>
 * <li>{@link #setResponseBody(String)}</li>
 * <li>{@link #setResponseHeader(String, String)}</li>
 * <li>{@link #setResponseHeaders(Map)}</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-12
 */
public class HttpResponse {

    private String              url;
    /** http response content **/
    private String              responseBody;
    private Map<String, Object> responseHeaders;
    /** type to mark this response **/
    private int                 type;
    /** expired time in milliseconds **/
    private long                expiredTime;
    /** this is a client mark, whether this response is in client cache **/
    private boolean             isInCache;

    private boolean             isInitExpiredTime;
    /**
     * An <code>int</code> representing the three digit HTTP Status-Code.
     * <ul>
     * <li>1xx: Informational
     * <li>2xx: Success
     * <li>3xx: Redirection
     * <li>4xx: Client Error
     * <li>5xx: Server Error
     * </ul>
     */
    private int                 responseCode = -1;

    public HttpResponse(String url) {
        this.url = url;
        type = 0;
        isInCache = false;
        isInitExpiredTime = false;
        responseHeaders = new HashMap<String, Object>();
    }

    public HttpResponse() {
        responseHeaders = new HashMap<String, Object>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    /**
     * get reponse code
     * 
     * @return An <code>int</code> representing the three digit HTTP Status-Code.
     *         <ul>
     *         <li>1xx: Informational
     *         <li>2xx: Success
     *         <li>3xx: Redirection
     *         <li>4xx: Client Error
     *         <li>5xx: Server Error
     *         <li>-1: http error
     *         </ul>
     */
    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * not avaliable now
     * 
     * @return
     */
    private Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, Object> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * get type
     * <ul>
     * <li>type to mark this response, default is 0</li>
     * <li>it will be used in {@link HttpCache#HttpCache(android.content.Context, int)}</li>
     * </ul>
     * 
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * set type
     * <ul>
     * <li>type to mark this response, default is 0, cannot be smaller than 0.</li>
     * <li>it will be used in {@link HttpCache#HttpCache(android.content.Context, int)}</li>
     * </ul>
     * 
     * @param type the type to set
     */
    public void setType(int type) {
        if (type < 0) {
            throw new IllegalArgumentException("The type of HttpResponse cannot be smaller than 0.");
        }
        this.type = type;
    }

    /**
     * set expired time in millis
     * 
     * @param expiredTime
     */
    public void setExpiredTime(long expiredTime) {
        isInitExpiredTime = true;
        this.expiredTime = expiredTime;
    }

    /**
     * get expired time in millis
     * <ul>
     * <li>if current time is bigger than expired time, it means this response is dirty</li>
     * </ul>
     * 
     * @return <ul>
     *         <li>if max-age in cache-control is exists, return current time plus it</li>
     *         <li>else return expires</li>
     *         <li>if something error, return -1</li>
     *         </ul>
     */
    public long getExpiredTime() {
        if (isInitExpiredTime) {
            return expiredTime;
        } else {
            isInitExpiredTime = true;
            return expiredTime = getExpiresInMillis();
        }
    }

    /**
     * whether this response has expired
     * 
     * @return
     */
    public boolean isExpired() {
        return TimeUtils.getCurrentTimeInLong() > expiredTime;
    }

    /**
     * get isInCache, this is a client mark, whethero is in client cache
     * 
     * @return the isInCache
     */
    public boolean isInCache() {
        return isInCache;
    }

    /**
     * set isInCache, this is a client mark, whethero is in client cache
     * 
     * @param isInCache the isInCache to set
     * @return
     */
    public HttpResponse setInCache(boolean isInCache) {
        this.isInCache = isInCache;
        return this;
    }

    /**
     * http expires in reponse header
     * 
     * @return null represents http error or no expires in response headers
     */
    public String getExpiresHeader() {
        try {
            return responseHeaders == null ? null : (String)responseHeaders.get(HttpConstants.EXPIRES);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * http cache-control in reponse header
     * 
     * @return -1 represents http error or no cache-control in response headers, or max-age in seconds
     */
    private int getCacheControlMaxAge() {
        try {
            String cacheControl = (String)responseHeaders.get(HttpConstants.CACHE_CONTROL);
            if (!StringUtils.isEmpty(cacheControl)) {
                int start = cacheControl.indexOf("max-age=");
                if (start != -1) {
                    int end = cacheControl.indexOf(",", start);
                    String maxAge;
                    if (end != -1) {
                        maxAge = cacheControl.substring(start + "max-age=".length(), end);
                    } else {
                        maxAge = cacheControl.substring(start + "max-age=".length());
                    }
                    return Integer.parseInt(maxAge);
                }
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * get expires
     * 
     * @return <ul>
     *         <li>if max-age in cache-control is exists, return current time plus it</li>
     *         <li>else return expires</li>
     *         <li>if something error, return -1</li>
     *         </ul>
     */
    private long getExpiresInMillis() {
        int maxAge = getCacheControlMaxAge();
        if (maxAge != -1) {
            return System.currentTimeMillis() + maxAge * 1000;
        } else {
            String expire = getExpiresHeader();
            if (!StringUtils.isEmpty(expire)) {
                return HttpUtils.parseGmtTime(getExpiresHeader());
            }
        }
        return -1;
    }

    /**
     * set response header
     * 
     * @param field
     * @param newValue
     */
    public void setResponseHeader(String field, String newValue) {
        if (responseHeaders != null) {
            responseHeaders.put(field, newValue);
        }
    }

    /**
     * get response header, not avaliable now
     * 
     * @param field
     */
    private Object getResponseHeader(String field) {
        return responseHeaders == null ? null : responseHeaders.get(field);
    }
}
