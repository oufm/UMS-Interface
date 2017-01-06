package com.sjj.echo.umsinterface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_help);
        TextView helpText = (TextView) findViewById(R.id.help_string);

//        Locale locale = getResources().getConfiguration().locale;
//        String language = locale.getLanguage();

        int helpRaw = R.raw.help_en;
        if(MainActivity.sLang.equals("zh"))
        {
            helpRaw = R.raw.help_zh;
        }
        byte[] buff = new byte[10*1024];
        InputStream inputStream = getResources().openRawResource(helpRaw);
        try {
            inputStream.read(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String helpString = new String(buff);
        helpText.setText(helpString);
    }
}
