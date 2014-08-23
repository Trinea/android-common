package cn.trinea.android.common.dao.impl;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import cn.trinea.android.common.constant.DbConstants;
import cn.trinea.android.common.dao.HttpCacheDao;
import cn.trinea.android.common.entity.HttpResponse;
import cn.trinea.android.common.util.SqliteUtils;
import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.TimeUtils;

/**
 * HttpCacheDaoImpl
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-5
 */
public class HttpCacheDaoImpl implements HttpCacheDao {

    private SqliteUtils sqliteUtils;

    public HttpCacheDaoImpl(SqliteUtils sqliteUtils) {
        this.sqliteUtils = sqliteUtils;
    }

    @Override
    public long insertHttpResponse(HttpResponse httpResponse) {
        ContentValues contentValues = httpResponseToCV(httpResponse);
        if (contentValues == null) {
            return -1;
        }
        synchronized (HttpCacheDaoImpl.class) {
            return sqliteUtils.getDb().replace(DbConstants.HTTP_CACHE_TABLE_TABLE_NAME, null, contentValues);
        }
    }

    @Override
    public HttpResponse getHttpResponse(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        StringBuilder appWhere = new StringBuilder();
        appWhere.append(DbConstants.HTTP_CACHE_TABLE_URL).append("=?");
        String[] appWhereArgs = {url};
        synchronized (HttpCacheDaoImpl.class) {
            Cursor cursor = sqliteUtils.getDb().query(DbConstants.HTTP_CACHE_TABLE_TABLE_NAME, null,
                    appWhere.toString(), appWhereArgs, null, null, null);
            if (cursor == null) {
                return null;
            }

            HttpResponse httpResponse = null;
            if (cursor.moveToFirst()) {
                httpResponse = cursorToHttpResponse(cursor, url);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return httpResponse;
        }
    }

    @Override
    public Map<String, HttpResponse> getHttpResponsesByType(int type) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(DbConstants.HTTP_CACHE_TABLE_TYPE).append("=?");
        String[] whereClauseArgs = {Integer.toString(type)};

        synchronized (HttpCacheDaoImpl.class) {
            Cursor cursor = sqliteUtils.getDb().query(DbConstants.HTTP_CACHE_TABLE_TABLE_NAME, null,
                    whereClause.toString(), whereClauseArgs, null, null, null);

            if (cursor == null) {
                return null;
            }

            Map<String, HttpResponse> httpResponseMap = new HashMap<String, HttpResponse>();
            if (cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String url = cursor.getString(DbConstants.HTTP_CACHE_TABLE_URL_INDEX);
                    if (StringUtils.isEmpty(url)) {
                        continue;
                    }

                    HttpResponse httpResponse = cursorToHttpResponse(cursor, url);
                    if (httpResponse != null) {
                        httpResponseMap.put(url, httpResponse);
                    }
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return httpResponseMap;
        }
    }

    @Override
    public int deleteAllHttpResponse() {
        return sqliteUtils.getDb().delete(DbConstants.HTTP_CACHE_TABLE_TYPE, null, null);
    }

    /**
     * convert cursor to HttpResponse
     * 
     * @param cursor
     * @param url
     * @return
     */
    private HttpResponse cursorToHttpResponse(Cursor cursor, String url) {
        if (cursor == null) {
            return null;
        }
        if (url == null) {
            url = cursor.getString(DbConstants.HTTP_CACHE_TABLE_URL_INDEX);
        }
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        HttpResponse httpResponse = new HttpResponse(url);
        httpResponse.setResponseBody(cursor.getString(DbConstants.HTTP_CACHE_TABLE_RESPONSE_INDEX));
        httpResponse.setExpiredTime(cursor.getLong(DbConstants.HTTP_CACHE_TABLE_EXPIRES_INDEX));
        httpResponse.setType(cursor.getInt(DbConstants.HTTP_CACHE_TABLE_TYPE_INDEX));
        return httpResponse;
    }

    /**
     * convert HttpResponse to ContentValues
     * 
     * @param httpResponse
     * @return
     */
    private static ContentValues httpResponseToCV(HttpResponse httpResponse) {
        if (httpResponse == null || StringUtils.isEmpty(httpResponse.getUrl())) {
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(DbConstants.HTTP_CACHE_TABLE_URL, httpResponse.getUrl());
        values.put(DbConstants.HTTP_CACHE_TABLE_RESPONSE, httpResponse.getResponseBody());
        values.put(DbConstants.HTTP_CACHE_TABLE_EXPIRES, httpResponse.getExpiredTime());
        values.put(DbConstants.HTTP_CACHE_TABLE_CREATE_TIME, TimeUtils.getCurrentTimeInString());
        values.put(DbConstants.HTTP_CACHE_TABLE_TYPE, httpResponse.getType());
        return values;
    }
}
