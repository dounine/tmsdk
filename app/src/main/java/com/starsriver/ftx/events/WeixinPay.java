package com.starsriver.ftx.events;

import com.dounine.tmsdk.model.Wechat;

public class WeixinPay {
    Wechat.OrderResponse response;

    public WeixinPay(Wechat.OrderResponse response) {
        this.response = response;
    }

    public Wechat.OrderResponse getResponse() {
        return response;
    }
}
