#!/system/bin/sh
set -x
script_path=`cd $(dirname "$0"); pwd`/`basename "$0"`
log_path=/sdcard/`basename "$0"`.log

termux_bin='/data/data/com.termux/files/usr/bin'
mount="$termux_bin"/mount
umount="$termux_bin"/umount
losetup="$termux_bin"/losetup
find="$termux_bin"/find
truncate="$termux_bin"/truncate
toast="$termux_bin"/termux-toast
mkfs=''

lun_file=''

# Default image size when creating the image.
default_size_mb=2048
# The image or block device path.
# A image will be created if this path doesn't exist.
src='/storage/emulated/0/mass_storage.img'
# The mount point where the image or block device mounted on.
# Leave it blank if you don't want to mount it.
dst='/sdcard/mass_storage'

retry() {
	local times=$1
	local interval=$2
	local i=0
	shift 2

	while [ "$i" -lt "$times" ]; do
		i=$((i+1))
		$@ && return 0
		sleep "$interval"
	done

	return 1
}

mount_fat() {
	$mount -o rw,shortname=mixed,utf8,fmask=0000,dmask=0000 "$block" "$dst" && return 0
	$mount -o rw,fmask=0000,dmask=0000 "$block" "$dst" && return 0
	
	return 1
}

init_env() {
	[ -z "$dst" -o -d "$dst" ] || {
		mkdir -p "$dst" || {
			$toast "make directory($dst) failed"
			exit 1
		}
	}

	[ -e "$src" ] && return 0
	
	$truncate -s "$default_size_mb"M "$src" || {
		$toast "create image($src) failed"
		exit 1
	}

	[ -z "$mkfs" ] && {
		if which mkfs.exfat; then
			mkfs="mkfs.exfat"
		elif which mkfs.vfat; then
			mkfs="mkfs.vfat"
		elif [ -e "$termux_bin/mkfs.exfat" ]; then
			mkfs="$termux_bin/mkfs.exfat"
		elif [ -e "$termux_bin/mkfs.vfat" ]; then
			mkfs="$termux_bin/mkfs.vfat"
		elif which busybox; then
			mkfs="busybox mkfs.vfat"
		elif [ -e "$termux_bin/busybox" ]; then
			mkfs="$termux_bin/busybox mkfs.vfat"
		else
			$toast "mkfs doesn't exist, $src is not formatted, you can format it manually."
			return 0
		fi
	}

	$mkfs "$src" || $toast "format $src failed"

	return 0
}

search_lun_file() {
	local configfs_mount
	local config_path

	configfs_mount=`$mount | grep ' type configfs '`
	if [ -n "$configfs_mount" ]; then
		config_path=`echo "$configfs_mount" | awk '{print $3}'`
		lun_file=`$find "$config_path" -type f -name file`
	else
		config_path=`$find /sys -type d -name 'f_mass_storage'`
		lun_file="$config_path/lun/file"
	fi
	
	[ -e "$lun_file" ] || {
		$toast "can't find lun file"
		exit 1
	}
}

run_with_root() {
	if which nsenter; then 
		nsenter -t 1 -m "$script_path" namespace >"$log_path" 2>&1
	else
		sh "$script_path" namespace >"$log_path" 2>&1
	fi
}

run_with_namespace() {
	local block

	[ -z "$lun_file" ] && search_lun_file
	init_env
	echo "$src" > "$lun_file"
	setprop sys.usb.config mass_storage

	[ -z "$dst" ] && {
		$toast "set mass storage file to $dst"
		return 0
	}

	$umount "$dst"
	if [ -b "$src" ]; then
		block="$src"
	else
		$losetup | grep "$src" | awk '{print $1}' | xargs $losetup -d
		block=`$losetup -f`
		$losetup "$block" "$src" || {
			$toast "attach $block to $src failed"
			return 0
		}
	fi

	retry 15 0.8 mount_fat || {
		$toast "can't mount $src to $dst"
		return 0
	}

	$toast "mount $src to $dst success"
}

if [ "$1" = "root" ]; then
	run_with_root
elif [ "$1" = "namespace" ]; then
	run_with_namespace
else
	su -c "$script_path" root
fi
