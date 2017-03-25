package com.sjj.echo.explorer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sjj.echo.routine.FileTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by SJJ on 2016/12/7.
 */


/**
 *The list view that show the files.
 * <p>the method "init()" must be called first,then call "setup()" to show the file list when it's ready
 * It show be used with FileAdapter,ListView is responsible for the logic and FileAdapter provide the views</p>
 * */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FileListView extends ListView implements View.OnKeyListener {
    /**
     * Listen the event when FileListView status changed or request to change
     * */
    public  interface OnListChangeListener {
        /**
         * called when the items mSelected or unselected <br/>
         * @param selectCount the count of the select items.
         * */
        void onSelectChange(int selectCount);
        /**
         * called when the open a directory or restore the FileListView from the back stack<br/>
         * @param title the path of the current directory
         * */
        void onTitleChange(String title);
        /**
         * called when a file item was pressed to request the fragment to open the directory in an new FileListView
         *  <br/>
         * @param path directory path to request
         * */
        void requestOpenDir(String path);
        /**
         * called when ".." was pressed or back_key was pressed to request the fragment
         * to restore the parent FileListView or open the parent directory in an new FileListView<br/>
         * @param path the parent directory path
         * */
        void requestPopBack(String path);
    }

    private OnListChangeListener mOnListChangeListener;
    protected List<FileItem> mFiles = new ArrayList<>();
    private String mCurPath = "/sdcard/";//end with '/'
    enum SortBy{SORT_NO,SORT_NAME,SORT_SIZE,SORT_TIME,SORT_TYPE}
    /**
     * should sort by rising or falling.
     * */
    public boolean mFileSortRising = true;
    /**
     * should directories be in the top of the fist
     * */
    public boolean mDirTop = true;
    /**
     * whether to sort the list whe ope the directory.
     * */
    public boolean mSortFile = true;
    /**
     * the basis to sort the list.
     * <b>should be in SORT_NO,SORT_NAME,SORT_SIZE,SORT_TIME,SORT_TYPE</b>
     * */
    public SortBy mSortBy = SortBy.SORT_NAME;
    private boolean mSelectMode = false;
    private Activity mActivity;
    protected FileAdapter mFileAdapter;
    /**
     * return the directory of the FileListView.
     * */
    public String getCurPath()
    {
        return mCurPath;
    }
    /**
     * return the parent directory of the FileListView.
     * */
    public String getParentPath()//return the parent path with '/'
    {
        if(mCurPath.equals("/"))
            return "/";
        String path;
        if(mCurPath.endsWith("/"))
            path = mCurPath.substring(0, mCurPath.length()-1);
        else
            path = mCurPath;
        int offset = path.lastIndexOf("/");
        if(offset<0)
            return "/";
        return path.substring(0,offset+1);

    }
    /**
     *initial the member variables,and open the launchDir
     * <p>this method should be called after construct.
     * it will new and set the adapter, so don't set adapter again</p>
     * @param activity the activity witch own this object.
     * @param launchDir the init directory path.
     * @param onListChangeListener the listener to register.
     * */
    public void init(Activity activity,OnListChangeListener onListChangeListener,String launchDir)
    {
        FileAdapter fileAdapter = new FileAdapter(activity);
        setAdapter(fileAdapter,activity,onListChangeListener,launchDir);
    }


    protected void setAdapter(FileAdapter fileAdapter, Activity activity, OnListChangeListener onListChangeListener, String curPath)
    {
        setAdapter(fileAdapter);
        mCurPath = curPath;
        mFileAdapter = fileAdapter;
        mOnListChangeListener = onListChangeListener;
        mActivity = activity;
        setup();
    }

    protected void setup()
    {
        this.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(mSelectMode)
                {
                    boolean isRoot = mCurPath.compareTo("/") == 0;
                    if (!isRoot) {
                        if (position == 0)
                            return;
                        position--;
                        int selectCount = mFileAdapter.touchItem(position,view);
                        if(0==selectCount)
                            mSelectMode = false;
                        mOnListChangeListener.onSelectChange(selectCount);
                    }
                }else{
                    onTouchItem((int) id);
                     //onFileClick(((TextView)view.findViewById(R.id.file_list_name)).getText().toString());
                }
            }


        });
        this.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isRoot = mCurPath.compareTo("/") == 0;
                if (!isRoot) {
                    if (position == 0)
                        return false;
                }
                    position--;
                if(position<=0)
                    position = 0;
                if(!mSelectMode) {
                    mSelectMode = true;
                    mFileAdapter.touchItem(position,view);
                    mOnListChangeListener.onSelectChange(1);
                    return true;
                }
                return false;
            }
        });
        openDirAuto(mCurPath,true);
        this.setFocusable(true);
        this.requestFocus();
        this.setOnKeyListener(this);
    }

    //when KEYCODE_BACK pressed, request the fragment to restore the parent FileListView or open the parent directory in an new FileListView
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
         {
             //it will be called twice one for down and another for up
             if(event.getAction()==KeyEvent.ACTION_DOWN) {
                 if (mSelectMode) {
                     exitSelect();
                 } else {
                     if(getCurPath().equals("/"))
                         mActivity.finish();
                     else {
                         String path = getParentPath();
                         if (path != null)
                             mOnListChangeListener.requestPopBack(path);
                         //openDirAuto(path, true);
                     }
                 }
             }
             return true;
        }
        return false;
    }

    /**
     * call this method to select all the file items except ".."
     * */
    public void selectAll()
    {
        mFileAdapter.selectAll();
        mOnListChangeListener.onSelectChange(mFileAdapter.getSelectCount());
    }
    /**
     * call this method to exit the select mode
     * <b>when long press the file item, it when enter the select mode automatically</b>
     * */
    public void exitSelect()
    {
        if(mSelectMode) {
            mSelectMode = false;
            mFileAdapter.clearSelect();
            mOnListChangeListener.onSelectChange(0);
        }
    }
    public FileListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public FileListView(Context context) {
        super(context);
    }

    /**
     * compare the string.
     * <b>the sequence of the char is 0,1,2...a,A,b,B...<b/>
     * */
    public static int trifleCaseCompare(String a,String b) {
        int ret = a.compareToIgnoreCase(b);
        if (ret!=0) {
            return ret;
        }
        return a.compareTo(b);
    }
    /**
     * call this method to reorder the file list.
     * <b>to set basis of the sorting, set mSortBy,mFileSortRising first.
     * when mDirTop is true ,directories will be in top top of the list</b>
     * */
    public void sortFile() {
        Collections.sort(mFiles, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem lhs, FileItem rhs) {
                // TODO 自动生成的方法存根
                int ret = 0;
                switch (mSortBy) {
                    case SORT_NAME:
                        ret = trifleCaseCompare((String)lhs.mName, (String)rhs.mName);
                        if(!mFileSortRising)
                            ret = -ret;
                        if(mDirTop)
                        {
                            if(lhs.mIsDir &&!rhs.mIsDir)
                                ret -= 0xffffff;
                            if(rhs.mIsDir &&!lhs.mIsDir)
                                ret += 0xffffff;
                        }
                        return ret;
                    case SORT_TIME:
                        ret = trifleCaseCompare((String)lhs.mTime, (String)rhs.mTime);
                        if(!mFileSortRising)
                            ret = -ret;
                        if(mDirTop)
                        {
                            if(lhs.mIsDir &&!rhs.mIsDir)
                                ret -= 0xffffff;
                            if(rhs.mIsDir &&!lhs.mIsDir)
                                ret += 0xffffff;
                        }
                        return ret;
                    case SORT_SIZE:
                        ret = (int) (lhs.mSize -rhs.mSize);
                        if(!mFileSortRising)
                            ret = -ret;
                        if(mDirTop)
                        {
                            if(lhs.mIsDir &&!rhs.mIsDir)
                                ret -= 0xffffff;
                            if(rhs.mIsDir &&!lhs.mIsDir)
                                ret += 0xffffff;
                        }
                        return ret;
                    case SORT_TYPE:
                        return 0;
                    default:
                        break;
                }
                return 0;
            }
        });
        mFileAdapter.changeDir(mFiles,mCurPath);
    }

    /**
     * open the directory in the FIleListView
     * <b></b>
     * @param _path the path of the directory to open,normally end with "/"
     * @param root whether to open as super user.
     * @param top should the FileListView scroll to the top
     * */
    public boolean openDir(String _path,boolean root,boolean top)
    {
        if(_path==null||_path.length()==0)
            return false;
        List<FileItem> list = null;
        String path = _path;//path not ended with '/'
        if(_path.length()>1&&_path.endsWith("/"))
        {
            path = _path.substring(0,_path.length()-1);
        }
        if(root)
            list = FileTool.openDirRoot(path);
        else
            list = FileTool.openDir(path);
        if(null == list)
            return false;
        mFiles = list;
        mCurPath = path;
        if(!mCurPath.endsWith("/"))
            mCurPath += "/";
        if(mSortFile)
            sortFile();
        else
            mFileAdapter.changeDir(mFiles,mCurPath);
        if(top)
            this.setSelection(0);//在最上方
        mOnListChangeListener.onTitleChange(path);
        return true;
    }
    /**
     * if the directory can be read open the directory normally or open as root
     * @param path the path of directory to open
     * @param top should the FileListView scroll to the top
     * */
    public void openDirAuto(String path,boolean top)
    {
        if(!openDir(path,false,top))
        {
            if(!openDir(path,true,top))
            {
             //   Toast.makeText(mActivity, "open fail", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }


    protected void openFile(String path)
    {
        ExplorerActivity activity = (ExplorerActivity)mActivity;
        if(activity.mRequest&&!activity.mDirectoryRequest)
            activity.returnActivity(path);
        else
            FileTool.callActivity(path, mActivity);

    }

    protected void onTouchItem(int id)
    {
        if (id<0)
        {
            mOnListChangeListener.requestPopBack(getParentPath());
            return;
        }

        FileItem fileItem = (FileItem) mFileAdapter.getItem(id);
        String path = mCurPath +fileItem.mName;
        if(!fileItem.mIsDir)
        {
            openFile(path);
            return;
        }
        mOnListChangeListener.requestOpenDir(path);
        //openDirAuto(path,true);
    }

//    protected void onFileClick(String mName)
//    {
//        String path = mCurPath+mName;
//        if(path.endsWith(".."))
//        {
//            if(path.length()<4)
//                path = "/";
//            else {
//                path = path.substring(0, path.length()-3);
//                path = path.substring(0,path.lastIndexOf("/")+1);
//            }
//        }
//        if(mName.equals(".."))
//        {
//            mOnListChangeListener.requestPopBack(path);
//            return;
//        }
//        File file = new File(path);
//
//        if(!file.exists())
//        {//有些文件夹isDirectory会判断错误,并且exists也为false,直接用root方式读取
//            if(!openDir(path,true,true))
//            {
//                Toast.makeText(mActivity, "unknown error", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            mOnListChangeListener.requestOpenDir(path);
//            openDir(getParentPath(),true,true);
//            return;
//        }
//
//        if(!file.isDirectory())//一些文件夹被判断错误,原因未知
//        {
//            openFile(path);
//            return;
//        }
//        mOnListChangeListener.requestOpenDir(path);
//        //openDirAuto(path,true);
//    }

}
