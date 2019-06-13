package com.ulez.bdxflibrary;

public class TtsException extends Exception {

    private int errorCode;

    public TtsException(int errorCode, String desc) {
        super(desc);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
