package cn.trinea.android.common.service;

import java.io.Serializable;

import cn.trinea.android.common.service.impl.ImageSDCardCache;

/**
 * File name rule, used when saving images in {@link ImageSDCardCache}
 * 
 * @author Trinea 2012-7-6
 */
public interface FileNameRule extends Serializable {

    /**
     * get file name, include suffix, it's optional to include folder.
     * 
     * @param imageUrl the url of image
     * @return
     */
    public String getFileName(String imageUrl);
}
