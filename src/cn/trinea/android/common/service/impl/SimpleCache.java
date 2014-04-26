package cn.trinea.android.common.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.service.Cache;
import cn.trinea.android.common.service.CacheFullRemoveType;
import cn.trinea.android.common.util.MapUtils;
import cn.trinea.android.common.util.SerializeUtils;

/**
 * Simple Cache<br/>
 * <ul>
 * <strong>Usage</strong>
 * <li>Use one of constructors below to construct cache</li>
 * <li>{@link #setCacheFullRemoveType(CacheFullRemoveType)} set remove type when cache is full</li>
 * <li>{@link #setValidTime(long)} set valid time of elements in cache, in mills</li>
 * <li>{@link #put(Object, CacheObject)} or {@link #put(Object, Object)} put element</li>
 * <li>{@link #get(Object)} get element</li>
 * <li>{@link #loadCache(String)} restore cache from file</li>
 * <li>{@link #saveCache(String, SimpleCache)} save cache to file</li>
 * </ul>
 * <ul>
 * <strong>Constructor</strong>
 * <li>{@link #SimpleCache()}</li>
 * <li>{@link #SimpleCache(int)}</li>
 * <li>{@link #loadCache(String)} restore cache from file</li>
 * </ul>
 * <ul>
 * <strong>About hit and miss of the cache</strong>
 * <li>{@link #getHitRate()} get hit rate of the cache</li>
 * <li>{@link #getHitCount()} get hit count of the cache</li>
 * <li>{@link #getMissCount()} get miss count of the cache</li>
 * </ul>
 * <ul>
 * <strong>About size of cache</strong>
 * <li>{@link #getMaxSize()} get the maximum capacity of the cache</li>
 * <li>{@link #getSize()} get the number of elements in the cache valid</li>
 * </ul>
 * <ul>
 * <strong>Other interfaces same to {@link Map} </strong>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-12-23
 */
public class SimpleCache<K, V> implements Cache<K, V>, Serializable {

    private static final long        serialVersionUID = 1L;

    /** default maximum capacity of the cache **/
    public static final int          DEFAULT_MAX_SIZE = 64;

    /** maximum size of the cache, if not set, use {@link #DEFAULT_MAX_SIZE} **/
    private final int                maxSize;

    /** valid time of elements in cache, in mills. It means not invalid if less than 0 **/
    private long                     validTime;

    /** remove type when cache is full **/
    private CacheFullRemoveType<V>   cacheFullRemoveType;

    /** map to storage element **/
    protected Map<K, CacheObject<V>> cache;

    /** hit count of cache **/
    protected AtomicLong             hitCount         = new AtomicLong(0);
    /** miss count of cache **/
    protected AtomicLong             missCount        = new AtomicLong(0);

    /**
     * <ul>
     * <li>Maximum size of the cache is {@link #DEFAULT_MAX_SIZE}</li>
     * <li>Elements of the cache will not invalid, can set by {@link #setValidTime(long)}</li>
     * <li>Remove type is {@link RemoveTypeEnterTimeFirst} when cache is full</li>
     * </ul>
     */
    public SimpleCache() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * <ul>
     * <li>Elements of the cache will not invalid, can set by {@link #setValidTime(long)}</li>
     * <li>Remove type is {@link RemoveTypeEnterTimeFirst} when cache is full</li>
     * </ul>
     * 
     * @param maxSize maximum size of the cache
     */
    public SimpleCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("The maxSize of cache must be greater than 0.");
        }

        this.maxSize = maxSize;
        this.cacheFullRemoveType = new RemoveTypeEnterTimeFirst<V>();
        this.validTime = -1;
        this.cache = new ConcurrentHashMap<K, CacheObject<V>>(maxSize);
    }

    /**
     * get the maximum capacity of the cache
     * 
     * @return
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * get valid time of elements in cache, in mills. It means not invalid if less than 0
     * 
     * @return
     */
    public long getValidTime() {
        return validTime;
    }

    /**
     * set valid time of elements in cache, in mills
     * 
     * @param validTime valid time of elements in cache, in mills. If less than 0, it will be set to -1 and means not
     *        invalid. Rule of invalid see {@link #isExpired(CacheObject)}
     */
    public void setValidTime(long validTime) {
        this.validTime = validTime <= 0 ? -1 : validTime;
    }

    /**
     * get remove type when cache is full
     * 
     * @return
     */
    public CacheFullRemoveType<V> getCacheFullRemoveType() {
        return cacheFullRemoveType;
    }

    /**
     * set remove type when cache is full
     * 
     * @param cacheFullRemoveType the cacheFullRemoveType to set
     */
    public void setCacheFullRemoveType(CacheFullRemoveType<V> cacheFullRemoveType) {
        if (cacheFullRemoveType == null) {
            throw new IllegalArgumentException("The cacheFullRemoveType of cache cannot be null.");
        }
        this.cacheFullRemoveType = cacheFullRemoveType;
    }

    /**
     * get the number of elements in the cache valid
     * 
     * @return
     */
    @Override
    public int getSize() {
        removeExpired();
        return cache.size();
    }

    /**
     * get element
     * 
     * @param key
     * @return element if this cache contains the specified key and the element is valid, null otherwise.
     */
    @Override
    public CacheObject<V> get(K key) {
        CacheObject<V> obj = cache.get(key);
        if (!isExpired(obj) && obj != null) {
            hitCount.incrementAndGet();
            setUsedInfo(obj);
            return obj;
        } else {
            missCount.incrementAndGet();
            return null;
        }
    }

    /**
     * set used info
     * 
     * @param obj
     */
    protected synchronized void setUsedInfo(CacheObject<V> obj) {
        if (obj != null) {
            obj.getAndIncrementUsedCount();
            obj.setLastUsedTime(System.currentTimeMillis());
        }
    }

    /**
     * put element, key not allowed to be null
     * 
     * @param key key
     * @param value data of {@link CacheObject}
     * @return return null if cache is full and cannot remove one, else return the value be putted
     * @see SimpleCache#put(Object, CacheObject)
     */
    @Override
    public CacheObject<V> put(K key, V value) {
        CacheObject<V> obj = new CacheObject<V>();
        obj.setData(value);
        obj.setForever(validTime == -1);
        return put(key, obj);
    }

    /**
     * put element, key and value both not allowed to be null
     * 
     * @param key
     * @param value
     * @return return null if cache is full and cannot remove one, else return the value be putted
     */
    @Override
    public synchronized CacheObject<V> put(K key, CacheObject<V> value) {
        if (cache.size() >= maxSize) {
            if (removeExpired() <= 0) {
                if (cacheFullRemoveType instanceof RemoveTypeNotRemove) {
                    return null;
                }
                if (fullRemoveOne() == null) {
                    return null;
                }
            }
        }
        value.setEnterTime(System.currentTimeMillis());
        cache.put(key, value);
        return value;
    }

    /**
     * pull all elements of cache2 to this
     * 
     * @param cache2
     */
    @Override
    public void putAll(Cache<K, V> cache2) {
        for (Entry<K, CacheObject<V>> e : cache2.entrySet()) {
            if (e != null) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * whether this cache contains the specified key.
     * 
     * @param key
     * @return true if this cache contains the specified key and the element is valid, false otherwise.
     */
    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key) ? !isExpired(key) : false;
    }

    /**
     * whether the element of the specified key has invalided
     * 
     * @param key
     * @return
     * @see SimpleCache#isExpired(CacheObject)
     */
    protected boolean isExpired(K key) {
        return validTime == -1 ? false : isExpired(cache.get(key));
    }

    /**
     * remove the specified key from cache, key not allowed to be null
     * 
     * @param key
     * @return the value of the removed or null if no mapping for the specified key was found.
     */
    @Override
    public CacheObject<V> remove(K key) {
        return cache.remove(key);
    }

    /**
     * remove a element when cache is full. according to {@link #getCacheFullRemoveType()}
     * <ul>
     * <li>if {@link #getCacheFullRemoveType()} is instance of {@link RemoveTypeNotRemove} return null, else</li>
     * <li>remove a element according to {@link #getCacheFullRemoveType()}</li>
     * </ul>
     * 
     * @param key
     * @return the value of the removed or null if no element can be remove.
     */
    protected CacheObject<V> fullRemoveOne() {
        if (MapUtils.isEmpty(cache) || cacheFullRemoveType instanceof RemoveTypeNotRemove) {
            return null;
        }

        K keyToRemove = null;
        CacheObject<V> valueToRemove = null;
        for (Entry<K, CacheObject<V>> entry : cache.entrySet()) {
            if (entry != null) {
                if (valueToRemove == null) {
                    valueToRemove = entry.getValue();
                    keyToRemove = entry.getKey();
                } else {
                    if (cacheFullRemoveType.compare(entry.getValue(), valueToRemove) < 0) {
                        valueToRemove = entry.getValue();
                        keyToRemove = entry.getKey();
                    }
                }
            }
        }
        if (keyToRemove != null) {
            cache.remove(keyToRemove);
        }
        return valueToRemove;
    }

    /**
     * remove invalid elements
     * 
     * @return the count be removed
     */
    protected synchronized int removeExpired() {
        if (validTime == -1) {
            return 0;
        }

        int count = 0;
        // because cache is instance of ConcurrentHashMap, so you can remove when iterator
        for (Entry<K, CacheObject<V>> entry : cache.entrySet()) {
            if (entry != null && isExpired(entry.getValue())) {
                cache.remove(entry.getKey());
                count++;
            }
        }
        return count;
    }

    /**
     * Removes all elements from this Map, leaving it empty.
     * 
     * @see Map#clear()
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * returns whether the element of the specified key has invalided
     * <ul>
     * <li>if {@link #getValidTime()} less than 0, return false, else</li>
     * <li>if element is null, return true, else</li>
     * <li>if {@link CacheObject#isExpired()} is true and {@link CacheObject#isForever()} is false, return true, else</li>
     * <li>if {@link CacheObject#getEnterTime()} add {@link #getValidTime()} less than current time, return true</li>
     * <li>return false</li>
     * </ul>
     * 
     * @param obj
     * @return
     */
    protected boolean isExpired(CacheObject<V> obj) {
        return validTime != -1
                && (obj == null || (obj.isExpired() && !obj.isForever()) || (obj.getEnterTime() + validTime) < System
                        .currentTimeMillis());
    }

    /**
     * get hit count
     **/
    public long getHitCount() {
        return hitCount.get();
    }

    /**
     * get miss count
     **/
    public long getMissCount() {
        return missCount.get();
    }

    /**
     * get hit rate
     * 
     * @return
     */
    @Override
    public synchronized double getHitRate() {
        long total = hitCount.get() + missCount.get();
        return (total == 0 ? 0 : ((double)hitCount.get()) / total);
    }

    /**
     * @return a set of the keys.
     * @see Map#keySet()
     */
    @Override
    public Set<K> keySet() {
        removeExpired();
        return cache.keySet();
    }

    /**
     * @return a set of the mappings
     * @see Map#entrySet()
     */
    @Override
    public Set<Map.Entry<K, CacheObject<V>>> entrySet() {
        removeExpired();
        return cache.entrySet();
    }

    /**
     * @return a collection of the values contained in this cache.
     * @see Map#values()
     */
    @Override
    public Collection<CacheObject<V>> values() {
        removeExpired();
        return cache.values();
    }

    /**
     * restore cache from file
     * 
     * @param filePath
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V> SimpleCache<K, V> loadCache(String filePath) {
        return (SimpleCache<K, V>)SerializeUtils.deserialization(filePath);
    }

    /**
     * save cache to file, the data of {@link CacheObject} should can be serializabled
     * 
     * @param <K>
     * @param <V>
     * @param filePath
     * @param cache
     */
    public static <K, V> void saveCache(String filePath, SimpleCache<K, V> cache) {
        SerializeUtils.serialization(filePath, cache);
    }
}
