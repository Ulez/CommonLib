package com.ulez.bdxflibrary.nlu;
import com.baidu.aip.nlp.AipNlp;
import com.baidu.aip.nlp.ESimnetType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class NluManager {
    private static NluManager instance;
    private final AipNlp client;

    //设置APPID/AK/SK
    public static final String APP_ID = "16537125";
    public static final String API_KEY = "QVUskzDfRiaCuM6QQ4BcWhEe";
    public static final String SECRET_KEY = "f2gT7q4qvr8CtpyfEq7cUSyoKFLgKOji";

    private NluManager() {
        // 初始化一个AipNlp
        client = new AipNlp(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
//        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
    }

    public static NluManager getInstance() {
        if (instance == null) {
            synchronized (NluManager.class) {
                if (instance == null)
                    instance = new NluManager();
            }
        }
        return instance;
    }

    /**
     * 词法分析
     *
     * @param text
     * @return
     */
    public String lexer(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.lexer(text, options);
        // 调用接口
        return getResult(res);
    }

    /**
     * 词法分析（定制版）
     *
     * @param text
     * @param options
     * @return
     */
    public String lexerCustom(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.lexerCustom(text, options);
        return getResult(res);
    }

    /**
     * 依存句法分析
     *
     * @param text
     * @param options
     * @return
     */
    public String depParser(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.depParser(text, options);
        return getResult(res);
    }

    /**
     * 词向量表示
     *
     * @param text
     * @param options
     * @return
     */
    public String wordEmbedding(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.wordEmbedding(text, options);
        return getResult(res);
    }

    /**
     * DNN语言模型
     *
     * @param text
     * @param options
     * @return
     */
    public String dnnlmCn(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.dnnlmCn(text, options);
        return getResult(res);
    }

    /**
     * 词义相似度
     *
     * @param text
     * @param options
     * @return
     */
    public String wordSimEmbedding(String text, String text2, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.wordSimEmbedding(text, text2, options);
        return getResult(res);
    }

    /**
     * 短文本相似度
     *
     * @param text
     * @param options
     * @return
     */
    public String simnet(String text, String text2, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.simnet(text, text2, options);
        return getResult(res);
    }

    /**
     * 评论观点抽取
     *
     * @param text
     * @param options
     * @return
     */
    public String commentTag(String text, ESimnetType eSimnetType, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.commentTag(text, eSimnetType, options);
        String result = res.toString();
        return result;
    }

    /**
     * 情感倾向分析
     *
     * @param text
     * @param options
     * @return
     */
    public String sentimentClassify(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.sentimentClassify(text, options);
        return getResult(res);
    }


    /**
     * 文章标签
     *
     * @param title
     * @param content
     * @param options
     * @return
     */
    public String keyword(String title, String content, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.keyword(title, content, options);
        return getResult(res);
    }

    /**
     * 文章分类
     *
     * @param title
     * @param content
     * @param options
     * @return
     */
    public String topic(String title, String content, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.topic(title, content, options);
        return getResult(res);
    }
    /**
     * 文本纠错
     *
     * @param text
     * @param options
     * @return
     */
    public String ecnet(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.ecnet(text,  options);
        return getResult(res);
    }
    /**
     * 对话情绪识别
     *
     * @param text
     * @param options
     * @return
     */
    public String emotion(String text, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.emotion(text,  options);
        return getResult(res);
    }
    /**
     * 新闻摘要接口
     *
     * @param text
     * @param options
     * @return
     */
    public String newsSummary(String text,int maxSummaryLen, HashMap<String, Object> options) {
        // 词法分析
        JSONObject res = client.newsSummary(text,maxSummaryLen,  options);
        return getResult(res);
    }


    private String getResult(JSONObject res) {
        String result = null;
        try {
            result = res.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
