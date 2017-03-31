package com.sjj.echo.umsinterface;

import com.sjj.echo.routine.ShellUnit;

import java.io.File;

/**
 * Created by SJJ on 2017/1/1.
 */

public class MassStorageUnit {
    static private String mConfigPath = "/sys/devices/virtual/android_usb/android0/";
    static public String mLunPath = "/sys/devices/virtual/android_usb/android0/f_mass_storage/lun";
    static public String mError = null;
    static public String mStatusEnable = "unknown";
    static public String mStatusFile = "unknown";
    static public String mStatusFunction = "unknown";
    static public String mStatusReadonly = "unknown";
    static public boolean mReady = false;

    static {
        configCheck();
    }

    public static void setConfigPath(String path)
    {
        mConfigPath = path;
        if(!mConfigPath.endsWith("/"))
            mConfigPath+="/";
        configCheck();
    }

    private static String samsungFix(String lun)
    {
        File _lun = new File(lun);
        if(_lun.exists()&&_lun.isDirectory())
            return lun;
        String _output = ShellUnit.execBusybox("du /sys  |"+ShellUnit.BUSYBOX+" grep \"/lun\"");
        if(_output == null||_output.length()==0)
            return null;
        int startOffset = _output.indexOf("/");
        if(startOffset<0)
            return null;
        int endOffset = _output.indexOf("/lun");
        if(endOffset<0)
            return null;
        endOffset++;
        endOffset = _output.indexOf("/",endOffset);
        //endOffset = _output.indexOf("/");
        if (endOffset<0)
            return null;
        return _output.substring(startOffset,endOffset);

    }

    static private boolean pathCheck()
    {
        File _config = new File(mConfigPath);
        File _functions = new File(mConfigPath+"functions");
        if(!_config.exists()||!_config.isDirectory()||!_functions.exists()||!_functions.isFile())
        {
            String _path = searchPath();
            if(_path!=null)
                mConfigPath = _path;
            return false;
        }
        return true;
    }

    /**
     * config usb mass storage
     * @param dev block device or image file
     * @param readonly  readonly or not
     * */
    static public boolean umsConfig(String dev,boolean readonly)
    {
        return umsConfig(dev,readonly,"mass_storage");
    }


    static private void configCheck()
    {
        mError = null;
        mReady = false;
        if(!pathCheck())
        {
            if(!pathCheck())
            {
                mError = "can't find write config path!";
                return;
            }
        }
        mLunPath = mConfigPath+"f_mass_storage/lun";
        mLunPath = samsungFix(mLunPath);
        if(mLunPath==null)
        {
            mError = "can't find 'lun'!";
            return;
        }
        mReady = true;
    }

    /**
     * config usb mass storage
     * @param dev block device or image file
     * @param readonly  readonly or not
     * */
    static public boolean umsConfig(String dev,boolean readonly,String function)
    {
        mError = null;
        if(!mReady)
        {
            mError = "MassStorageUnit is not ready!";
            return false;
        }
        String cmd = "";
        //must disable usb device frist.
        cmd += "echo 0 > " + mConfigPath +"enable\n";
        cmd += "echo "+function+" > "+mConfigPath+"functions\n";
        cmd += "echo "+dev+" > "+mLunPath+"/file\n";
        if(readonly)
            cmd += "echo 1 > "+mLunPath+"/ro\n";
        cmd += "echo 1 > "+mConfigPath+"enable\n";
        //it maybe make no difference without setting sys.usb.config .
        cmd += "setprop sys.usb.config "+function+"\n";
        ShellUnit.execRoot(cmd);
        mError = ShellUnit.stdErr;
        return true;
    }

    /**
     * search the config path.
     * @return the config path ,null if no find
     * */
    static private String searchPath()
    {
        final String target = "/android_usb/android0";
        String output = ShellUnit.execBusybox("du /sys  |"+ShellUnit.BUSYBOX+" grep "+target);
        if(output == null||output.length()==0)
            return null;
        int startOffset = output.indexOf("/");
        if(startOffset<0)
            return null;
        int endOffset = output.indexOf("/android_usb/android0");
        if(endOffset>startOffset)
        {
            return output.substring(startOffset,endOffset+target.length())+"/";
        }
        return null;
    }

    /**
     * read the usb mass storage to the member variables
     * @return exit value of the 'su'
     * */
    static public boolean refreshStatus()
    {
        mError = null;
        if(!mReady)
        {
            mError = "MassStorageUnit is not ready!";
            return false;
        }
        mStatusEnable = ShellUnit.execRoot("cat \""+mConfigPath+"enable"+"\"");
        if(ShellUnit.stdErr!=null) {
            mError = ShellUnit.stdErr;
            return false;
        }
        mStatusFunction = ShellUnit.execRoot("cat \""+mConfigPath+"functions"+"\"");
        if(ShellUnit.stdErr!=null) {
            mError = ShellUnit.stdErr;
            return false;
        }
        mStatusFile = ShellUnit.execRoot("cat \""+mLunPath+"/file"+"\"");
        if(ShellUnit.stdErr!=null) {
            mError = ShellUnit.stdErr;
            return false;
        }
        mStatusReadonly = ShellUnit.execRoot("cat \""+mLunPath+"/ro"+"\"");
        int offset;
        if(mStatusEnable==null)
            mStatusEnable = "unknown";
        else{
            offset = mStatusEnable.indexOf("\n");
            if(offset>0&&offset<=mStatusEnable.length())
                mStatusEnable = mStatusEnable.substring(0,offset);
        }

        if(mStatusFile==null)
            mStatusFile = "unknown";
        else{
            offset = mStatusFile.indexOf("\n");
            if(offset>0&&offset<=mStatusFile.length())
                mStatusFile = mStatusFile.substring(0,offset);
        }
        if(mStatusFunction==null)
            mStatusFunction = "unknown";
        else {
            offset = mStatusFunction.indexOf("\n");
            if(offset>0&&offset<=mStatusFunction.length())
                mStatusFunction= mStatusFunction.substring(0,offset);
        }
        if(mStatusReadonly==null)
            mStatusReadonly = "unknown";
        else {
            offset = mStatusReadonly.indexOf("\n");
            if(offset>0&&offset<=mStatusReadonly.length())
                mStatusReadonly = mStatusReadonly.substring(0,offset);
        }
        return true;
    }


//    static public boolean umsConfig(String configPath,String dev,boolean readonly)
//    {
//        mConfigPath = configPath;
//        return umsConfig(dev,readonly);
//    }

//    /**
//     * read the usb mass storage to the member variables
//     * @param configPath the base directory for configuration
//     * @return exit value of the 'su'
//     * */
//    static public boolean refreshStatus(String configPath)
//    {
//        mConfigPath = configPath;
//        return refreshStatus();
//    }

}
