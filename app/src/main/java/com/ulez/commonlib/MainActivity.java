package com.ulez.commonlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ulez.bdxflibrary.asr.AsrListener;
import com.ulez.bdxflibrary.asr.AsrManager;
import com.ulez.bdxflibrary.asr.WakeListener;
import com.ulez.bdxflibrary.tts.TtsManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    AsrManager asrManager;
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean havePermissons = true;
    private TextView textView;
    private Button bt_Asr;
    private Button bt_Asr2;
    private Button bt_Tts;
    private Button bt_Tts2;
    private Handler mainHandler;
    private TtsManager ttsManager;
    private EditText etTts;
    private TextView wakeText;
    private TextView wakeTextBd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handle(msg);
            }

        };
        textView = findViewById(R.id.result);
        wakeText = findViewById(R.id.textWake);
        wakeTextBd = findViewById(R.id.textWake_bd);

        findViewById(R.id.bt_wake_bd).setOnClickListener(this);
        findViewById(R.id.bt_wake_xf).setOnClickListener(this);

        bt_Asr = findViewById(R.id.bt_asr);
        bt_Asr.setOnClickListener(this);

        bt_Asr2 = findViewById(R.id.bt_asr2);
        bt_Asr2.setOnClickListener(this);

        bt_Tts = findViewById(R.id.bt_tts);
        bt_Tts.setOnClickListener(this);

        bt_Tts2 = findViewById(R.id.bt_tts2);
        bt_Tts2.setOnClickListener(this);

        etTts = findViewById(R.id.et_tts);

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

    protected void handle(Message msg) {
        int what = msg.what;
        Log.i(TAG, "arg1=" + msg.arg1);
        Log.i(TAG, "msg=" + msg.toString());
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
        asrManager = AsrManager.getInstance(this, AsrManager.TYPE_B, new AsrListener() {
            @Override
            public void onResult(String result, boolean isLast) {
                Log.e(TAG, "result=" + result + isLast);
                textView.setText(result);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Exception=" + e.getMessage());
            }
        }, new WakeListener() {
            @Override
            public void onReady() {
                Log.i(TAG, "wake onReady");
                wakeTextBd.setText("请说唤醒词");
            }

            @Override
            public void onResult(String result) {
                Log.i(TAG, "wake onResult=" + result);
                wakeTextBd.setText("唤醒结果：" + result);
            }

            @Override
            public void onWakeInitError(RuntimeException e) {
                wakeText.setText(e.getMessage());
            }
        });
        ttsManager = TtsManager.getInstance(this, mainHandler, TtsManager.TTS_BD, "bdxfTTS");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_wake_bd:
                asrManager.initWakeUp(AsrManager.TYPE_B);
                break;
            case R.id.bt_wake_xf:
                asrManager.initWakeUp(AsrManager.TYPE_X);
                break;
            case R.id.bt_asr:
                textView.setText("百度：请说话...");
                String audioName = FileUtil.ASR_PCM_SAVE_PATH(this) + System.currentTimeMillis() + ".wav";
                asrManager.start(audioName, AsrManager.TYPE_B);
                break;
            case R.id.bt_asr2:
                textView.setText("讯飞：请说话...");
                String audioName2 = FileUtil.ASR_PCM_SAVE_PATH(this) + System.currentTimeMillis() + ".wav";
                asrManager.start(audioName2, AsrManager.TYPE_X);
                break;
            case R.id.bt_tts:
                ttsManager.speak(etTts.getText().toString(), TtsManager.TTS_BD, "output-bd.pcm");
                break;
            case R.id.bt_tts2:
                ttsManager.speak(etTts.getText().toString(), TtsManager.TTS_XF, "output-xf.pcm");
                break;
        }
    }
}
