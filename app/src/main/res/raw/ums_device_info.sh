#!/system/bin/sh

echo "" > /sdcard/ums_device_info.log
BUSYBOX='/data/data/com.sjj.echo.umsinterface/bin/busybox '
TARGET=/data/data/com.sjj.echo.umsinterface/ums_device_info.log
log_exec(){
	echo '>>>'$1 >> $TARGET
	$($1 >>$TARGET 2>>$TARGET)
}


log_exec "$BUSYBOX ls -al /sys/devices/virtual/android_usb/android*/"
log_exec "$BUSYBOX ls -al /sys/devices/virtual/android_usb/android*/f_mass_storage/"
log_exec "$BUSYBOX ls -al /sys/devices/virtual/android_usb/android*/f_mass_storage/lun*/"
log_exec "$BUSYBOX ls -al /sys/class/android_usb/android*/"
log_exec "$BUSYBOX ls -al /sys/class/android_usb/android*/f_mass_storage/"
log_exec "$BUSYBOX ls -al /sys/class/android_usb/android*/f_mass_storage/lun*/"
log_exec "echo '~~~~search lun ~~~~~~~~~~~~~~~~~~~~'"
log_exec "echo $($BUSYBOX du -ah /sys|$BUSYBOX grep '/lun')"
log_exec "$BUSYBOX ls -al $($BUSYBOX du /sys|$BUSYBOX grep '/lun' 2>/dev/null|$BUSYBOX cut -c 3- )"
log_exec "echo '~~~~search android_usb ~~~~~~~~~~~~~~~~~~~~'"
log_exec "echo $($BUSYBOX du -ah /sys|$BUSYBOX grep 'android_usb')"
log_exec "$BUSYBOX ls -al $($BUSYBOX du /sys|$BUSYBOX grep 'android_usb' 2>/dev/null|$BUSYBOX cut -c 3- )"
log_exec "echo '~~~~search f_mass_storage ~~~~~~~~~~~~~~~~~~~~'"
log_exec "echo $($BUSYBOX du -ah /sys|$BUSYBOX grep 'f_mass_storage')"
log_exec "$BUSYBOX ls -al $($BUSYBOX du /sys|$BUSYBOX grep 'f_mass_storage' 2>/dev/null|$BUSYBOX cut -c 3- )"
log_exec "echo '~~~~sys file tree ~~~~~~~~~~~~~~~~~~~~'"
log_exec "$BUSYBOX du -ah /sys"

