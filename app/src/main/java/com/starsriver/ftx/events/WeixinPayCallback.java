package com.starsriver.ftx.events;

public class WeixinPayCallback {

    private String result;

    public WeixinPayCallback(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
