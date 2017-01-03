#UMS Interface
## screen shots

![](http://upload-images.jianshu.io/upload_images/4238248-f8b9143a9901d9ee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](http://upload-images.jianshu.io/upload_images/4238248-5c5d90c37fed3572.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](http://upload-images.jianshu.io/upload_images/4238248-852d720ef4fe6afd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](http://upload-images.jianshu.io/upload_images/4238248-a12d3fc5f71303d2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
##introduction
 * Root permission is needed,or ANR when launch , if you want to use mount function, busybox is needed too.
 * This program allows you to chose a block device or disk image as a 'disk',and communicate with PC through usb mass storage (just as usb disk do).
 * Since the configuration of android phones are different, you should select the correct config path firstly,you can simply press 'SEARCH' button to find the path if available ,or specify it manually.
 * There are some special usages. For example, you can select the whole emmc '/dev/block/mmcblk0' and mount it to pc linux, so that you can some tool like gparted to do something like partition.

##简介
 * 必须确保有root权限,否则将在启动时无响应,如果使用mount功能的话还需要busybox.
 * 本程序用来配置usb mass storage(U 盘与PC通讯的方式,大容量存储),可以选择一个块设备或磁盘镜像文件来作为'磁盘'.
 * 由于安卓手机配置不同,必须先设置配置目录,如果可以的话点击search即可(启动时也会自动寻找),否者可能暂不支持你的设备,或自己手动选择.
 * 也可以有一些特殊用法如选择整块emmc(/dev/block/mmcblk0)并挂到linux的pc上,就可以使用gparted等工具进行分区等操作."
 
##see more
 * [android6.0.1 usb mass storage](http://blog.csdn.net/outofmemo/article/details/53348552)