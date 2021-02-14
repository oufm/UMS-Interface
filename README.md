[English Description](#english-description)  |  [中文描述](#中文描述)  |  [APP Description](https://github.com/outofmemo/UMS-Interface/blob/master/README-app.md)



## English Description



## 中文描述

基本功能: 使用 `usb gadget` 驱动使指定的镜像或块设备可作为 USB Drive 被PC访问; 同时将此镜像或块设备挂载到Android本地.

使用场景:

* 摆脱`MTP`, 实现PC与Android的文件传输, Mass Storage 比 MTP 兼容性更好.
* 替代实体U盘, 携带手机后, 无需再带U盘.
* 使用已有的iso或pe镜像, 用于PC系统维护, 无需刻盘, 替代PC启动盘.



UMSInterface APP后面不再维护. 如果仍然想使用此APP, 可参考 [APP Description](https://github.com/outofmemo/UMS-Interface/blob/master/README-app.md).



作为替代, 下面提供一个基于`Termux`环境的shell脚本示例, 来完成大致相同的功能. 此方法需要使用者对linux和shell有一定的了解. 同时, 挂载/卸载分区, 以及对块设备的操作本来就是高危操作, 如果使用者对linux环境不熟悉, 可能会时手机变砖, 这也是APP不再维护的原因之一.



替代方法如下:

1. 此shell脚本运行环境基于`Termux`, 请先安装 [Termux](https://termux.com/).

   同时, 为了方便一键操作, 建议安装 [Termux:Widget](https://wiki.termux.com/wiki/Termux:Widget);

   为了方便显示toast消息, 建议安装 [Termux:API](https://wiki.termux.com/wiki/Termux:API).

2. 将 [mass_storage.sh](https://github.com/outofmemo/UMS-Interface/blob/master/mass_storage.sh) 拷贝至 `/data/data/com.termux/files/home/.shortcuts/`.

    其中的部分参数可根据实际情况进行修改. 如:

    https://github.com/outofmemo/UMS-Interface/blob/87ce4fe5ae81baaf5a846c680f3fcff3fed9292e/mass_storage.sh#L17

    * `default_size_mb`: 默认创建的镜像大小, 单位: MB.
    * `dst`: 挂载点路径. 留空则不挂载.
    * `src`: 镜像文件或块设备路径. 
      * 可以指定一个不存在的路径, 此时会自动创建一个空的镜像. 
      * 可以指定一个块设备路径. 但要谨慎这么做, 否则操作失误可能会导致手机无法启动.
      * 可以指定一个已存在的镜像. 比如可以指定iso镜像 或 pe磁盘镜像用于PC的系统维护. 或者也可以指定一个使用`dd`命令拷贝的磁盘镜像.

3. 为`mass_storage.sh`添加可执行权限:

    ```bash
    chmod +x /data/data/com.termux/files/home/.shortcuts/mass_storage.sh
    ```

4. 如果有安装 `Termux:Widget`, 则可在桌面添加相应的小部件, 点击小部件上的 `mass_storage.sh` 即可执行此脚本.

    如果没有安装, 可直接在 `Termux` 中执行此脚本.

    ![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/widget.png)

5. 如果希望开机时自动运行此脚本:

    1. 安装 [Termux:Boot](https://wiki.termux.com/wiki/Termux:Boot).

    2. 应用设置中授予 `Termux:Boot` 自启动权限

    3. 将 `mass_storage.sh` 拷贝至 `/data/data/com.termux/files/home/.termux/boot`, 并添加可执行权限.

        ```bash
        cp /data/data/com.termux/files/home/.shortcuts/mass_storage.sh /data/data/com.termux/files/home/.termux/boot
        chmod +x /data/data/com.termux/files/home/.termux/boot/mass_storage.sh
        ```



注意事项:

* PC 端向 USB Driver 中写入文件后, Android 无法感知文件系统的变化. 可以通过再次执行 `mass_storage.sh` 来刷新.
* 同理, Android 端向镜像或块设备中写入文件后, PC也无法感知文件系统的变化. 可以通过重新插拔USB或再次执行 `mass_storage.sh` 来刷新.
* PC 和 Android 端不要同时对文件系统进行写操作(如: 文件的移动, 复制, 重命名, 创建, 删除, 写入), 否则会损坏文件系统, 导致文件丢失.
* 不要在没有副本的情况下, 把重要文件存放到上述脚本指定的镜像或块设备中.

