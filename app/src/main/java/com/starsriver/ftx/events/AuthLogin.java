package com.starsriver.ftx.events;

import com.dounine.tmsdk.model.Wechat;

public class AuthLogin {

    private Wechat.LoginResponse response;

    public AuthLogin(Wechat.LoginResponse response) {
        this.response = response;
    }

    public Wechat.LoginResponse getResponse() {
        return response;
    }
}
