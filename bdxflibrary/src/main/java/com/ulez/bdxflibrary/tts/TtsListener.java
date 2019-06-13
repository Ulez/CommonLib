package com.ulez.bdxflibrary.tts;

import com.ulez.bdxflibrary.TtsException;

public interface TtsListener {
    void onResult(String result);
    void onError(TtsException e);
}
