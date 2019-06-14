package com.ulez.bdxflibrary.asr;

public interface AsrListener {
    //识别结果回调
    void onResult(String result, boolean isLast);

    void onError(Exception e);
}
