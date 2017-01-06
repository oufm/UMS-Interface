package com.sjj.echo.umsinterface;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sjj.echo.routine.FileTool;

public class DownLoadReceiver extends BroadcastReceiver {
    public DownLoadReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("@echo off","onReceive");
        if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
            if(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)==MainActivity.sDownloadId)
            {
                if(MainActivity.sDownloadPath!=null)
                    FileTool.callActivity(MainActivity.sDownloadPath,context);
            }

        }else if(intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)){

        }
    }
}
