package com.ulez.bdxflibrary.asr;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
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
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.ulez.bdxflibrary.AsrException;
import com.ulez.bdxflibrary.R;
import com.ulez.bdxflibrary.util.FileUtil;
import com.ulez.bdxflibrary.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class AsrManager {
    // xf语音唤醒对象
    private VoiceWakeuper mIvw;
    // xf唤醒结果内容
    private String resultString;
    private int curThresh = 1450;
    private String keep_alive = "1";
    private String ivwNetMode = "0";

    private int asr_status = AsrStatus.ASR_STATUS_ORINGIN;

    private final String TAG = "AsrManager";
    public static final int TYPE_B = 0;//百度
    public static final int TYPE_X = 1;//讯飞
    private static AsrManager instance;
    private final Context context;
    private AsrListener asrListener;
    private WakeListener wakeListener;
    private int asrType = 0;
    private EventManager bdAsr;
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private String resultType = "json";
    int ret = 0; // 函数调用返回值
    private SpeechRecognizer mIat;

    public static AsrManager getInstance(Context context, int asrType, AsrListener asrListener, WakeListener wakeListener) {
        if (instance == null) {
            synchronized (AsrManager.class) {
                instance = new AsrManager(context, asrType, asrListener, wakeListener);
            }
        }
        return instance;
    }

    private AsrManager(Context context, int asrType, final AsrListener asrListener, WakeListener wakeListener) {
        this.context = context.getApplicationContext();
        this.asrType = asrType;
        this.asrListener = asrListener;
        this.wakeListener = wakeListener;
        SpeechUtility.createUtility(context, "appid=" + "5cf72474");
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
                Log.e(TAG, "name=" + name + ",,,params=" + params);
                try {
                    if (TextUtils.isEmpty(params)) return;
                    JSONObject json = new JSONObject(params);
                    int errorCode = json.getInt("errorCode");
                    if (errorCode != 0) {
                        String desc = json.getString("desc");
                        asrListener.onError(new AsrException(errorCode, desc));
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (name) {
                    case "asr.partial":
                        JSONObject jsonObject = new JSONObject(params);
                        String result = jsonObject.getString("result_type");
                        if ("final_result".equals(result)) {
                            if (asrListener != null)
                                asrListener.onResult(jsonObject.getString("best_result"), true);
                        }
                        break;
                    case "asr.finish":
                        FileUtil.pcm2wav(outPath);
                        outPath = null;
                        asr_status = AsrStatus.ASR_STATUS_ORINGIN;
                        break;
                    case "wp.data":
                        if (wakeListener != null)
                            wakeListener.onResult(params);
                        break;
                    case "wp.ready":
                        if (wakeListener != null)
                            wakeListener.onReady();
                        break;
                }
            } catch (JSONException e) {
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
            if (asrListener != null)
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
            if (isLast) {
                if (asrListener != null)
                    asrListener.onResult(resultBuffer.toString(), true);
            }
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
                if (asr_status == AsrStatus.ASR_STATUS_ASR_RECORDING) {
                    return;
                }
                if (bdAsr != null && (asr_status == AsrStatus.ASR_STATUS_WAKE_RECORDING)) {
                    bdAsr.send(com.baidu.speech.asr.SpeechConstant.WAKEUP_STOP, null, null, 0, 0);
                    bdAsr.unregisterListener(bdListener);
                    bdAsr = null;
                }
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
                asr_status = AsrStatus.ASR_STATUS_ASR_RECORDING;
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

    public void initWakeUp(int asrType) {
        switch (asrType) {
            case TYPE_B:
                initBdWake();
                break;
            case TYPE_X:
                initXfWake();
                break;
        }
    }

    public void initWakeUp() {
        initWakeUp(asrType);
    }

    // TODO: 2019/6/12 百度唤醒初始化等待唤醒
    private void initBdWake() {
        if (asr_status == AsrStatus.ASR_STATUS_WAKE_RECORDING) {
            return;
        }
        if (bdAsr != null && (asr_status == AsrStatus.ASR_STATUS_ASR_RECORDING)) {
            bdAsr.send(com.baidu.speech.asr.SpeechConstant.ASR_STOP, null, null, 0, 0);
            bdAsr.unregisterListener(bdListener);
            bdAsr = null;
        }
        bdAsr = EventManagerFactory.create(context, "wp");
        // 基于SDK唤醒词集成1.3 注册输出事件
        bdAsr.registerListener(bdListener); //  EventListener 中 onEvent方法
        Map<String, Object> params = new TreeMap<String, Object>();
        params.put(com.baidu.speech.asr.SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(com.baidu.speech.asr.SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");//小度你好
        params.put(com.baidu.speech.asr.SpeechConstant.APP_ID, "16497560");// TODO: 2019/6/12
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        String json = null; // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        bdAsr.send(com.baidu.speech.asr.SpeechConstant.WAKEUP_START, json, null, 0, 0);
        asr_status = AsrStatus.ASR_STATUS_WAKE_RECORDING;
    }

    private void initXfWake() {
        mIvw = VoiceWakeuper.createWakeuper(context, null);
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            Log.i(TAG, "setRadioEnable(false)");
            resultString = "";
//            textView.setText(resultString);
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            Log.i(TAG, "// 启动唤醒");
            mIvw.startListening(mWakeuperListener);
        } else {
            if (wakeListener != null)
                wakeListener.onWakeInitError(new RuntimeException("xf唤醒未初始化"));
        }
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d(TAG, "onResult");
            if (!"1".equalsIgnoreCase(keep_alive)) {
                Log.i(TAG, "setRadioEnable true");
            }
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            Log.i(TAG, "resultString=" + resultString);
        }

        @Override
        public void onError(SpeechError error) {
            Log.e(TAG, error.getPlainDescription(true));
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };


    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + context.getString(R.string.app_id_xf) + ".jet");
        Log.i(TAG, "resPath: " + resPath);
        return resPath;
    }

}
