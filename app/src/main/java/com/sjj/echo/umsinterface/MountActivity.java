package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;
import com.sjj.echo.routine.ShellUnit;

public class MountActivity extends AppCompatActivity {

    EditText mDevEdit;
    EditText mTargetEdit;
    CheckBox mReadonlyCheck;
    CheckBox mLoopCheck;
    Spinner mFilesystemSpinner;

    static final String KEY_MOUNT_DEV = "KEY_MOUNT_DEV";
    static final String KEY_MOUNT_TARGET = "KEY_MOUNT_TARGET";
    static final String KEY_MOUNT_READONLY = "KEY_MOUNT_READONLY";
    static final String KEY_MOUNT_LOOP = "KEY_MOUNT_LOOP";
    static final String KEY_MOUNT_TYPE = "KEY_MOUNT_TYPE";
    SharedPreferences mSharedPreferences;

    @Override
    protected void onPause() {
        super.onPause();
        saveStatus();
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
    }

    /**
     * check whether the 'loop' selection is suitable for the device file
     * */
    private void checkLoop()
    {
        //check if the file is a block device
        String _cmd = "ls -l \""+mDevEdit.getText().toString()+"\"";
        String _output = ShellUnit.execRoot(_cmd);
        if(_output!=null&&ShellUnit.stdErr==null)
        {
            boolean isBlock = false;
            if(_output.startsWith("b"))
                isBlock = true;//block device file
            else if(_output.startsWith("l"))
            {//link file
                String _target = "-> ";
                int _offset = _output.indexOf(_target);
                if(_offset>0)
                {
                    _offset += _target.length();
                    int _offsetEnd = _output.indexOf("\n");
                    if(_offsetEnd > _offset)
                    {
                        String _path = _output.substring(_offset,_offsetEnd);
                        String __output = ShellUnit.execRoot("ls -l \""+_path+"\"");
                        if(_output!=null&&ShellUnit.stdErr==null)
                        {
                            if(__output.startsWith("b"))
                                isBlock = true;
                        }
                    }
                }
            }
            boolean _loop = mLoopCheck.isChecked();
            if(isBlock&&_loop)
            {
                new AlertDialog.Builder(MountActivity.this).setTitle(R.string.tip)
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
                new AlertDialog.Builder(MountActivity.this).setTitle(R.string.tip)
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mount_activitry);
        setTitle(getString(R.string.mount_title));
        Button devBtn = (Button) findViewById(R.id.mount_dev_btn);
        Button targetBtn = (Button) findViewById(R.id.mount_target_btn);
        mDevEdit = (EditText) findViewById(R.id.mount_dev_edit);
        mTargetEdit = (EditText) findViewById(R.id.mount_target_edit);
        mReadonlyCheck = (CheckBox) findViewById(R.id.mount_readonly);
        mLoopCheck = (CheckBox) findViewById(R.id.mount_loop);
        Button mountBtn = (Button) findViewById(R.id.mount_btn);
        mFilesystemSpinner = (Spinner) findViewById(R.id.mount_file_system_spinner);

        final String[] fileSystems = {" "+getString(R.string.filesystem),"minix","ext","ext2","ext3","ext4","Reiserfs","XFS"
                ,"JFS","xia","msdos","umsdos","vfat","ntfs","proc","nfs","iso9660"
                ,"hpfs","sysv","smb","ncpfs"};
        mFilesystemSpinner.setAdapter(new ArrayAdapter<String>(this,R.layout.file_system_list_layout,fileSystems));

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String _dev = mSharedPreferences.getString(KEY_MOUNT_DEV,null);
        if(_dev!=null)
            mDevEdit.setText(_dev);
        String _target = mSharedPreferences.getString(KEY_MOUNT_TARGET,null);
        if(_target!=null)
            mTargetEdit.setText(_target);
        int _type = mSharedPreferences.getInt(KEY_MOUNT_TYPE,0);
        mFilesystemSpinner.setSelection(_type);
        boolean _readonly = mSharedPreferences.getBoolean(KEY_MOUNT_READONLY,false);
        mReadonlyCheck.setChecked(_readonly);
        boolean _loop = mSharedPreferences.getBoolean(KEY_MOUNT_LOOP,false);
        mLoopCheck.setChecked(_loop);

        //checkLoop();

        devBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MountActivity.this,ExplorerActivity.class);
                intent.setType("file/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });

        targetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MountActivity.this,ExplorerActivity.class);
                intent.setType("directory/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,2);
            }
        });

        mountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // mount operation
                boolean _readonly = mReadonlyCheck.isChecked();
                final boolean _loop = mLoopCheck.isChecked();
                String _type = fileSystems[mFilesystemSpinner.getSelectedItemPosition()];

                String cmd = "mount ";
                if(_readonly)
                    cmd +="-o ro ";
                else
                    cmd +="-o rw ";
                if(_loop)
                    cmd +="-o loop ";
                if(!_type.startsWith(" "))
                {
                    cmd +="-t "+_type+" ";
                }
                cmd += "\""+ mDevEdit.getText().toString()+"\" \""+ mTargetEdit.getText().toString()+"\"";
                String busyboxCmd = "busybox "+cmd;
                ShellUnit.execRoot(busyboxCmd);
                if(ShellUnit.exitValue==ShellUnit.EXEC_ERR) {
                    ShellUnit.execRoot(cmd);
                    if(ShellUnit.exitValue!=0||ShellUnit.stdErr!=null)
                        Toast.makeText(MountActivity.this, "execute fail,check busybox and root permission", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(MountActivity.this,"mount success!",Toast.LENGTH_SHORT).show();
                }
                else if(ShellUnit.exitValue!=0||ShellUnit.stdErr!=null)
                    Toast.makeText(MountActivity.this,"mount fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(MountActivity.this,"mount success!",Toast.LENGTH_SHORT).show();
                saveStatus();
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
                checkLoop();
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String path = uri.toString().substring(7);
            if(requestCode ==1)
            {
                mDevEdit.setText(path);
                checkLoop();
            }

            if(requestCode ==2)
            {
                mTargetEdit.setText(path);
            }
        }
    }

}
