package com.ulez.bdxflibrary.asr;
public interface AsrListener {
    void onResult(String result, boolean isLast);

    void onError(Exception e);


}
