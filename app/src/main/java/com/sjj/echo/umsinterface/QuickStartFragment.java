package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;
import com.sjj.echo.routine.ShellUnit;

import java.util.LinkedList;

import static com.sjj.echo.umsinterface.UmsFragment.KEY_DEVICE_HISTORY_BASE;
import static com.sjj.echo.umsinterface.UmsFragment.KEY_DEVICE_HISTORY_COUNT;


/**
 * Created by SJJ on 2017/3/9.
 */

public class QuickStartFragment extends Fragment {

    String mStatusFile;
    String mStatusMountpoint = "";
    boolean mStatusUsb ;
    Boolean mStatusMount;

    Activity mActivity;
    TextView mUseText;
    Button mUsbBtn;
    Button mMountBtn;
    TextView mMountText;
    Button mCreateBtn;
    EditText mCreateEdit;
    ListView mHistoryList;
    TextView mUseFileText;
    TextView mUsbText;
    View mUseGroup;
    View mStatusView;
    View mRefreshIcon;
    private String mImgDir = "/data/ums_mnt";

    View mSavedView;

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

    private void getMount(String _dev,boolean _loop)
    {
        mStatusMount = null;
        if(_dev==null)
            _dev = mStatusFile;
        String _output =ShellUnit.execBusybox("mount|grep '" + _dev + "'");
        if(ShellUnit.stdErr!=null)
        {
            Toast.makeText(mActivity,"fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
            return;
        }
        if (_output.length() > 0) {
            mStatusMount = true;
            int _begin = _output.indexOf(" /");
            int _end = _output.indexOf(" type");
            if(_begin>0&&_end>0&&_end>_begin)
            {
                _begin++;
                mStatusMountpoint = _output.substring(_begin,_end);
            }
        }
        else if(_loop)
        {
            String _lo = ShellUnit.execBusybox("losetup|grep '"+ mStatusFile + "'");
            if(ShellUnit.stdErr!=null)
            {
                Toast.makeText(mActivity,"fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                return;
            }
            if(_lo.length()>0)
            {
                int _end = _lo.indexOf(":");
                if(_end<=0)
                    return;
                getMount(_lo.substring(0,_end),false);
//                int _begin = _lo.indexOf(" /");
//                if(_begin<=0)
//                    return;
//                int _end = _lo.indexOf("\n");
//                if(_end<0)
//                    _end = _lo.length()-1;
//                if(_end<_begin)
//                    return;
//                _begin++;
//                getMount(_lo.substring(_begin,_end),false);
            }

        }
    }

    private void getStatus() {
        //mStatusFile = null;
        MassStorageUnit.refreshStatus();
        String _path = MassStorageUnit.mStatusFile;
        if(_path.length()==0)
            return;
        if (_path == null)
            return;
        if (ShellUnit.execRoot("ls \""+_path+"\"").length()==0)
            return;
        mStatusFile = _path;
        if (MassStorageUnit.mStatusFunction.equals("mass_storage") && MassStorageUnit.mStatusEnable.equals("1"))
            mStatusUsb = true;
        else
            mStatusUsb = false;
        getMount(null,true);
//        String _output =ShellUnit.execRoot("mount|grep '" + _path + "'");
//        if (_output.length() > 0) {
//            mStatusMount = true;
//            //mStatusMountpoint = "";
//            int _begin = _output.indexOf(" /");
//            int _end = _output.indexOf(" type");
//            if(_begin>0&&_end>0&&_end>_begin)
//            {
//                mStatusMountpoint = _output.substring(_begin,_end);
//            }
//        }
    }

    private void updateUIStatus(boolean detect)
    {
        Animation _anim = AnimationUtils.loadAnimation(mActivity,R.anim.circle_rotate);
        mRefreshIcon.startAnimation(_anim);
        if(detect)
            getStatus();
        if(mStatusFile!=null) {
            mUsbBtn.setVisibility(View.VISIBLE);
            mMountBtn.setVisibility(View.VISIBLE);
            String[] _strs =mStatusFile.split("/");
            mUseText.setText(getString(R.string.using) + " " + _strs[_strs.length-1]);
            mUseFileText.setText(mStatusFile);
            if(mStatusUsb) {
                mUsbText.setText(R.string.usb_connection);
                mUsbBtn.setText(R.string.close);
            }
            else {
                mUsbText.setText(R.string.quick_no_usb);
                mUsbBtn.setText(R.string.launch);
            }
            if(mStatusMount==null||!mStatusMount)
            {
                mMountText.setText(R.string.quick_no_mount);
                mMountBtn.setText(R.string.mount);
            }else {
                mMountText.setText(getString(R.string.local_mount) + " " + mStatusMountpoint);
                mMountBtn.setText(R.string.umount);
            }
        }else
        {
            mUseText.setText(getString(R.string.no_using));
            mUseFileText.setText(mStatusFile);
            mUsbBtn.setVisibility(View.INVISIBLE);
            mMountBtn.setVisibility(View.INVISIBLE);
            mUseText.setText(R.string.quick_no_usb);
            mMountText.setText(R.string.quick_no_mount);
        }
        _anim.cancel();

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

        _historyCount = mSharedPreferences.getInt(KEY_DEVICE_HISTORY_COUNT,0);
        for(int i=0;i<_historyCount;i++)
        {
            String _path = mSharedPreferences.getString(KEY_DEVICE_HISTORY_BASE +i,"");
            for(String str:mHistory)
            {
                if(str.equals(_path))
                {
                    mHistory.remove(str);//removed element must break!
                    break;
                }
            }
            mHistory.add(_path);
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

    private void setImgDir()
    {
        String _target = "UMS_SDCARD_REAL_PATH";
        String _output=ShellUnit.execBusybox("touch /sdcard/"+_target+" &&find /data/media/ -name "+_target);
        if(ShellUnit.stdErr!=null)
        {
            Toast.makeText(mActivity,"search sdcard path fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
        }
        int _offset = _output.indexOf(_target);
        if(_offset<=0)
            return;
        mImgDir = _output.substring(0,_offset)+"ums_mnt";

    }

    private String getMountPoint()
    {
        ShellUnit.execRoot("mkdir "+mImgDir+";chomd 777 /data/ums_mnt");
        int num=0;
        while(ShellUnit.execBusybox("mount|grep "+mImgDir+"/mnt"+num).length()>0)
            num++;
        ShellUnit.execRoot("mkdir "+mImgDir+"/mnt"+num+";chmod 777 "+mImgDir+"/mnt"+num);
        return mImgDir+"/mnt"+num;
    }

    public void useImage(String _path)
    {
        ShellUnit.execRoot("ls \""+_path+"\"");
        if(ShellUnit.stdErr!=null)
        {
            Toast.makeText(mActivity,R.string.image_no_find,Toast.LENGTH_LONG).show();
            for(String str:mHistory)
            {
                if(str.equals(_path))
                {
                    saveHistory(_path,false);
                    break;
                }
            }
            return;
        }
        FrameActivity _activity = (FrameActivity) mActivity;
        String mountpoint = getMountPoint();
        Boolean _block = MountFragment.isBlockDev(_path);
        if(_block == null)
            _block =true;
        boolean ok_mount = _activity.mount(false,!_block,!_block,!_block,_path,mountpoint);
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
        if(ok_mount||ok_ums)
            saveHistory(_path);
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
        String _mountPoint = getMountPoint();
        boolean ok_mount = _activity.mount(false,true,true,true,file,_mountPoint);
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
        if(ok_create||ok_mount||ok_ums)
            saveHistory(file);
        mStatusMount = ok_mount;
        mStatusFile = file;
        mStatusMountpoint = _mountPoint;
        mStatusUsb = ok_ums;
        updateUIStatus(false);
        ShellUnit.execBusybox("chmod 777 " + file);
    }

    private void saveHistory(String _path)
    {
        saveHistory(_path,true);
    }

    private void saveHistory(String _path,boolean save)
    {
        for(String str: mHistory)
        {
            if(_path.equals(str))
            {
                mHistory.remove(str);
                break;
            }
        }
        if(save)
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
        if(mSavedView !=null)
            return mSavedView;
        View rootView = inflater.inflate(R.layout.quick_start_layout,container,false);
        mSavedView = rootView;
        mUsbBtn = (Button) rootView.findViewById(R.id.quick_usb_btn);
        mMountBtn = (Button) rootView.findViewById(R.id.quick_mount_btn);
        mMountText = (TextView) rootView.findViewById(R.id.quick_mount_txt);
        mUseText = (TextView) rootView.findViewById(R.id.quick_use);
        mCreateBtn = (Button) rootView.findViewById(R.id.quick_create_btn);
        mCreateEdit = (EditText) rootView.findViewById(R.id.quick_create_size);
        mHistoryList = (ListView) rootView.findViewById(R.id.quick_history_list);
        mUseFileText = (TextView) rootView.findViewById(R.id.quick_use_detail);
        mUsbText = (TextView) rootView.findViewById(R.id.quick_usb_text);
        mUseGroup = rootView.findViewById(R.id.quick_use_group);
        mRefreshIcon = rootView.findViewById(R.id.refresh_icon);
        mStatusView = rootView.findViewById(R.id.quick_status);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                setImgDir();
                setupList();
                updateUIStatus(true);
            }
        },300);
        mStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStatusMountpoint!=null&&mStatusMountpoint.length()>0) {
                    Intent intent = new Intent(mActivity, ExplorerActivity.class);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    if(mStatusMountpoint!=null&&mStatusMountpoint.length()>0)
                        intent.setDataAndType(Uri.parse(mStatusMountpoint),"directory/*");
                    else
                        intent.setDataAndType(Uri.parse("/"),"directory/*");
                    mActivity.startActivity(intent);
                }
            }
        });
        mUseGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUIStatus(true);
            }
        });
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
                if(QuickStartFragment.this.mStatusMount!=null&&QuickStartFragment.this.mStatusMount) {
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
                    Boolean _block = MountFragment.isBlockDev(mStatusFile);
                    if(_block == null)
                        _block =false;
                    String _mountpoint = getMountPoint();
                    if(((FrameActivity)mActivity).mount(false,!_block,!_block,!_block,mStatusFile,_mountpoint)) {
                        mStatusMount = true;
                        mStatusMountpoint = _mountpoint;
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
                    if (((FrameActivity) mActivity).ums("","mtp,ffs"))
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
                updateUIStatus(false);
            }
        });

        return rootView;
    }
}
