package com.ulez.bdxflibrary;
public interface AsrListener {
    void onResult(String result, boolean isLast);

    void onError(Exception e);
}
