package com.sjj.echo.explorer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.sjj.echo.umsinterface.R;

import java.util.LinkedList;

/**
 * Created by SJJ on 2016/12/10.
 */

public class FileFragment extends  android.support.v4.app.Fragment implements FileListView.OnListChangeListener
{

    interface OnListChangeListener
    {
        void onSelectChange(FileFragment fileFragment, int selectCount);
        void onTitleChange(FileFragment fileFragment,String title);
    }
    //static int sNextId = 0;
    //private int mId;

    private Activity mActivity;
    public String mLaunchDir;
    private OnListChangeListener mOnListChangeListener;
    private ViewGroup mRootView = null;
    protected FileListView mFileList;
    protected LinkedList<FileListView> mBackStack = new LinkedList<>();
    /**
     * max back stack mSize
     * */
    public final int mBacksStackSize = 10;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public FileFragment()
    {
        super();
        //mId = sNextId;
        //sNextId++;
    }


    /**
     * init the fragment
     * <p>should be called after construct</p>
     * @param activity the activity witch own this object
     * @param launchDir the init directory
     * @param onListChangeListener the listener to register
     * */
    public void init(Activity activity, String launchDir,OnListChangeListener onListChangeListener) {
        this.mActivity = activity;
        this.mLaunchDir = launchDir;
        this.mOnListChangeListener = onListChangeListener;
    }

    @Override
    public void onSelectChange(int selectCount) {
        mOnListChangeListener.onSelectChange(this,selectCount);

    }

    @Override
    public void onTitleChange(String title) {
        mOnListChangeListener.onTitleChange(this,title);
    }

    @Override
    public void requestOpenDir(String path) {
        openDir(path);
    }

    @Override
    public void requestPopBack(String path) {
        lastDir(path);
    }

    /**
     * if the back stack is not empty, a FileListView will be restored or
     * the lastPath will be opened in an new FileListView.
     **/
    public void lastDir(String lastPath)
    {
        if(!popBackStack())
        {
            mRootView.removeAllViews();
            mFileList = new FileListView(mActivity);
            mFileList.setDivider(null);
            mFileList.init(mActivity,this,lastPath);
            mRootView.addView(mFileList);
        }
        mOnListChangeListener.onTitleChange(this,lastPath);
    }
    /**
     * restore th FIleListView with backStack
     * */
    public boolean popBackStack()
    {
        if(mBackStack.size()<=0)
            return false;
        mRootView.removeAllViews();
        mFileList = mBackStack.get(mBackStack.size()-1);
        mBackStack.remove(mBackStack.size()-1);
        mRootView.addView(mFileList);
        mFileList.requestFocus();
        return true;
    }
    /**
     * open the directory in an new FileListView, old FileListView will be add to backStack.
     * if backStack is full ,header will be dequeue.
     * */
    public void openDir(String path)
    {
        mBackStack.add(mFileList);
        if(mBackStack.size()> mBacksStackSize)
            mBackStack.remove(0);
        mRootView.removeAllViews();
        mFileList = new FileListView(mActivity);
        mFileList.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        mFileList.setDivider(null);
        mRootView.addView(mFileList);
        mFileList.init(mActivity,this,path);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mRootView ==null)
        {
            mRootView = (ViewGroup) inflater.inflate(R.layout.explorer_content_main,container,false);
            mFileList = (FileListView) mRootView.findViewById(R.id.file_list);
            mFileList.init(mActivity,this, mLaunchDir);
        }
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.explorer_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * refresh the directory
     * @param moveTop should the FileListView should scroll to the top
     * */
    public void refresh(boolean moveTop)
    {
        if(mFileList !=null)
            mFileList.openDirAuto(mFileList.getCurPath(),moveTop);
    }
    public void refresh()
    {
        refresh(true);
    }
}
