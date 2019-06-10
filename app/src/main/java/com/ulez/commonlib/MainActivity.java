package com.ulez.commonlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ulez.bdxflibrary.AsrListener;
import com.ulez.bdxflibrary.AsrManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    TextView textView;
    Button button;
    AsrManager asrManager;
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean havePermissons = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.result);
        button = findViewById(R.id.bt);
        button.setOnClickListener(this);

        int permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(PERMISSIONS, 4);
                havePermissons = false;
            }
        }
        if (havePermissons) onSuccessPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults[0] == 0) {
            Log.i(TAG, "授权成功");
            onSuccessPermission();
        } else {
            Log.e(TAG, "授权失败");
        }
    }

    private void onSuccessPermission() {
        asrManager = AsrManager.getInstance(this, AsrManager.TYPE_X, new AsrListener() {
            @Override
            public void onResult(String result, boolean isLast) {
                Log.e(TAG, "result=" + result + isLast);
                textView.setText(result);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Exception=" + e.getMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt:
                textView.setText(" ");
                asrManager.start();
                Log.e(TAG, "asrManager.start");
                break;
        }
    }
}
