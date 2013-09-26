package cn.trinea.android.common.service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import cn.trinea.android.common.entity.CacheObject;

/**
 * Cache interface
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-12-23
 */
public interface Cache<K, V> {

    /**
     * get object in cache
     * 
     * @return
     */
    public int getSize();

    /**
     * get object
     * 
     * @param key
     * @return
     */
    public CacheObject<V> get(K key);

    /**
     * put object
     * 
     * @param key key
     * @param value data in object, {@link CacheObject#getData()}
     * @return
     */
    public CacheObject<V> put(K key, V value);

    /**
     * put object
     * 
     * @param key key
     * @param value object
     * @return
     */
    public CacheObject<V> put(K key, CacheObject<V> value);

    /**
     * put all object in cache2
     * 
     * @param cache2
     */
    public void putAll(Cache<K, V> cache2);

    /**
     * whether key is in cache
     * 
     * @param key
     * @return
     */
    public boolean containsKey(K key);

    /**
     * remove object
     * 
     * @param key
     * @return the object be removed
     */
    public CacheObject<V> remove(K key);

    /**
     * clear cache
     */
    public void clear();

    /**
     * get hit rate
     * 
     * @return
     */
    public double getHitRate();

    /**
     * key set
     * 
     * @return
     */
    public Set<K> keySet();

    /**
     * key value set
     * 
     * @return
     */
    public Set<Map.Entry<K, CacheObject<V>>> entrySet();

    /**
     * value set
     * 
     * @return
     */
    public Collection<CacheObject<V>> values();
}
