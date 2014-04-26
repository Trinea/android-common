package cn.trinea.android.common.service.impl;

import android.graphics.Bitmap;
import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.service.CacheFullRemoveType;
import cn.trinea.android.common.util.ImageUtils;

/**
 * Remove type when cache is full, data type of cache is bitmap.<br/>
 * <ul>
 * <li>if bitmap is bigger, remove it first</li>
 * <li>if bitmap is equal to each other, remove the one which is used less</li>
 * <li>if bitmap is equal to each other and used count is equal, remove the one which is first in</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-12-26
 */
public class RemoveTypeBitmapLarge implements CacheFullRemoveType<Bitmap> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<Bitmap> obj1, CacheObject<Bitmap> obj2) {
        long sizeOfFile1 = getSize(obj1);
        long sizeOfFile2 = getSize(obj2);
        if (sizeOfFile1 == sizeOfFile2) {
            if (obj1.getUsedCount() == obj2.getUsedCount()) {
                return (obj1.getEnterTime() > obj2.getEnterTime()) ? 1
                        : ((obj1.getEnterTime() == obj2.getEnterTime()) ? 0 : -1);
            }
            return (obj1.getUsedCount() > obj2.getUsedCount() ? 1 : -1);
        }
        return (sizeOfFile2 > sizeOfFile1 ? 1 : -1);
    }

    /**
     * get size of bitmap
     * 
     * @param o
     * @return
     */
    private long getSize(CacheObject<Bitmap> o) {
        if (o == null) {
            return -1;
        }

        // TODO is there any more efficient way?
        byte[] b = ImageUtils.bitmapToByte(o.getData());
        return (b == null ? -1 : b.length);
    }
}
