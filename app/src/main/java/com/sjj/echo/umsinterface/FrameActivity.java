package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;
import com.sjj.echo.routine.FileTool;
import com.sjj.echo.routine.LogUnit;
import com.sjj.echo.routine.PermissionUnit;
import com.sjj.echo.routine.ShellUnit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Locale;

import static com.sjj.echo.umsinterface.R.raw.ums_device_info;

/**
 * Created by SJJ on 2017/3/8.
 */

public class FrameActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener  {

    private ViewPager mViewPager;
    static public String sLang;
    static public long sDownloadId = 0;
    static public String sDownloadPath;
    public final static String KEY_FIRST_RUN = "KEY_FIRST_RUN";
    public final static String KEY_VERSION = "KEY_VERSION";
    public final static String KEY_LAST_CHECK_UPDATE = "KEY_LAST_CHECK_UPDATE";
    public final static String KEY_IGNORE_VERSION = "KEY_IGNORE_VERSION";
    protected SharedPreferences mSharedPreferences;
    private UmsFragment mUmsFragment;
    private MountFragment mMountFragment;
    private InfoFragment mInfoFragment;
    private CreateImageFragment mCreateImageFragment;
    private HelpFragment mHelpFragment;
    private QuickStartFragment mQuickStartFragment;
    private Fragment[] mFragments;

    public static String APP_DIR = "/data/data/com.sjj.echo.umsinterface";
    public static LogUnit sLog = LogUnit.getDefaultLog();
    public static int sVersioncode = 0;
    public static void logInfo(String _str)
    {
        sLog.logWrite("INFO",_str);
        //Log.d("UMS_DEBUG","[ INFO]"+_str);
    }


//    private void addShortcut()
//    {
//        Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//
//        // 不允许重复创建
//        addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
//        // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
//        // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
//        // 屏幕上没有空间时会提示
//        // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式
//
//        // 名字
//        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.ums_explorer));
//        // 图标
//        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//                Intent.ShortcutIconResource.fromContext(this,
//                        R.mipmap.ic_explorer_launcher));
//
//        // 设置关联程序
//        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
//        launcherIntent.setClass(this, ExplorerActivity.class);
//        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        addShortcutIntent
//                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
//
//        // 发送广播
//        sendBroadcast(addShortcutIntent);
//    }

    public String getMountPoint()
    {
        return mQuickStartFragment.getMountPoint();
    }

    public String getImagePath()
    {
        return mQuickStartFragment.getFilename();
    }

    private void createReport()
    {
       // if(!_script.exists()||!_script.isFile())
        //{
        ShellUnit.execRoot("rm "+APP_DIR+"/ums_device_info.sh");
        if(!FileTool.streamToFile(getResources().openRawResource(ums_device_info),APP_DIR+"/ums_device_info.sh"))
            Toast.makeText(this,"create report fail!",Toast.LENGTH_LONG).show();
        //}
        ShellUnit.execBusybox("sh "+APP_DIR+"/ums_device_info.sh");

        String _reportName = "ums_device_info.log";
        String _logName = "ums.log";
        String _targetPath = "/sdcard/ums_log.tar.gz";
        String _message = getString(R.string.reportfail);
        ShellUnit.execRoot("ls "+APP_DIR+"/"+_reportName);
        if(ShellUnit.stdErr==null) {
            ShellUnit.execRoot("rm "+_targetPath);
            ShellUnit.execBusybox("tar -czf "+_targetPath+" -C "+APP_DIR+" "+_reportName+" "+_logName);
            if(ShellUnit.stdErr==null)
                _message = getString(R.string.reportok) + " \n"+_targetPath;
            ShellUnit.execRoot("rm "+APP_DIR+"/"+_reportName);
        }
        new AlertDialog.Builder(FrameActivity.this).setMessage(_message).setPositiveButton(R.string.ok,null)
                .setNeutralButton(R.string.see, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(FrameActivity.this, ExplorerActivity.class);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setDataAndType(Uri.parse("/sdcard"),"directory/*");
                        FrameActivity.this.startActivity(intent);
                    }
                }).create().show();
    }

//    protected void initBusybox()
//    {
//        logInfo("initBusybox");
//        File _bin = new File(APP_DIR+"/bin");
//        if(!_bin.isDirectory()&&!_bin.mkdir())
//        {
//            Toast.makeText(this,"init fail!",Toast.LENGTH_LONG).show();
//            return;
//        }
//        File _busybox = new File(APP_DIR+"/bin/busybox");
//        if(!_busybox.isFile())
//        {
//            addShortcut();
//            if(!FileTool.streamToFile(getResources().openRawResource(R.raw.busybox),APP_DIR+"/bin/busybox"))
//                Toast.makeText(this,"init fail!",Toast.LENGTH_LONG).show();
//        }
//        ShellUnit.execRoot("chmod 777 "+APP_DIR+"/bin/busybox");
//        if(ShellUnit.stdErr!=null)
//            Toast.makeText(this,"init fail!",Toast.LENGTH_LONG).show();
//    }

    /**
     * return the content for a URL
     * */
    private String getFromUrl(String urlStr)
    {
        try {
            URL url = new URL(urlStr);
            byte[] buff = new byte[30*1024];
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            int count = input.read(buff);
            //never use new String (byte[].int).it return a wrong string
            return new String(buff,0,count);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * download update package
     * */
    private void downloadUpdate()
    {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/update/app-release.apk"));
        //设置在什么网络情况下进行下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(getString(R.string.update_download_title));
        request.setDescription(getString(R.string.update_download_desc));
        //设置文件存放目录
        //String _dir = Environment.DIRECTORY_DOWNLOADS;
        String _dir = "update";
        String _apk = "UMS Interface.apk";
        request.setDestinationInExternalFilesDir(this,_dir ,_apk);
        sDownloadPath =getExternalFilesDir(null)+"/"+ _dir+"/"+_apk;
        Log.d("@echo off","path="+sDownloadPath);
        FileTool.deleteFile(sDownloadPath);
        DownloadManager downManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        sDownloadId = downManager.enqueue(request);
    }

    private void checkVersion()
    {
        try {
            String _packageName = this.getPackageName();
            if(_packageName==null)
                return;
            sVersioncode = getPackageManager().getPackageInfo(_packageName, PackageManager.GET_CONFIGURATIONS).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        boolean firstRun = mSharedPreferences.getBoolean(KEY_FIRST_RUN,true);
        //if it's the first time run ,show the help information.
        if(firstRun)
        {
            //TO DO:show help
            mSharedPreferences.edit().putBoolean(KEY_FIRST_RUN,false).commit();
        }

        int _version = mSharedPreferences.getInt(KEY_VERSION,0);
        if(_version <sVersioncode)
        {
            ShortcutActivity.addShortcut(this);
            mSharedPreferences.edit().putInt(KEY_VERSION,sVersioncode).commit();
        }

        long _lastCheck = mSharedPreferences.getLong(KEY_LAST_CHECK_UPDATE,0);
        if(new Date().getTime()-_lastCheck>12*60*60*1000)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    checkUpdate(true);
                }
            }).start();
            //never use ".run()"!!!
        }

    }

    private boolean checkUpdate(boolean allowIgnore)
    {

        String updateUrl = "https://raw.githubusercontent.com/outofmemo/UMS-Interface/master/update/";
        String _version = getFromUrl(updateUrl+"version");
        if(_version!=null) {
            int __newVersion = 0;
            try{
                if(_version.length()>3)
                    _version = _version.substring(0,3);
                __newVersion = Integer.parseInt(_version);
            }catch (java.lang.NumberFormatException e)
            {
                e.printStackTrace();
            }
            final int _newVersion = __newVersion;
//            int _versionCode = 0;
//            try {
//                String _packageName = this.getPackageName();
//                if(_packageName==null)
//                    return false;
//                _versionCode = getPackageManager().getPackageInfo(_packageName, PackageManager.GET_CONFIGURATIONS).versionCode;
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
            mSharedPreferences.edit().putLong(KEY_LAST_CHECK_UPDATE, new Date().getTime()).commit();
            int _versionCode = sVersioncode;
            if(_newVersion > _versionCode)
            {
                if(allowIgnore) {
                    int _ignore = mSharedPreferences.getInt(KEY_IGNORE_VERSION, 0);
                    if (_ignore == _newVersion)
                        return true;
                }

                String _logs = "";
                while(_versionCode++<_newVersion)
                {
                    String _log = getFromUrl(updateUrl+"log_"+sLang+"_"+_versionCode);
                    if(_log!=null) {
                        _logs+=_log+"\n";
                    }
                }
                final String _versionName = getFromUrl(updateUrl+"version_name");
                final String final_logs = _logs;
                mViewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        String _title = getString(R.string.new_version_title);
                        if(_versionName!=null)
                            _title+=_versionName;
                        new AlertDialog.Builder(FrameActivity.this).setTitle(_title)
                                .setMessage(final_logs).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadUpdate();
                            }
                        }).setNegativeButton(R.string.no,null).setNeutralButton(R.string.update_ignore, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSharedPreferences.edit().putInt(KEY_IGNORE_VERSION,_newVersion).commit();
                            }
                        }).create().show();
                    }
                });
                return true;
            }
        }
        return false;
    }

    public boolean createImage(String _path,int _size,boolean _format)
    {

        return mCreateImageFragment.createImage(_path,_size,_format);
    }

    public boolean mount(boolean _readonly,boolean _loop,boolean _charset,boolean _mask,String _source,String _point)
    {
        return mMountFragment.mount(_readonly,_loop,_charset,_mask,_source,_point);
    }


    public boolean ums(String _dev,String function)
    {
        logInfo("ums(dev="+_dev+",function="+function+")");
        boolean ret;
        if(function==null)
            ret=MassStorageUnit.umsConfig(_dev,false);
        else
            ret=MassStorageUnit.umsConfig(_dev,false,function);
        return ret;
    }

    public void umsRun(String dev)
    {
        logInfo("umsRun(dev="+dev+")");
        mViewPager.setCurrentItem(1);
        mUmsFragment.doConfigRun(dev);
    }
    //TO DO:
    //set the path which the activity return
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = uri.toString().substring(7);
//            Bundle bundle =data.getExtras();
//            if(bundle!=null&&bundle.containsKey(KEY_INTENT_CONFIG)&&bundle.getBoolean(KEY_INTENT_CONFIG))
//            {
//                mViewPager.setCurrentItem(0);
//                mUmsFragment.umsRun(path,null,true);
//            }
            if(requestCode ==1)
                mUmsFragment.doConfig(path,null);
            if(requestCode ==2)
                mUmsFragment.doConfig(null,path);
            if(requestCode ==3)
                mMountFragment.doConfig(path,null);
            if(requestCode ==4)
                mMountFragment.doConfig(null,path);
            if(requestCode ==6)
                mCreateImageFragment.doConfig(path);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void initShell()
    {
        ShellUnit.initBusybox(getResources().openRawResource(R.raw.busybox));
        if(!ShellUnit.sRootReady)
            new AlertDialog.Builder(this).setTitle(R.string.error).setMessage(R.string.no_root_tip)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FrameActivity.this.finish();
                }
            }).create().show();
        if(!ShellUnit.sSuReady||!ShellUnit.sBusyboxReady)
            Toast.makeText(this,getString(R.string.init_fail),Toast.LENGTH_LONG).show();
        logInfo("SUReady="+ShellUnit.sSuReady+";BusyboxReady="+ShellUnit.sBusyboxReady);
        ShellUnit.execBusybox("echo 'initShell finished'");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        Log.d("UMS_DEBUG","[ INIT]onCreate instance="+this.hashCode() );

        Locale locale = getResources().getConfiguration().locale;
        sLang = locale.getLanguage();
        PermissionUnit.getPermission(new String[]{"android.permission.INTERNET","android.permission.RECEIVE_BOOT_COMPLETED"},this);
        initShell();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkVersion();

        mUmsFragment=new UmsFragment();
        mUmsFragment.init(this);
        mMountFragment= new MountFragment();
        mMountFragment.init(this);
        mInfoFragment= new InfoFragment();
        mInfoFragment.init(this);
        mCreateImageFragment = new CreateImageFragment();
        mCreateImageFragment.init(this);
        mHelpFragment = new HelpFragment();
        mHelpFragment.init(this);
        mQuickStartFragment = new QuickStartFragment();
        mQuickStartFragment.init(this);
        mFragments = new Fragment[] {mQuickStartFragment,mUmsFragment,mMountFragment,mInfoFragment,mCreateImageFragment,mHelpFragment};
        setContentView(R.layout.activity_main);

        //Toolbar _toolbar = new Toolbar(this);
        //setSupportActionBar(_toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FrameActivity.this.openOptionsMenu();
                PopupMenu popup = new PopupMenu(FrameActivity.this, fab);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.main, popup.getMenu());
                popup.setOnMenuItemClickListener(FrameActivity.this);
                popup.show();
            }
        });
        String[] _titles = getResources().getStringArray(R.array.tab_titles);
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        UmsPagerAdapter pagerAdapter = new UmsPagerAdapter(getSupportFragmentManager(),mFragments,_titles);
        mViewPager.setAdapter(pagerAdapter);
        TabLayout _tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        _tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShellUnit.close();
        sLog.close();
    }

    private boolean onMenu(int id)
    {
        if(id == R.id.action_exec)
        {
            final EditText editText = new EditText(this);
            new AlertDialog.Builder(this).setTitle(R.string.ums_cmd)
                    .setView(editText).setNegativeButton(R.string.execute, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String _output = ShellUnit.execRoot(editText.getText().toString());
                    if(_output!=null)
                    {
                        new AlertDialog.Builder(FrameActivity.this).setTitle("output of the shell:")
                                .setMessage(_output).create().show();
                    }
                    if(ShellUnit.stdErr!=null)
                    {
                        Toast.makeText(FrameActivity.this,"error information:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                }
            }).create().show();
        }else if(id==R.id.action_reboot_kernel)
        {
            reboot("reboot");
        }else if(id==R.id.action_reboot_recovery)
        {
            reboot("reboot recovery");
        }else if(id == R.id.action_reboot_fastboot)
        {
            reboot("reboot bootloader");
        }else if(id==R.id.action_reboot_android)
        {
            reboot("stop && start");
        }else if(id == R.id.action_check_update)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(!checkUpdate(false))
                    {
                        mViewPager.post(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(FrameActivity.this).setMessage(getString(R.string.no_newer_version))
                                        .setTitle(R.string.check_update)
                                        .setPositiveButton(R.string.ok,null).create().show();
                            }
                        });
                    }
                }
            }).start();
        }else if(id == R.id.action_report)
        {
            final AlertDialog _dialog=new AlertDialog.Builder(FrameActivity.this).setMessage(R.string.log_wait).create();
            _dialog.show();
            mViewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    createReport();
                    _dialog.cancel();
                }
            },300);

        }else if(id == R.id.open_explorer)
        {
            Intent intent = new Intent(this, ExplorerActivity.class);
            startActivity(intent);
        }else
            return false;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return onMenu(item.getItemId());
        //noinspection SimplifiableIfStatement
        //return super.onOptionsItemSelected(item);
    }
    private void reboot(final String cmd)
    {
        new AlertDialog.Builder(this).setTitle(R.string.reboot)
                .setMessage(R.string.reboot_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ShellUnit.execRoot(cmd);
                    }
                }).setNegativeButton(R.string.no,null).create().show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onMenu(item.getItemId());
    }
}
