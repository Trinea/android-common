package cn.trinea.android.common.entity;

/**
 * get data failed reason
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-25
 */
public class FailedReason {

    private FailedType           failedType;
    /** reserved field, it's no use now, waiting to be perfect^_^ **/
    private FailedException cause;

    public FailedReason(FailedType failedType, FailedException cause){
        this.failedType = failedType;
        this.cause = cause;
    }

    /**
     * get failedType
     * 
     * @return the failedType
     */
    public FailedType getFailedType() {
        return failedType;
    }

    /**
     * get cause
     * 
     * @return the cause
     * @deprecated Reserved field, it's no use now, waiting to be perfect^_^
     */
    public FailedException getCause() {
        return cause;
    }

    public static enum FailedType {
        /** get image error from network **/
        ERROR_NETWORK,
        /** save image to sdcard error **/
        ERROR_IO,
        /** reserved field, it's no use now, waiting to be perfect^_^ **/
        ERROR_OUT_OF_MEMORY,
        /** reserved field, it's no use now, waiting to be perfect^_^ **/
        ERROR_UNKNOWN,
    }
}
