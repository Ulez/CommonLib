package com.ulez.commonlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ulez.bdxflibrary.TtsException;
import com.ulez.bdxflibrary.asr.AsrListener;
import com.ulez.bdxflibrary.asr.AsrManager;
import com.ulez.bdxflibrary.asr.WakeListener;
import com.ulez.bdxflibrary.nlu.NluManager;
import com.ulez.bdxflibrary.tts.TtsListener;
import com.ulez.bdxflibrary.tts.TtsManager;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    AsrManager asrManager;
    @BindView(R.id.textWake_bd)
    TextView textWakeBd;
    @BindView(R.id.bt_wake_bd)
    Button btWakeBd;
    @BindView(R.id.bt_wake_xf)
    Button btWakeXf;
    @BindView(R.id.result)
    TextView tvResult;
    @BindView(R.id.bt_asr)
    Button btAsr;
    @BindView(R.id.bt_asr2)
    Button btAsr2;
    @BindView(R.id.et_tts)
    EditText etTts;
    @BindView(R.id.bt_tts)
    Button btTts;
    @BindView(R.id.bt_tts_pause)
    Button btTtsPause;
    @BindView(R.id.bt_tts_resume)
    Button btTtsResume;
    @BindView(R.id.bt_tts_stop)
    Button btTtsStop;
    @BindView(R.id.bt_tts2)
    Button btTts2;
    @BindView(R.id.bt_tts2_pause)
    Button btTts2Pause;
    @BindView(R.id.bt_tts2_resume)
    Button btTts2Resume;
    @BindView(R.id.bt_tts2_stop)
    Button btTts2Stop;
    @BindView(R.id.et_nlu)
    EditText etNlu;
    @BindView(R.id.bt_lexer)
    Button btLexer;
    @BindView(R.id.bt_lexerCustom)
    Button btLexerCustom;
    @BindView(R.id.bt_wordEmbedding)
    Button btWordEmbedding;
    @BindView(R.id.bt_dnnlmCn)
    Button btDnnlmCn;
    @BindView(R.id.bt_wordSimEmbedding)
    Button btWordSimEmbedding;
    @BindView(R.id.bt_simnet)
    Button btSimnet;
    @BindView(R.id.bt_commentTag)
    Button btCommentTag;
    @BindView(R.id.bt_sentimentClassify)
    Button btSentimentClassify;
    @BindView(R.id.bt_keyword)
    Button btKeyword;
    @BindView(R.id.bt_topic)
    Button btTopic;
    @BindView(R.id.bt_ecnet)
    Button btEcnet;
    @BindView(R.id.bt_emotion)
    Button btEmotion;
    @BindView(R.id.bt_newsSummary)
    Button btNewsSummary;
    @BindView(R.id.tv_nlu)
    TextView tvNlu;
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean havePermissons = true;
    private static Handler mainHandler;
    private TtsManager ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handle(msg);
                switch (msg.what) {
                    case 10086:
                        tvNlu.setText((String) msg.obj);
                        break;
                }

            }

        };
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

    @OnClick({R.id.textWake_bd, R.id.bt_wake_bd, R.id.bt_wake_xf, R.id.result, R.id.bt_asr, R.id.bt_asr2, R.id.et_tts, R.id.bt_tts, R.id.bt_tts_pause, R.id.bt_tts_resume, R.id.bt_tts_stop, R.id.bt_tts2, R.id.bt_tts2_pause, R.id.bt_tts2_resume, R.id.bt_tts2_stop, R.id.et_nlu, R.id.bt_lexer, R.id.bt_lexerCustom, R.id.bt_wordEmbedding, R.id.bt_dnnlmCn, R.id.bt_wordSimEmbedding, R.id.bt_simnet, R.id.bt_commentTag, R.id.bt_sentimentClassify, R.id.bt_keyword, R.id.bt_topic, R.id.bt_ecnet, R.id.bt_emotion, R.id.bt_newsSummary, R.id.tv_nlu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_wake_bd:
                asrManager.startWakeUp(AsrManager.TYPE_B);
                textWakeBd.setText("唤醒词：百度一下、小度你好");

                break;
            case R.id.bt_wake_xf:
                textWakeBd.setText("唤醒词：小安小安");
                asrManager.startWakeUp(AsrManager.TYPE_X);
                break;
            case R.id.bt_asr:
                tvResult.setText("百度：请说话...");
                String audioName = FileUtil.ASR_PCM_SAVE_PATH(this) + System.currentTimeMillis() + ".wav";
                asrManager.start(audioName, AsrManager.TYPE_B);
                break;
            case R.id.bt_asr2:
                tvResult.setText("讯飞：请说话...");
                String audioName2 = FileUtil.ASR_PCM_SAVE_PATH(this) + System.currentTimeMillis() + ".wav";
                asrManager.start(audioName2, AsrManager.TYPE_X);
                break;
            case R.id.bt_tts:
                ttsManager.speak(etTts.getText().toString(), TtsManager.TTS_BD, "output-bd.pcm");
                break;
            case R.id.bt_tts2:
                ttsManager.speak(etTts.getText().toString(), TtsManager.TTS_XF, "output-xf.pcm");
                break;
            case R.id.bt_tts_pause:
            case R.id.bt_tts2_pause:
                ttsManager.pause();
                break;
            case R.id.bt_tts_resume:
            case R.id.bt_tts2_resume:
                ttsManager.resume();
                break;
            case R.id.bt_tts_stop:
            case R.id.bt_tts2_stop:
                ttsManager.stop();
                break;
            case R.id.bt_lexer:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, Object> options = new HashMap<String, Object>();
                        String result = NluManager.getInstance().lexerCustom(etNlu.getText().toString(), options);
                        Log.e(TAG, result);
                        Message ms = mainHandler.obtainMessage();
                        ms.obj = result;
                        ms.what = 10086;
                        mainHandler.sendMessage(ms);
                    }
                }).start();
                break;
        }
    }

    private void onSuccessPermission() {
        asrManager = AsrManager.getInstance(this, AsrManager.TYPE_B, new AsrListener() {
            @Override
            public void onResult(String result, boolean isLast) {
                Log.e(TAG, "result=" + result + isLast);
                tvResult.setText(result);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Exception=" + e.getMessage());
            }
        }, new WakeListener() {
            @Override
            public void onReady() {
                Log.i(TAG, "wake onReady");
                textWakeBd.setText("请说唤醒词");
            }

            @Override
            public void onResult(String result) {
                Log.i(TAG, "wake onResult=" + result);
                textWakeBd.setText("唤醒结果：" + result);
            }

            @Override
            public void onWakeInitError(RuntimeException e) {
                textWakeBd.setText(e.getMessage());
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "唤醒error" + error.getMessage());
            }
        });
        ttsManager = TtsManager.getInstance(this, mainHandler, TtsManager.TTS_BD, "bdxfTTS", new TtsListener() {

            @Override
            public void onResult(String result) {

            }

            @Override
            public void onError(TtsException e) {
                T(e.getMessage());
            }
        });
    }


    private void T(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
