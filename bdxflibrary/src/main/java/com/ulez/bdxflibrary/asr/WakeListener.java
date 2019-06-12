package com.ulez.bdxflibrary.asr;
public interface WakeListener {
    void onReady();
    void onResult(String result);
    void onWakeInitError(RuntimeException e);
}
