package com.trinea.android.common.serviceImpl;

import java.util.Calendar;

import com.trinea.android.common.service.FileNameRule;
import com.trinea.java.common.FileUtils;

/**
 * 文件名规则--以当前时间为文件名，以文件网络url后缀为后缀
 * 
 * @author Trinea 2012-7-6 上午11:15:53
 */
public class FileNameRuleCurrentTime implements FileNameRule {

    private static final long  serialVersionUID    = 1L;
    /** 默认文件后缀 **/
    public static final String DEFAULT_FILE_SUFFIX = ".jpg";

    private TimeRule           timeRule;

    /**
     * @param timeRule 时间规则，见{@link TimeRule}
     * @return
     */
    public FileNameRuleCurrentTime(TimeRule timeRule){
        super();
        this.timeRule = timeRule;
    }

    @Override
    public String getFileName(Object feature) {
        long time;
        Calendar now = Calendar.getInstance();
        switch (timeRule) {
            case TO_MILLIS:
                time = now.getTimeInMillis();
                break;
            case HOUR_OF_DAY_TO_MILLIS:
                time = ((now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)) * 60 + now.get(Calendar.SECOND))
                       * 1000 + now.get(Calendar.MILLISECOND);
                break;
            case YEAR:
                time = now.get(Calendar.YEAR);
                break;
            case DAY_OF_MONTH:
                time = now.get(Calendar.DAY_OF_MONTH);
                break;
            case MILLISECOND:
                time = now.get(Calendar.MILLISECOND);
                break;
            case HOUR_OF_DAY_TO_MINUTE:
                time = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
                break;
            case HOUR_TO_MILLIS:
                time = ((now.get(Calendar.HOUR) * 60 + now.get(Calendar.MINUTE)) * 60 + now.get(Calendar.SECOND))
                       * 1000 + now.get(Calendar.MILLISECOND);
                break;
            case MINUTE_TO_SECOND:
                time = now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND);
                break;
            case TO_SECOND:
                time = now.getTimeInMillis() / 1000;
                break;
            default:
                time = now.getTimeInMillis();
                break;
        }
        return Long.toString(time) + "."
               + ((feature instanceof String) ? FileUtils.getFileExtension((String)feature) : DEFAULT_FILE_SUFFIX);
    }

    /**
     * 时间规则
     * <ul>
     * <li>{@link #YEAR} 当前年份，E.g., at 2012-7-6 14:37:58.365 PM result is 2012</li>
     * <li>{@link #DAY_OF_MONTH} 当前月份中的第几天，E.g., at 2012-7-6 14:37:58.365 PM result is 6</li>
     * <li>{@link #MILLISECOND} 当前毫秒，E.g., at 2012-7-6 14:37:58.365 PM result is 365</li>
     * <li>{@link #HOUR_OF_DAY_TO_MILLIS} 当前时间中小时（24小时制）到毫秒部分转换为毫秒，E.g., at 2012-7-6 14:37:58.365 PM result is 52678365</li>
     * <li>{@link #HOUR_OF_DAY_TO_MINUTE} 当前时间中小时（24小时制）到分钟部分转换为分钟，E.g., at 2012-7-6 14:37:58.365 PM result is 877</li>
     * <li>{@link #HOUR_TO_MILLIS} 当前时间中小时（12小时制）到毫秒部分转换为毫秒，E.g., at 2012-7-6 14:37:58.365 PM result is 9478365</li>
     * <li>{@link #MINUTE_TO_SECOND} 当前时间中分钟到秒部分转换为秒，E.g., at 2012-7-6 14:37:58.365 PM result is 2278</li>
     * <li>{@link #TO_MILLIS} 当前时间转换为毫秒，E.g., at 2012-7-6 14:37:58.365 PM result is 1341556678365</li>
     * <li>{@link #TO_SECOND} 当前时间转换为秒，E.g., at 2012-7-6 14:37:58.365 PM result is 1341556678</li>
     * </ul>
     * 
     * @author Trinea 2012-7-6 下午02:09:46
     */
    public enum TimeRule {
        YEAR, DAY_OF_MONTH, MILLISECOND, HOUR_OF_DAY_TO_MILLIS, HOUR_OF_DAY_TO_MINUTE, HOUR_TO_MILLIS,
        MINUTE_TO_SECOND, TO_MILLIS, TO_SECOND
    }
}
