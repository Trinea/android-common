package cn.trinea.android.common.constant;

/**
 * Some constants about db
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-10-21
 */
public class DbConstants {

    public static final String       DB_NAME                                       = "trinea_android_common.db";
    public static final int          DB_VERSION                                    = 1;

    private static final String      TERMINATOR                                    = ";";

    /** image sdcard cache table **/
    public static final StringBuffer CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL           = new StringBuffer();
    public static final StringBuffer CREATE_IMAGE_SDCARD_CACHE_TABLE_INDEX_SQL     = new StringBuffer();
    public static final String       IMAGE_SDCARD_CACHE_TABLE_TABLE_NAME           = "image_sdcard_cache";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_ID                   = android.provider.BaseColumns._ID;
    public static final String       IMAGE_SDCARD_CACHE_TABLE_TAG                  = "tag";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_URL                  = "url";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_PATH                 = "path";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_ENTER_TIME           = "enter_time";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_LAST_USED_TIME       = "last_used_time";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_USED_COUNT           = "used_count";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_PRIORITY             = "priority";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_IS_EXPIRED           = "is_expired";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_IS_FOREVER           = "is_forever";

    public static final String       IMAGE_SDCARD_CACHE_TABLE_INDEX_TAG            = "image_sdcard_cache_table_index_tag";
    public static final String       IMAGE_SDCARD_CACHE_TABLE_INDEX_URL            = "image_sdcard_cache_table_index_url";

    public static final int          IMAGE_SDCARD_CACHE_TABLE_ID_INDEX             = 0;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_TAG_INDEX            = 1;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_URL_INDEX            = 2;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_PATH_INDEX           = 3;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_ENTER_TIME_INDEX     = 4;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_LAST_USED_TIME_INDEX = 5;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_USED_COUNT_INDEX     = 6;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_PRIORITY_INDEX       = 7;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_IS_EXPIRED_INDEX     = 8;
    public static final int          IMAGE_SDCARD_CACHE_TABLE_IS_FOREVER_INDEX     = 9;

    /** http response cache table **/
    public static final StringBuffer CREATE_HTTP_CACHE_TABLE_SQL                   = new StringBuffer();
    public static final StringBuffer CREATE_HTTP_CACHE_TABLE_INDEX_SQL             = new StringBuffer();
    public static final StringBuffer CREATE_HTTP_CACHE_TABLE_UNIQUE_INDEX          = new StringBuffer();
    public static final String       HTTP_CACHE_TABLE_TABLE_NAME                   = "http_cache";
    public static final String       HTTP_CACHE_TABLE_ID                           = android.provider.BaseColumns._ID;
    public static final String       HTTP_CACHE_TABLE_URL                          = "url";
    public static final String       HTTP_CACHE_TABLE_RESPONSE                     = "response";
    public static final String       HTTP_CACHE_TABLE_EXPIRES                      = "expires";
    public static final String       HTTP_CACHE_TABLE_CREATE_TIME                  = "gmt_create";
    public static final String       HTTP_CACHE_TABLE_TYPE                         = "type";

    public static final String       HTTP_CACHE_TABLE_UNIQUE_INDEX_URL             = "http_cache_table_unique_index_url";
    public static final String       HTTP_CACHE_TABLE_INDEX_TYPE                   = "http_cache_table_index_type";

    public static final int          HTTP_CACHE_TABLE_ID_INDEX                     = 0;
    public static final int          HTTP_CACHE_TABLE_URL_INDEX                    = 1;
    public static final int          HTTP_CACHE_TABLE_RESPONSE_INDEX               = 2;
    public static final int          HTTP_CACHE_TABLE_EXPIRES_INDEX                = 3;
    public static final int          HTTP_CACHE_TABLE_CREATE_TIME_INDEX            = 4;
    public static final int          HTTP_CACHE_TABLE_TYPE_INDEX                   = 5;

    static {
        /**
         * sql to image sdcard cache table
         **/
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append("CREATE TABLE ").append(IMAGE_SDCARD_CACHE_TABLE_TABLE_NAME);
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(" (").append(IMAGE_SDCARD_CACHE_TABLE_ID)
                .append(" integer primary key autoincrement,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_TAG).append(" text,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_URL).append(" text,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_PATH).append(" text,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_ENTER_TIME).append(" integer,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_LAST_USED_TIME).append(" integer,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_USED_COUNT).append(" integer,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_PRIORITY).append(" integer,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_IS_EXPIRED).append(" integer,");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(IMAGE_SDCARD_CACHE_TABLE_IS_FOREVER).append(" integer)");
        CREATE_IMAGE_SDCARD_CACHE_TABLE_SQL.append(TERMINATOR);

        CREATE_IMAGE_SDCARD_CACHE_TABLE_INDEX_SQL.append("CREATE INDEX ").append(IMAGE_SDCARD_CACHE_TABLE_INDEX_TAG)
                .append(" ON ").append(IMAGE_SDCARD_CACHE_TABLE_TABLE_NAME).append("(")
                .append(IMAGE_SDCARD_CACHE_TABLE_TAG).append(")").append(TERMINATOR).append("CREATE INDEX ")
                .append(IMAGE_SDCARD_CACHE_TABLE_INDEX_URL).append(" ON ").append(IMAGE_SDCARD_CACHE_TABLE_TABLE_NAME)
                .append("(").append(IMAGE_SDCARD_CACHE_TABLE_URL).append(")").append(TERMINATOR);

        /**
         * sql to http response table
         **/
        CREATE_HTTP_CACHE_TABLE_SQL.append("CREATE TABLE ").append(HTTP_CACHE_TABLE_TABLE_NAME);
        CREATE_HTTP_CACHE_TABLE_SQL.append(" (").append(HTTP_CACHE_TABLE_ID)
                .append(" integer primary key autoincrement,");
        CREATE_HTTP_CACHE_TABLE_SQL.append(HTTP_CACHE_TABLE_URL).append(" text,");
        CREATE_HTTP_CACHE_TABLE_SQL.append(HTTP_CACHE_TABLE_RESPONSE).append(" text,");
        CREATE_HTTP_CACHE_TABLE_SQL.append(HTTP_CACHE_TABLE_EXPIRES).append(" integer,");
        CREATE_HTTP_CACHE_TABLE_SQL.append(HTTP_CACHE_TABLE_CREATE_TIME).append(" integer,");
        CREATE_HTTP_CACHE_TABLE_SQL.append(HTTP_CACHE_TABLE_TYPE).append(" integer)").append(TERMINATOR);

        CREATE_HTTP_CACHE_TABLE_UNIQUE_INDEX.append("CREATE UNIQUE INDEX ").append(HTTP_CACHE_TABLE_UNIQUE_INDEX_URL)
                .append(" ON ").append(HTTP_CACHE_TABLE_TABLE_NAME).append("(").append(HTTP_CACHE_TABLE_URL)
                .append(")").append(TERMINATOR);
        CREATE_HTTP_CACHE_TABLE_INDEX_SQL.append("CREATE INDEX ").append(HTTP_CACHE_TABLE_INDEX_TYPE).append(" ON ")
                .append(HTTP_CACHE_TABLE_TABLE_NAME).append("(").append(HTTP_CACHE_TABLE_TYPE).append(")")
                .append(TERMINATOR);

    }
}
