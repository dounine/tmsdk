package com.starsriver.ftx.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dounine.tmsdk.core.TMSdk;
import com.dounine.tmsdk.model.Wechat;
import com.starsriver.ftx.MainActivity;
import com.starsriver.ftx.events.AuthLogin;
import com.starsriver.ftx.events.WeixinPayCallback;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.greenrobot.eventbus.EventBus;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private String TAG = "TMSdk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "wxPayEntryActivity onCreate");
        try {
            MainActivity.wx_api.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.i(TAG, "wechat auth request");
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.i(TAG, "wechat auth response " + baseResp.getType());
        switch (baseResp.getType()) {
            case ConstantsAPI.COMMAND_PAY_BY_WX:
                Log.d(TAG, "支付完成,errCode=" + baseResp.errCode);
                switch (baseResp.errCode) {
                    case 0:
                        EventBus.getDefault().post(
                                new WeixinPayCallback(
                                        "支付成功"
                                )
                        );
                        finish();
                        break;
                    default:
                        EventBus.getDefault().post(
                                new WeixinPayCallback(
                                        "支付失败"
                                )
                        );
                        finish();
                        break;
                }
        }
    }
}
