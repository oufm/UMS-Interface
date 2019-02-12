package com.sjj.echo.umsinterface;

import com.sjj.echo.routine.ShellUnit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sjj.echo.routine.ShellUnit.execBusybox;

/**
 * Created by SJJ on 2019/2/11.
 */

public class MassStorageConfigfs extends MassStorage {
    private String mLunPath;
    private final String defaultPath = "/cache/usm_config";
    private void searchPath() {
        mReady = false;
        mError = null;
        String output = execBusybox("mount | grep 'type configfs'");
        Pattern p = Pattern.compile("on\\s+([\\w\\d-/]+)\\s+");
        Matcher m = p.matcher(output);
        String mountPath = null;
        if(m.find()) {
            mountPath = m.group(1);
        }else {
            ShellUnit.execRoot("mkdir -p " + defaultPath);
            execBusybox("mount -t configfs none " + defaultPath);
            if(ShellUnit.stdErr == null){
                mountPath = defaultPath;
            }else{
                mError = "configfs is not ready";
                return;
            }
        }

        String massPath = ShellUnit.execBusybox("find " + mountPath + " -name mass_storage*").trim();
        if(ShellUnit.stdErr != null || massPath.isEmpty()) {
            mError = "can't find mass_storage path";
            return;
        }

        String lunPath = ShellUnit.execBusybox("find " + massPath + " -name lun*").trim();
        if(ShellUnit.stdErr != null || massPath.isEmpty()) {
            mError = "can't find lun path";
            return;
        }

        mLunPath = lunPath;
        mReady = true;

    }

    public MassStorageConfigfs()
    {
        searchPath();
    }

    @Override
    public void setConfigPath(String path) {

    }

    @Override
    public boolean umsConfig(String dev, boolean readonly, String function) {
        mError = null;
        if(!mReady)
        {
            mError = "MassStorageConfigfs is not ready!";
            return false;
        }
        String cmd = "";
        cmd += "echo '" + dev + "' > " + mLunPath+"/file\n";
        if(readonly)
            cmd += "echo 1 > " + mLunPath + "/ro\n";
        cmd += "setprop sys.usb.config " + function + "\n";
        ShellUnit.execRoot(cmd);
        mError = ShellUnit.stdErr;
        return true;
    }

    @Override
    public boolean refreshStatus() {
        mError = null;
        if(!mReady)
        {
            mError = "MassStorageConfigfs is not ready!";
            return false;
        }
        mStatusEnable = "1";

        mStatusFunction = ShellUnit.execRoot("getprop sys.usb.config").trim();
        if(ShellUnit.stdErr!=null) {
            mStatusFunction = "unknown";
            mError = ShellUnit.stdErr;
            return false;
        }

        mStatusFile = ShellUnit.execRoot("cat \""+mLunPath+"/file"+"\"").trim();
        if(ShellUnit.stdErr!=null) {
            mStatusFile = "unknown";
            mError = ShellUnit.stdErr;
            return false;
        }

        mStatusReadonly = ShellUnit.execRoot("cat \""+mLunPath+"/ro"+"\"").trim();

        if(mStatusReadonly.isEmpty() || ShellUnit.stdErr!=null) {
            mStatusReadonly = "unknown";
        }


        return true;
    }
}
