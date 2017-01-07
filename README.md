#UMS Interface
## screen shots

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p1.png)

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p2.png)

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p3.png)

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p4.png)
##introduction
 * Root permission is needed,or ANR when launch , if you want to use mount function, busybox is needed too.
 * This program allows you to chose a block device or disk image as a 'disk',and communicate with PC through usb mass storage (just as usb disk do).
 * Since the configuration of android phones are different, you should select the correct config path firstly,you can simply press 'SEARCH' button to find the path if available ,or specify it manually.
 * There are some special usages. For example, you can select the whole emmc '/dev/block/mmcblk0' and mount it to pc linux, so that you can some tool like gparted to do something like partition.And you can also use it as boot disk.

##简介
 * 必须确保有root权限,否则将在启动时无响应,如果使用mount功能的话还需要busybox.
 * 本程序用来配置usb mass storage(U 盘与PC通讯的方式,大容量存储),可以选择一个块设备或磁盘镜像文件来作为'磁盘'.
 * 由于安卓手机配置不同,必须先设置配置目录,如果可以的话点击search即可(启动时也会自动寻找),否者可能暂不支持你的设备,或自己手动选择.
 * 也可以有一些特殊用法如选择整块emmc(/dev/block/mmcblk0)并挂到linux的pc上,就可以使用gparted等工具进行分区等操作,也可以作为启动盘.
 
##see more
 * [android6.0.1 usb mass storage](http://blog.csdn.net/outofmemo/article/details/53348552)
 * [coolapk](http://www.coolapk.com/apk/com.sjj.echo.umsinterface)
 * [boot disk image(PE),password: er8y](http://pan.baidu.com/s/1gfa9GbD)
 
##last version
 * 1.1.2 [download apk](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/update/app-release.apk)