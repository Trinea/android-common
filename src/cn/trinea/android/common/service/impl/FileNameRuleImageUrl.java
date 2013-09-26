package cn.trinea.android.common.service.impl;

import cn.trinea.android.common.service.FileNameRule;
import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.StringUtils;

/**
 * File name rule, used when saving images in {@link ImageSDCardCache}
 * <ul>
 * <li>use image url as file name, replace char with _ if not letter or number</li>
 * <li>use file suffix in url as target file suffix</li>
 * </ul>
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-11-21
 */
public class FileNameRuleImageUrl implements FileNameRule {

    private static final long  serialVersionUID     = 1L;

    /** default file name if image url is empty **/
    public static final String DEFAULT_FILE_NAME    = "ImageSDCardCacheFile.jpg";
    /** max length of file name, not include suffix **/
    public static final int    MAX_FILE_NAME_LENGTH = 127;

    @Override
    public String getFileName(String imageUrl) {
        if (StringUtils.isEmpty(imageUrl)) {
            return DEFAULT_FILE_NAME;
        }

        String ext = FileUtils.getFileExtension(imageUrl);
        String fileName = (imageUrl.length() >= MAX_FILE_NAME_LENGTH
            ? imageUrl.substring(imageUrl.length() - MAX_FILE_NAME_LENGTH, imageUrl.length()) : imageUrl).replaceAll("[\\W]",
                                                                                                                     "_");
        return StringUtils.isEmpty(ext) ? fileName
            : (new StringBuilder().append(fileName).append(".").append(ext).toString());
    }
}
