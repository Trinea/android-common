package cn.trinea.android.common.entity;

import java.io.Serializable;

import cn.trinea.android.common.util.ObjectUtils;

/**
 * Object in cache
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-12-23
 */
public class CacheObject<V> implements Serializable, Comparable<CacheObject<V>> {

    private static final long serialVersionUID = 1L;

    /** time first put into cache, in mills **/
    protected long            enterTime;
    /** time last used(got), in mills **/
    protected long            lastUsedTime;
    /** used(got) count **/
    protected long            usedCount;
    /** priority, default is zero **/
    protected int             priority;

    /** whether has expired, default is false **/
    protected boolean         isExpired;
    /** whether is valid forever, default is false **/
    protected boolean         isForever;

    /** data **/
    protected V               data;

    public CacheObject() {
        this.enterTime = System.currentTimeMillis();
        this.lastUsedTime = System.currentTimeMillis();
        this.usedCount = 0;
        this.priority = 0;
        this.isExpired = false;
        this.isForever = false;
    }

    public CacheObject(V data) {
        this();
        this.data = data;
    }

    /**
     * Get time first put into cache, in mills
     * 
     * @return
     */
    public long getEnterTime() {
        return enterTime;
    }

    /**
     * Set time first put into cache, in mills
     * 
     * @param enterTime
     */
    public void setEnterTime(long enterTime) {
        this.enterTime = enterTime;
    }

    /**
     * Get time last used(got), in mills
     * 
     * @return
     */
    public long getLastUsedTime() {
        return lastUsedTime;
    }

    /**
     * Set time last used(got), in mills
     * 
     * @param lastUsedTime
     */
    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    /**
     * Get used(got) count
     * 
     * @return
     */
    public long getUsedCount() {
        return usedCount;
    }

    /**
     * Set used(got) count
     * 
     * @param usedCount
     */
    public void setUsedCount(long usedCount) {
        this.usedCount = usedCount;
    }

    /**
     * Atomically increments by one the used(got) count
     * 
     * @return the previous used(got) count
     */
    public synchronized long getAndIncrementUsedCount() {
        return usedCount++;
    }

    /**
     * Get priority, default is zero
     * 
     * @return
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set priority, default is zero
     * 
     * @param priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Get whether has expired, default is false
     * 
     * @return
     */
    public boolean isExpired() {
        return isExpired;
    }

    /**
     * Set whether has expired, default is false
     * 
     * @param isExpired
     */
    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    /**
     * Get whether is valid forever, default is false
     * 
     * @return
     */
    public boolean isForever() {
        return isForever;
    }

    /**
     * Set whether is valid forever, default is false
     * 
     * @param isForever
     */
    public void setForever(boolean isForever) {
        this.isForever = isForever;
    }

    /**
     * Get data
     * 
     * @return
     */
    public V getData() {
        return data;
    }

    /**
     * Set data
     * 
     * @param data
     */
    public void setData(V data) {
        this.data = data;
    }

    /**
     * compare with data
     * 
     * @param o
     * @return
     */
    @Override
    public int compareTo(CacheObject<V> o) {
        return o == null ? 1 : ObjectUtils.compare(this.data, o.data);
    }

    /**
     * if data, enterTime, priority, isExpired, isForever all equals
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        CacheObject<V> obj = (CacheObject<V>)(o);
        return (ObjectUtils.isEquals(this.data, obj.data) && this.enterTime == obj.enterTime
                && this.priority == obj.priority && this.isExpired == obj.isExpired && this.isForever == obj.isForever);
    }

    @Override
    public int hashCode() {
        return data == null ? 0 : data.hashCode();
    }
}
