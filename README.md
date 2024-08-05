[English Description](#english-description)  |  [中文描述](#中文描述)  |  [APP Description](https://github.com/outofmemo/UMS-Interface/blob/master/README-app.md)



## English Description

### Use case:

* Transfer file between PC and Android without `MTP`. Mass Storage is more compatible than `MTP`.
* Boot from an ISO or PE image for PC system maintenance without burning a disk.


### How it works:

This script create a virtual usb flash disk with an image, and mount the image on Android. So that you can access the files in the image on both Android and PC.

---

**The UMSInterface APP is no longer maintained. If you still want to use the APP, refer to the [APP Description](https://github.com/outofmemo/UMS-Interface/blob/master/README-app.md).**



As an alternative, a shell script is provided to realize roughly the same function. This method requires users to have a certain understanding of linux and shell.

---

Alternative method:

1. This script is based on `Termux` environment, please install `Termux` APP first.

   In order to run the script conveniently, it is recommended to install [Termux:Widget](https://wiki.termux.com/wiki/Termux:Widget).

   In order to show toast messages, [Termux:API](https://wiki.termux.com/wiki/Termux:API) is needed.

   For new `Termux` which has no `mount` command, install `mount-utils` additionally in `Termux` by `pkg install mount-utils`.

2. Copy [mass_storage.sh](https://github.com/outofmemo/UMS-Interface/blob/master/mass_storage.sh) to `/data/data/com.termux/files/home/.shortcuts/`.

   Some of the parameters can be modified when needed. For example:

   https://github.com/outofmemo/UMS-Interface/blob/87ce4fe5ae81baaf5a846c680f3fcff3fed9292e/mass_storage.sh#L17

   * `default_size_mb`: The size(MB) of the image created by default.
   * `dst`: Mount point path. Leave blank to not mount.
   * `src`: Image or block device path.
     * You can specify a path that does not exist, and an empty image will be created automatically.
     * You can specify a block device path. **DO NOT DO IT, if you have no idea what it means.**
     * You can specify an existing image. For example, you can specify an iso image or a PE disk image for PC system maintenance. You can also specify a disk image copied with `dd` command.

3. Add executable permissions to `mass_storage.sh`:

   ```bash
   chmod +x /data/data/com.termux/files/home/.shortcuts/mass_storage.sh
   ```

4. If `Termux: Widget` is installed, you can add the widget to desktop, so that you can execute the script by clicking the widget.

   ![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/widget.png)

   Or you can run `adb shell am start-activity -n com.termux.widget/.TermuxCreateShortcutActivity` to create a shortcut for the script.

   If `Termux: Widget` is not installed, you can execute the script in `Termux`: `bash /data/data/com.termux/files/home/.shortcuts/mass_storage.sh`.

5. If you want to run this script automatically at boot:

   1. Install [Termux:Boot](https://wiki.termux.com/wiki/Termux:Boot).

   2. Grant `Termux:Boot` self-start permission in the application settings.

   3. Copy `mass_storage.sh` to `/data/data/com.termux/files/home/.termux/boot`, and add executable permissions.

      ```bash
      cp /data/data/com.termux/files/home/.shortcuts/mass_storage.sh /data/data/com.termux/files/home/.termux/boot
      chmod +x /data/data/com.termux/files/home/.termux/boot/mass_storage.sh
      ```



Notice:

* After PC writes a file to the USB Driver, Android cannot perceive the file system changes. You can refresh by executing `mass_storage.sh` again.
* After Android writes a file to the image or block device, PC cannot perceive the change. You can refresh it by plugging and unplugging the USB or executing `mass_storage.sh` again.
* **Do not write to the file system at the same time on PC and Android** (such as: file movement, copy, rename, create, delete, write), otherwise the file system will be damaged and the file will be lost.
* **Don't store important files** in the image or block device used by this script without backup.

---
---

## 中文描述

基本功能: 使用 `usb gadget` 驱动使指定的镜像或块设备可作为 USB Drive 被PC访问; 同时将此镜像或块设备挂载到Android本地.



使用场景:

* 摆脱`MTP`, 实现PC与Android的文件传输, Mass Storage 比 MTP 兼容性更好.
* 使用已有的iso或pe镜像, 用于PC系统维护, 无需刻盘, 替代PC启动盘.



**UMSInterface APP后面不再维护. 如果仍然想使用此APP, 可参考 [APP Description](https://github.com/outofmemo/UMS-Interface/blob/master/README-app.md).**



作为替代, 下面提供一个基于`Termux`环境的shell脚本示例, 来完成大致相同的功能. 此方法需要使用者对linux和shell有一定的了解.



替代方法如下:

1. 此shell脚本运行环境基于`Termux`, 请先安装 [Termux](https://termux.com/).

   同时, 为了方便一键操作, 建议安装 [Termux:Widget](https://wiki.termux.com/wiki/Termux:Widget).

   为了方便显示toast消息, 建议安装 [Termux:API](https://wiki.termux.com/wiki/Termux:API).

   较新版本的软件包未自带 `mount` 命令, 需要在 `Termux` 中额外安装 `mount-utils`: `pkg install mount-utils`.

2. 将 [mass_storage.sh](https://github.com/outofmemo/UMS-Interface/blob/master/mass_storage.sh) 拷贝至 `/data/data/com.termux/files/home/.shortcuts/`.

    其中的部分参数可根据实际情况进行修改. 如:

    https://github.com/outofmemo/UMS-Interface/blob/87ce4fe5ae81baaf5a846c680f3fcff3fed9292e/mass_storage.sh#L17

    * `default_size_mb`: 默认创建的镜像大小, 单位: MB.
    * `dst`: 挂载点路径. 留空则不挂载.
    * `src`: 镜像文件或块设备路径. 
      * 可以指定一个不存在的路径, 此时会自动创建一个空的镜像. 
      * 可以指定一个块设备路径. 但要谨慎这么做, 否则**操作失误可能会导致手机无法启动**.
      * 可以指定一个已存在的镜像. 比如可以指定iso镜像 或 pe磁盘镜像用于PC的系统维护. 或者也可以指定一个使用`dd`命令拷贝的磁盘镜像.

3. 为`mass_storage.sh`添加可执行权限:

    ```bash
    chmod +x /data/data/com.termux/files/home/.shortcuts/mass_storage.sh
    ```

4. 如果有安装 `Termux:Widget`, 则可在桌面添加相应的小部件, 点击小部件上的 `mass_storage.sh` 即可执行此脚本.

    ![](https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/screenshots/widget.png)

    或执行 `adb shell am start-activity -n com.termux.widget/.TermuxCreateShortcutActivity` 来创建脚本的桌面快捷方式.

    如果没有安装, 可直接在 `Termux` 中执行此脚本.

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
* **PC 和 Android 端不要同时对文件系统进行写操作**(如: 文件的移动, 复制, 重命名, 创建, 删除, 写入), 否则会损坏文件系统, 导致文件丢失.
* **不要在没有副本的情况下, 把重要文件存放到上述脚本指定的镜像或块设备中**.

