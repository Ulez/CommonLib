package com.ulez.bdxflibrary.bean;

public class BdWakeResult {

    /**
     * 百度唤醒结果
     * errorDesc : wakup success
     * errorCode : 0
     * word : 小度你好
     */

    private String errorDesc;
    private int errorCode;
    private String word;

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
