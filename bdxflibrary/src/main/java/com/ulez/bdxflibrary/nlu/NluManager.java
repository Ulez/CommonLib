package com.ulez.bdxflibrary.nlu;

import com.baidu.aip.nlp.AipNlp;

import org.json.JSONException;
import org.json.JSONObject;

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
                instance = new NluManager();
            }
        }
        return instance;
    }

    /**
     * 词法分析（定制版）
     *
     * @param text
     * @return
     */
    public String lexer_custom(String text) {
        // 调用接口
        JSONObject res = client.lexer(text, null);
        String result = null;
        try {
            result = res.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
