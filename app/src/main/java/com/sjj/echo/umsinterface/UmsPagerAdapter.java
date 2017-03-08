package com.sjj.echo.umsinterface;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.sjj.echo.lib.FragmentStatePagerAdapterFix;

/**
 * Created by SJJ on 2017/3/8.
 */

public class UmsPagerAdapter extends FragmentStatePagerAdapterFix {

    Fragment[] mFragments;
    String[] mTitles;

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    public UmsPagerAdapter(FragmentManager fragmentManager, Fragment[] fragments,String[] titles) {
        super(fragmentManager);
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public int getItemPosition(Object object) {
        int i=0;
        int count = mFragments.length;
        for(;i<count;i++)
        {
            if(mFragments[i]==object)
                return i;
        }
        return -1;
    }
}
