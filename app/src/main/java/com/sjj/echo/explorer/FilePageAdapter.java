package com.sjj.echo.explorer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.sjj.echo.routine.FileTool;
import com.sjj.echo.lib.FragmentStatePagerAdapterFix;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by SJJ on 2016/12/11.
 */
/*don't extends FragmentPagerAdapter ! It will be in mess after delete a fragment */
/*there is a bug in FragmentStatePagerAdapter , use FragmentStatePagerAdapterFix*/
public class FilePageAdapter extends FragmentStatePagerAdapterFix {
    private ExplorerActivity mActivity;
    private ViewPager mViewPager;
    protected List<FileFragment> mFileFragments = new LinkedList<>();
    private String[] mInitDirs;
    public FilePageAdapter(FragmentManager fm, ExplorerActivity activity, ViewPager viewPager, String[] initDirs) {
        super(fm);
        this.mActivity = activity;
        this.mViewPager = viewPager;
        //String[] _initDirs;
        if(initDirs!=null&&initDirs.length>0)
            mInitDirs = initDirs;
        else
            mInitDirs =new String[] {"/sdcard/","/dev/block/","/sys/","/data/"};
        //mInitDirs = _initDirs;
        int count = mInitDirs.length;
        for(int i=0;i<count;i++)
        {
            FileFragment fileFragment =new FileFragment();
            fileFragment.init(activity, mInitDirs[i],activity);
            mFileFragments.add(fileFragment);
        }
    }

    /**
     * add a tab with this init directory path
     * */
    public void addTab(String initPath)
    {
        FileFragment fileFragment = new FileFragment();
        fileFragment.init(mActivity,initPath, mActivity);
        mFileFragments.add(fileFragment);
        this.notifyDataSetChanged();
        mViewPager.setCurrentItem(mFileFragments.size()-1);
    }
    /**
     * remove the tab
     * */
    public void removeTab(int index)
    {
        if(mFileFragments.size()<=1)
            return;
        mFileFragments.remove(index);
        this.notifyDataSetChanged();
    }

    /**
     * return the directory path of the tab
     * */
    @Override
    public CharSequence getPageTitle(int position) {
        FileFragment fileFragment = (FileFragment)getItem(position);
        FileListView fileListView = fileFragment.mFileList;
        String path = "/";
        if(fileListView!=null)
            path = fileListView.getCurPath();
        else
            path = fileFragment.mLaunchDir;
        return FileTool.pathToName(path);
    }

    @Override
    public Fragment getItem(int position) {
        return mFileFragments.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        int index = mFileFragments.indexOf(object);
        if(index<0)
            return PagerAdapter.POSITION_NONE;
        return index;
    }

    @Override
    public int getCount() {
        return mFileFragments.size();
    }
}
