package com.ulez.bdxflibrary.asr;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.ulez.bdxflibrary.util.FileUtil;
import com.ulez.bdxflibrary.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AsrManager {
    private final String TAG = "AsrManager";
    public static final int TYPE_B = 0;//百度
    public static final int TYPE_X = 1;//讯飞
    private static AsrManager instance;
    private final Context context;
    private AsrListener asrListener;
    private int asrType = 0;
    private EventManager bdAsr;
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private String resultType = "json";
    int ret = 0; // 函数调用返回值
    private SpeechRecognizer mIat;

    public static AsrManager getInstance(Context context, int asrType, AsrListener asrListener) {
        if (instance == null) {
            synchronized (AsrManager.class) {
                instance = new AsrManager(context, asrType, asrListener);
            }
        }
        return instance;
    }

    private AsrManager(Context context, int asrType, final AsrListener asrListener) {
        this.context = context.getApplicationContext();
        this.asrType = asrType;
        this.asrListener = asrListener;
        switch (asrType) {
            case TYPE_B:
                initBdAsr(context);
                break;
            case TYPE_X:
                initXfAsr(context);
                break;
        }
    }

    private void initXfAsr(Context context) {
        SpeechUtility.createUtility(context, "appid=" + "5cf72474");
//                FlowerCollector.onEvent(context, "iat_recognize");
        //初始化识别无UI识别对象
//使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
//设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
        mIat.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
        mIat.setParameter(SpeechConstant.SUBJECT, null);
//设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
//此处engineType为“cloud”
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
//设置语音输入语言，zh_cn为简体中文
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//设置结果返回语言
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
// 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
//取值范围{1000～10000}
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
//设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
//自动停止录音，范围{0~10000}
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
//设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
    }

    private void initBdAsr(Context context) {
        bdAsr = EventManagerFactory.create(context, "asr");
        bdAsr.registerListener(bdListener);
    }

    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG, "初始化失败，错误码：" + code);
            }
        }
    };

    private EventListener bdListener = new EventListener() {
        @Override
        public void onEvent(String name, String params, byte[] data, int offset, int length) {
            try {
//                Log.e(TAG, "name=" + name + ",,,params=" + params);
                if ("asr.partial".equals(name)) {
                    JSONObject jsonObject = new JSONObject(params);
                    String result = jsonObject.getString("result_type");
                    if ("final_result".equals(result)) {
                        asrListener.onResult(jsonObject.getString("best_result"), true);
                    }
                } else if ("asr.finish".equals(name)) {
                    FileUtil.pcm2wav(outPath);
                    outPath = null;
                }
            } catch (JSONException e) {
                asrListener.onError(e);
                e.printStackTrace();
            }
        }
    };
    private RecognizerListener xfListener = new RecognizerListener() {
        private HashMap<String, String> mIatResults;

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.e(TAG, "开始说话");
            if (mIatResults == null)
                mIatResults = new LinkedHashMap<String, String>();
            mIatResults.clear();
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            if (error.getErrorCode() == 14002) {
                Log.e(TAG, error.getPlainDescription(true) + "\n请确认是否已开通翻译功能");
            } else {
                Log.e(TAG, error.getPlainDescription(true));
            }
            asrListener.onError(error);
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.e(TAG, "结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, "results.getResultString=" + results.getResultString());
            //处理结果，
            String text = JsonParser.parseIatResult(results.getResultString());
            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults.put(sn, text);
            Log.e(TAG, mIatResults.toString());
            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }
            if (isLast) asrListener.onResult(resultBuffer.toString(), true);
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            Log.e(TAG, "当前正在说话，音量大小：" + volume);
//            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };
    private String outPath = null;

    public void start(String outPath, int asrType) {
        switch (asrType) {
            case TYPE_B:
                if (bdAsr == null) {
                    initBdAsr(context);
                }
                Map<String, Object> params = new LinkedHashMap<String, Object>();
                params.put(com.baidu.speech.asr.SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
                params.put(com.baidu.speech.asr.SpeechConstant.ACCEPT_AUDIO_DATA, true);
                FileUtil.createASRFile(outPath, context);
                if (!TextUtils.isEmpty(outPath)) {
                    FileUtil.createASRFile(outPath, context);
                    if (outPath.contains(".wav")) {
                        this.outPath = outPath.replace(".wav", ".pcm");
                        params.put(com.baidu.speech.asr.SpeechConstant.OUT_FILE, this.outPath);
                    }
                }
                params.put(com.baidu.speech.asr.SpeechConstant.VAD, com.baidu.speech.asr.SpeechConstant.VAD_DNN);
                params.put(com.baidu.speech.asr.SpeechConstant.VAD_ENDPOINT_TIMEOUT, 1000);
                params.put(com.baidu.speech.asr.SpeechConstant.PROP, 20000);
                params.put(com.baidu.speech.asr.SpeechConstant.PID, 1536); // 中文输入法模型，有逗号
                String json = null; // 可以替换成自己的json
                json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
                bdAsr.send(com.baidu.speech.asr.SpeechConstant.ASR_START, json, null, 0, 0);
                break;
            case TYPE_X:
                if (mIat == null) {
                    initXfAsr(context);
                }
                // 不显示听写对话框
                // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
                if (!TextUtils.isEmpty(outPath)) {
                    mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
                    mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, outPath);
                }
                ret = mIat.startListening(xfListener);
                if (ret != ErrorCode.SUCCESS) {
                    Log.e(TAG, "听写失败,错误码：" + ret);
                } else {
                    Log.e(TAG, "请开始说话…");
                }
                break;
        }
    }

    public void start(String outPath) {
        start(outPath, asrType);
    }
}
