package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;
import com.sjj.echo.routine.ShellUnit;

import static com.sjj.echo.umsinterface.FrameActivity.logInfo;

/**
 * Created by SJJ on 2017/3/8.
 */

public class CreateImageFragment extends Fragment {

    private EditText mDirEdit;
    private EditText mNameEdit;
    private EditText mSizeEdit;
    private Activity mActivity;
    private Spinner mFormat;
    public String mStderr;
    private View mView;

    public void init(Activity activity){
        mActivity =activity;
    }

    public void doConfig(String dir)
    {
        if(dir!=null)
            mDirEdit.setText(dir);
    }

    public boolean createImage(String _path, int size, boolean format)
    {
        logInfo("createImage(path="+_path+",size="+size+",format="+format+")");
        String cmd = "dd bs=1048576 if=/dev/zero ";
        cmd+="of=\""+_path+"\" ";
        cmd+="count="+size;
        if(format)
            cmd+="&&"+ShellUnit.BUSYBOX+" mkfs.vfat \""+_path+"\"";
        ShellUnit.execBusybox(cmd);
        //there seems a bug with command "dd" ,it always print the information through stderr,to check whether success "ls ..."
        mStderr = ShellUnit.stdErr;
        ShellUnit.execRoot("ls \""+_path+"\"");
        if(ShellUnit.stdErr==null)
            return true;
        else
            return false;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        View rootView = inflater.inflate(R.layout.activity_new_image,container,false);
        mView = rootView;

        mDirEdit = (EditText) rootView.findViewById(R.id.image_dir_edit);
        mNameEdit = (EditText) rootView.findViewById(R.id.image_file_edit);
        mSizeEdit = (EditText) rootView.findViewById(R.id.image_size_edit);
        Button _dirBtn = (Button) rootView.findViewById(R.id.image_dir_btn);
        Button _newBtn = (Button) rootView.findViewById(R.id.image_create);
        Button _allocBtn = (Button) rootView.findViewById(R.id.image_allocate);
        mFormat = (Spinner) rootView.findViewById(R.id.image_format);
        mFormat.setSelection(1);
        _allocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _path = ((FrameActivity)mActivity).getImagePath();
                int _offset = _path.lastIndexOf("/");
                String _dir = _path.substring(0,_offset);
                String _name = _path.substring(_offset+1,_path.length());
                mDirEdit.setText(_dir);
                mNameEdit.setText(_name);
            }
        });
        _dirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity,ExplorerActivity.class);
                intent.setType("directory/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mActivity.startActivityForResult(intent,6);
            }
        });

        _newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = 0;
                try {
                    size  = Integer.parseInt(mSizeEdit.getText().toString());
                }catch(Exception e)
                {
                    Toast.makeText(mActivity,"the size is not a valid number",Toast.LENGTH_LONG).show();
                }
                String _path = mDirEdit.getText().toString();
                if(!_path.endsWith("/"))
                    _path+="/";
                _path+=mNameEdit.getText().toString();
                boolean _format = mFormat.getSelectedItemPosition()>0;
                boolean _ok = createImage(_path,size,_format);
                if(_ok) {
                    //Toast.makeText(NewImageActivity.this, "create image file success!", Toast.LENGTH_SHORT).show();
                    final String final_path = _path;
                    new AlertDialog.Builder(mActivity).setMessage(R.string.new_image_success_tip)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((FrameActivity)mActivity).umsRun(final_path);
//                                    Intent intent = mActivity.getIntent();
//                                    intent.putExtra(UmsFragment.KEY_INTENT_CONFIG,true);
//                                    intent.setData(Uri.parse("file://"+ final_path));
//                                    mActivity.setResult(Activity.RESULT_OK,intent);
                                   //TO DO: switch to tab ums
                                }
                            }).setNegativeButton(getString(R.string.no),null).create().show();
                }
                else {
                    String tip = "create image file fail";
                    if(mStderr!=null)
                        tip+=":"+mStderr;
                    Toast.makeText(mActivity, tip, Toast.LENGTH_LONG).show();
                }
            }
        });


        return rootView;
    }
}
