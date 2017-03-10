package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sjj.echo.routine.ShellUnit;

import java.io.File;
import java.util.LinkedList;


/**
 * Created by SJJ on 2017/3/9.
 */

public class QuickStartFragment extends Fragment {

    String mStatusFile;
    String mStatusMountpoint;
    boolean mStatusUsb;
    Boolean mStatusMount;

    Activity mActivity;
    TextView mUseText;
    Button mUsbBtn;
    Button mMountBtn;
    TextView mMountText;
    Button mCreateBtn;
    EditText mCreateEdit;
    ListView mHistoryList;

    View mView;

    static String KEY_QUICK_HISTORY_COUNT = "KEY_QUICK_HISTORY_COUNT";
    static String KEY_QUICK_HISTORY_BASE = "KEY_QUICK_HISTORY_BASE";
    static int MAX_HISTORY = 50;

    SharedPreferences mSharedPreferences;
    protected LinkedList<String> mHistory = new LinkedList<>();
    QuickHistoryAdapter mAdapter = new QuickHistoryAdapter();

    public void init(Activity activity)
    {
        mActivity = activity;
    }

    private void getStatus() {
        mStatusFile = null;
        MassStorageUnit.refreshStatus();
        String _path = MassStorageUnit.mStatusFile;
        if (_path == null)
            return;
        if (ShellUnit.execRoot("ls "+_path).length()==0)
            return;
        mStatusFile = _path;
        mStatusMount = null;
        if (MassStorageUnit.mStatusFunction.equals("mass_storage") && MassStorageUnit.mStatusEnable.equals("1"))
            mStatusUsb = true;
        else
            mStatusUsb = false;
        String _output =ShellUnit.execRoot("mount|grep '" + _path + "'");
        if (_output.length() > 0) {
            mStatusMount = true;
            mStatusMountpoint = "";
            int _begin = _output.indexOf(" /");
            int _end = _output.indexOf(" type");
            if(_begin>0&&_end>0&&_end>_begin)
            {
                mStatusMountpoint = _output.substring(_begin,_end);
            }
        }
    }

    private void updateUIStatus(boolean detect)
    {
        if(detect)
            getStatus();
        if(mStatusFile!=null) {
            String[] _strs =mStatusFile.split("/");
            mUseText.setText(getString(R.string.using) + " " + _strs[_strs.length-1]);
            if(mStatusUsb)
                mUsbBtn.setText(getString(R.string.end));
            else
                mUsbBtn.setText(getString(R.string.start));
            if(mStatusMount==null)
            {
                mMountText.setText(getString(R.string.local_mount));
                mMountBtn.setVisibility(View.INVISIBLE);
            }else {
                mMountBtn.setVisibility(View.VISIBLE);
                if (mStatusMount) {
                    mMountText.setText(getString(R.string.local_mount) + " " + mStatusMountpoint);
                    mMountBtn.setText(getString(R.string.end));
                } else {
                    mMountText.setText(getString(R.string.local_mount));
                    mMountBtn.setText(getString(R.string.start));
                }
            }
        }else
        {
            mUseText.setText(getString(R.string.no_using));
            mMountText.setText("");
            mUsbBtn.setText(getString(R.string.start));
            mMountBtn.setText(getString(R.string.start));
            mMountText.setText(R.string.local_mount);
        }

    }

    private void setupList()
    {
        restoreHistory();
        mAdapter.init(mHistory,mActivity,this);
        mHistoryList.setAdapter(mAdapter);
    }

    private void restoreHistory()
    {

        int _historyCount = mSharedPreferences.getInt(KEY_QUICK_HISTORY_COUNT,0);
        for(int i=0;i<_historyCount;i++)
        {
            mHistory.add(mSharedPreferences.getString(KEY_QUICK_HISTORY_BASE +i,""));
        }
    }

    private String getFilename()
    {
        ShellUnit.execRoot("mkdir /sdcard/ums_img;chomd 777 /sdcard/ums_img");
        int num=0;
        while (ShellUnit.execRoot("ls /sdcard/ums_img/"+num+".img").length()>0)
            num++;
        return "/sdcard/ums_img/"+num+".img";
    }

    private String getMountPoint()
    {
        ShellUnit.execRoot("mkdir /data/ums_mnt;chomd 777 /data/ums_mnt");
        int num=0;
        while(ShellUnit.execBusybox("mount|grep /data/ums_mnt/mnt"+num).length()>0)
            num++;
        ShellUnit.execRoot("mkdir /data/ums_mnt/mnt"+num+";chmod 777 /data/ums_mnt/mnt"+num);
        return "/data/ums_mnt/mnt"+num;
    }

    public void useImage(String _path)
    {
        File file = new File(_path);
        if(!file.isFile())
        {
            Toast.makeText(mActivity,"file not exit!",Toast.LENGTH_LONG).show();
            return;
        }
        FrameActivity _activity = (FrameActivity) mActivity;
        String mountpoint = getMountPoint();
        boolean ok_mount = _activity.mount(false,true,true,true,_path,mountpoint);
        boolean ok_ums = _activity.ums(_path,null);
        String tip = "";
        if(ok_mount)
            tip+="mount success!";
        else
            tip+="mount fail!";
        if(ok_ums)
            tip+="usb success!";
        else
            tip+="usb fail!";
        Toast.makeText(mActivity,tip,Toast.LENGTH_LONG).show();
        savehistory(_path);
        mStatusMount = ok_mount;
        mStatusMountpoint = mountpoint;
        mStatusFile = _path;
        mStatusUsb = ok_ums;

        updateUIStatus(false);
    }

    public void quickCreate()
    {
        int size = 0;
        try {
            size  = Integer.parseInt(mCreateEdit.getText().toString());
        }catch(Exception e)
        {
            Toast.makeText(mActivity,"the size is not a valid number",Toast.LENGTH_LONG).show();
        }
        String file = getFilename();
        FrameActivity _activity = (FrameActivity) mActivity;
        boolean ok_create = _activity.createImage(file,size,true);
        if(!ok_create)
        {
            Toast.makeText(mActivity,"file:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
            return;
        }
        String mountpoint = getMountPoint();
        boolean ok_mount = _activity.mount(false,true,true,true,file,mountpoint);
        boolean ok_ums = _activity.ums(file,null);
        String tip ="";
        if(ok_mount)
            tip+="mount success!";
        else
            tip+="mount fail!";
        if(ok_ums)
            tip+="usb success!";
        else
            tip+="usb fail!";
        if(ok_create)
            tip+="create image success!";
        else
            tip+="create image fail!";
        Toast.makeText(mActivity,tip,Toast.LENGTH_LONG).show();
        savehistory(file);
        mStatusMount = ok_mount;
        mStatusFile = file;
        mStatusMountpoint = mountpoint;
        mStatusUsb = ok_ums;
        updateUIStatus(false);
        ShellUnit.execRoot("chmod 777 " + file);
    }

    private void savehistory(String _path)
    {
        for(String str: mHistory)
        {
            if(_path.equals(str))
            {
                mHistory.remove(str);
                break;
            }
        }
        mHistory.addFirst(_path);
        if(mHistory.size()>MAX_HISTORY)
            mHistory.removeLast();

        int _i = 0;
        mSharedPreferences.edit().putInt(KEY_QUICK_HISTORY_COUNT,mHistory.size()).commit();
        for(String str:mHistory)
        {
            mSharedPreferences.edit().putString(KEY_QUICK_HISTORY_BASE+_i,str).commit();
            _i++;
        }
        mAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        View rootView = inflater.inflate(R.layout.quick_start_layout,container,false);
        mView = rootView;
        mUsbBtn = (Button) rootView.findViewById(R.id.quick_usb_btn);
        mMountBtn = (Button) rootView.findViewById(R.id.quick_mount_btn);
        mMountText = (TextView) rootView.findViewById(R.id.quick_mount_txt);
        mUseText = (TextView) rootView.findViewById(R.id.quick_use);
        mCreateBtn = (Button) rootView.findViewById(R.id.quick_create_btn);
        mCreateEdit = (EditText) rootView.findViewById(R.id.quick_create_size);
        mHistoryList = (ListView) rootView.findViewById(R.id.quick_history_list);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        setupList();

        updateUIStatus(true);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuickStartFragment.this.quickCreate();
            }
        });

        mMountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStatusFile==null)
                    return;
                if(QuickStartFragment.this.mStatusMount) {
                    if(QuickStartFragment.this.mStatusMountpoint!=null) {
                        ShellUnit.execBusybox("umount \"" + QuickStartFragment.this.mStatusMountpoint + "\"");
                        if(ShellUnit.stdErr!=null)
                        {
                            Toast.makeText(mActivity,"fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                        }
                        else {
                            mStatusMount = false;
                            QuickStartFragment.this.updateUIStatus(false);
                        }
                    }

                }else
                {
                    if(((FrameActivity)mActivity).mount(false,true,true,true,mStatusFile,getMountPoint())) {
                        mStatusMount = true;
                        QuickStartFragment.this.updateUIStatus(false);
                    }
                    else
                        Toast.makeText(mActivity,"fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                }


            }
        });

        mUsbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStatusFile==null)
                    return;
                if(mStatusUsb) {
                    if (((FrameActivity) mActivity).ums("","mtp"))
                    {
                        mStatusUsb = false;
                    }
                    else
                        Toast.makeText(mActivity,"fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();

                }
                else
                {
                    if(((FrameActivity) mActivity).ums(mStatusFile,null))
                    {
                        mStatusUsb = true;
                    }
                    else
                        Toast.makeText(mActivity,"fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }
}
