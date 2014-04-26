package cn.trinea.android.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Map Utils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-7-22
 */
public class MapUtils {

    /** default separator between key and value **/
    public static final String DEFAULT_KEY_AND_VALUE_SEPARATOR      = ":";
    /** default separator between key-value pairs **/
    public static final String DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR = ",";

    /**
     * is null or its size is 0
     * 
     * <pre>
     * isEmpty(null)   =   true;
     * isEmpty({})     =   true;
     * isEmpty({1, 2})    =   false;
     * </pre>
     * 
     * @param sourceMap
     * @return if map is null or its size is 0, return true, else return false.
     */
    public static <K, V> boolean isEmpty(Map<K, V> sourceMap) {
        return (sourceMap == null || sourceMap.size() == 0);
    }

    /**
     * add key-value pair to map, and key need not null or empty
     * 
     * @param map
     * @param key
     * @param value
     * @return <ul>
     *         <li>if map is null, return false</li>
     *         <li>if key is null or empty, return false</li>
     *         <li>return {@link Map#put(Object, Object)}</li>
     *         </ul>
     */
    public static boolean putMapNotEmptyKey(Map<String, String> map, String key, String value) {
        if (map == null || StringUtils.isEmpty(key)) {
            return false;
        }

        map.put(key, value);
        return true;
    }

    /**
     * add key-value pair to map, both key and value need not null or empty
     * 
     * @param map
     * @param key
     * @param value
     * @return <ul>
     *         <li>if map is null, return false</li>
     *         <li>if key is null or empty, return false</li>
     *         <li>if value is null or empty, return false</li>
     *         <li>return {@link Map#put(Object, Object)}</li>
     *         </ul>
     */
    public static boolean putMapNotEmptyKeyAndValue(Map<String, String> map, String key, String value) {
        if (map == null || StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return false;
        }

        map.put(key, value);
        return true;
    }

    /**
     * add key-value pair to map, key need not null or empty
     * 
     * @param map
     * @param key
     * @param value
     * @param defaultValue
     * @return <ul>
     *         <li>if map is null, return false</li>
     *         <li>if key is null or empty, return false</li>
     *         <li>if value is null or empty, put defaultValue, return true</li>
     *         <li>if value is neither null nor empty，put value, return true</li>
     *         </ul>
     */
    public static boolean putMapNotEmptyKeyAndValue(Map<String, String> map, String key, String value,
            String defaultValue) {
        if (map == null || StringUtils.isEmpty(key)) {
            return false;
        }

        map.put(key, StringUtils.isEmpty(value) ? defaultValue : value);
        return true;
    }

    /**
     * add key-value pair to map, key need not null
     * 
     * @param map
     * @param key
     * @param value
     * @return <ul>
     *         <li>if map is null, return false</li>
     *         <li>if key is null, return false</li>
     *         <li>return {@link Map#put(Object, Object)}</li>
     *         </ul>
     */
    public static <K, V> boolean putMapNotNullKey(Map<K, V> map, K key, V value) {
        if (map == null || key == null) {
            return false;
        }

        map.put(key, value);
        return true;
    }

    /**
     * add key-value pair to map, both key and value need not null
     * 
     * @param map
     * @param key
     * @param value
     * @return <ul>
     *         <li>if map is null, return false</li>
     *         <li>if key is null, return false</li>
     *         <li>if value is null, return false</li>
     *         <li>return {@link Map#put(Object, Object)}</li>
     *         </ul>
     */
    public static <K, V> boolean putMapNotNullKeyAndValue(Map<K, V> map, K key, V value) {
        if (map == null || key == null || value == null) {
            return false;
        }

        map.put(key, value);
        return true;
    }

    /**
     * get key by value, match the first entry front to back
     * <ul>
     * <strong>Attentions:</strong>
     * <li>for HashMap, the order of entry not same to put order, so you may need to use TreeMap</li>
     * </ul>
     * 
     * @param <V>
     * @param map
     * @param value
     * @return <ul>
     *         <li>if map is null, return null</li>
     *         <li>if value exist, return key</li>
     *         <li>return null</li>
     *         </ul>
     */
    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        if (isEmpty(map)) {
            return null;
        }

        for (Entry<K, V> entry : map.entrySet()) {
            if (ObjectUtils.isEquals(entry.getValue(), value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * parse key-value pairs to map, ignore empty key
     * 
     * <pre>
     * parseKeyAndValueToMap("","","",true)=null
     * parseKeyAndValueToMap(null,"","",true)=null
     * parseKeyAndValueToMap("a:b,:","","",true)={(a,b)}
     * parseKeyAndValueToMap("a:b,:d","","",true)={(a,b)}
     * parseKeyAndValueToMap("a:b,c:d","","",true)={(a,b),(c,d)}
     * parseKeyAndValueToMap("a=b, c = d","=",",",true)={(a,b),(c,d)}
     * parseKeyAndValueToMap("a=b, c = d","=",",",false)={(a, b),( c , d)}
     * parseKeyAndValueToMap("a=b, c=d","=", ",", false)={(a,b),( c,d)}
     * parseKeyAndValueToMap("a=b; c=d","=", ";", false)={(a,b),( c,d)}
     * parseKeyAndValueToMap("a=b, c=d", ",", ";", false)={(a=b, c=d)}
     * </pre>
     * 
     * @param source key-value pairs
     * @param keyAndValueSeparator separator between key and value
     * @param keyAndValuePairSeparator separator between key-value pairs
     * @param ignoreSpace whether ignore space at the begging or end of key and value
     * @return
     */
    public static Map<String, String> parseKeyAndValueToMap(String source, String keyAndValueSeparator,
            String keyAndValuePairSeparator, boolean ignoreSpace) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        if (StringUtils.isEmpty(keyAndValueSeparator)) {
            keyAndValueSeparator = DEFAULT_KEY_AND_VALUE_SEPARATOR;
        }
        if (StringUtils.isEmpty(keyAndValuePairSeparator)) {
            keyAndValuePairSeparator = DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR;
        }
        Map<String, String> keyAndValueMap = new HashMap<String, String>();
        String[] keyAndValueArray = source.split(keyAndValuePairSeparator);
        if (keyAndValueArray == null) {
            return null;
        }

        int seperator;
        for (String valueEntity : keyAndValueArray) {
            if (!StringUtils.isEmpty(valueEntity)) {
                seperator = valueEntity.indexOf(keyAndValueSeparator);
                if (seperator != -1) {
                    if (ignoreSpace) {
                        MapUtils.putMapNotEmptyKey(keyAndValueMap, valueEntity.substring(0, seperator).trim(),
                                valueEntity.substring(seperator + 1).trim());
                    } else {
                        MapUtils.putMapNotEmptyKey(keyAndValueMap, valueEntity.substring(0, seperator),
                                valueEntity.substring(seperator + 1));
                    }
                }
            }
        }
        return keyAndValueMap;
    }

    /**
     * parse key-value pairs to map, ignore empty key
     * 
     * @param source key-value pairs
     * @param ignoreSpace whether ignore space at the begging or end of key and value
     * @return
     * @see {@link MapUtils#parseKeyAndValueToMap(String, String, String, boolean)}, keyAndValueSeparator is
     *      {@link #DEFAULT_KEY_AND_VALUE_SEPARATOR}, keyAndValuePairSeparator is
     *      {@link #DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR}
     */
    public static Map<String, String> parseKeyAndValueToMap(String source, boolean ignoreSpace) {
        return parseKeyAndValueToMap(source, DEFAULT_KEY_AND_VALUE_SEPARATOR, DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR,
                ignoreSpace);
    }

    /**
     * parse key-value pairs to map, ignore empty key, ignore space at the begging or end of key and value
     * 
     * @param source key-value pairs
     * @return
     * @see {@link MapUtils#parseKeyAndValueToMap(String, String, String, boolean)}, keyAndValueSeparator is
     *      {@link #DEFAULT_KEY_AND_VALUE_SEPARATOR}, keyAndValuePairSeparator is
     *      {@link #DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR}, ignoreSpace is true
     */
    public static Map<String, String> parseKeyAndValueToMap(String source) {
        return parseKeyAndValueToMap(source, DEFAULT_KEY_AND_VALUE_SEPARATOR, DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR,
                true);
    }

    /**
     * join map
     * 
     * @param map
     * @return
     */
    public static String toJson(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return null;
        }

        StringBuilder paras = new StringBuilder();
        paras.append("{");
        Iterator<Map.Entry<String, String>> ite = map.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>)ite.next();
            paras.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            if (ite.hasNext()) {
                paras.append(",");
            }
        }
        paras.append("}");
        return paras.toString();
    }
}
