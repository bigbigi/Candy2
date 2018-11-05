package com.amway.wifianalyze.home;

/**
 * Created by big on 2018/10/26.
 */

public class DetectResult {
    public enum Status {LOADING, SUCCESS, WARN, ERROR}

    private Status status;
    private String content;
    private int code;

    public DetectResult(Status status, int code, String content) {
        this.status = status;
        this.code = code;
        this.content = content;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
