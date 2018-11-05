package com.amway.wifianalyze.home;

/**
 * Created by big on 2018/10/26.
 */

public class DetectResult {
    public enum Status {SUCCESS, WARN, ERROR}

    private Status status;
    private String content;
    private boolean loading=true;

    public DetectResult(Status status, String content) {
        this.status = status;
        this.content = content;
    }

    public Status getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
