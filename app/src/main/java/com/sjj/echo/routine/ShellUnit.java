package com.sjj.echo.routine;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static com.sjj.echo.umsinterface.FrameActivity.APP_DIR;

/**
 * Created by SJJ on 2017/1/1.
 */

public class ShellUnit {

    public static String BUSYBOX = APP_DIR+"/bin/busybox ";
    public static LogUnit sLog = LogUnit.getDefaultLog();
    private static Process sProcess;
    private static OutputStreamWriter sInStream;
    private static InputStreamReader sOutStream;
    private static InputStreamReader sErrStream;
    private static int sTaskID = 0;
    public static boolean sSuReady = false;
    public static boolean sBusyboxReady = false;
    public static boolean sRootReady = false;

   // static LogUnit rawLog = new LogUnit("/data/data/com.sjj.echo.umsinterface/raw_ums.log");

    /**
     * last error output for root,null if success
     * */
    static public String stdErr;

    static {
        sLog.logWrite("INIT","init ShellUnit");
        ProcessBuilder tb = new ProcessBuilder("su");
        Process tp = null;
        char[] buff = new char[1024];
        try {
            tp = tb.start();
            OutputStreamWriter tIn = new OutputStreamWriter(tp.getOutputStream());
            InputStreamReader tOut = new InputStreamReader(tp.getInputStream());
            String rootText = "UMS_ROOT_TEST";
            tIn.write("\necho "+rootText+"\n");
            tIn.flush();
            tIn.write("exit\n");
            tIn.flush();
            tp.waitFor();
            int count =tOut.read(buff);
            if(count>0)
            {
                if(new String(buff,0,count).startsWith(rootText))
                    sRootReady = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        init();

    }

    public static void restart()
    {
        if(sProcess==null)
            init();
    }

    private static void init()
    {
        if(sRootReady) {
            try {
                ProcessBuilder pb = new ProcessBuilder("su");
                //pb.redirectErrorStream(true);
                sProcess = pb.start();
                sProcess = Runtime.getRuntime().exec("su");//don't do it in this way,will block
                sInStream = new OutputStreamWriter(sProcess.getOutputStream());
                sOutStream = new InputStreamReader(sProcess.getInputStream());
                sErrStream = new InputStreamReader(sProcess.getErrorStream());
                sSuReady = true;
            } catch (IOException e) {
                e.printStackTrace();
                stdErr = "init LogUnit fail:" + e.toString();
            }
        }
    }

    public static void close()
    {
        if(sProcess!=null) {
            sProcess.destroy();
            sProcess = null;
            sInStream = null;
            sOutStream = null;
            sErrStream = null;
        }
    }

    /**
     * must be called firstly.
     * */
    public static boolean initBusybox(InputStream inputStream)
    {
        if(!sSuReady)
        {
            stdErr = "su is not ready";
            return false;
        }
        stdErr = "init busybox fail";
        sLog.logWrite("INIT","init Busybox");
        File _bin = new File(APP_DIR+"/bin");
        if(!_bin.isDirectory()&&!_bin.mkdir())
        {
            return false;
        }
        File _busybox = new File(APP_DIR+"/bin/busybox");
        if(!_busybox.isFile())
        {
            if(!FileTool.streamToFile(inputStream,APP_DIR+"/bin/busybox"))
                return false;
        }
        ShellUnit.execRoot("chmod 777 "+APP_DIR+"/bin/busybox");
        if(ShellUnit.stdErr!=null)
            return false;
        sBusyboxReady =true;
        stdErr = null;
        return true;
    }

//    public static final int EXEC_ERR = 1010100;
    /**
     * last exit code for root
     * */
//    static public int exitValue;
//    /**
//     * execute the command through shell
//     * @param root should be execute as root
//     *@param cmd command
//     * */
//    static private String execSU(String cmd,boolean root)
//    {
//        String outString = "";
//        try {
//            char[] buff = new char[1024*30];
//            Process process;
//            if(root)
//                process = Runtime.getRuntime().execSU("su");
//            else
//                process = Runtime.getRuntime().execSU("sh");
//            OutputStreamWriter stdinStream = new OutputStreamWriter(process.getOutputStream());
//            InputStreamReader stdoutStream = new InputStreamReader(process.getInputStream());
//            stdinStream.write(cmd+"\n");
//            stdinStream.write("exit\n");
//            stdinStream.flush();
//            process.waitFor();
//            int __count = stdoutStream.read(buff);
//            if(__count>0)
//            {
//                outString = new String(buff,0,__count);
//            }
//            //}
//            stdErr = null;
//            int count = new InputStreamReader(process.getErrorStream()).read(buff);
//            if(count > 0)
//                stdErr = new String(buff,0,count);
//        } catch (IOException e) {
//            // TODO 自动生成的 catch 块
//            e.printStackTrace();
//            stdErr = "by execSU:IOException,process.execSU fail";
//            //return null;
//            return "";
//        } catch (InterruptedException e) {
//            // TODO 自动生成的 catch 块
//            e.printStackTrace();
//            stdErr = "by execSU:InterruptedException,process.waitFor fail";
//            //return null;
//            return "";
//        }
//        return outString;
//    }

    static synchronized private String execSU(String cmd)//remember synchronized
    {
        char[] buff = new char[1024*30];
        StringBuffer strBuff = new StringBuffer();
        stdErr = null;
        if(!sSuReady)
        {
            stdErr = "su is not ready";
            return "";
        }
        String finishflag = "~~~ums_task_"+sTaskID+"_finished~~~";
        sTaskID++;
        int flagLength = finishflag.length();
        String task = cmd+"\necho "+finishflag+"\n";//remember '\n'
        try {
            int count ;
           // rawLog.logWrite("RIN",task);
           // Log.d("UMS_DEBUG","[   IN]>>"+task);
            sInStream.write(task);
            sInStream.flush();
            while (true) {
                count = sOutStream.read(buff);//remember check ready,or block.
                if (count > 0) {
                    strBuff.append(buff, 0, count);
                  //  rawLog.logWrite("ROUT",strBuff.toString());
                  //  Log.d("UMS_DEBUG","[  OUT]>>"+strBuff.toString());
                    int searchBegin = strBuff.length() - count - flagLength;
                    if(searchBegin<0)
                        searchBegin = 0;
                    if(strBuff.indexOf(finishflag,searchBegin)>=0)
                        break;
                }
            }
            if(sErrStream.ready()) {
                count = sErrStream.read(buff);
                if (count > 0) {
                    stdErr = new String(buff, 0, count);
                  //  rawLog.logWrite("RERR",stdErr);
                  //  Log.d("UMS_DEBUG","[  ERR]>>"+stdErr);
                }
            }
            strBuff.delete(strBuff.length()-flagLength-1,strBuff.length());
            return strBuff.toString();
        } catch (IOException e) {
            e.printStackTrace();
            stdErr = "exec SU fail:"+e.toString();
            return "";
        }
    }
    /**
     * execute the command as root*/
    static public String execRoot(String cmd) {
        sLog.logWrite("IN",cmd);
        String _out = execSU(cmd);
        if(_out!=null&&_out.length()>0)
            sLog.logWrite("OUT",_out);
        if(stdErr!=null)
            sLog.logWrite("ERR",stdErr);
        return _out;
    }

    static public String execNoLog(String cmd) {
        return execSU(cmd);
    }

    static public String execBusybox(String cmd){
        if(!sBusyboxReady)
        {
            stdErr = "busybox is not ready";
            return "";
        }
        return execRoot(BUSYBOX +cmd);
    }
/*
    static public String execSU(final int overtime, String cmd, boolean root)
    {
        String outString = "";
        try {
            char[] buff = new char[1024*30];
            String _shell = null;
            if(root)
                _shell = "su";
            else
                _shell = "sh";
            final Process process = Runtime.getRuntime().execSU(_shell);
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
            stdErr = "by execSU:IOException,process.execSU fail";
            exitValue = EXEC_ERR;
            return null;
        } catch (InterruptedException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            stdErr = "by execSU:InterruptedException,process.waitFor fail";
            exitValue = EXEC_ERR;
            return null;
        }
        return outString;
    }

    static public String execRoot(int overtime,String cmd) {
        return execSU(overtime,cmd,true);
    }
*/

}
