package com.sjj.echo.routine;

import com.sjj.echo.umsinterface.FrameActivity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by SJJ on 2017/1/1.
 */

public class ShellUnit {

    public static String BUSYBOX = FrameActivity.APP_DIR+"/bin/busybox ";

    public static final int EXEC_ERR = 1010100;
    /**
     * last error output for root,null if success
     * */
    static public String stdErr;
    /**
     * last exit code for root
     * */
    static public int exitValue;
    /**
     * execute the command through shell
     * @param root should be execute as root
     *@param cmd command
     * */
    static private String exec(String cmd,boolean root)
    {
        String outString = "";
        try {
            char[] buff = new char[1024*30];
            Process process;
            if(root)
                process = Runtime.getRuntime().exec("su");
            else
                process = Runtime.getRuntime().exec("sh");
            OutputStreamWriter stdin = new OutputStreamWriter(process.getOutputStream());
            InputStreamReader stdout = new InputStreamReader(process.getInputStream());
            stdin.write(cmd+"\n");
            stdin.write("exit\n");
            stdin.flush();
            exitValue = process.waitFor();
            //if(exitValue==0)
            //{
            int __count = stdout.read(buff);
            if(__count>0)
            {
                outString = new String(buff,0,__count);
            }
            //}
            stdErr = null;
            int count = new InputStreamReader(process.getErrorStream()).read(buff);
            if(count > 0)
                stdErr = new String(buff,0,count);
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            stdErr = "by exec:IOException,process.exec fail";
            exitValue = EXEC_ERR;
            return null;
        } catch (InterruptedException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            stdErr = "by exec:InterruptedException,process.waitFor fail";
            exitValue = EXEC_ERR;
            return null;
        }
        return outString;
    }
    /**
     * execute the command as root*/
    static public String execRoot(String cmd) {
        FrameActivity.sLog.logWrite("IN",cmd);
        String _out = exec(cmd,true);
        if(_out.length()>0)
            FrameActivity.sLog.logWrite("OUT",_out);
        if(stdErr!=null)
            FrameActivity.sLog.logWrite("ERR",stdErr);
        return _out;
    }

    static public String execNoLog(String cmd) {
        return exec(cmd,true);
    }

    static public String execBusybox(String cmd){
        return execRoot(BUSYBOX +cmd);
    }
/*
    static public String exec(final int overtime, String cmd, boolean root)
    {
        String outString = "";
        try {
            char[] buff = new char[1024*30];
            String _shell = null;
            if(root)
                _shell = "su";
            else
                _shell = "sh";
            final Process process = Runtime.getRuntime().exec(_shell);
            OutputStreamWriter stdin = new OutputStreamWriter(process.getOutputStream());
            InputStreamReader stdout = new InputStreamReader(process.getInputStream());
            stdin.write(cmd+"\n");
            stdin.write("exit\n");
            stdin.flush();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(overtime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        process.destroy();
                    }catch (Exception e)
                    {

                    }
                }
            }).start();

            exitValue = process.waitFor();
            //if(exitValue==0)
            //{
            int __count = stdout.read(buff);
            if(__count>0)
            {
                outString = new String(buff,0,__count);
            }
            //}
            stdErr = null;
            int count = new InputStreamReader(process.getErrorStream()).read(buff);
            if(count > 0)
                stdErr = new String(buff,0,__count);
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            stdErr = "by exec:IOException,process.exec fail";
            exitValue = EXEC_ERR;
            return null;
        } catch (InterruptedException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            stdErr = "by exec:InterruptedException,process.waitFor fail";
            exitValue = EXEC_ERR;
            return null;
        }
        return outString;
    }

    static public String execRoot(int overtime,String cmd) {
        return exec(overtime,cmd,true);
    }
*/

}
