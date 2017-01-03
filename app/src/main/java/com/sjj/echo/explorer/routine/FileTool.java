package com.sjj.echo.explorer.routine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.util.Log;

import com.sjj.echo.explorer.FileItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SJJ on 2016/12/7.
 */
public class FileTool {
    private FileTool(){}
    static String[][] sMediaTypes ={
            {"3gp","video/3gpp"},
            {"apk","application/vnd.android.package-archive"},
            {"asf","video/x-ms-asf"},
            {"avi","video/x-msvideo"},
            {"bin","application/octet-stream"},
            {"bmp","image/bmp"},
            {"c","text/plain"},
            {"class","application/octet-stream"},
            {"conf","text/plain"},
            {"cpp","text/plain"},
            {"doc","application/msword"},
            {"docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {"xls","application/vnd.ms-excel"},
            {"xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {"exe","application/octet-stream"},
            {"gif","image/gif"},
            {"gtar","application/x-gtar"},
            {"gz","application/x-gzip"},
            {"h","text/plain"},
            {"htm","text/html"},
            {"html","text/html"},
            {"jar","application/java-archive"},
            {"java","text/plain"},
            {"jpeg","image/jpeg"},
            {"jpg","image/jpeg"},
            {"js","application/x-JavaScript"},
            {"log","text/plain"},
            {"m3u","audio/x-mpegurl"},
            {"m4a","audio/mp4a-latm"},
            {"m4b","audio/mp4a-latm"},
            {"m4p","audio/mp4a-latm"},
            {"m4u","video/vnd.mpegurl"},
            {"m4v","video/x-m4v"},
            {"mov","video/quicktime"},
            {"mp2","audio/x-mpeg"},
            {"mp3","audio/x-mpeg"},
            {"mp4","video/mp4"},
            {"mpc","application/vnd.mpohun.certificate"},
            {"mpe","video/mpeg"},
            {"mpeg","video/mpeg"},
            {"mpg","video/mpeg"},
            {"mpg4","video/mp4"},
            {"mpga","audio/mpeg"},
            {"msg","application/vnd.ms-outlook"},
            {"ogg","audio/ogg"},
            {"pdf","application/pdf"},
            {"png","image/png"},
            {"pps","application/vnd.ms-powerpoint"},
            {"ppt","application/vnd.ms-powerpoint"},
            {"pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {"prop","text/plain"},
            {"rc","text/plain"},
            {"rmvb","audio/x-pn-realaudio"},
            {"rtf","application/rtf"},
            {"sh","text/plain"},
            {"tar","application/x-tar"},
            {"tgz","application/x-compressed"},
            {"txt","text/plain"},
            {"wav","audio/x-wav"},
            {"wma","audio/x-ms-wma"},
            {"wmv","audio/x-ms-wmv"},
            {"wps","application/vnd.ms-works"},
            {"xml","text/plain"},
            {"z","application/x-compress"},
            {"zip","application/x-zip-compressed"},
            {"","*/*"}};

    /**
     * return the extension of the file
     * @param name name or path of the file
     * */
    public static String getExtension(String name) {
        int offset = name.lastIndexOf("/");//可能为路径,且路径中有"."
        String suffix = name.substring(offset+1, name.length());
        offset = suffix.lastIndexOf(".");
        if (offset > 0) {
            return suffix.substring(offset+1, suffix.length());
        }
        return "";
    }
    /**
     * launch the suitable activity according to the file name
     * */
    static public void callActivity(String path, Activity activity) {
        String extString = getExtension(path);
        int count = sMediaTypes.length;
        for(int i=0;i<count;i++)
        {
            if(extString.compareToIgnoreCase(sMediaTypes[i][0])==0)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://"+path), sMediaTypes[i][1]);
                activity.startActivity(intent);
                return;
            }
        }

    }

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
    static public String exec(String cmd,boolean root)
    {
        String outString = "";
        try {
            char[] buff = new char[1024*10];
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
                    outString = new String(buff);
                }
            //}
            stdErr = null;
            int count = new InputStreamReader(process.getErrorStream()).read(buff);
            if(count > 0)
                stdErr = new String(buff);
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        return outString;
    }
    /**
     * execute the command as root*/
    static public String execRoot(String cmd) {
       return exec(cmd,true);
    }
    /**
     *get the internal or outside sd card path
     * @param is_removale true is is outside sd card
     * */
    public static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * return the file name according to the path
     * */
    static public String pathToName(String path)//path that doesn't ended with '/'
    {
        if(path.equals("/"))
            return path;
        if(path.endsWith("/"))
            path=path.substring(0,path.length()-1);
        return (String) path.subSequence(path.lastIndexOf("/")+1,path.length());

    }
    /**
     * open the directory and return thr fileItems
     * */
    static public List<FileItem> openDir(String path)//path that doesn't ended with '/' except rootpath
    {
        //Log.d("@echo off","openDir|path="+path);
        File file = new File(path);
        List<FileItem> list = new ArrayList<>();

        File[] allFile = file.listFiles();
        if(allFile == null) {
            Log.d("@echo off","openDir|fail");
            return null;
        }
        int count = allFile.length;
        for(int i=0;i<count;i++)
        {
            boolean isDir = false;
            String premString;
            File tFile = allFile[i];
            if(tFile.isDirectory())
            {
                premString = "d";
                isDir = true;
            }
            else {
                premString = "-";
            }
            if(tFile.canRead())
            {
                premString+="r";
            }
            else {
                premString+="-";
            }
            if(tFile.canWrite())
            {
                premString+="w";
            }else {
                premString+="-";
            }
            premString+="-";
            String timeString = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ").format(new Date(tFile.lastModified()));
            long size = tFile.length();
            list.add(new FileItem(tFile.getName(),timeString,size,premString,null,isDir));
        }
        return list;

    }

    /**
     * open the directory as root through shell
     * */
    static public List<FileItem> openDirRoot(String path)
    {
        //Log.d("@echo off","openDirRoot|path="+path);
        if(!path.endsWith("/"))//以'/'结尾,可以作为判断,如果真的不是目录,自然出错
            path+="/";
        String resultString = FileTool.execRoot("ls -al "+path);
        if(resultString == null||stdErr!=null||exitValue!=0) {
            return null;
        }
        List<FileItem> list = new ArrayList<>();
        Pattern linePattern = Pattern.compile("(\\n|^).+");//匹配每行
        Matcher lineMatcher = linePattern.matcher(resultString);
        while(lineMatcher.find())
        {
            String lineString = lineMatcher.group();
            if(lineString.startsWith("\n"))//开头可能有'\n',必须去掉
                lineString=lineString.substring(1, lineString.length());
            Pattern premPattern = Pattern.compile("^(\\w|-)+"); //匹配权限
            Matcher preMatcher = premPattern.matcher(lineString);
            if(!preMatcher.find()) break;
            String premString = preMatcher.group();
            Pattern timePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}");
            Matcher timeMatcher = timePattern.matcher(lineString);
            if(!timeMatcher.find()) break;
            String timeString = timeMatcher.group();
            String sizeString = "";
            long size = 0;
            boolean dir = premString.startsWith("d");
            if(!dir)
            {
                Pattern sizePattern = Pattern.compile("\\d+");
                Matcher sizeMatcher = sizePattern.matcher(lineString);
                if(sizeMatcher.find())
                {
                    sizeString = sizeMatcher.group();
                    if(sizeString!=null)
                    {
                        size = Long.parseLong(sizeString);
                    }
                }
            }
            Pattern namePattern = Pattern.compile(".+");
            Matcher nameMatcher = namePattern.matcher(lineString);
            if(!nameMatcher.find(timeMatcher.end()+1)) break;
            String nameString = nameMatcher.group();
            int offset = nameString.indexOf(" -> ");
            String linkString = "";
            if(offset>0)
            {
                linkString = nameString.substring(offset+4,nameString.length());
                nameString = nameString.substring(0, offset);
            }
            list.add(new FileItem(nameString,timeString,size,premString,linkString,dir));
        }

        return list;
    }

    /**rename the file**/
    static public boolean reName(String srcPath,String dstPath){
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);
        if(!srcFile.renameTo(dstFile))
        {
            FileTool.execRoot("mv \""+srcPath+"\" \""+dstPath+"\"");
            if(stdErr!=null||exitValue!=0)
                return false;
        }
        return true;
    }
    /**
     * delete the file
     * */
    static public boolean deleteFile(String path)
    {
        File file = new File(path);
        if(file.isDirectory())
        {
            String[] subFiles = file.list();
            String dirString =null;
            if(path.endsWith("/"))
                dirString = path;
            else {
                dirString = path+"/";
            }
            if(subFiles!=null) {
                int count = subFiles.length;
                for (int i = 0; i < count; i++) {
                    deleteFile(dirString + subFiles[i]);//递归删除子文件(目录)
                }
            }
        }
        if(!file.delete()) {
            FileTool.execRoot("rm -rf \"" + path + "\"");
            if(stdErr!=null|exitValue!=0)
                return false;
        }
        return true;
    }
    /**
     * create the file
     * */
    static public boolean newFile(String path){
        File file = new File(path);
        try {
            if(!file.createNewFile())
            {
                FileTool.execRoot("touch \""+path+"\"");
                if(stdErr!=null||exitValue!=0)
                    return false;
            }
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
            FileTool.execRoot("touch \""+path+"\"");
            if(stdErr!=null||exitValue!=0)
                return false;
        }
        return true;
    }
    /**
     * create the directory*/
    static public boolean newDir(String path){
        File file = new File(path);
        if(!file.mkdirs())
        {
            FileTool.execRoot("mkdir \""+path+"\"");
            if(stdErr!=null||exitValue!=0)
                return false;
        }
        return true;
    }
    /**
     * copy the file or whole the directory
     * */
    static public boolean copy(String srcFile,String dstDir){
        //srcFile 结尾无"/",,dstDir必须存在
        File file = new File(srcFile);
        String dirString =null;
        if(dstDir.endsWith("/"))//必须使dstDir结尾有"/"
            dirString = dstDir;
        else {
            dirString = dstDir+"/";
        }
        if(file.isDirectory())
        {
            String[] subFiles = file.list();
            int count = subFiles.length;
            String subDir =dirString+ srcFile.substring(srcFile.lastIndexOf("/")+1,srcFile.length());
            FileTool.newDir(subDir);//必须先创建子目录
            for(int i=0;i<count;i++)
            {
                copy(srcFile+"/"+subFiles[i],subDir);
            }
        }
        else {
            FileChannel inputChannel = null;
            FileChannel outputChannel = null;
            try {
                inputChannel = new FileInputStream(srcFile).getChannel();
                outputChannel = new FileOutputStream(dirString+srcFile.substring(srcFile.lastIndexOf("/")+1,srcFile.length())).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
                //java方式复制失败,尝试使用shell命令
                FileTool.execRoot("cp -R \""+srcFile+"\" \""+dstDir+"\"");
                if(stdErr!=null||exitValue!=0)
                    return false;
            } finally {
                try {
                    if(null!=outputChannel)
                        outputChannel.close();
                    if(null!=inputChannel)
                        inputChannel.close();
                } catch (IOException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
    /*move the file oe directory*/
    static public boolean move(String srcString,String dstDir)
    {
        File file = new File(srcString);
        String dstString = dstDir + srcString.substring(srcString.lastIndexOf("/")+1, srcString.length());
        if(!file.renameTo(new File(dstString))) {
            FileTool.execRoot("mv \"" + srcString + "\" \"" + dstString + "\"");
            if(stdErr!=null||exitValue!=0)
                return false;
        }
        return true;
    }
    static public void zip(String[] srcString,String dstDir){

    }
    static public boolean tar(String[] names,String tarName,String baseDir){
        String cmd;
        cmd = "tar -c -z -T";
        int count = names.length;
        for(int i=0;i<count;i++)
        {
            cmd+=" \"";
            cmd+=names[i];
            cmd+="\"";
        }
        cmd+=" -f \"";
        cmd+=tarName;
        cmd+="\" -C \"";
        cmd+=baseDir;
        cmd+="\"";
        exec(cmd,false);
        if(stdErr!=null||exitValue!=0)
        {
            exec(cmd,true);
            if(stdErr!=null)
            {
                return false;
            }
        }
        return true;
    }

}
