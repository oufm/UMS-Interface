package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sjj.echo.explorer.ExplorerActivity;
import com.sjj.echo.routine.ShellUnit;

public class NewImageActivity extends AppCompatActivity {

    private EditText mDirEdit;
    private EditText mNameEdit;
    private EditText mSizeEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_image);
        setTitle(getString(R.string.new_image_title));
        mDirEdit = (EditText) findViewById(R.id.image_dir_edit);
        mNameEdit = (EditText) findViewById(R.id.image_file_edit);
        mSizeEdit = (EditText) findViewById(R.id.image_size_edit);
        Button _dirBtn = (Button) findViewById(R.id.image_dir_btn);
        Button _newBtn = (Button) findViewById(R.id.image_create);

        _dirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewImageActivity.this,ExplorerActivity.class);
                intent.setType("directory/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,2);
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
                    Toast.makeText(NewImageActivity.this,"the size is not a valid number",Toast.LENGTH_LONG).show();
                }
                String _path = mDirEdit.getText().toString();
                if(!_path.endsWith("/"))
                    _path+="/";
                _path+=mNameEdit.getText().toString();
                String cmd = "dd bs=1m if=/dev/zero ";
                cmd+="of=\""+_path+"\" ";
                cmd+="count="+size;
                ShellUnit.execRoot(cmd);
                //there seems a bug with command "dd" ,it always print the information through stderr,to check whether success "ls ..."
                String _stderr = ShellUnit.stdErr;
                ShellUnit.execRoot("ls \""+_path+"\"");
                if(ShellUnit.exitValue==0&&ShellUnit.stdErr==null) {
                    //Toast.makeText(NewImageActivity.this, "create image file success!", Toast.LENGTH_SHORT).show();
                    final String final_path = _path;
                    new AlertDialog.Builder(NewImageActivity.this).setMessage(R.string.new_image_success_tip)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = getIntent();
                                    intent.putExtra(MainActivity.KEY_INTENT_CONFIG,true);
                                    intent.setData(Uri.parse("file://"+ final_path));
                                    NewImageActivity.this.setResult(Activity.RESULT_OK,intent);
                                    NewImageActivity.this.finish();
                                }
                            }).setNegativeButton(getString(R.string.no),null).create().show();
                }
                else {
                    String tip = "create image file fail";
                    if(_stderr!=null)
                        tip+=":"+_stderr;
                    Toast.makeText(NewImageActivity.this, tip, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            String path = uri.toString().substring(7);
//            if(requestCode ==1)
//            {
//                mDevEdit.setText(path);
//                checkLoop();
//            }

            if(requestCode ==2)
            {
                mDirEdit.setText(path);
            }
        }
    }

}
