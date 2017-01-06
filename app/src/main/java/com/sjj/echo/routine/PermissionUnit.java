package com.sjj.echo.routine;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by SJJ on 2017/1/6.
 */

public class PermissionUnit {
    @TargetApi(M)
    static public void getPermission(String[] permissions,AppCompatActivity activity)
    {
        if(Build.VERSION.SDK_INT>=23) {
            ArrayList<String> preToDo = new ArrayList<>();
            boolean tip = false;
            for (String pre : permissions) {
                if (activity.checkSelfPermission(pre) != PackageManager.PERMISSION_GRANTED) {
                    preToDo.add(pre);
                    if (activity.shouldShowRequestPermissionRationale(pre)) {
                        tip = true;
                    }
                }
            }
            if (preToDo.size() == 0)
                return;
            if (tip)
                Toast.makeText(activity, "please approve the authorization for file manager", Toast.LENGTH_LONG).show();
            activity.requestPermissions(preToDo.toArray(new String[preToDo.size()]), 0);
        }

    }
}
