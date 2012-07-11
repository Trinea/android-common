package com.trinea.android.common.serviceImpl;

import android.graphics.drawable.Drawable;

import com.trinea.android.common.util.ImageUtils;
import com.trinea.java.common.entity.CacheObject;
import com.trinea.java.common.service.CacheFullRemoveType;

/**
 * 缓存满时删除数据的类型--Drawable小先删除；若Drawable大小相同，对象使用次数(即被get的次数)少先删除；若对象使用次数相同，对象进入缓存时间早先删除
 * 
 * @author Trinea 2012-6-30 下午11:30:01
 */
public class RemoveTypeDrawableSmall implements CacheFullRemoveType<Drawable> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<Drawable> obj1, CacheObject<Drawable> obj2) {
        long sizeOfFile1 = getSize(obj1);
        long sizeOfFile2 = getSize(obj2);
        if (sizeOfFile1 == sizeOfFile2) {
            if (obj1.getUsedCount() == obj2.getUsedCount()) {
                return (obj1.getEnterTime() > obj2.getEnterTime()) ? 1 : ((obj1.getEnterTime() == obj2.getEnterTime()) ? 0 : -1);
            }
            return (obj1.getUsedCount() > obj2.getUsedCount() ? 1 : -1);
        }
        return (sizeOfFile1 > sizeOfFile2 ? 1 : -1);
    }

    /**
     * 得到CacheObject中Drawable的大小
     * 
     * @param o
     * @return
     */
    private long getSize(CacheObject<Drawable> o) {
        if (o == null) {
            return -1;
        }

        byte[] b = ImageUtils.drawableToByte(o.getData());
        return (b == null ? -1 : b.length);
    }
}
