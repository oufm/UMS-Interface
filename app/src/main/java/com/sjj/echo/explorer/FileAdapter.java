package com.sjj.echo.explorer;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjj.echo.umsinterface.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

/**
 * Created by SJJ on 2016/12/7.
 */
/**
 * provide the view fo the file item.
 * <p>should be used with FileListView.when call {@link FileListView}.init(),
 * FileListView will be create and set automatically.</p>
 * */
public class FileAdapter extends BaseAdapter {

    static public int BACK_COLOR = Color.WHITE;
    static public int BACK_COLOR_SELECT = 0xffcccccc;

    private List<FileItem> mItems = new ArrayList<FileItem>();
    private int mSelectCount = 0;
    private Activity mActivity;
    private String mBaseDir;
    boolean mIsRoot = false;

    public FileAdapter(Activity activity)
    {
        super();
        this.mActivity = activity;
        Thumbnail.mContext = activity;
    }

    @Override
    public int getCount() {
        int count = mItems.size();
        if(mIsRoot)
            return count;
        else
            return count+1;
    }

    private View getView(final String name, String desc, boolean isDir, boolean selected
                         , View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(view == null)
            view = mActivity.getLayoutInflater().inflate(R.layout.exlporer_file_list,parent,false);
        if(selected)
            view.setBackgroundColor(BACK_COLOR_SELECT);
        else
            view.setBackgroundColor(BACK_COLOR);
        TextView nameText= ((TextView) view.findViewById(R.id.file_list_name));
        nameText.setText(name);
        TextView descText= ((TextView) view.findViewById(R.id.file_list_desc));
        descText.setText(desc);
        final ImageView imageView = (ImageView) view.findViewById(R.id.file_list_icon);
        if(isDir) {
         //   imageView.setImageResource(R.mipmap.ic_folder_gray_48dp);
            Thumbnail.setThumbnail(mBaseDir + name, MICRO_KIND, imageView,true);
        }
        else {
         //   imageView.setImageResource(R.mipmap.ic_file_gray_48dp);
            Thumbnail.setThumbnail(mBaseDir + name, MICRO_KIND, imageView,false);
        }
        return  view;

    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        if(mIsRoot)
            return position;
        else
            return position-1;
    }

    /**
     * return the mSize with suitable unit
     * @param size mSize(byte)
     * */
    public static String getEasySize(long size)
    {
        Float value = (float) size;
        String unit = "B";
        if(value > 1024)
        {
            value = value/1024;
            unit = "KB";
            if(value > 1024)
            {
                value = value/1024;
                unit = "MB";
                if(value > 1024)
                {
                    value = value/1024;
                    unit = "GB";
                }
            }
        }
        return String.format("%6.2f%s",value,unit);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(!mIsRoot)
        {
            if(0 == position)
                return getView("..",mActivity.getString(R.string.parent_dir),true,false,convertView,parent);
            position --;
        }
        FileItem item = mItems.get(position);
        return  getView(item.mName,item.mTime +" "+getEasySize(item.mSize)+" "+item.mPrem
                +(item.mLink.length()>0?" >":" ")+item.mLink,
                item.mIsDir,item.mSelected,convertView,parent);
    }
    /**change the content of the list .
     * <p></p>
     * @param baseDir the path of the directory
     * @param items the files to set
     * */
    public void changeDir(List<FileItem> items,String baseDir)
    {
        if(baseDir.endsWith("/"))
            mBaseDir = baseDir;
        else
            mBaseDir = baseDir+"/";
        this.mItems = items;
        this.mIsRoot = baseDir.equals("/");
        this.notifyDataSetChanged();
    }
    /**
     * set item.mSelected, so that background of the mSelected items can be changed
     * @param index the index of the mItems ,if current directory is not "/",it should be position -1
     * @param selectView the view of the item, so the background can be changed immediately
     * */
    public int touchItem(int index,View selectView)//the index of mItems.not id or position
    {

        FileItem item = mItems.get(index);
        if(item.mSelected)
        {
            mSelectCount--;
            item.mSelected = false;
            selectView.setBackgroundColor(BACK_COLOR);
        }else{
            mSelectCount++;
            item.mSelected = true;
            selectView.setBackgroundColor(BACK_COLOR_SELECT);
        }
        return mSelectCount;
    }
    /**
     * clear all the mSelected items
     * */
    public void clearSelect()
    {
        if(mSelectCount == 0)
            return;
        Iterator<FileItem> iterator = mItems.iterator();
        while(iterator.hasNext())
        {
            iterator.next().mSelected = false;
        }
        this.notifyDataSetChanged();
    }
    /**
     * select all the items
     * */
    public void selectAll()
    {
        Iterator<FileItem> iterator = mItems.iterator();
        while(iterator.hasNext())
        {
            iterator.next().mSelected = true;
        }
        mSelectCount = mItems.size();
        this.notifyDataSetChanged();
    }
    /**
     * return all the mSelected names
     * <p>note that they are names not paths</p>
     * */
    public List<String> getSelect()//return the mSelected names
    {
        List<String> list = new ArrayList<>();
        Iterator<FileItem> iterator = mItems.iterator();
        while(iterator.hasNext())
        {
            FileItem fileItem = iterator.next();
            if(fileItem.mSelected)
                list.add(fileItem.mName);
        }
        return list;
    }
    /**
     * return the mSelected count
     * */
    public int getSelectCount()
    {
        return mSelectCount;
    }
}
