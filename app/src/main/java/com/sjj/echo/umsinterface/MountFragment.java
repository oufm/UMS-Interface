package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;
import com.sjj.echo.routine.ShellUnit;

import static com.sjj.echo.umsinterface.FrameActivity.logInfo;

/**
 * Created by SJJ on 2017/3/8.
 */

public class MountFragment extends Fragment {


    EditText mDevEdit;
    EditText mTargetEdit;
    CheckBox mReadonlyCheck;
    CheckBox mLoopCheck;
    Spinner mFilesystemSpinner;
    Activity mActivity;

    CheckBox mCheckUserRead;
    CheckBox mCheckUserWrite;
    CheckBox mCheckUserExexcute;
    CheckBox mCheckGroupRead;
    CheckBox mCheckGroupWrite;
    CheckBox mCheckGroupExexcute;
    CheckBox mCheckOtherRead;
    CheckBox mCheckOtherWrite;
    CheckBox mCheckOtherExexcute;

    EditText mIocharset;
    EditText mCodepage;

    CheckBox mCheckCharset;
    CheckBox mCheckMask;
    View mContentCharset;
    View mContentMask;

    static final String KEY_MOUNT_DEV = "KEY_MOUNT_DEV";
    static final String KEY_MOUNT_TARGET = "KEY_MOUNT_TARGET";
    static final String KEY_MOUNT_READONLY = "KEY_MOUNT_READONLY";
    static final String KEY_MOUNT_LOOP = "KEY_MOUNT_LOOP";
    static final String KEY_MOUNT_TYPE = "KEY_MOUNT_TYPE";
    static final String KEY_MOUNT_MASK = "KEY_MOUNT_MASK";
    static final String KEY_MOUNT_CHARSET = "KEY_MOUNT_CHARSET";
    SharedPreferences mSharedPreferences;
    View mView;

    public void init(Activity activity)
    {
        mActivity = activity;
    }

    public void doConfig(String dev,String target)
    {
        if(dev!=null)
            mDevEdit.setText(dev);
        if(target!=null)
            mTargetEdit.setText(target);
    }

    private void checkCharset()
    {
        if(mCheckCharset.isChecked())
            mContentCharset.setVisibility(View.VISIBLE);
        else
            mContentCharset.setVisibility(View.INVISIBLE);
    }
    private void checkMask()
    {
        if(mCheckMask.isChecked())
            mContentMask.setVisibility(View.VISIBLE);
        else
            mContentMask.setVisibility(View.INVISIBLE);
    }

    protected String getmask()
    {
        int _userflag = 0;
        if(!mCheckUserRead.isChecked())
            _userflag += 4;
        if(!mCheckUserWrite.isChecked())
            _userflag += 2;
        if(!mCheckUserExexcute.isChecked())
            _userflag +=1;
        int _groupflag = 0;
        if(!mCheckGroupRead.isChecked())
            _groupflag += 4;
        if(!mCheckGroupWrite.isChecked())
            _groupflag += 2;
        if(!mCheckGroupExexcute.isChecked())
            _groupflag +=1;
        int _otherflag = 0;
        if(!mCheckOtherRead.isChecked())
            _otherflag += 4;
        if(!mCheckOtherWrite.isChecked())
            _otherflag += 2;
        if(!mCheckOtherExexcute.isChecked())
            _otherflag +=1;
        return "0"+_userflag+_groupflag+_otherflag;
    }

    public static Boolean isBlockDev(String _file)
    {
        String _cmd = "ls -l \""+_file+"\"";
        String _output = ShellUnit.execRoot(_cmd);
        if(_output!=null&&ShellUnit.stdErr==null) {
            boolean isBlock = false;
            if (_output.startsWith("b"))
                isBlock = true;//block device file
            else if (_output.startsWith("l")) {//link file
                String _target = "-> ";
                int _offset = _output.indexOf(_target);
                if (_offset > 0) {
                    _offset += _target.length();
                    int _offsetEnd = _output.indexOf("\n");
                    if (_offsetEnd > _offset) {
                        String _path = _output.substring(_offset, _offsetEnd);
                        String __output = ShellUnit.execRoot("ls -l \"" + _path + "\"");
                        if (_output != null && ShellUnit.stdErr == null) {
                            if (__output.startsWith("b"))
                                isBlock = true;
                        }
                    }
                }
            }
            return isBlock;
        }
        return null;
    }

    private Boolean isBlockDev()
    {
        return isBlockDev(mDevEdit.getText().toString());
        //check if the file is a block device

    }

    /**
     * check whether the 'loop' selection is suitable for the device file
     * */
    private void checkLoop()
    {
        Boolean isBlock = isBlockDev();
        if(isBlock==null)
            return;
        boolean _loop = mLoopCheck.isChecked();
        if(isBlock&&_loop)
        {
            new AlertDialog.Builder(mActivity).setTitle(R.string.tip)
                    .setMessage(R.string.deselect_loop_tip)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //mLoopCheck.setSelected(false);
                            mLoopCheck.setChecked(false);
                        }
                    }).setNegativeButton(getString(R.string.no), null).create().show();
        }
        if(!isBlock&&!_loop)
        {
            new AlertDialog.Builder(mActivity).setTitle(R.string.tip)
                    .setMessage(R.string.select_loop_tip)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //mLoopCheck.setSelected(true);
                            mLoopCheck.setChecked(true);
                        }
                    }).setNegativeButton(getString(R.string.no), null).create().show();
        }
    }

    /**
     * save the mount select information
     * */
    protected void saveStatus()
    {
        mSharedPreferences.edit().putString(KEY_MOUNT_DEV,mDevEdit.getText().toString()).commit();
        mSharedPreferences.edit().putString(KEY_MOUNT_TARGET,mTargetEdit.getText().toString()).commit();
        mSharedPreferences.edit().putInt(KEY_MOUNT_TYPE,mFilesystemSpinner.getSelectedItemPosition()).commit();
        mSharedPreferences.edit().putBoolean(KEY_MOUNT_READONLY,mReadonlyCheck.isChecked()).commit();
        mSharedPreferences.edit().putBoolean(KEY_MOUNT_LOOP,mLoopCheck.isChecked()).commit();
        mSharedPreferences.edit().putBoolean(KEY_MOUNT_MASK,mCheckMask.isChecked()).commit();
        mSharedPreferences.edit().putBoolean(KEY_MOUNT_CHARSET,mCheckCharset.isChecked()).commit();
    }

    public boolean mount(boolean _readonly,boolean _loop,boolean _charset,boolean _mask,String _source,String _point)
    {
        String _iocharset = null;
        String _codepage = null;
        String _maskStr = null;
        if(_charset)
        {
            _iocharset = "utf8";
            _codepage = "936";
        }
        if(_mask)
            _maskStr = "0000";

        return mount(_readonly,_loop,_iocharset,_codepage,_maskStr,_source,_point,null);
    }

    public boolean mount(boolean _readonly,boolean _loop,String _iocharset,String _codepage,String _mask,String _source,String _point,String _type)
    {
        logInfo("mount(readonly="+_readonly+",loop="+_loop+",iocharset="+ (_iocharset==null?"null":_iocharset)
                +",codepage="+ (_codepage==null?"null":_codepage)+",mask="+(_mask==null?"null":_mask)
                +",source="+_source+",mountpoint="+_point+",type="+ (_type==null?"null":_type)+")");
        String cmd = "mount ";
        cmd +=" ";
        if(_type!=null)
        {
            cmd +="-t "+_type+" ";
        }
        if(_readonly)
            cmd +="-o ro";
        else
            cmd +="-o rw";
        if(_iocharset!=null&&_codepage!=null)
            cmd +=",iocharset="+_iocharset+",codepage="+_codepage;
        if(_mask!=null)
        {
            cmd +=",fmask="+_mask+",dmask="+_mask;
        }
        if(_loop)
            cmd +=",loop";
        cmd +=" ";
        cmd += "\""+ _source+"\" \""+ _point+"\"";
        ShellUnit.execBusybox(cmd);
        if(ShellUnit.exitValue==ShellUnit.EXEC_ERR) {
            return false;
        }
        else if(ShellUnit.exitValue!=0||ShellUnit.stdErr!=null)
            return false;
        else
            return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        View rootView = inflater.inflate(R.layout.activity_add_mount_activitry,container,false);
        mView = rootView;

        mCheckGroupExexcute = (CheckBox) rootView.findViewById(R.id.check_group_execute);
        mCheckGroupWrite = (CheckBox) rootView.findViewById(R.id.check_group_write);
        mCheckGroupRead = (CheckBox) rootView.findViewById(R.id.check_group_read);
        mCheckOtherExexcute = (CheckBox) rootView.findViewById(R.id.check_other_execute);
        mCheckOtherWrite = (CheckBox) rootView.findViewById(R.id.check_other_write);
        mCheckOtherRead = (CheckBox) rootView.findViewById(R.id.check_other_read);
        mCheckUserExexcute = (CheckBox) rootView.findViewById(R.id.check_user_execute);
        mCheckUserWrite = (CheckBox) rootView.findViewById(R.id.check_user_write);
        mCheckUserRead = (CheckBox) rootView.findViewById(R.id.check_user_read);

        mIocharset = (EditText) rootView.findViewById(R.id.edit_iocharset);
        mCodepage = (EditText) rootView.findViewById(R.id.edit_codepage);
        mCheckMask = (CheckBox) rootView.findViewById(R.id.check_mask);
        mContentMask = rootView.findViewById(R.id.content_mask);
        mCheckCharset = (CheckBox) rootView.findViewById(R.id.check_charset);
        mContentCharset = rootView.findViewById(R.id.content_charset);

        Button devBtn = (Button) rootView.findViewById(R.id.mount_dev_btn);
        Button targetBtn = (Button) rootView.findViewById(R.id.mount_target_btn);
        mDevEdit = (EditText) rootView.findViewById(R.id.mount_dev_edit);
        mTargetEdit = (EditText) rootView.findViewById(R.id.mount_target_edit);
        mReadonlyCheck = (CheckBox) rootView.findViewById(R.id.mount_readonly);
        mLoopCheck = (CheckBox) rootView.findViewById(R.id.mount_loop);
        final Button mountBtn = (Button) rootView.findViewById(R.id.mount_btn);
        mFilesystemSpinner = (Spinner) rootView.findViewById(R.id.mount_file_system_spinner);


        final String[] fileSystems = {" "+getString(R.string.filesystem),"minix","ext","ext2","ext3","ext4","Reiserfs","XFS"
                ,"JFS","xia","msdos","umsdos","vfat","ntfs","proc","nfs","iso9660"
                ,"hpfs","sysv","smb","ncpfs"};
        mFilesystemSpinner.setAdapter(new ArrayAdapter<String>(mActivity,R.layout.file_system_list_layout,fileSystems));

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String _dev = mSharedPreferences.getString(KEY_MOUNT_DEV,null);
        if(_dev!=null)
            mDevEdit.setText(_dev);
        final String _target = mSharedPreferences.getString(KEY_MOUNT_TARGET,null);
        if(_target!=null)
            mTargetEdit.setText(_target);
        int _type = mSharedPreferences.getInt(KEY_MOUNT_TYPE,0);
        mFilesystemSpinner.setSelection(_type);
        boolean _readonly = mSharedPreferences.getBoolean(KEY_MOUNT_READONLY,false);
        mReadonlyCheck.setChecked(_readonly);
        boolean _loop = mSharedPreferences.getBoolean(KEY_MOUNT_LOOP,false);
        mLoopCheck.setChecked(_loop);
        mCheckMask.setChecked(mSharedPreferences.getBoolean(KEY_MOUNT_MASK,false));
        mCheckCharset.setChecked(mSharedPreferences.getBoolean(KEY_MOUNT_CHARSET,false));
        if(fileSystems[mFilesystemSpinner.getSelectedItemPosition()].equals("vfat"))
            mCheckMask.setChecked(true);
        checkMask();
        checkCharset();

        mCheckMask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MountFragment.this.checkMask();
            }
        });
        mCheckCharset.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MountFragment.this.checkCharset();
            }
        });
        //checkLoop();

        devBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity,ExplorerActivity.class);
                intent.setType("file/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mActivity.startActivityForResult(intent,3);
            }
        });

        targetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity,ExplorerActivity.class);
                intent.setType("directory/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mActivity.startActivityForResult(intent,4);
            }
        });

        mountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkLoop();
                // mount operation
                mTargetEdit.requestFocus();
                mountBtn.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean _readonly = mReadonlyCheck.isChecked();
                        final boolean _loop = mLoopCheck.isChecked();
                        boolean _mask = mCheckMask.isChecked();
                        boolean _charset = mCheckCharset.isChecked();
                        String _type = fileSystems[mFilesystemSpinner.getSelectedItemPosition()];
                        if(_type.startsWith(" "))
                            _type = null;
                        String _iocharset = null;
                        String _codepage = null;
                        String _maskStr = null;
                        if(_charset)
                        {
                            _iocharset = mIocharset.getText().toString();
                            _codepage = mCodepage.getText().toString();
                        }
                        if(_mask)
                            _maskStr = getmask();
                        if(!mount(_readonly,_loop,_iocharset,_codepage,_maskStr,mDevEdit.getText().toString()
                                ,mTargetEdit.getText().toString(),_type))
                        {
                            Toast.makeText(mActivity,getString(R.string.mount)+" "+getString(R.string.fail)+":"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                        }else
                        {
                            Toast.makeText(mActivity,getString(R.string.mount)+" "+getString(R.string.success),Toast.LENGTH_LONG).show();
                        }

                        saveStatus();
                    }
                }, 500);

            }
        });

        mLoopCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLoop();
            }
        });

        mDevEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //checkLoop();
                mDevEdit.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Boolean _isBlock = isBlockDev();
                        if(_isBlock!=null)
                        {
                            if(_isBlock)
                                mLoopCheck.setChecked(false);
                            else
                                mLoopCheck.setChecked(true);
                            //checkLoop();
                        }
                    }
                }, 200);

            }
        });
        mFilesystemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(fileSystems[position].equals("vfat"))
                    mCheckMask.setChecked(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveStatus();
    }
}
