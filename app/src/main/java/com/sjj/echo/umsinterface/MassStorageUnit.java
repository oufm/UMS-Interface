package com.sjj.echo.umsinterface;

import com.sjj.echo.routine.ShellUnit;

/**
 * Created by SJJ on 2017/1/1.
 */

public class MassStorageUnit {
    static public String mConfigPath = "/sys/devices/virtual/android_usb/android0/";
    static public String mError = null;
    static public String mStatusEnable;
    static public String mStatusFile;
    static public String mStatusFunction;
    static public String mStatusReadonly;

    private static String samsungFix(String lun)
    {
        String output = ShellUnit.execRoot("ls \""+lun+"\"");
        if(output != null&&output.indexOf("No such file or directory")>=0)
        {
            String _output = ShellUnit.execRoot("du /sys  |grep \"/lun\"");
            if(_output == null||ShellUnit.exitValue!=0)
                return lun;
            int startOffset = _output.indexOf("/");
            if(startOffset<0)
                return lun;
            int endOffset = _output.indexOf("/android_usb/android0");
            if(endOffset>startOffset)
            {
                return _output.substring(startOffset,endOffset+4);
            }
        }
        return lun;

    }

    /**
     * config usb mass storage
     * @param dev block device or image file
     * @param readonly  readonly or not
     * */
    static public int umsConfig(String dev,boolean readonly)
    {
        Boolean ok =true;
        mError = null;
        String cmd = "";
        //must disable usb device frist.
        cmd += "echo 0 > " + mConfigPath +"enable\n";
        cmd += "echo mass_storage > "+mConfigPath+"functions\n";
        String _lun = mConfigPath+"f_mass_storage/lun";
        _lun = samsungFix(_lun);
        cmd += "echo "+dev+" > "+_lun+"/file\n";
        cmd += "echo "+(readonly?"1":"0")+" > "+mConfigPath+"f_mass_storage/lun/ro\n";
        cmd += "echo 1 > "+mConfigPath+"enable\n";
        //it maybe make no difference without setting sys.usb.config .
        cmd += "setprop sys.usb.config mass_storage\n";
        ShellUnit.execRoot(cmd);
        mError = ShellUnit.stdErr;
        return ShellUnit.exitValue;
    }

    /**
     * search the config path.
     * @return the config path ,null if no find
     * */
    static public String searchPath()
    {
        final String target = "/android_usb/android0";
        String output = ShellUnit.execRoot("du /sys  |grep "+target);
        if(output == null||ShellUnit.exitValue!=0)
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
    static public int refreshStatus()
    {
        int exitValue = 0;
        mStatusEnable = ShellUnit.execRoot("cat \""+mConfigPath+"enable"+"\"");
        if(ShellUnit.exitValue!=0)
            exitValue = ShellUnit.exitValue;
        mStatusFunction = ShellUnit.execRoot("cat \""+mConfigPath+"functions"+"\"");
        if(ShellUnit.exitValue!=0)
            exitValue = ShellUnit.exitValue;
        mStatusFile = ShellUnit.execRoot("cat \""+mConfigPath+"f_mass_storage/lun/file"+"\"");
        if(ShellUnit.exitValue!=0)
            exitValue = ShellUnit.exitValue;
        mStatusReadonly = ShellUnit.execRoot("cat \""+mConfigPath+"f_mass_storage/lun/ro"+"\"");
        if(ShellUnit.exitValue!=0)
            exitValue = ShellUnit.exitValue;
        mError = ShellUnit.stdErr;
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

        return exitValue;
    }

    /**
     * config usb mass storage
     * @param configPath the base directory for configuration
     * @param dev block device or image file
     * @param readonly  readonly or not
     * */
    static public int umsConfig(String configPath,String dev,boolean readonly)
    {
        mConfigPath = configPath;
        return umsConfig(dev,readonly);
    }

    /**
     * read the usb mass storage to the member variables
     * @param configPath the base directory for configuration
     * @return exit value of the 'su'
     * */
    static public int refreshStatus(String configPath)
    {
        mConfigPath = configPath;
        return refreshStatus();
    }

}
