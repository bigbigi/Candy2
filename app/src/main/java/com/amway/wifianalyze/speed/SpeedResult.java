package com.amway.wifianalyze.speed;

/**
 * Created by big on 2018/10/26.
 */

public class SpeedResult {
    private String name;
    private String result;

    public SpeedResult(String name, String result) {
        this.name = name;
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public String getResult() {
        return result;
    }
}
