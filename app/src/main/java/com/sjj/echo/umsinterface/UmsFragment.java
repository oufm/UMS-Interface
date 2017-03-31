package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;

import java.util.LinkedList;

import static com.sjj.echo.umsinterface.FrameActivity.logInfo;

/**
 * Created by SJJ on 2017/3/8.
 */

public class UmsFragment extends Fragment {
    Activity mActivity;

    private EditText mPathEdit;
    private Button mPathBtn;
    private EditText mDevEdit;
    private Button mDevBtn;
    private Button mGadgetBtn;
    private Button mDevHistoryBtn;
    //private Button mConfigSearchBtn;
    private CheckBox mReadonlyCheck;
    private TextView mStatusTxt;
    private CheckBox mConfigCheck;
    private View mConfigView;
    View mView;
//    private View mInfoTxt;

//    public final static String KEY_INTENT_CONFIG = "KEY_INTENT_CONFIG";
    public final static String KEY_CONFIG_PATH = "KEY_CONFIG_PATH";
    public final static String KEY_DEVICE_FILE =  "KEY_DEVICE_FILE";
    public final static String KEY_READ_ONLY = "KEY_READ_ONLY";
    public final static String KEY_DEVICE_HISTORY_COUNT = "KEY_DEVICE_HISTORY_COUNT";
    public final static String KEY_DEVICE_HISTORY_BASE = "KEY_DEVICE_HISTORY_BASE";

    public final static int MAX_HISTORY = 20;
    protected SharedPreferences mSharedPreferences;

    //protected boolean mReadonly = false;
    protected LinkedList<String> mDevHistory = new LinkedList<>();

    public void init(Activity activity)
    {
        mActivity = activity;
    }


    /**
     * show the mass storage file history
     * */
    private void showHistory()
    {
        String[] strs = new String[mDevHistory.size()];
        final String[] _histories = mDevHistory.toArray(strs);
        new AlertDialog.Builder(mActivity).setTitle("select a history")
                .setItems(_histories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDevEdit.setText(_histories[which]);
                        dialog.cancel();
                    }
                }).create().show();
    }
//    /**
//     * search the config path ,set it if success.
//     * */
//    protected void setConfigPath()
//    {
//        logInfo("ums setConfigPath()");
//        String _path = MassStorageUnit.searchPath();
//        if(_path!=null) {
//            mPathEdit.setText(_path);
//            MassStorageUnit.mConfigPath = _path;
//            //Toast.makeText(mActivity,"search config path success!",Toast.LENGTH_SHORT).show();
//        }
//        else
//            Toast.makeText(mActivity,"search config path fail!",Toast.LENGTH_LONG).show();
//    }

    /**
     * update the selection information and save it
     * */
    protected void saveStatus()
    {
        String _dev = mDevEdit.getText().toString();
        mSharedPreferences.edit().putString(KEY_DEVICE_FILE,_dev).commit();
        boolean _readonly = mReadonlyCheck.isChecked();
        mSharedPreferences.edit().putBoolean(KEY_READ_ONLY,_readonly).commit();
        //MassStorageUnit.mConfigPath = mPathEdit.getText().toString();
        mSharedPreferences.edit().putString(KEY_CONFIG_PATH,mPathEdit.getText().toString()).commit();
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

    }


    /**
     * read the gadget status information and show it
     * */
    protected void refreshStatus()
    {
        logInfo("ums refreshStatus()");
        if(!MassStorageUnit.refreshStatus())
            Toast.makeText(mActivity,MassStorageUnit.mError,Toast.LENGTH_LONG).show();
        else
        {
            //Toast.makeText(mActivity,"status update ok!",Toast.LENGTH_SHORT).show();
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
    protected void umsRun()
    {
        //String _configPath =  mPathEdit.getText().toString();
        String _devPath = mDevEdit.getText().toString();
        boolean _readonly = mReadonlyCheck.isChecked();
        logInfo("ums umsRun(devPath="+_devPath+",readonly="+_readonly);
        if(!MassStorageUnit.umsConfig(_devPath,_readonly))
            Toast.makeText(mActivity,"UMS"+" "+getString(R.string.fail)+":"+MassStorageUnit.mError,Toast.LENGTH_LONG).show();
        else {
            Toast.makeText(mActivity, "UMS"+" "+getString(R.string.success), Toast.LENGTH_SHORT).show();
            saveStatus();
            ((FrameActivity)mActivity).saveHistory(_devPath,true);
        }
        refreshStatus();
    }

    public void doConfigRun(String device)
    {
        if(device!=null)
            mDevEdit.setText(device);
        umsRun();
    }

    public void doConfig(String device,String config)
    {
        if(device!=null)
            mDevEdit.setText(device);
        if(config!=null) {
            mPathEdit.setText(config);
            MassStorageUnit.setConfigPath(config);
        }
    }

    //TO DO:interface to operate

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        View rootView = inflater.inflate(R.layout.content_main,container,false);
        mView = rootView;
        mPathBtn = (Button) rootView.findViewById(R.id.config_btn);
        mPathEdit = (EditText) rootView.findViewById(R.id.config_edit);
        mDevBtn = (Button) rootView.findViewById(R.id.source_btn);
        mDevEdit = (EditText) rootView.findViewById(R.id.source_edit);
        mReadonlyCheck = (CheckBox) rootView.findViewById(R.id.readonly);
        mGadgetBtn = (Button) rootView.findViewById(R.id.gadget_btn);
        mStatusTxt = (TextView) rootView.findViewById(R.id.status);
//        mInfoTxt = rootView.findViewById(R.id.about);
        mDevHistoryBtn = (Button) rootView.findViewById(R.id.source_history_btn);
        //mConfigSearchBtn = (Button) rootView.findViewById(R.id.config_search_btn);
        mConfigCheck = (CheckBox) rootView.findViewById(R.id.ums_config_check);
        mConfigView = rootView.findViewById(R.id.ums_config_group);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        String _dev = mSharedPreferences.getString(KEY_DEVICE_FILE,null);
        if(_dev!=null&&!_dev.isEmpty())
        {
            mDevEdit.setText(_dev);
        }

        final String _configPath = mSharedPreferences.getString(KEY_CONFIG_PATH,null);
        if(_configPath!=null&&! _configPath.isEmpty())
        {
            mPathEdit.setText(_configPath);
        }
        else
        {
            mPathEdit.setText("/sys/devices/virtual/android_usb/android0/");
        }
//        else
//        {
//            mDevEdit.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    setConfigPath();
//                }
//            },1000);
//
//        }
//        if(MassStorageUnit.mConfigPath!=null)
//            mPathEdit.setText(MassStorageUnit.mConfigPath);

        //mReadonly = mSharedPreferences.getBoolean(KEY_READ_ONLY,false);
        mReadonlyCheck.setChecked(false);

        int _historyCount = mSharedPreferences.getInt(KEY_DEVICE_HISTORY_COUNT,0);
        for(int i=0;i<_historyCount;i++)
        {
            mDevHistory.add(mSharedPreferences.getString(KEY_DEVICE_HISTORY_BASE +i,""));
        }

        mConfigCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    mConfigView.setVisibility(View.VISIBLE);
                else
                    mConfigView.setVisibility(View.INVISIBLE);
            }
        });

        mGadgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean _block = MountFragment.isBlockDev(mDevEdit.getText().toString());
                if(_block!=null&&_block)
                {
                    new AlertDialog.Builder(mActivity).setTitle(R.string.warning)
                            .setMessage(R.string.umsdev).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UmsFragment.this.umsRun();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
                }
                else
                    umsRun();
            }
        });

        mPathEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                MassStorageUnit.setConfigPath(mDevEdit.getText().toString());
            }
        });

//        mInfoTxt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO 自动生成的方法存根
//                Intent intent =new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("https://github.com/outofmemo/UMS-Interface"));
//                startActivity(intent);
//            }
//        });

        mStatusTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshStatus();
            }
        });

        mPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity,ExplorerActivity.class);
                intent.setType("directory/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mActivity.startActivityForResult(intent,2);
            }
        });

        mDevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity,ExplorerActivity.class);
                intent.setType("file/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mActivity.startActivityForResult(intent,1);
            }
        });

        mDevHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHistory();
            }
        });

//        mConfigSearchBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               MassStorageUnit.setConfigPath(mPathEdit.getText().toString());
//
//            }
//        });
        mPathEdit.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshStatus();
            }
        },1800);


        return rootView;
    }
}
