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

import static com.sjj.echo.routine.ShellUnit.execRoot;
import static com.sjj.echo.umsinterface.FrameActivity.logInfo;


/**
 * Created by SJJ on 2017/3/9.
 */

public class QuickStartFragment extends Fragment {

    String mStatusFile;
    String mStatusMountpoint = "";
    boolean mStatusUsb ;
    Boolean mStatusMount;
    String mStatusSD;

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
    ViewGroup mExtendGroup;
    View mExtendView;
    Button mExtendBtn;
    TextView mExtendText;
    public static String mImgDir = "/data/ums_mnt";

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

    /**
     * return the two dir are same or not
     * */
    static public boolean sameDir(String dir1,String dir2)
    {
        String targetName = "/UMS_CHECK_DIR";
        String val = Math.random()+"";
        execRoot("echo '"+val+"' > '"+dir1+targetName+"'");
        execRoot("chmod 777 "+"'"+dir1+targetName+"'");
        String output = execRoot("cat "+"'"+dir2+targetName+"'");
        execRoot("rm "+"'"+dir1+targetName+"'");
        if(output.compareTo(val)==0)
            return true;
        else
            return false;

    }

    /**
     * search the mount information
     * */
    private void getMount(String _dev,boolean _loop)
    {
        mStatusMount = null;
        if(_dev==null)
            _dev = mStatusFile;
        String _output =ShellUnit.execBusybox("mount|"+ShellUnit.BUSYBOX+" grep '" + _dev + "'");
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
            String _lo = ShellUnit.execBusybox("losetup|"+ShellUnit.BUSYBOX+" grep '"+ mStatusFile + "'");
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
            }

        }
    }

    private void getSd()
    {
        if(mStatusSD!=null)
            return;
        getSdByMount();
        if(mStatusSD!=null)
            return;
        String out = ShellUnit.execRoot("ls /dev/block/vold/|"+ShellUnit.BUSYBOX+" grep public");
        if(ShellUnit.stdErr==null&&out.length()>0)
        {
            int end = out.indexOf("\n");
            if(end<0)
                end = out.length();
            mStatusSD = "/dev/block/vold/"+out.substring(0,end);
        }
    }

    private void getSdByMount()
    {
        String out = ShellUnit.execBusybox("mount|"+ShellUnit.BUSYBOX+" grep '/mnt/media_rw'");
        if(out.length()<2)
            return;
        int end = out.indexOf(" on");
        if(end<0)
            return;
        mStatusSD = out.substring(0,end);
    }

    /***
     * search current status
     */
    private void getStatus() {
        //mStatusFile = null;
        getSd();
        MassStorageUnit.refreshStatus();
        String _path = MassStorageUnit.mStatusFile;
        if (_path == null)
            return;
        if(_path.length()==0)
            return;
        if (execRoot("ls \""+_path+"\"").length()==0)
            return;
        if(mStatusFile==null||!sameDir(mStatusFile,_path))
            mStatusFile = _path;
        if (MassStorageUnit.mStatusFunction.equals("mass_storage") && MassStorageUnit.mStatusEnable.equals("1"))
            mStatusUsb = true;
        else
            mStatusUsb = false;
        getMount(null,true);
    }

    /**
     * update status for UI
     * @param detect whether to research the status.
     * */
    private void updateUIStatus(boolean detect)
    {
        logInfo("quick_start updateUIStatus detect="+detect);
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
            mUsbText.setText(R.string.quick_no_usb);
            mMountText.setText(R.string.quick_no_mount);
        }
        if(mStatusSD!=null)
        {
            if(mExtendGroup.getChildCount()==0)
                mExtendGroup.addView(mExtendView);
            mExtendText.setText(mStatusSD);
        }else {
            if(mExtendGroup.getChildCount()>0)
                mExtendGroup.removeAllViews();
        }
        _anim.cancel();

    }

    /**
     * init history list
     * */
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

    /**
     * allocate an image path
     * */
    public String getFilename()
    {
        logInfo("quick_start getFilename");
        execRoot("mkdir /sdcard/ums_img;chomd 777 /sdcard/ums_img");
        int num=0;
        while (execRoot("ls /sdcard/ums_img/"+num+".img").length()>0)
            num++;
        return "/sdcard/ums_img/"+num+".img";
    }

    /**
     * choose the default mount point base dir.
     * */
    private void setImgDir()
    {
        logInfo("quick_start setImgDir");
        String _target = "UMS_SDCARD_REAL_PATH";
        ShellUnit.execBusybox("touch /sdcard/"+_target);
        String _output=ShellUnit.execBusybox("find /data/media/ -maxdepth 3 -name "+_target);
        if(ShellUnit.stdErr!=null||_output.length()<=1)
        {
            _output=ShellUnit.execBusybox("find /mnt/media_rw/ -maxdepth 3 -name "+_target);
        }
        execRoot("rm /sdcard/"+_target);
//        if(ShellUnit.stdErr!=null)
//        {
//            Toast.makeText(mActivity,"search sdcard path fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
//        }
        int _offset = _output.indexOf(_target);
        if(_offset<=0)
            return;
        mImgDir = _output.substring(0,_offset)+"ums_mnt";

    }

    /**
     * allocate a mount point
     * */
    public String getMountPoint()
    {
        logInfo("quick_start getMountPoint");
        execRoot("mkdir "+mImgDir+";chmod 777 '"+mImgDir+"'");
        int num=0;
        while(ShellUnit.execBusybox("mount|"+ShellUnit.BUSYBOX+" grep "+mImgDir+"/mnt"+num).length()>0)
            num++;
        execRoot("mkdir "+mImgDir+"/mnt"+num+";chmod 777 "+mImgDir+"/mnt"+num);
        return mImgDir+"/mnt"+num;
    }

    /**
     * use the item in history automatically
     * */
    public void useImage(String _path)
    {
        logInfo("quick_start useImage path="+_path);
        execRoot("ls \""+_path+"\"");
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

        boolean ok_ums = _activity.ums(_path,null);
        MassStorageUnit.refreshStatus();
        if(MassStorageUnit.mStatusFile!=null&&MassStorageUnit.mStatusFile.length()>0)//change it to the original path
            _path = MassStorageUnit.mStatusFile;
        boolean ok_mount = true;
        String tip = "";
        String _out = ShellUnit.execBusybox("mount|"+ShellUnit.BUSYBOX+" grep \""+_path+"\"");
        if(ShellUnit.stdErr==null&&_out.length()==0) {
            String mountpoint = getMountPoint();
            ok_mount = _activity.mount(false, true, _path, mountpoint);
            if(ok_mount) {
                mStatusMountpoint = mountpoint;
                tip += "mount success!";
            }
            else
                tip+="mount fail!";
        }else
        {
            ok_mount = true;
            int _start = _out.indexOf(" /");
            if(_start>0)
            {
                _start++;
                int _end = _out.indexOf(" ",_start);
                if(_end>0)
                    mStatusMountpoint = _out.substring(_start,_end);
            }
        }
        if(ok_ums)
            tip+="usb success!";
        else
            tip+="usb fail!";
        if(!ok_mount||!ok_ums)
            Toast.makeText(mActivity,tip,Toast.LENGTH_LONG).show();
        if(ok_mount||ok_ums)
            saveHistory(_path);
        mStatusMount = ok_mount;
        mStatusFile = _path;
        mStatusUsb = ok_ums;
        updateUIStatus(false);
    }

    /**
     * create a new image and use it automatically
     * */
    public void quickCreate()
    {
        logInfo("quick_start quickCreate");
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
            Toast.makeText(mActivity,getString(R.string.fail)+":"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
            return;
        }
        String _mountPoint = getMountPoint();
        boolean ok_ums = _activity.ums(file,null);
        MassStorageUnit.refreshStatus();
        if(MassStorageUnit.mStatusFile!=null&&MassStorageUnit.mStatusFile.length()>0)
            file = MassStorageUnit.mStatusFile;
        boolean ok_mount = _activity.mount(false,true,file,_mountPoint);
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
        if(!ok_create||!ok_ums||!ok_mount)
            Toast.makeText(mActivity,tip,Toast.LENGTH_LONG).show();
        logInfo("quickCreate:"+tip);
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

    public void saveHistory(String _path,boolean save)
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
        mExtendBtn = (Button) rootView.findViewById(R.id.quick_extend_sd);
        mExtendGroup = (ViewGroup) rootView.findViewById(R.id.extend_sd_root);
        mExtendText = (TextView) rootView.findViewById(R.id.extend_sd_text);
        mExtendView = rootView.findViewById(R.id.extend_sd_view);
        mExtendGroup.removeViewAt(0);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                setupList();
            }
        },100);
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateUIStatus(true);
            }
        },200);
//        rootView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setImgDir();
//            }
//        },300);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setImgDir();
            }
        }).start();
        mStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStatusMountpoint!=null&&mStatusMountpoint.length()>0) {
                    String _path = mStatusMountpoint;
                    if(_path.startsWith("/data/media")||_path.startsWith("/mnt/media_rw"))
                    {
                        if(_path.startsWith(mImgDir))
                        {
                            _path = _path.replace(mImgDir,"/sdcard/ums_mnt");
                        }
                    }
                    if(_path.startsWith("/mnt/media_rw"))
                    {
                        String npath = _path.replace("/mnt/media_rw","/storage");
                        ShellUnit.execRoot("ls \""+npath+"\"");
                        if(ShellUnit.stdErr==null)
                        {
                            _path = npath;
                        }

                    }
                    Intent intent = new Intent(mActivity, ExplorerActivity.class);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setDataAndType(Uri.parse(_path),"directory/*");
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
                logInfo("quick_start mMountBtn mStatusMount="+(mStatusMount!=null&&mStatusMount));
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
                    String _mountpoint = getMountPoint();
                    if(((FrameActivity)mActivity).mount(false,true,mStatusFile,_mountpoint)) {
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
                logInfo("quick_start mUsbBtn mStatusUsb="+mStatusUsb);
                if(mStatusUsb) {
                    if (((FrameActivity) mActivity).ums(mStatusFile,"mtp,adb"))
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

        mExtendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameActivity.logInfo("QuickStartFragment.this.useImage mStatusSD=QuickStartFragment.this.mStatusSD");
                QuickStartFragment.this.useImage(QuickStartFragment.this.mStatusSD);
            }
        });
        return rootView;
    }
}
