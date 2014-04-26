package cn.trinea.android.common.dao;

import cn.trinea.android.common.service.impl.ImageSDCardCache;

/**
 * ImageSDCardCacheDao
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-10-21
 */
public interface ImageSDCardCacheDao {

    /**
     * put all rows in db whose tag is same to tag to imageSDCardCache
     * <ul>
     * <strong>Attentions:</strong>
     * <li>If imageSDCardCache is null, do nothing</li>
     * <li>If tag is null or empty, do nothing</li>
     * </ul>
     * 
     * @param imageSDCardCache
     * @param tag tag used to mark this cache when save to and load from db, should be unique and cannot be null or
     *        empty
     * @return
     */
    public boolean putIntoImageSDCardCache(ImageSDCardCache imageSDCardCache, String tag);

    /**
     * delete all rows in db whose tag is same to tag at first, and insert all data in imageSDCardCache to db
     * <ul>
     * <strong>Attentions:</strong>
     * <li>If imageSDCardCache is null, do nothing</li>
     * <li>If tag is null or empty, do nothing</li>
     * <li>Will delete all rows in db whose tag is same to tag at first</li>
     * </ul>
     * 
     * @param imageSDCardCache
     * @return
     */
    public boolean deleteAndInsertImageSDCardCache(ImageSDCardCache imageSDCardCache, String tag);
}
