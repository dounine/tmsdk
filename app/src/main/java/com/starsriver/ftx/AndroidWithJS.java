package com.starsriver.ftx;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.dounine.tmsdk.core.TMSdk;
import com.dounine.tmsdk.model.Wechat;
import com.dounine.tmsdk.util.StaticConfig;
import com.starsriver.ftx.events.WeixinPay;

import org.greenrobot.eventbus.EventBus;

/**
 * 接受来自页面数据
 */
public class AndroidWithJS {

    private Context context;

    public AndroidWithJS(Context context) {
        this.context = context;
    }

    /**
     * 初始化appid,programId,channel
     */
    @JavascriptInterface
    public void init(String appid, String programId, String channel, String weixinLoginCallbackName, String weixinPayCallbackName) {
        StaticConfig.Companion.setAPPID(appid);
        StaticConfig.Companion.setProgramId(programId);
        StaticConfig.Companion.setCHANNEL(channel);
        MainActivity.weixinInit(appid);
        MainActivity.weixinLoginCallbackName = weixinLoginCallbackName;
        MainActivity.weixinPayCallbackName = weixinPayCallbackName;
    }

    /**
     * 手动控制SDK初始化，主要是为了使用授权后的用户ID
     *
     * @param userId 用户自定义id,使用open_id,或者union_id
     */
    @JavascriptInterface
    public String loginReport(String userId) {
        TMSdk.Companion.init(context, StaticConfig.Companion.getAPPID(), StaticConfig.Companion.getProgramId(), userId, StaticConfig.Companion.getCHANNEL());
        TMSdk.Companion.appStart();
        return "初始化成功";
    }

    /**
     * 微信授权登录
     */
    @JavascriptInterface
    public void weixinLogin() {
        MainActivity.weixinLogin();
    }

    /**
     * 微信拉起支付支付
     */
    @JavascriptInterface
    public void weixinPay(int coin, String userId, String programParam) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Wechat.OrderResponse response = TMSdk.Companion.weixinPayCreateOrder(StaticConfig.Companion.getProgramId(), coin, userId, programParam);
                EventBus.getDefault().post(
                        new WeixinPay(
                                response
                        )
                );
            }
        }).start();
    }

    /**
     * 查询定单状态
     */
    /**
     * 查询定单状态
     *
     * @param orderId 定单id
     * @return {
     * "err": 0,     //0成功，非0请求异常
     * "data": {
     * "pay_status": 0,      //支付状态：0待支付，1支付成功，2支付失败
     * "order_status": 2     //订单状态：0下单，1待发货，2取消订单，3发货成功
     * },
     * "msg": "请求成功"
     * }
     */
    @JavascriptInterface
    public String queryOrder(String orderId) {
        return TMSdk.Companion.objectToJson(MainActivity.weixinOrderQuery(orderId));
    }

    /**
     * 用户实时认证查询
     *
     * @return {
     * "err": 0,     //0成功，非0请求异常
     * "data": {
     * "result": true  //true认证成功，false认证失败
     * },
     * "msg": "请求成功"
     * }
     */
    @JavascriptInterface
    public String identifyQuery(String userId) {
        return TMSdk.Companion.objectToJson(MainActivity.identifyQuery(userId));
    }

    /**
     * @param name
     * @param id
     * @return {
     * "err": 0,     //0成功，非0请求异常
     * "data": {
     * "result": true  //true认证成功，false需要认证
     * },
     * "msg": "请求成功"
     * }
     */
    @JavascriptInterface
    public String identify(String userId, String name, String id) {
        return TMSdk.Companion.objectToJson(MainActivity.identify(userId, name, id));
    }

}
