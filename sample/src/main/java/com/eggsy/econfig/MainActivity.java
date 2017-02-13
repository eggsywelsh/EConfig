package com.eggsy.econfig;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    Button mShowConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mShowConfig = (Button) findViewById(R.id.btn_show_config);
        mShowConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, RuntimeConfig.obtain().baseSdcardDir);
                Log.i(TAG, RuntimeConfig.obtain().dbName);
                Log.i(TAG, RuntimeConfig.obtain().dateTime + "");
                Log.i(TAG, RuntimeConfig.obtain().logLevel+ "");
                Log.i(TAG, RuntimeConfig.obtain().minFreeSdcardSize+ "");
                Log.i(TAG, RuntimeConfig.obtain().version+ "");
            }
        });
    }
}
