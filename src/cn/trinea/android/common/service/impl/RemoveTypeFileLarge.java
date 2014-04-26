package cn.trinea.android.common.service.impl;

import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.service.CacheFullRemoveType;
import cn.trinea.android.common.util.FileUtils;

/**
 * Remove type when cache is full, data type of cache is string, and it represents the path of a file.<br/>
 * <ul>
 * <li>if file is larger, remove it first</li>
 * <li>if file is equal to each other, remove the one which is used less</li>
 * <li>if file is equal to each other and used count is equal, remove the one which is first in</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-12-26
 */
public class RemoveTypeFileLarge implements CacheFullRemoveType<String> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<String> obj1, CacheObject<String> obj2) {
        long sizeOfFile1 = (obj1 == null ? -1 : FileUtils.getFileSize(obj1.getData()));
        long sizeOfFile2 = (obj2 == null ? -1 : FileUtils.getFileSize(obj2.getData()));
        if (sizeOfFile1 == sizeOfFile2) {
            if (obj1.getUsedCount() == obj2.getUsedCount()) {
                return (obj1.getEnterTime() > obj2.getEnterTime()) ? 1
                        : ((obj1.getEnterTime() == obj2.getEnterTime()) ? 0 : -1);
            }
            return (obj1.getUsedCount() > obj2.getUsedCount() ? 1 : -1);
        }
        return (sizeOfFile2 > sizeOfFile1 ? 1 : -1);
    }
}
