package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sjj.echo.routine.ShellUnit;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity
{

    private EditText mConfigEdit;
    private Button mConfigBtn;
    private EditText mSourceEdit;
    private Button mSourceBtn;
    private Button mGadgetBtn;
    private Button mSourceHistoryBtn;
    private Button mConfigSearchBtn;
    private CheckBox mReadonlyCheck;
    private TextView mStatusTxt;
    private View mInfoTxt;
    public final static String KEY_CONFIG_PATH = "KEY_CONFIG_PATH";
    public final static String KEY_SOURCE_PATH =  "KEY_SOURCE_PATH";
    public final static String KEY_READ_ONLY = "KEY_READ_ONLY";
    public final static String KEY_SOURCE_HISTORY_COUNT = "KEY_SOURCE_HISTORY_COUNT";
    public final static String KEY_SOURCE_HISTORY_BASE = "KEY_SOURCE_HISTORY_BASE";
    public final static String KEY_FIRST_RUN = "KEY_FIRST_RUN";
    public final static int MAX_HISTORY = 20;
    protected SharedPreferences mSharedPreferences;
    protected String mSource ;
    protected boolean mReadonly = false;
    protected LinkedList<String> mSourceHistory = new LinkedList<>();

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

        mConfigBtn = (Button) findViewById(R.id.config_btn);
        mConfigEdit = (EditText) findViewById(R.id.config_edit);
        mSourceBtn = (Button) findViewById(R.id.source_btn);
        mSourceEdit = (EditText) findViewById(R.id.source_edit);
        mReadonlyCheck = (CheckBox) findViewById(R.id.readonly);
        mGadgetBtn = (Button) findViewById(R.id.gadget_btn);
        mStatusTxt = (TextView) findViewById(R.id.status);
        mInfoTxt = findViewById(R.id.about);
        mSourceHistoryBtn = (Button) findViewById(R.id.source_history_btn);
        mConfigSearchBtn = (Button) findViewById(R.id.config_search_btn);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRun = mSharedPreferences.getBoolean(KEY_FIRST_RUN,true);
        //if it's the first time run ,show the help information.
        if(firstRun)
        {
            onHelp();
            mSharedPreferences.edit().putBoolean(KEY_FIRST_RUN,false).commit();
        }
        String _sourcePath = mSharedPreferences.getString(KEY_SOURCE_PATH,null);
        if(_sourcePath!=null&&!_sourcePath.isEmpty())
        {
            mSource = _sourcePath;
        }
        if(mSource!=null)
            mSourceEdit.setText(mSource);

        String _configPath = mSharedPreferences.getString(KEY_CONFIG_PATH,null);
        if(_configPath!=null&&! _configPath.isEmpty())
        {
            MassStorageUnit.mConfigPath = _configPath;
        }else
        {
            mSourceEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String _path = MassStorageUnit.searchPath();
                    if(_path!=null)
                        MassStorageUnit.mConfigPath = _path;
                    else
                        Toast.makeText(MainActivity.this,"search config path fail!",Toast.LENGTH_LONG).show();
                }
            },2000);

        }
        if(MassStorageUnit.mConfigPath!=null)
            mConfigEdit.setText(MassStorageUnit.mConfigPath);

        mReadonly = mSharedPreferences.getBoolean(KEY_READ_ONLY,false);
        mReadonlyCheck.setChecked(mReadonly);

        int _historyCount = mSharedPreferences.getInt(KEY_SOURCE_HISTORY_COUNT,0);
        for(int i=0;i<_historyCount;i++)
        {
            mSourceHistory.add(mSharedPreferences.getString(KEY_SOURCE_HISTORY_BASE+i,""));
        }

        mGadgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
                doConfig();
            }
        });

        mInfoTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 自动生成的方法存根
                Intent intent =new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://blog.csdn.net/outofmemo/article/details/53348552"));
                startActivity(intent);
            }
        });

        mStatusTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshStatus();
            }
        });

        mConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,com.sjj.echo.explorer.MainActivity.class);
                intent.setType("directory/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,2);
            }
        });

        mSourceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,com.sjj.echo.explorer.MainActivity.class);
                intent.setType("file/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });

        mSourceHistoryBtn.setOnClickListener(new View.OnClickListener() {
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

        mConfigEdit.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshStatus();
            }
        },2000);
    }

    /**
     * show the mass storage file history
     * */
    private void showHistory()
    {
        String[] strs = new String[mSourceHistory.size()];
        final String[] _histories = mSourceHistory.toArray(strs);
        new AlertDialog.Builder(MainActivity.this).setTitle("select a history")
                .setItems(_histories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSourceEdit.setText(_histories[which]);
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
            mConfigEdit.setText(_path);
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
                mSourceEdit.setText(path);
            }

            if(requestCode ==2)
            {
                mConfigEdit.setText(path);
            }
        }
    }

    /**
     * update the selection information and save it
     * */
    protected void saveStatus()
    {
        mSource = mSourceEdit.getText().toString();
        mSharedPreferences.edit().putString(KEY_SOURCE_PATH,mSource).commit();
        mReadonly = mReadonlyCheck.isChecked();
        mSharedPreferences.edit().putBoolean(KEY_READ_ONLY,mReadonly).commit();
        MassStorageUnit.mConfigPath = mConfigEdit.getText().toString();
        mSharedPreferences.edit().putString(KEY_CONFIG_PATH,MassStorageUnit.mConfigPath).commit();
        boolean _exist = false;
        for(String str:mSourceHistory)
        {
            if(mSource.equals(str))
            {
                _exist = true;
                break;
            }
        }
        if(!_exist)
        {
            mSourceHistory.add(mSource);
            if(mSourceHistory.size()>MAX_HISTORY)
                mSourceHistory.pollFirst();
            int _i = 0;
            mSharedPreferences.edit().putInt(KEY_SOURCE_HISTORY_COUNT,mSourceHistory.size()).commit();
            for(String str:mSourceHistory)
            {
                mSharedPreferences.edit().putString(KEY_SOURCE_HISTORY_BASE+_i,str).commit();
                _i++;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveStatus();
    }

    /**
     * read the gadget status information and show it
     * */
    protected void refreshStatus()
    {
        int ret = MassStorageUnit.refreshStatus();
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
            info += "mass storage mode: " + (MassStorageUnit.mStatusReadonly.equals("1")?"readonly":"readwrite")+"\n";
            mStatusTxt.setText(info);
        }
    }

    /**
     * config the usb gadget
     * */
    protected void doConfig()
    {
        int ret = MassStorageUnit.umsConfig(mSource,mReadonly);
        if(ret== ShellUnit.EXEC_ERR)
            Toast.makeText(this,"please check root permission",Toast.LENGTH_SHORT).show();
        else if(ret !=0||MassStorageUnit.mError!=null)
            Toast.makeText(this,MassStorageUnit.mError,Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,"config success!",Toast.LENGTH_SHORT).show();
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
            ShellUnit.execRoot("reboot");
        }else if(id==R.id.action_reboot_recovery)
        {
            ShellUnit.execRoot("reboot recovery");
        }else if(id == R.id.action_reboot_fastboot)
        {
            ShellUnit.execRoot("reboot bootloader");
        }
        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    private void onHelp()
    {
        Intent intent = new Intent(MainActivity.this,HelpActivity.class);
        startActivity(intent);
    }


}
