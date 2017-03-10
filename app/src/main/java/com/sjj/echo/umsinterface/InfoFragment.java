package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.sjj.echo.routine.ShellUnit;

import java.util.ArrayList;

/**
 * Created by SJJ on 2017/3/8.
 */

public class InfoFragment extends Fragment {
    ArrayList<String> mInfos = new ArrayList<>();
    ListView mListView;
    Activity mActivity;
    View mView;

    public void init(Activity activity)
    {
        mActivity = activity;
    }

    /*set the list Strings*/
    private void setAdapter()
    {
        mInfos.clear();
        String output = ShellUnit.execRoot("mount");
        if(output==null||ShellUnit.exitValue!=0)
        {
            Toast.makeText(mActivity,"get mount information fail!",Toast.LENGTH_LONG).show();
        }
        int lineOffset = 0;
        boolean lastLine = false;
        while(true)
        {
            String line = null;
            int offset = output.indexOf("\n",lineOffset);
            if(offset<0) {
                lastLine = true;
                if(lineOffset<output.length())
                {
                    line = output.substring(lineOffset);
                }
            }else
            {
                line = output.substring(lineOffset,offset);
                lineOffset = offset + 1;
                if(lineOffset>=output.length())
                    lastLine = true;
            }

            if(line!=null)
            {
                int endOffset = line.indexOf(",");
                if(endOffset>0)
                {
                    mInfos.add(line.substring(0,endOffset));
                }
            }

            if(lastLine)
                break;
        }
        String[] _tmp = new String[mInfos.size()];
        mListView.setAdapter(new ArrayAdapter<String>(mActivity,R.layout.mount_list_layout,mInfos.toArray(_tmp)));
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(mView!=null)
            return mView;
        View rootView = inflater.inflate(R.layout.activity_mount,container,false);
        mView = rootView;

        mListView = (ListView) rootView.findViewById(R.id.mount_list);
        setAdapter();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String itemSelect = mInfos.get(position);

                new AlertDialog.Builder(mActivity)
                        .setTitle(getString(R.string.mount_info_opera_title))
                        .setItems(new String[]{getString(R.string.mount_info_opera_umount),getString(R.string.mount_info_opera_config)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0)
                                {
                                    //umount operation
                                    //ask to confirm first
                                    new AlertDialog.Builder(mActivity).setTitle(getString(R.string.warning))
                                            .setMessage(getString(R.string.mount_info_umount_warning))
                                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    int offset = itemSelect.indexOf(" /");
                                                    if(offset > 0)
                                                    {
                                                        offset++;
                                                        int offsetEnd = itemSelect.indexOf(" ",offset);
                                                        if(offsetEnd > 0)
                                                        {
                                                            String mountPath = (String) itemSelect.subSequence(offset,offsetEnd);
                                                            ShellUnit.execRoot("umount "+ mountPath);
                                                            if(ShellUnit.exitValue==0&&ShellUnit.stdErr==null)
                                                            {
                                                                Toast.makeText(mActivity,"umount success!",Toast.LENGTH_SHORT).show();
                                                                setAdapter();
                                                                return;
                                                            }
                                                        }
                                                    }
                                                    Toast.makeText(mActivity,"umount fail:"+ShellUnit.stdErr,Toast.LENGTH_LONG).show();

                                                }
                                            })
                                            .setNegativeButton(getString(R.string.no),null).create().show();
                                }else if(which == 1)
                                {
                                    //select as device operation
                                    int offsetEnd = itemSelect.indexOf(" ");
                                    if(offsetEnd>0)
                                    {
                                        String _path = itemSelect.substring(0,offsetEnd);
                                        ((FrameActivity)mActivity).doUmsConfig(_path);
//                                        Intent intent = mActivity.getIntent();
//                                        intent.putExtra(UmsFragment.KEY_INTENT_CONFIG,true);
//                                        intent.setData(Uri.parse("file://"+_path));
//                                        mActivity.setResult(Activity.RESULT_OK,intent);
                                        //TO DO :swith to UMS TAB
                                    }
                                    else
                                        Toast.makeText(mActivity,"set path fail!",Toast.LENGTH_LONG).show();
                                }
                                dialog.cancel();
                            }
                        }).create().show();
            }
        });

        return rootView;
    }
}
