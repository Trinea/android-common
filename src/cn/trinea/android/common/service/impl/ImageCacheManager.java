package cn.trinea.android.common.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import cn.trinea.android.common.entity.CacheObject;
import cn.trinea.android.common.service.impl.ImageCache.OnImageCallbackListener;
import cn.trinea.android.common.service.impl.PreloadDataCache.OnGetDataListener;
import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ImageUtils;
import cn.trinea.android.common.util.StringUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

/**
 * @author maxiaohui hackooo@sina.cn
 * 
 *         图片缓存管理，有两级缓存，第一级为
 *         本地图片文件与内存的缓存，第二层结合ImageSDCardCacheManager，为网络与本地文件之间的缓存。
 *         使用方法：导入这个类，或者直接作为内部类，获取图片的时候跟使用ImageCache一样，例如： ImageCacheManager
 *         imgCacheManager = new ImageCacheManager(context);
 * 
 *         参数的设置与原来的ImageCache和ImageSDCardCache一致(可以删除代码中的一些默认的设置) ImageCache
 *         IMAGE_CACHE = imgCacheManager。getImgCache();
 * 
 *         IMAGE_CACHE.setCacheFullRemoveType(new
 *         RemoveTypeLastUsedTimeFirst<Drawable>());
 *         IMAGE_CACHE.setHttpReadTimeOut(10000);
 *         IMAGE_CACHE.setOpenWaitingQueue(true); IMAGE_CACHE.setValidTime(-1);
 *         IMAGE_CACHE.setOnImageCallbackListener(new OnImageCallbackListener(){ 
 *           private static final long serialVersionUID = 1L;
 *           @Override 
 *           public void onImageLoaded(String imageUrl, Drawable imageDrawable,View view, boolean isInCache) 
 *           { 
 *             if (view != null && imageDrawable!= null) { 
 *               ImageView imageView = (ImageView)view;
 *               imageView.setImageDrawable(imageDrawable); 
 *               if (!isInCache) {
 *                 imageView.startAnimation(getInAlphaAnimation(2000)); 
 *               } 
 *             }
 *            } 
 *          });
 *           。。。。。。 
 *          ImageSDCardCache IMAGE_SD_CACHE = imgCacheManager.getImgSDCardCache();
 *          IMAGE_SD_CACHE.setCacheFullRemoveType(new RemoveTypeLastUsedTimeFirst<String>());
 *          IMAGE_SD_CACHE.setCacheFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TrineaAndroidCommon");
 *          IMAGE_SD_CACHE.setFileNameRule(new FileNameRuleImageUrl());
 *          IMAGE_SD_CACHE.setHttpReadTimeOut(10000);
 *          IMAGE_SD_CACHE.setOpenWaitingQueue(true);
 *          IMAGE_SD_CACHE.setValidTime(-1); 
 *          。。。。。。 获取图片的时候
 *          IMAGE_CACHE.get(String url,View view);
 */
public class ImageCacheManager {
	private final ImageCache IMAGE_CACHE = new ImageCache(128);
	private Context context;
	private String TAG = "ImageCacheManager";

	private ImageSDCardCacheManager imgSDCardCacheManager;

	public ImageCacheManager(Context ctx) {
		context = ctx;
		imgSDCardCacheManager = new ImageSDCardCacheManager();
		IMAGE_CACHE.setOnGetDataListener(new OnGetDataListener<String, Drawable>() {
					private static final long serialVersionUID = 1L;

					@Override
					public CacheObject<Drawable> onGetData(String key) {
						Drawable d = null;
						try {
							String filepath;
							// 1.从SDCardCache的cache队列查询
							CacheObject<String> co = imgSDCardCacheManager.getImgCache().get(key);
							if (null != co) {
								filepath = co.getData();
							} else {
								// 2.调用SDCardCache的onGetData(),该步骤分两步：1.查找本地文件；2网络获取
								imgSDCardCacheManager.getImgCache().remove(key);
								CacheObject<String> cs = imgSDCardCacheManager
										.getImgCache().getOnGetDataListener()
										.onGetData(key);
								filepath = cs != null ? cs.getData() : "";
							}
							if (!StringUtils.isEmpty(filepath) && FileUtils.isFileExist(filepath)) {
								Bitmap bm = BitmapFactory.decodeFile(filepath,null);
								d = new BitmapDrawable(context.getResources(),bm);
							}
						} catch (Exception e) {
							Log.e(TAG, "get drawable exception, imageUrl is:" + key, e);
						}
						return (d == null ? null : new CacheObject<Drawable>(d));
					}
				});
	}

	public AlphaAnimation getInAlphaAnimation(long durationMillis) {
		AlphaAnimation inAlphaAnimation = new AlphaAnimation(0, 1);
		inAlphaAnimation.setDuration(durationMillis);
		return inAlphaAnimation;
	}

	public ImageCache getImgCache() {
		return IMAGE_CACHE;
	}

	public ImageSDCardCache getImgSDCardCache() {
		return imgSDCardCacheManager.getImgCache();
	}

	/**
	 * 图片缓存管理，这一层只负责网络和本地图片之间的缓存
	 * 
	 * @author maxiaohui hackooo@sina.cn
	 * 
	 */
	private class ImageSDCardCacheManager {
		public final ImageSDCardCache IMAGE_SD_CACHE = new ImageSDCardCache();

		public ImageSDCardCacheManager() {

			IMAGE_SD_CACHE.setOnGetDataListener(new OnGetDataListener<String, String>() {
						private static final long serialVersionUID = 1L;

						@Override
						public CacheObject<String> onGetData(String key) {
							// 1.先从本地文件查找；2.从网络获取
							String imgPath = IMAGE_SD_CACHE.getCacheFolder()+ File.separator+ IMAGE_SD_CACHE.getFileNameRule().getFileName(key);
							File img = new File(imgPath);
							if (img.exists()) {
								return new CacheObject<String>(imgPath);
							} else {
								return IMAGE_SD_CACHE.getDefaultOnGetImageListener().onGetData(key);
							}
						}
					});
		}

		public ImageSDCardCache getImgCache() {
			return IMAGE_SD_CACHE;
		}
	}
}
