# UMS Interface
This program allows you to chose a block device or disk image as a 'disk',and communicate with PC through usb mass storage (just as usb disk do).Root permission is needed.

 * [download latest version](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/update/app-release.apk)

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p1.png)

## introduction
<h4 style="color:#0F0">quick start</h4>
<b style="color:#009;">usb connection:</b>
set current data source as U disk<br/>
<b style="color:#009;">mount to:</b>
mount current data source<br/>
<b style="color:#009;">quick create:</b>
create a image with the given size,format as vfat,mount it,and use it as U disk<br/><br/>
<b style="color:#900;">
    *If the file you just copied can not be displayed immediately, try click 'close'/'launch' and 'umount'/'mount' continuously.</b><br/>
<h4 style="color:#0F0">U disk</h4>
<b style="color:#009;">block device:</b>
Representing a disk or partition, usually located in /dev/block or /dev.<br/>
<b style="color:#009;">image file:</b>
 Usually named as *.img .An image file with a file system can be mounted like normal disk.<br/>
<b style="color:#009;">data source:</b>
source of the data in U disk,an image file or a block device is available .<br/>
<b style="color:#009;">config path:</b>
path this software relied.Like  /sys/class/android_usb/android0 or  /sys/devices/virtual/android_usb/android0.<br/>
<b style="color:#009;">readonly:</b>may have no effect for some phone
<br/>
<h4 style="color:#0F0">mount</h4>
<b style="color:#009;">data source:</b>
source of the data to mount,an image file or a block device is available .<br/>
<b style="color:#009;">mount point:</b>
chose a directory to mount.<br/>
<b style="color:#009;">file system:</b>
will be automatically chose if not specified.<br/>
<b style="color:#009;">mask:</b>
used to set dmask and fmask.<br/><br/>
<b style="color:#900;">
    *You must disable 'mount namespace separation' in SuperSu, or it won't work.</b><br/>
<h4 style="color:#0F0">create image</h4>
<b style="color:#009;">format:</b>
create file system for image file.<br/><br/>
It can be used as a boot disk for PC when select a solved image.<br/>

## screen shots
![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p2.png)

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p3.png)

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p4.png)

![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/p5.png)

## see more
 * [android6.0.1 usb mass storage](http://blog.csdn.net/outofmemo/article/details/53348552)
 * [coolapk](http://www.coolapk.com/apk/com.sjj.echo.umsinterface)
 * [boot disk image(PE),password: er8y](http://pan.baidu.com/s/1gfa9GbD)

## 简介
<h4 style="color:#0F0">快捷启动</h4>
<b style="color:#009;">U盘连接:</b>
将当前数据源作为U盘,点击'终止'取消 <br/>
<b style="color:#009;">挂载到:</b>
自动挂载当前数据源,点击'终止'取消<br/>
<b style="color:#009;">一键启动:</b>
点击创建,创建指定大小镜像文件,格式化vfat,并将其挂载,同时作为U盘使用<br/><br/>
<b style="color:#900;">  
    *如果刚复制的文件无法立即显示,尝试连续点击'关闭'/'启动','卸载'/'挂载'.</b><br/>
<h4 style="color:#0F0">U盘</h4>
<b style="color:#009;">块设备:</b>
代表一个存储器或分区,通常位于/dev/block 或 /dev.<br/>
<b style="color:#009;">镜像文件:</b>
通常命名为*.img .一个内含文件系统的镜像文件可被挂载,和普通存储器一样使用.<br/>
<b style="color:#009;">数据源:</b>
可选任意镜像文件或块设备.若选则块设备,请谨慎操作,以免数据丢失.<br/>
<b style="color:#009;">配置目录:</b>
通常会自动寻找,可点击"选择"以手动指定.多数手机上此目录为  /sys/class/android_usb/android0 或  /sys/devices/virtual/android_usb/android0.<br/>
<b style="color:#009;">只读:</b>
U盘只读,很多手机上可能无效<br/><br/>
<b style="color:#900;">
    *由于内核原因,使用三星芯片的一些手机使用此功能会被作为ＣＤ　ＲＯＭ</b><br/>
<h4 style="color:#0F0">挂载</h4>
<b style="color:#009;">数据源:</b>
可以指定内含文件系统的镜像文件或块设备.<br/>
<b style="color:#009;">挂载点:</b>
指定一个目录,"磁盘"中的文件会被放到这里.<br/>
<b style="color:#009;">文件系统:</b>
忽略,会自动判断.<br/>
<b style="color:#009;">权限掩码:</b>
用于挂载vfat时指定文件访问权限.<br/><br/>
<b style="color:#900;"> 
    *SuperSU 中的"挂载空间分离"可能会影响本功能,可尝试在SuperSu中取消.</b><br/>
<h4 style="color:#0F0">创建镜像</h4>
<b style="color:#009;">格式化:</b>
为文件建立文件系统,以便立即挂载.<br/>
<b style="color:#900;"></b>
可将启动盘的磁盘镜像作为U盘的数据源,这样便可方便的将手机作为电脑启动盘.<br/>
 
## 指南
 * [安卓4.4以上版本将手机作为U盘使用](http://jingyan.baidu.com/article/a3f121e4be8e7ffc9052bb19.html)
