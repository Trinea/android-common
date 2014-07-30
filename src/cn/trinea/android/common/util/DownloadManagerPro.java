package cn.trinea.android.common.util;

import java.lang.reflect.Method;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

/**
 * DownloadManagerPro
 * <ul>
 * <strong>Get download info</strong>
 * <li>{@link #getStatusById(long)} get download status</li>
 * <li>{@link #getDownloadBytes(long)} get downloaded byte, total byte</li>
 * <li>{@link #getBytesAndStatus(long)} get downloaded byte, total byte and download status</li>
 * <li>{@link #getFileName(long)} get download file name</li>
 * <li>{@link #getUri(long)} get download uri</li>
 * <li>{@link #getReason(long)} get failed code or paused reason</li>
 * <li>{@link #getPausedReason(long)} get paused reason</li>
 * <li>{@link #getErrorCode(long)} get failed error code</li>
 * </ul>
 * <ul>
 * <strong>Operate download</strong>
 * <li>{@link #isExistPauseAndResumeMethod()} whether exist pauseDownload and resumeDownload method in
 * {@link DownloadManager}</li>
 * <li>{@link #pauseDownload(long...)} pause download. need pauseDownload(long...) method in {@link DownloadManager}</li>
 * <li>{@link #resumeDownload(long...)} resume download. need resumeDownload(long...) method in {@link DownloadManager}</li>
 * </ul>
 * <ul>
 * <strong>RequestPro</strong>
 * <li>{@link RequestPro#setNotiClass(String)} set noti class</li>
 * <li>{@link RequestPro#setNotiExtras(String)} set noti extras</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-4
 */
public class DownloadManagerPro {

    public static final Uri    CONTENT_URI                 = Uri.parse("content://downloads/my_downloads");
    /** represents downloaded file above api 11 **/
    public static final String COLUMN_LOCAL_FILENAME       = "local_filename";
    /** represents downloaded file below api 11 **/
    public static final String COLUMN_LOCAL_URI            = "local_uri";

    public static final String METHOD_NAME_PAUSE_DOWNLOAD  = "pauseDownload";
    public static final String METHOD_NAME_RESUME_DOWNLOAD = "resumeDownload";

    private static boolean     isInitPauseDownload         = false;
    private static boolean     isInitResumeDownload        = false;

    private static Method      pauseDownload               = null;
    private static Method      resumeDownload              = null;

    private DownloadManager    downloadManager;

    public DownloadManagerPro(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    /**
     * get download status
     * 
     * @param downloadId
     * @return
     */
    public int getStatusById(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_STATUS);
    }

    /**
     * get downloaded byte, total byte
     * 
     * @param downloadId
     * @return a int array with two elements
     *         <ul>
     *         <li>result[0] represents downloaded bytes, This will initially be -1.</li>
     *         <li>result[1] represents total bytes, This will initially be -1.</li>
     *         </ul>
     */
    public int[] getDownloadBytes(long downloadId) {
        int[] bytesAndStatus = getBytesAndStatus(downloadId);
        return new int[] {bytesAndStatus[0], bytesAndStatus[1]};
    }

    /**
     * get downloaded byte, total byte and download status
     * 
     * @param downloadId
     * @return a int array with three elements
     *         <ul>
     *         <li>result[0] represents downloaded bytes, This will initially be -1.</li>
     *         <li>result[1] represents total bytes, This will initially be -1.</li>
     *         <li>result[2] represents download status, This will initially be 0.</li>
     *         </ul>
     */
    public int[] getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = new int[] {-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return bytesAndStatus;
    }

    /**
     * pause download
     * 
     * @param ids the IDs of the downloads to be paused
     * @return the number of downloads actually paused, -1 if exception or method not exist
     */
    public int pauseDownload(long... ids) {
        initPauseMethod();
        if (pauseDownload == null) {
            return -1;
        }

        try {
            return ((Integer)pauseDownload.invoke(downloadManager, ids)).intValue();
        } catch (Exception e) {
            /**
             * accept all exception, include ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
             * NullPointException
             */
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * resume download
     * 
     * @param ids the IDs of the downloads to be resumed
     * @return the number of downloads actually resumed, -1 if exception or method not exist
     */
    public int resumeDownload(long... ids) {
        initResumeMethod();
        if (resumeDownload == null) {
            return -1;
        }

        try {
            return ((Integer)resumeDownload.invoke(downloadManager, ids)).intValue();
        } catch (Exception e) {
            /**
             * accept all exception, include ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
             * NullPointException
             */
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * whether exist pauseDownload and resumeDownload method in {@link DownloadManager}
     * 
     * @return
     */
    public static boolean isExistPauseAndResumeMethod() {
        initPauseMethod();
        initResumeMethod();
        return pauseDownload != null && resumeDownload != null;
    }

    private static void initPauseMethod() {
        if (isInitPauseDownload) {
            return;
        }

        isInitPauseDownload = true;
        try {
            pauseDownload = DownloadManager.class.getMethod(METHOD_NAME_PAUSE_DOWNLOAD, long[].class);
        } catch (Exception e) {
            // accept all exception
            e.printStackTrace();
        }
    }

    private static void initResumeMethod() {
        if (isInitResumeDownload) {
            return;
        }

        isInitResumeDownload = true;
        try {
            resumeDownload = DownloadManager.class.getMethod(METHOD_NAME_RESUME_DOWNLOAD, long[].class);
        } catch (Exception e) {
            // accept all exception
            e.printStackTrace();
        }
    }

    /**
     * get download file name
     * 
     * @param downloadId
     * @return
     */
    public String getFileName(long downloadId) {
        return getString(downloadId, (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? COLUMN_LOCAL_URI
                : COLUMN_LOCAL_FILENAME));
    }

    /**
     * get download uri
     * 
     * @param downloadId
     * @return
     */
    public String getUri(long downloadId) {
        return getString(downloadId, DownloadManager.COLUMN_URI);
    }

    /**
     * get failed code or paused reason
     * 
     * @param downloadId
     * @return <ul>
     *         <li>if status of downloadId is {@link DownloadManager#STATUS_PAUSED}, return
     *         {@link #getPausedReason(long)}</li>
     *         <li>if status of downloadId is {@link DownloadManager#STATUS_FAILED}, return {@link #getErrorCode(long)}</li>
     *         <li>if status of downloadId is neither {@link DownloadManager#STATUS_PAUSED} nor
     *         {@link DownloadManager#STATUS_FAILED}, return 0</li>
     *         </ul>
     */
    public int getReason(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    /**
     * get paused reason
     * 
     * @param downloadId
     * @return <ul>
     *         <li>if status of downloadId is {@link DownloadManager#STATUS_PAUSED}, return one of
     *         {@link DownloadManager#PAUSED_WAITING_TO_RETRY}<br/>
     *         {@link DownloadManager#PAUSED_WAITING_FOR_NETWORK}<br/>
     *         {@link DownloadManager#PAUSED_QUEUED_FOR_WIFI}<br/>
     *         {@link DownloadManager#PAUSED_UNKNOWN}</li>
     *         <li>else return {@link DownloadManager#PAUSED_UNKNOWN}</li>
     *         </ul>
     */
    public int getPausedReason(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    /**
     * get failed error code
     * 
     * @param downloadId
     * @return one of {@link DownloadManager#ERROR_*}
     */
    public int getErrorCode(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    public static class RequestPro extends DownloadManager.Request {

        public static final String METHOD_NAME_SET_NOTI_CLASS  = "setNotiClass";
        public static final String METHOD_NAME_SET_NOTI_EXTRAS = "setNotiExtras";

        private static boolean     isInitNotiClass             = false;
        private static boolean     isInitNotiExtras            = false;

        private static Method      setNotiClass                = null;
        private static Method      setNotiExtras               = null;

        /**
         * @param uri the HTTP URI to download.
         */
        public RequestPro(Uri uri) {
            super(uri);
        }

        /**
         * set noti class, only init once
         * 
         * @param className full class name
         */
        public void setNotiClass(String className) {
            synchronized (this) {

                if (!isInitNotiClass) {
                    isInitNotiClass = true;
                    try {
                        setNotiClass = Request.class.getMethod(METHOD_NAME_SET_NOTI_CLASS, CharSequence.class);
                    } catch (Exception e) {
                        // accept all exception
                        e.printStackTrace();
                    }
                }
            }

            if (setNotiClass != null) {
                try {
                    setNotiClass.invoke(this, className);
                } catch (Exception e) {
                    /**
                     * accept all exception, include ClassNotFoundException, NoSuchMethodException,
                     * InvocationTargetException, NullPointException
                     */
                    e.printStackTrace();
                }
            }
        }

        /**
         * set noti extras, only init once
         * 
         * @param extras
         */
        public void setNotiExtras(String extras) {
            synchronized (this) {

                if (!isInitNotiExtras) {
                    isInitNotiExtras = true;
                    try {
                        setNotiExtras = Request.class.getMethod(METHOD_NAME_SET_NOTI_EXTRAS, CharSequence.class);
                    } catch (Exception e) {
                        // accept all exception
                        e.printStackTrace();
                    }
                }
            }

            if (setNotiExtras != null) {
                try {
                    setNotiExtras.invoke(this, extras);
                } catch (Exception e) {
                    /**
                     * accept all exception, include ClassNotFoundException, NoSuchMethodException,
                     * InvocationTargetException, NullPointException
                     */
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * get string column
     * 
     * @param downloadId
     * @param columnName
     * @return
     */
    private String getString(long downloadId, String columnName) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        String result = null;
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getString(c.getColumnIndex(columnName));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    /**
     * get int column
     * 
     * @param downloadId
     * @param columnName
     * @return
     */
    private int getInt(long downloadId, String columnName) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        int result = -1;
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getInt(c.getColumnIndex(columnName));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }
}
