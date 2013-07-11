个人博客  [http://www.trinea.cn/](http://www.trinea.cn/)
=============
本工程为总结的一些android公共库，包含**缓存**(图片缓存、预取缓存)、**公共View**(下拉及底部加载更多ListView、底部加载更多ScrollView、滑动一页Gallery)、及**工具类**(下载管理、静默安装、shell工具类等等)，具体使用可见[http://trinea.iteye.com/blog/1564055](总结的一些android公共库)。Demo地址见[https://code.google.com/p/trinea-android-demo/](AndroidDemo)
主要包括
####一. 缓存类
(1).使用简单 (2). 轻松获取及预取取新图片(3).可选择多种缓存算法(FIFO、LIFO、LRU、MRU、LFU、MFU等13种)或自定义缓存算法 (4).省流量性能佳(有且仅有一个线程获取图片) (5).支持不同类型网络处理(6).可根据系统配置初始化缓存(7).扩展性强 (8).支持队列(9). 缓存可序列化到本地缓存 可从文件中恢复(10)包含map的大多数接口。
#####1. 图片内存缓存
使用见：[http://www.trinea.cn/?p=704](图片内存缓存的使用), 适用：应用中获取图片较多且图片不大的应用，如新浪微博、twitter、微信头像、美丽说、蘑菇街、花瓣、淘宝等等。

#####2. 图片SD卡缓存
使用见：[http://trinea.iteye.com/blog/1595423](图片SD卡缓存的使用), 适用：应用中获取图片较多且图片较大的情况，在微博、花瓣、美丽说、path这类应用中可以起到很好的效果。

####二. 公用的view
#####1. 下拉刷新及滚动到底部加载更多的Listview
使用: [http://www.trinea.cn/android/滚动到底部加载更多及下拉刷新listview的使用](滚动到底部加载更多及下拉刷新listview的使用), 实现原理: [http://trinea.iteye.com/blog/1562281](http://trinea.iteye.com/blog/1562281)

#####2. 滑动一页(一个Item)的Gallery
使用[http://www.trinea.cn/android/滚动到底部加载更多及下拉刷新listview的使用](滚动到底部加载更多及下拉刷新listview的使用), 实现原理: [http://www.trinea.cn/android/gallery%E6%BB%91%E5%8A%A8%E4%B8%80%E9%A1%B5%E4%B8%80%E4%B8%AAitem%E6%95%88%E6%9E%9C/](Gallery滑动一页(一个Item)效果)

#####3. 滑动到底部或顶部响应的ScrollView
使用: [http://www.trinea.cn/android/%E6%BB%9A%E5%8A%A8%E5%88%B0%E5%BA%95%E9%83%A8%E6%88%96%E9%A1%B6%E9%83%A8%E5%93%8D%E5%BA%94%E7%9A%84scrollview%E4%BD%BF%E7%94%A8/](滚动到底部或顶部响应的ScrollView使用), 实现原理: [http://www.trinea.cn/android/%E6%BB%91%E5%8A%A8%E5%88%B0%E5%BA%95%E9%83%A8%E6%88%96%E9%A1%B6%E9%83%A8%E5%93%8D%E5%BA%94%E7%9A%84scrollview/](滑动到底部或顶部响应的ScrollView实现)


三. 工具类
-------------
#####1. 图片工具类
(1)Drawable、Bitmap、byte数组相互转换; (2)根据url获得InputStream、Drawable、Bitmap
