package com.ulez.bdxflibrary.tts;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

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
    protected String appId = "11005757";
    protected String appKey = "Ovcz19MGzIKoDDb3IsFFncG1";
    protected String secretKey = "e72ebb6d43387fc7f85205ca7e6706e2";
    protected TtsMode ttsMode = TtsMode.MIX;
    protected String offlineVoice = OfflineResource.VOICE_MALE;
    protected MySyntherizer synthesizer;
    public static TtsManager instance;
    private Handler mainHandler;
    private Context context;

    private TtsManager(Context context, Handler mainHandler) {
        this.context = context;
        this.mainHandler = mainHandler;
        initialTts();
    }

    public static TtsManager getInstance(Context context, Handler mainHandler) {
        if (instance == null) {
            synchronized (TtsManager.class) {
                instance = new TtsManager(context.getApplicationContext(), mainHandler);
            }
        }
        return instance;
    }

    /**
     * 文本转化为语音，播放并且保存录音。
     */
    public void speak(String text) {
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);
        if (result != 0) {
            Log.e(TAG, "error code :" + result + " method:speak" + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    private void initialTts() {
        String tmpDir = FileUtil.createTmpDir(context,"baiduTTS");
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new FileSaveListener(mainHandler, tmpDir);
        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        synthesizer = new MySyntherizer(context, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    protected Map<String, String> getParams() {
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
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(context, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.e(TAG, "【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }
}
