package com.trinea.android.common.serviceImpl;

import com.trinea.java.common.FileUtils;
import com.trinea.java.common.entity.CacheObject;
import com.trinea.java.common.service.CacheFullRemoveType;

/**
 * 缓存满时删除数据的类型--文件大先删除；若文件大小相同，对象使用次数(即被get的次数)少先删除；若对象使用次数相同，对象进入缓存时间早先删除
 * 
 * @author Trinea 2012-6-30 下午11:30:01
 */
public class RemoveTypeFileLarge implements CacheFullRemoveType<String> {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CacheObject<String> obj1, CacheObject<String> obj2) {
        long sizeOfFile1 = (obj1 == null ? -1 : FileUtils.getFileSize(obj1.getData()));
        long sizeOfFile2 = (obj2 == null ? -1 : FileUtils.getFileSize(obj2.getData()));
        if (sizeOfFile1 == sizeOfFile2) {
            if (obj1.getUsedCount() == obj2.getUsedCount()) {
                return (obj1.getEnterTime() > obj2.getEnterTime()) ? 1 : ((obj1.getEnterTime() == obj2.getEnterTime()) ? 0 : -1);
            }
            return (obj1.getUsedCount() > obj2.getUsedCount() ? 1 : -1);
        }
        return (sizeOfFile2 > sizeOfFile1 ? 1 : -1);
    }
}
