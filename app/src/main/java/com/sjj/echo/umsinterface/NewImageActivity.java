package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sjj.echo.routine.ShellUnit;

public class NewImageActivity extends AppCompatActivity {

    private EditText mDirEdit;
    private EditText mNameEdit;
    private EditText mSizeEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_image);
        setTitle("create a image file");
        mDirEdit = (EditText) findViewById(R.id.image_dir_edit);
        mNameEdit = (EditText) findViewById(R.id.image_file_edit);
        mSizeEdit = (EditText) findViewById(R.id.image_size_edit);
        Button _dirBtn = (Button) findViewById(R.id.image_dir_btn);
        Button _newBtn = (Button) findViewById(R.id.image_create);

        _dirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewImageActivity.this,com.sjj.echo.explorer.MainActivity.class);
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
                if(ShellUnit.exitValue==0&&ShellUnit.stdErr==null)
                    Toast.makeText(NewImageActivity.this,"create image file success!",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(NewImageActivity.this,"create image file fail:"+ShellUnit.stdErr!=null?ShellUnit.stdErr:"",Toast.LENGTH_LONG).show();
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
