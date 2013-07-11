个人博客  [http://www.trinea.cn/](http://www.trinea.cn/)
=============
本工程为总结的一些android公共库，包含公共的View、缓存以及一些工具类，具体使用可见[http://trinea.iteye.com/blog/1564055](总结的一些android公共库)。Demo地址见[https://code.google.com/p/trinea-android-demo/](AndroidDemo)
主要包括
一. 公用的view
-------------
###1. 下拉刷新及滚动到底部加载更多的Listview([https://code.google.com/p/trinea-android-common/source/browse/trunk/src/cn/trinea/android/common/view/DropDownListView.java DropDownListView])
使用: [http://www.trinea.cn/android/滚动到底部加载更多及下拉刷新listview的使用 滚动到底部加载更多及下拉刷新listview的使用], 实现原理: [http://trinea.iteye.com/blog/1562281]

###2. 滑动一页(一个Item)的Gallery([https://code.google.com/p/trinea-android-common/source/browse/trunk/src/cn/trinea/android/common/view/SlideOnePageGallery.java SlideOnePageGallery])
使用及实现原理: [http://www.trinea.cn/android/gallery%E6%BB%91%E5%8A%A8%E4%B8%80%E9%A1%B5%E4%B8%80%E4%B8%AAitem%E6%95%88%E6%9E%9C/ Gallery滑动一页(一个Item)效果]

###3. 滑动到底部或顶部响应的ScrollView([https://code.google.com/p/trinea-android-common/source/browse/trunk/src/cn/trinea/android/common/view/BorderScrollView.java  BorderScrollView])
使用: [http://www.trinea.cn/android/%E6%BB%9A%E5%8A%A8%E5%88%B0%E5%BA%95%E9%83%A8%E6%88%96%E9%A1%B6%E9%83%A8%E5%93%8D%E5%BA%94%E7%9A%84scrollview%E4%BD%BF%E7%94%A8/ 滚动到底部或顶部响应的ScrollView使用], 实现原理: [http://www.trinea.cn/android/%E6%BB%91%E5%8A%A8%E5%88%B0%E5%BA%95%E9%83%A8%E6%88%96%E9%A1%B6%E9%83%A8%E5%93%8D%E5%BA%94%E7%9A%84scrollview/ 滑动到底部或顶部响应的ScrollView实现]

二. 缓存类
-------------
主要特性：使用简单、可自动获取新数据、可序列化、可从文件中恢复、多种缓存清除方式、包含map的大多数接口等。
###1. 图片内存缓存
使用见：[http://trinea.iteye.com/blog/1564533 图片内存缓存的使用], 适用：应用中获取图片较多且图片不大的应用，在微博、花瓣、美丽说、path这类应用中可以起到很好的效果。

###2. 图片SD卡缓存
使用见：[http://trinea.iteye.com/blog/1595423 图片SD卡缓存的使用], 适用：应用中获取图片较多且图片较大的情况，在微博、花瓣、美丽说、path这类应用中可以起到很好的效果。


三. 工具类
-------------
###1. 图片工具类
(1)Drawable、Bitmap、byte数组相互转换; (2)根据url获得InputStream、Drawable、Bitmap
