package com.sjj.echo.routine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by SJJ on 2017/3/17.
 */

public class LogUnit {
    private String mPath;
    private Thread mThread;
    private StringBuffer mBuffer = new StringBuffer();
    private File mFile;
    private static int sMaxLogSize = 4*1024*1024;
    private FileOutputStream mOutputStream;
    private void checkSize()
    {
        long _size = mFile.length();
        if(_size>sMaxLogSize)
        {
            String _newName = mPath+"1";
            ShellUnit.execBusybox("dd bs=1 if="+mPath+" of="+_newName+" count="+sMaxLogSize/2+" skip="+sMaxLogSize/2);
            try {
                if(ShellUnit.stdErr!=null)
                {
                    mOutputStream.close();
                    mOutputStream = null;
                    return;
                }
                mOutputStream.close();
                mOutputStream=null;
                ShellUnit.execRoot("rm "+mPath);
                ShellUnit.execRoot("mv "+_newName+" "+mPath);
                mOutputStream = new FileOutputStream(mPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void logWrite(String tag,String log)
    {
        String _head = String.format("\n[%5s]>>",tag);
        synchronized(mBuffer){
            mBuffer.append(_head);
            mBuffer.append(log);
        }
    }
    public LogUnit(String _path)
    {
        mPath = _path;
        mFile = new File(_path);
        if(!mFile.isFile())
            return;
        try {
            mOutputStream = new FileOutputStream(mPath);
            mOutputStream.write(("\n ~~~~~"+ new Date().toString()+"~~~~").getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final FileOutputStream finalOutputStream = mOutputStream;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized(mBuffer)
                {
                    while(true) {
                        if(mOutputStream==null)
                            return;
                        checkSize();
                        try {
                            if (mBuffer.length() == 0)
                                mBuffer.wait();
                            finalOutputStream.write(mBuffer.toString().getBytes());
                            mBuffer.setLength(0);
                            mBuffer.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });

    }
}
