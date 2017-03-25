package com.sjj.echo.routine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by SJJ on 2017/3/17.
 */

public class LogUnit {
    class LogThread extends Thread
    {
        public boolean mClose = false;
        public boolean mRunning = false;
        @Override
        public void run() {
            mRunning = true;
            byte[] _buf;
            while(true) {
                if(mClose)
                    break;
                if(mOutputStream==null)
                    return;
                checkSize();
                try {
                    synchronized(mBuffer) {
                        if (mBuffer.size() == 0) {
                            mBuffer.wait(1000*60*3);//thread will exit if no log in three minutes.
                            if(mBuffer.size()==0)
                                break;
                        }
                        _buf = mBuffer.poll().getBytes();
                    }
                    mOutputStream.write(_buf);
                } catch (InterruptedException e) {
                    mRunning = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    mRunning = false;
                    e.printStackTrace();
                }

            }
            mRunning = false;
        }
    }
    private String mPath;
    //private Thread mThread;
    private Queue<String> mBuffer = new LinkedList<>();
    private File mFile;
    private static int sMaxLogSize = 2*1024*1024;
    private FileOutputStream mOutputStream;
    private LogThread mThread;
    private static String sDefaultPath = "/data/data/com.sjj.echo.umsinterface/ums.log";
    private static LogUnit sDefaultLog;
    static {
        sDefaultLog = new LogUnit(sDefaultPath);
    }
    public static LogUnit getDefaultLog()
    {
        return sDefaultLog;
    }
    private void checkSize()
    {
        long _size = mFile.length();
        if(_size>sMaxLogSize)
        {
            String _newName = mPath+"1";
            ShellUnit.execNoLog(ShellUnit.BUSYBOX+" dd bs=1 if="+mPath+" of="+_newName+" count="+sMaxLogSize/2+" skip="+(_size-sMaxLogSize/2));
            try {
//                if(ShellUnit.stdErr!=null)
//                {
//                    mOutputStream.close();
//                    mOutputStream = null;
//                    return;
//                }
                mOutputStream.close();
                mOutputStream.flush();
                mOutputStream=null;
                ShellUnit.execNoLog("busybox mv -f "+_newName+" "+mPath);
                ShellUnit.execNoLog("chmod 777 "+mPath);
                mOutputStream = new FileOutputStream(mPath,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void logWrite(String tag,String log)
    {
        if(mOutputStream==null)
            return;
        if(!mThread.mRunning) {
            mThread = new LogThread();//thread already exit
            mThread.start();
        }
        String _str = String.format("\n[%5s]>>%s",tag,log);
        synchronized(mBuffer){
           mBuffer.add(_str);
            mBuffer.notify();
        }
    }
    public void close()
    {
        mThread.mClose = true;
        mThread = null;
        mOutputStream = null;
        logWrite("EXIT","EXIT");//awake the waiting thread.
    }
    public LogUnit(String _path)
    {
        mPath = _path;
        init();
    }
    public void restart()
    {
        if(mThread==null)
            init();
    }
    private void init()
    {
        mFile = new File(mPath);
        try {
            if(!mFile.isFile())
                if(!mFile.createNewFile())
                    return;

            mOutputStream = new FileOutputStream(mPath,true);
            mOutputStream.write(("\n ~~~~~"+ new Date().toString()+"~~~~").getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mThread = new LogThread();
        mThread.start();
    }
}
