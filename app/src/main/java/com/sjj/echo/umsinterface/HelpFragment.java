package com.sjj.echo.umsinterface;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by SJJ on 2017/3/8.
 */

public class HelpFragment extends Fragment {

    private Activity mActivity;



    public void init(Activity activity)
    {
        mActivity = activity;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_help,container,false);

        TextView helpText = (TextView) rootView.findViewById(R.id.help_string);
        TextView helpInfo = (TextView) rootView.findViewById(R.id.help_info);

        helpInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/outofmemo/UMS-Interface"));
                mActivity.startActivity(intent);
            }
        });

        int helpRaw = R.raw.help_en;
        if(FrameActivity.sLang.equals("zh"))
        {
            helpRaw = R.raw.help_zh;
        }
        byte[] buff = new byte[20*1024];
        InputStream inputStream = getResources().openRawResource(helpRaw);
        try {
            inputStream.read(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String helpString = new String(buff);
        helpText.setText(Html.fromHtml(helpString));


        return rootView;
    }
}
