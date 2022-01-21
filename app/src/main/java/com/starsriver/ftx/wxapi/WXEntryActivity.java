package com.starsriver.ftx.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dounine.tmsdk.core.TMSdk;
import com.dounine.tmsdk.model.Wechat;
import com.dounine.tmsdk.util.StaticConfig;
import com.starsriver.ftx.MainActivity;
import com.starsriver.ftx.events.AuthLogin;
import com.starsriver.ftx.events.WeixinPayCallback;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.greenrobot.eventbus.EventBus;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private String TAG = "TMSdk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "wxEntryActivity onCreate");
        try {
            MainActivity.wx_api.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.i("sdk", "wechat auth request");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.i(TAG, "wechat auth response " + baseResp.getType());
        switch (baseResp.getType()) {
            case ConstantsAPI.COMMAND_SENDAUTH:
                String result = "授权成功";
                String code;
                switch (baseResp.errCode) {
                    case BaseResp.ErrCode.ERR_OK:
                        Log.i(TAG, "授权成功");
                        code = ((SendAuth.Resp) baseResp).code;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Wechat.LoginResponse response = TMSdk.Companion.wechatUserInfo(StaticConfig.Companion.getProgramId(), code);
                                EventBus.getDefault().post(
                                        new AuthLogin(
                                                response
                                        )
                                );
                            }
                        }).start();
                        finish();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        Log.i(TAG, "用户取消");
                        result = "用户取消";
                        EventBus.getDefault().post(new AuthLogin(
                                new Wechat.LoginResponse(
                                        -1,
                                        null,
                                        result
                                )
                        ));
                        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        Log.i(TAG, "用户拒绝授权");
                        result = "用户拒绝授权";
                        EventBus.getDefault().post(new AuthLogin(
                                new Wechat.LoginResponse(
                                        -1,
                                        null,
                                        result
                                )
                        ));
                        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    default:
                        Log.i(TAG, "返回");
                        result = "返回";
                        EventBus.getDefault().post(new AuthLogin(
                                new Wechat.LoginResponse(
                                        -1,
                                        null,
                                        result
                                )
                        ));
                        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                        finish();
                        break;
                }
        }
    }
}
