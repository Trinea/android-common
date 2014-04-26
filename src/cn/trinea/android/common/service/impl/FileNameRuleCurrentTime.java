package cn.trinea.android.common.service.impl;

import java.util.Calendar;

import cn.trinea.android.common.service.FileNameRule;
import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.StringUtils;

/**
 * File name rule, used when saving images in {@link ImageSDCardCache}
 * <ul>
 * <li>use {@link TimeRule} as file name</li>
 * <li>use file suffix in url as target file suffix</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-7-6
 */
public class FileNameRuleCurrentTime implements FileNameRule {

    private static final long serialVersionUID = 1L;

    private TimeRule          timeRule;

    /**
     * @param timeRule see {@link TimeRule}
     */
    public FileNameRuleCurrentTime(TimeRule timeRule) {
        super();
        this.timeRule = timeRule;
    }

    @Override
    public String getFileName(String imageUrl) {
        long time;
        Calendar now = Calendar.getInstance();
        switch (timeRule) {
            case TO_MILLIS:
                time = now.getTimeInMillis();
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
            case HOUR_OF_DAY_TO_MILLIS:
                time = ((now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)) * 60 + now.get(Calendar.SECOND))
                        * 1000 + now.get(Calendar.MILLISECOND);
                break;
            case HOUR_OF_DAY_TO_SECONDS:
                time = (now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)) * 60 + now.get(Calendar.SECOND);
                break;
            case HOUR_OF_DAY_TO_MINUTES:
                time = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
                break;
            case HOUR_TO_MILLIS:
                time = ((now.get(Calendar.HOUR) * 60 + now.get(Calendar.MINUTE)) * 60 + now.get(Calendar.SECOND))
                        * 1000 + now.get(Calendar.MILLISECOND);
                break;
            case MINUTE_TO_SECONDS:
                time = now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND);
                break;
            case TO_SECONDS:
                time = now.getTimeInMillis() / 1000;
                break;
            default:
                time = now.getTimeInMillis();
                break;
        }
        String ext = FileUtils.getFileExtension(imageUrl);
        return StringUtils.isEmpty(ext) ? Long.toString(time) : (Long.toString(time) + "." + ext);
    }

    /**
     * Time Rule
     * <ul>
     * <li>{@link #YEAR} year of current time, E.g., at 2012-7-6 14:37:58.365 PM result is 2012</li>
     * <li>{@link #DAY_OF_MONTH} indicating the day of the month. The first day of the month has value 1. E.g., at
     * 2012-7-6 14:37:58.365 PM result is 6</li>
     * <li>{@link #MILLISECOND} milliseconds of current time, E.g., at 2012-7-6 14:37:58.365 PM result is 365</li>
     * <li>{@link #HOUR_OF_DAY_TO_MILLIS} milliseconds of current time from hour(24 hours), E.g., at 2012-7-6
     * 14:37:58.365 PM result is 52678365</li>
     * <li>{@link #HOUR_OF_DAY_TO_SECONDS} seconds of current time from hour(24 hours), E.g., at 2012-7-6 14:37:58.365
     * PM result is 52678</li>
     * <li>{@link #HOUR_OF_DAY_TO_MINUTES} minutes of current time from hour(24 hours), E.g., at 2012-7-6 14:37:58.365
     * PM result is 877</li>
     * <li>{@link #HOUR_TO_MILLIS} milliseconds of current time from hour(12 hours), E.g., at 2012-7-6 14:37:58.365 PM
     * result is 9478365</li>
     * <li>{@link #MINUTE_TO_SECONDS} seconds of current time from hour(12 hours), E.g., at 2012-7-6 14:37:58.365 PM
     * result is 2278</li>
     * <li>{@link #TO_MILLIS} current time in milliseconds, E.g., at 2012-7-6 14:37:58.365 PM result is 1341556678365</li>
     * <li>{@link #TO_SECONDS} current time in seconds, E.g., at 2012-7-6 14:37:58.365 PM result is 1341556678</li>
     * </ul>
     * 
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-7-6
     */
    public enum TimeRule {
        YEAR, DAY_OF_MONTH, MILLISECOND, HOUR_OF_DAY_TO_MILLIS, HOUR_OF_DAY_TO_SECONDS, HOUR_OF_DAY_TO_MINUTES,
        HOUR_TO_MILLIS, MINUTE_TO_SECONDS, TO_MILLIS, TO_SECONDS
    }
}
