package com.ulez.bdxflibrary.bean;

public class XfWakeResult {


    @Override
    public String toString() {
        return "XfWakeResult{" +
                "操作类型sst='" + sst + '\'' +
                ", 唤醒词id=" + id +
                ", 得分score=" + score +
                ", 前端点bos=" + bos +
                ", 尾端点eos=" + eos +
                ", keyword='" + keyword + '\'' +
                '}';
    }

    /**
     * 讯飞唤醒结果
     * sst : wakeup
     * id : 0
     * score : 2041
     * bos : 5950
     * eos : 6740
     * keyword : xiao3-an1-xiao3-an1
     * 【操作类型】wakeup
     * 【唤醒词id】0
     * 【得分】2041
     * 【前端点】5950
     * 【尾端点】6740
     */

    private String sst;
    private int id;
    private int score;
    private int bos;
    private int eos;
    private String keyword;

    public String getSst() {
        return sst;
    }

    public void setSst(String sst) {
        this.sst = sst;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getBos() {
        return bos;
    }

    public void setBos(int bos) {
        this.bos = bos;
    }

    public int getEos() {
        return eos;
    }

    public void setEos(int eos) {
        this.eos = eos;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
