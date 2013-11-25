package cn.trinea.android.common.entity;

/**
 * get data failed exception
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-11-25
 */
public class FailedException extends Throwable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently
     * be initialized by a call to {@link #initCause}.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     * method.
     */
    public FailedException(String message){
        super(message);
    }
}
