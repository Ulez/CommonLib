package com.ulez.bdxflibrary;

public class AsrException extends Exception {

    private int errorCode;

    public AsrException(int errorCode, String desc) {
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
