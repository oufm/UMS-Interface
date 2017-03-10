package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by SJJ on 2017/3/10.
 */

public class QuickHistoryAdapter extends BaseAdapter {

    List<String> mHistory;
    Activity mActivity;
    QuickStartFragment mFragment;

    public void init(List<String> history,Activity activity,QuickStartFragment fragment)
    {
        mHistory =history;
        mActivity = activity;
        mFragment = fragment;
    }

    @Override
    public int getCount() {
        return mHistory.size();
    }

    @Override
    public Object getItem(int position) {
        return mHistory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if(convertView!=null)
            view =convertView;
        else
            view = mActivity.getLayoutInflater().inflate(R.layout.quick_list_cell_layout,parent,false);
        ((TextView)view.findViewById(R.id.quick_list_cell_txt)).setText(mHistory.get(position));
        view.findViewById(R.id.quick_list_cell_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuickHistoryAdapter.this.mFragment.useImage(mHistory.get(position));
            }
        });
        return view;
    }
}
