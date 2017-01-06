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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;
import com.sjj.echo.routine.FileTool;
import com.sjj.echo.routine.PermissionUnit;
import com.sjj.echo.routine.ShellUnit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

    private EditText mPathEdit;
    private Button mPathBtn;
    private EditText mDevEdit;
    private Button mDevBtn;
    private Button mGadgetBtn;
    private Button mDevHistoryBtn;
    private Button mConfigSearchBtn;
    private CheckBox mReadonlyCheck;
    private TextView mStatusTxt;
    private View mInfoTxt;
    static public String sLang;
    static public long sDownloadId = 0;
    static public String sDownloadPath;
//    static public Activity sSelf;
    public final static String KEY_CONFIG_PATH = "KEY_CONFIG_PATH";
    public final static String KEY_DEVICE_FILE =  "KEY_DEVICE_FILE";
    public final static String KEY_READ_ONLY = "KEY_READ_ONLY";
    public final static String KEY_DEVICE_HISTORY_COUNT = "KEY_DEVICE_HISTORY_COUNT";
    public final static String KEY_DEVICE_HISTORY_BASE = "KEY_DEVICE_HISTORY_BASE";
    public final static String KEY_FIRST_RUN = "KEY_FIRST_RUN";
    public final static String KEY_INTENT_CONFIG = "KEY_INTENT_CONFIG";
    public final static String KEY_LAST_CHECK_UPDATE = "KEY_LAST_CHECK_UPDATE";
    public final static String KEY_IGNORE_VERSION = "KEY_IGNORE_VERSION";

    public final static int MAX_HISTORY = 20;
    protected SharedPreferences mSharedPreferences;
//    protected String mSource ;
    protected boolean mReadonly = false;
    protected LinkedList<String> mDevHistory = new LinkedList<>();

//    static public String getResString(int resId)
//    {
//        return sSelf.getString(resId);
//    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHelp();
            }
        });

        Locale locale = getResources().getConfiguration().locale;
        sLang = locale.getLanguage();
        PermissionUnit.getPermission(new String[]{"android.permission.INTERNET"},this);

        mPathBtn = (Button) findViewById(R.id.config_btn);
        mPathEdit = (EditText) findViewById(R.id.config_edit);
        mDevBtn = (Button) findViewById(R.id.source_btn);
        mDevEdit = (EditText) findViewById(R.id.source_edit);
        mReadonlyCheck = (CheckBox) findViewById(R.id.readonly);
        mGadgetBtn = (Button) findViewById(R.id.gadget_btn);
        mStatusTxt = (TextView) findViewById(R.id.status);
        mInfoTxt = findViewById(R.id.about);
        mDevHistoryBtn = (Button) findViewById(R.id.source_history_btn);
        mConfigSearchBtn = (Button) findViewById(R.id.config_search_btn);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = mSharedPreferences.getBoolean(KEY_FIRST_RUN,true);
        //if it's the first time run ,show the help information.
        if(firstRun)
        {
            onHelp();
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

        String _dev = mSharedPreferences.getString(KEY_DEVICE_FILE,null);
        if(_dev!=null&&!_dev.isEmpty())
        {
            mDevEdit.setText(_dev);
        }

        String _configPath = mSharedPreferences.getString(KEY_CONFIG_PATH,null);
        if(_configPath!=null&&! _configPath.isEmpty())
        {
            MassStorageUnit.mConfigPath = _configPath;
        }else
        {
            mDevEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setConfigPath();
//                    String _path = MassStorageUnit.searchPath();
//                    if(_path!=null)
//                        MassStorageUnit.mConfigPath = _path;
//                    else
//                        Toast.makeText(ExplorerActivity.this,"search config path fail!",Toast.LENGTH_LONG).show();
                }
            },2000);

        }
        if(MassStorageUnit.mConfigPath!=null)
            mPathEdit.setText(MassStorageUnit.mConfigPath);

        mReadonly = mSharedPreferences.getBoolean(KEY_READ_ONLY,false);
        mReadonlyCheck.setChecked(mReadonly);

        int _historyCount = mSharedPreferences.getInt(KEY_DEVICE_HISTORY_COUNT,0);
        for(int i=0;i<_historyCount;i++)
        {
            mDevHistory.add(mSharedPreferences.getString(KEY_DEVICE_HISTORY_BASE +i,""));
        }

        mGadgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doConfig();
            }
        });

        mPathEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                MassStorageUnit.mConfigPath = mDevEdit.getText().toString();
            }
        });

        mInfoTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 自动生成的方法存根
                Intent intent =new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/outofmemo/UMS-Interface"));
                startActivity(intent);
            }
        });

        mStatusTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshStatus();
            }
        });

        mPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ExplorerActivity.class);
                intent.setType("directory/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,2);
            }
        });

        mDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ExplorerActivity.class);
                intent.setType("file/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });

        mDevHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistory();
            }
        });

        mConfigSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfigPath();

            }
        });
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

       // NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);

        mPathEdit.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshStatus();
            }
        },1800);
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
                mGadgetBtn.post(new Runnable() {
                    @Override
                    public void run() {
                        String _title = getString(R.string.new_version_title);
                        if(_versionName!=null)
                            _title+=_versionName;
                        new AlertDialog.Builder(MainActivity.this).setTitle(_title)
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


    /**
     * show the mass storage file history
     * */
    private void showHistory()
    {
        String[] strs = new String[mDevHistory.size()];
        final String[] _histories = mDevHistory.toArray(strs);
        new AlertDialog.Builder(MainActivity.this).setTitle("select a history")
                .setItems(_histories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDevEdit.setText(_histories[which]);
                        dialog.cancel();
                    }
                }).create().show();
    }
    /**
     * search the config path ,set it if success.
     * */
    protected void setConfigPath()
    {
        String _path = MassStorageUnit.searchPath();
        if(_path!=null) {
            mPathEdit.setText(_path);
            MassStorageUnit.mConfigPath = _path;
            Toast.makeText(MainActivity.this,"search config path success!",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(MainActivity.this,"search config path fail!",Toast.LENGTH_LONG).show();
    }

    //set the path which the activity return
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String path = uri.toString().substring(7);
            if(requestCode ==1)
            {
                mDevEdit.setText(path);
                Bundle bundle =data.getExtras();
                if(bundle!=null&&bundle.containsKey(KEY_INTENT_CONFIG)&&bundle.getBoolean(KEY_INTENT_CONFIG))
                {
                    doConfig();
                }
            }

            if(requestCode ==2)
            {
                mPathEdit.setText(path);
                MassStorageUnit.mConfigPath = path;
            }
        }
    }

    /**
     * update the selection information and save it
     * */
    protected void saveStatus()
    {
        String _dev = mDevEdit.getText().toString();
        mSharedPreferences.edit().putString(KEY_DEVICE_FILE,_dev).commit();
        mReadonly = mReadonlyCheck.isChecked();
        mSharedPreferences.edit().putBoolean(KEY_READ_ONLY,mReadonly).commit();
        MassStorageUnit.mConfigPath = mPathEdit.getText().toString();
        mSharedPreferences.edit().putString(KEY_CONFIG_PATH,MassStorageUnit.mConfigPath).commit();
        //boolean _exist = false;
        for(String str: mDevHistory)
        {
            if(_dev.equals(str))
            {
                mDevHistory.remove(str);
                break;
            }
        }
        mDevHistory.addFirst(_dev);
        if(mDevHistory.size()>MAX_HISTORY)
            mDevHistory.removeLast();

        int _i = 0;
        mSharedPreferences.edit().putInt(KEY_DEVICE_HISTORY_COUNT,mDevHistory.size()).commit();
        for(String str:mDevHistory)
        {
            mSharedPreferences.edit().putString(KEY_DEVICE_HISTORY_BASE+_i,str).commit();
            _i++;
        }

//        if(!_exist)
//        {
//            mDevHistory.add(mSource);
//            if(mDevHistory.size()>MAX_HISTORY)
//                mDevHistory.pollFirst();
//            int _i = 0;
//            mSharedPreferences.edit().putInt(KEY_DEVICE_HISTORY_COUNT,mDevHistory.size()).commit();
//            for(String str:mDevHistory)
//            {
//                mSharedPreferences.edit().putString(KEY_DEVICE_HISTORY_BASE+_i,str).commit();
//                _i++;
//            }
//        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        saveStatus();
//    }

    /**
     * read the gadget status information and show it
     * */
    protected void refreshStatus()
    {
        int ret = MassStorageUnit.refreshStatus(mPathEdit.getText().toString());
        if(ret==ShellUnit.EXEC_ERR)
            Toast.makeText(this,"please check root permission",Toast.LENGTH_LONG).show();
        else if(ret !=0&&MassStorageUnit.mError!=null)
            Toast.makeText(this,MassStorageUnit.mError,Toast.LENGTH_LONG).show();
        else
        {
            Toast.makeText(this,"status update ok!",Toast.LENGTH_SHORT).show();
            String info = "(click to refresh)\n";
            info += "gadget status: "+ (MassStorageUnit.mStatusEnable.equals("1")?"enabled":"disabled")+"\n";
            info += "gadget functions: "+ MassStorageUnit.mStatusFunction + "\n" ;
            info += "mass storage file: " + MassStorageUnit.mStatusFile + "\n";
            info += "mass storage mode: " + (MassStorageUnit.mStatusReadonly.equals("1")?"readonly":"readwrite");
            mStatusTxt.setText(info);
        }
    }

    /**
     * config the usb gadget
     * */
    protected void doConfig()
    {
        int ret = MassStorageUnit.umsConfig(mPathEdit.getText().toString(),mDevEdit.getText().toString(),mReadonly);
        if(ret== ShellUnit.EXEC_ERR)
            Toast.makeText(this,"please check root permission",Toast.LENGTH_SHORT).show();
        else if(ret !=0||MassStorageUnit.mError!=null)
            Toast.makeText(this,MassStorageUnit.mError,Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this, "config success!", Toast.LENGTH_SHORT).show();
            saveStatus();
        }
        refreshStatus();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_mount_info)
        {
            Intent intent = new Intent(MainActivity.this,MountInfoActivity.class);
            intent.setType("file/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent,1);
        }else if(id == R.id.action_exec)
        {
            final EditText editText = new EditText(this);
            new AlertDialog.Builder(this).setTitle("execute shell command")
                    .setView(editText).setNegativeButton("execute", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String _output = ShellUnit.execRoot(editText.getText().toString());
                    if(_output!=null)
                    {
                        new AlertDialog.Builder(MainActivity.this).setTitle("output of the shell:")
                                .setMessage(_output).create().show();
                    }
                    if(ShellUnit.stdErr!=null)
                    {
                        Toast.makeText(MainActivity.this,"error information:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                    }
                    dialog.cancel();
                }
            }).create().show();
        }else if(id == R.id.action_help)
        {
            onHelp();
        }else if(id == R.id.action_mount_add)
        {
            Intent intent = new Intent(MainActivity.this,MountActivity.class);
            startActivity(intent);
        }else if(id == R.id.action_file_history)
        {
            showHistory();
        }else if(id== R.id.action_search_config)
        {
            setConfigPath();
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
            reboot("busybox killall zygote & busybox killall zygote64 & killall zygote & killall zygote64");
        } else if(id == R.id.action_new_image)
        {
            Intent intent = new Intent(MainActivity.this,NewImageActivity.class);
            intent.setType("file/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent,1);
        }else if(id == R.id.action_check_update)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(!checkUpdate(false))
                    {
                        mGadgetBtn.post(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this).setMessage(getString(R.string.no_newer_version))
                                        .setTitle(R.string.check_update)
                                        .setPositiveButton(R.string.ok,null).create().show();
                            }
                        });
                    }
                }
            }).start();
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

    private void onHelp()
    {
        Intent intent = new Intent(MainActivity.this,HelpActivity.class);
        startActivity(intent);
    }


}
