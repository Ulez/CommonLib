package com.ulez.bdxflibrary.tts;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.socks.library.KLog;
import com.ulez.bdxflibrary.TtsException;
import com.ulez.bdxflibrary.util.FileUtil;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.ulez.bdxflibrary.util.OfflineResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TtsManager {
    private static final String TAG = "TtsManager";
    public static final int TTS_BD = 0;
    public static final int TTS_XF = 1;
    private final String baseDirs;
    private int ttsType = 0;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    protected String appId = "11005757";
    protected String appKey = "Ovcz19MGzIKoDDb3IsFFncG1";
    protected String secretKey = "e72ebb6d43387fc7f85205ca7e6706e2";
    protected TtsMode ttsMode = TtsMode.MIX;
    protected String offlineVoice = OfflineResource.VOICE_MALE;
    protected MySyntherizer bdSynthesizer = null;
    private String voicer = "xiaoyan";
    private com.iflytek.cloud.SpeechSynthesizer xfSynthesizer = null;
    public static TtsManager instance;
    private Handler mainHandler;
    private Context context;
    private String fileName = null;

    private TtsManager(Context context, Handler mainHandler, int ttsType, String baseDirs, TtsListener ttsListener) {
        this.context = context.getApplicationContext();
        this.mainHandler = mainHandler;
        this.ttsType = ttsType;
        this.baseDirs = baseDirs;
        this.ttsListener = ttsListener;
        SpeechUtility.createUtility(context, "appid=" + "5cf72474");
        switch (ttsType) {
            case TTS_BD:
                initBdSynthesizer(baseDirs);
                break;
            case TTS_XF:
                xfSynthesizer = com.iflytek.cloud.SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
                break;
        }
    }

    public static TtsManager getInstance(Context context, Handler mainHandler, int ttsType, String baseDirs, TtsListener ttsListener) {
        if (instance == null) {
            synchronized (TtsManager.class) {
                instance = new TtsManager(context, mainHandler, ttsType, baseDirs, ttsListener);
            }
        }
        return instance;
    }

    /**
     * 文本转化为语音，播放并且保存录音。
     */
    public void speak(String text, String fileName) {
        speak(text, this.ttsType, fileName);
    }

    /**
     * 文本转化为语音，播放并且保存录音。
     */
    public void speak(String text, int ttsType, String fileName) {
        this.fileName = fileName;
        switch (ttsType) {
            case TTS_BD:
                // 合成前可以修改参数：
                // Map<String, String> params = getBdParams();
                // bdSynthesizer.setParams(params);
                if (bdSynthesizer == null) {
                    initBdSynthesizer(baseDirs);
                }
                ((FileSaveListener) bdSynthesizer.getInitConfig().getListener()).outName = fileName;
                KLog.i(TAG, "bdSynthesizer.speak(text)");
                int result = bdSynthesizer.speak(text);
                if (result != 0) {
                    KLog.e(TAG, "error code :" + result + " method:speak" + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
                }
                break;
            case TTS_XF:
                // 移动数据分析，收集开始合成事件
                if (xfSynthesizer == null) {
                    xfSynthesizer = com.iflytek.cloud.SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
                }
                // 设置参数
                setXfParam();
                int code = xfSynthesizer.startSpeaking(text, xfTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
			/*String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
			int code = xfSynthesizer.synthesizeToUri(text, path, xfTtsListener);*/
                if (code != ErrorCode.SUCCESS) {
                    KLog.e(TAG, "语音合成失败,错误码: " + code);
                    if (ttsListener != null)
                        ttsListener.onError(new TtsException(code, "xf语音合成失败,错误码: " + code));
                }
                break;
        }
    }

    private TtsListener ttsListener;

    /**
     * 合成回调监听。
     */
    private SynthesizerListener xfTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            KLog.i(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            KLog.i(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            KLog.i(TAG, "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            KLog.i(TAG, "合成进度=" + percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            KLog.i(TAG, "播放进度=" + percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                KLog.i(TAG, "播放完成");
            } else if (error != null) {
                if (ttsListener != null) {
                    ttsListener.onError(new TtsException(1, error.getPlainDescription(true)));
                }
                KLog.i(TAG, error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}

            //当设置SpeechConstant.TTS_DATA_NOTIFY为1时，抛出buf数据
			/*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
						byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
						KLog.e("MscSpeechLog", "buf is =" + buf);
					}*/

        }
    };
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                KLog.e(TAG, "初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private void setXfParam() {
        // 清空参数
        xfSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            xfSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //支持实时音频返回，仅在synthesizeToUri条件下支持
            //xfSynthesizer.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
            // 设置在线合成发音人
            xfSynthesizer.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            xfSynthesizer.setParameter(SpeechConstant.SPEED, "50");
            //设置合成音调
            xfSynthesizer.setParameter(SpeechConstant.PITCH, "50");
            //设置合成音量
            xfSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        } else {
            xfSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            xfSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        //设置播放器音频流类型
        xfSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        xfSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        if (!TextUtils.isEmpty(fileName)) {
            // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
            xfSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
            xfSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/" + baseDirs + "/" + fileName);
        }
    }

    private void initBdSynthesizer(String baseDirs) {
        KLog.i(TAG, "initBdSynthesizer");
        String tmpDir = FileUtil.createTmpDir(context, baseDirs);
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new FileSaveListener(mainHandler, tmpDir);
        Map<String, String> params = getBdParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        bdSynthesizer = new MySyntherizer(context, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    protected Map<String, String> getBdParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createBdOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createBdOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(context, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            KLog.e(TAG, "【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }
}
