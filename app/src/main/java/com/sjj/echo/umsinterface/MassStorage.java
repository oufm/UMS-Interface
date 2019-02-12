package com.sjj.echo.umsinterface;

/**
 * Created by SJJ on 2019/2/11.
 */

public abstract class MassStorage {
    public String mError = null;
    public String mStatusEnable = "unknown";
    public String mStatusFile = "unknown";
    public String mStatusFunction = "unknown";
    public String mStatusReadonly = "unknown";
    public boolean mReady = false;
    static public MassStorage sMassStorage = null;

    abstract public void setConfigPath(String path);


    /**
     * config usb mass storage
     * @param dev block device or image file
     * @param readonly  readonly or not
     * */
    public boolean umsConfig(String dev,boolean readonly)
    {
        return umsConfig(dev,readonly,"mass_storage");
    }


    /**
     * config usb mass storage
     * @param dev block device or image file
     * @param readonly  readonly or not
     * */
    abstract public boolean umsConfig(String dev,boolean readonly,String function);

    /**
     * read the usb mass storage to the member variables
     * @return exit value of the 'su'
     * */
    abstract public boolean refreshStatus();

}
