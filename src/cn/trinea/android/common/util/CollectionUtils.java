package cn.trinea.android.common.util;

import java.util.Collection;

/**
 * CollectionUtils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-7-22
 */
public class CollectionUtils {

    /** default join separator **/
    public static final String DEFAULT_JOIN_SEPARATOR = ",";

    private CollectionUtils() {
        throw new AssertionError();
    }

    /**
     * is null or its size is 0
     * 
     * <pre>
     * isEmpty(null)   =   true;
     * isEmpty({})     =   true;
     * isEmpty({1})    =   false;
     * </pre>
     * 
     * @param <V>
     * @param c
     * @return if collection is null or its size is 0, return true, else return false.
     */
    public static <V> boolean isEmpty(Collection<V> c) {
        return (c == null || c.size() == 0);
    }

    /**
     * join collection to string, separator is ","
     * 
     * <pre>
     * join(null)      =   "";
     * join({})        =   "";
     * join({a,b})     =   "a,b";
     * </pre>
     * 
     * @param c
     * @return join collection to string, separator is ",". if collection is empty, return ""
     */
    public static <T> String join(Collection<T> c) {
        return join(c, DEFAULT_JOIN_SEPARATOR);
    }

    /**
     * join collection to string
     * 
     * <pre>
     * join(null, '#')     =   "";
     * join({}, '#')       =   "";
     * join({a,b,c}, ' ')  =   "abc";
     * join({a,b,c}, '#')  =   "a#b#c";
     * </pre>
     * 
     * @param collection
     * @param separator
     * @return join collection to string. if collection is empty, return ""
     */
    public static <T> String join(Collection<T> c, char separator) {
        return join(c, new String(new char[] {separator}));
    }

    /**
     * join collection to string. if separator is null, use {@link #DEFAULT_JOIN_SEPARATOR}
     * 
     * <pre>
     * join(null, "#")     =   "";
     * join({}, "#$")      =   "";
     * join({a,b,c}, null) =   "a,b,c";
     * join({a,b,c}, "")   =   "abc";
     * join({a,b,c}, "#")  =   "a#b#c";
     * join({a,b,c}, "#$") =   "a#$b#$c";
     * </pre>
     * 
     * @param <T>
     * 
     * @param c
     * @param separator
     * @return join collection to string with separator. if collection is empty, return ""
     */
    public static <T> String join(Collection<T> c, String separator) {
        if (isEmpty(c)) {
            return "";
        }
        if (separator == null) {
            separator = DEFAULT_JOIN_SEPARATOR;
        }

        StringBuilder joinStr = new StringBuilder();
        int count = 0, lastIndex = c.size() - 1;
        Boolean isString = null;

        for (T item : c) {
            if (isString == null) {
                isString = item instanceof String;
            }

            if (item == null) {
                joinStr.append("");
            } else {
                joinStr.append(isString ? (String)item : item.toString());
            }

            if (count != lastIndex) {
                joinStr.append(separator);
            }
            count++;
        }

        return joinStr.toString();
    }
}
