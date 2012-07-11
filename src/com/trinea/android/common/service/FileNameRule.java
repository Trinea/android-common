package com.trinea.android.common.service;

import java.io.Serializable;

import com.trinea.android.common.cache.ImageSDCardCache;

/**
 * 文件名规则，用于{@link ImageSDCardCache}保存图片时使用
 * 
 * @author Trinea 2012-7-6 上午11:03:16
 */
public interface FileNameRule extends Serializable {

    /**
     * 得到文件名，包含后缀。可包含文件夹目录结构。
     * 
     * @param feature
     * @return
     */
    public String getFileName(Object feature);
}
