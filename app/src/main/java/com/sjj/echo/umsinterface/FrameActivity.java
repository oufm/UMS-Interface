package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sjj.echo.routine.FileTool;
import com.sjj.echo.routine.PermissionUnit;
import com.sjj.echo.routine.ShellUnit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Locale;

/**
 * Created by SJJ on 2017/3/8.
 */

public class FrameActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    static public String sLang;
    static public long sDownloadId = 0;
    static public String sDownloadPath;
    public final static String KEY_FIRST_RUN = "KEY_FIRST_RUN";
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

    public static String APP_DIR = "/data/data//com.sjj.echo.umsinterface";


    private void creatReport()
    {
        File _script = new File(APP_DIR+"/ums_device_info.sh");
        if(!_script.exists()||!_script.isFile())
        {
            if(!FileTool.streamToFile(getResources().openRawResource(R.raw.ums_device_info),APP_DIR+"/ums_device_info.sh"))
                Toast.makeText(this,"create report fail!",Toast.LENGTH_LONG).show();
        }
        ShellUnit.execRoot("sh "+APP_DIR+"/ums_device_info.sh");
        String _targetpath = "/sdcard/ums_device_info.sh";
        File _report = new File(_targetpath);
        String _message = getString(R.string.reportfail);
        if(_report.isFile()&&_report.length()>0)
            _message = getString(R.string.reportok);
        new AlertDialog.Builder(FrameActivity.this).setMessage(_message+_targetpath).create().show();

    }

    protected void initBusybox()
    {

        File _bin = new File(APP_DIR+"/bin");
        if(!_bin.isDirectory()&&!_bin.mkdir())
        {
            Toast.makeText(this,"init fail!",Toast.LENGTH_LONG).show();
            return;
        }
        File _busybox = new File(APP_DIR+"/bin/busybox");
        if(!_busybox.isFile())
        {
            if(!FileTool.streamToFile(getResources().openRawResource(R.raw.busybox),APP_DIR+"/bin/busybox"))
                Toast.makeText(this,"init fail!",Toast.LENGTH_LONG).show();
        }
        ShellUnit.execRoot("chmod 777 "+APP_DIR+"/bin/busybox");
        if(ShellUnit.stdErr!=null)
            Toast.makeText(this,"init fail!",Toast.LENGTH_LONG).show();
    }

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
            int _versionCode = 0;
            try {
                _versionCode = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            mSharedPreferences.edit().putLong(KEY_LAST_CHECK_UPDATE, new Date().getTime()).commit();
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
        return mCreateImageFragment.creatImage(_path,_size,_format);
    }

    public boolean mount(boolean _readonly,boolean _loop,boolean _charset,boolean _mask,String _source,String _point)
    {
        return mMountFragment.mount(_readonly,_loop,_charset,_mask,_source,_point);
    }

    public boolean ums(String _dev,String function)
    {

        int ret;
        if(function==null)
            ret=MassStorageUnit.umsConfig(_dev,false);
        else
            ret=MassStorageUnit.umsConfig(_dev,false,function);
        if(ret !=0||MassStorageUnit.mError!=null||ret== ShellUnit.EXEC_ERR)
            return false;
        return true;
    }

    public void doUmsConfig(String dev)
    {
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
//                mUmsFragment.doConfig(path,null,true);
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        Locale locale = getResources().getConfiguration().locale;
        sLang = locale.getLanguage();
        PermissionUnit.getPermission(new String[]{"android.permission.INTERNET"},this);
        initBusybox();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = mSharedPreferences.getBoolean(KEY_FIRST_RUN,true);
        //if it's the first time run ,show the help information.
        if(firstRun)
        {
            //TO DO:show help
            mSharedPreferences.edit().putBoolean(KEY_FIRST_RUN,false).commit();
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

        Toolbar _toolbar = new Toolbar(this);
        setSupportActionBar(_toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameActivity.this.openOptionsMenu();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_exec)
        {
            final EditText editText = new EditText(this);
            new AlertDialog.Builder(this).setTitle("execute shell command")
                    .setView(editText).setNegativeButton("execute", new DialogInterface.OnClickListener() {
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
            creatReport();
        }
        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
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

}
