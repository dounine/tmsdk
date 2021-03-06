package com.starsriver.ftx;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.dounine.tmsdk.core.TMSdk;
import com.dounine.tmsdk.model.Identifys;
import com.dounine.tmsdk.model.Wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;

import com.dounine.tmsdk.util.StaticConfig;
import com.starsriver.ftx.events.AuthLogin;
import com.starsriver.ftx.events.WeixinPay;
import com.starsriver.ftx.events.WeixinPayCallback;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.vasdolly.helper.ChannelReaderUtil;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;
import com.xuexiang.xupdate.utils.UpdateUtils;

import android.view.Menu;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    public static IWXAPI wx_api;
    private static final String TAG = "TMSdk";

    public Wechat.OrderResponse order;
    private static Context context;
    public static String weixinLoginCallbackName;
    public static String weixinPayCallbackName;
    private GameWebView gameWebView = new GameWebView();
    public static Application application;

    /**
     * 微信SDK初始化(包含授权登录与支付)
     */
    public static void weixinInit(String appid) {
        wx_api = WXAPIFactory.createWXAPI(context, appid, true);
        wx_api.registerApp(appid);
    }

    /**
     * 微信定单查询
     *
     * @param orderId 定单ID
     * @return 定单信息
     */
    public static Wechat.OrderQueryResponse weixinOrderQuery(String orderId) {
        return TMSdk.Companion.orderQuery(StaticConfig.Companion.getProgramId(), orderId);
    }

    /**
     * 实时认证查询
     *
     * @param userId 用户ID
     * @return 认证信息
     */
    public static Identifys.QueryResponse identifyQuery(String userId) {
        return TMSdk.Companion.identifyQuery(StaticConfig.Companion.getProgramId(), userId);
    }

    /**
     * 实时认证
     *
     * @param userId 用户id
     * @param name   身份证名字
     * @param id     身份证号
     * @return 认证是否成功
     */
    public static Identifys.IdentifyResponse identify(String userId, String name, String id) {
        return TMSdk.Companion.identify(StaticConfig.Companion.getProgramId(), userId, name, id);
    }

    /**
     * 拉起微信授权
     */
    public static void weixinLogin() {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "";
        wx_api.sendReq(req);
    }


    /**
     * 微信支付结果
     *
     * @param result 结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void weixinPayResult(WeixinPayCallback result) {
        if (result.getResult() != "支付成功") {
            order = null;
        }
        gameWebView.payCallback(
                new Wechat.PayResponse(
                        "ok",
                        result.getResult() == "支付成功" ? order.getData().getItem().getOrder_code() : null,
                        result.getResult()
                )
        );
    }

    /**
     * 微信支付创建
     *
     * @param weixinPay 支付定单响应
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void weixinPayCreate(WeixinPay weixinPay) {
        order = weixinPay.getResponse();
        if (order.getErr() != 0) {
            /**
             * 创建定单失败
             */
            gameWebView.payCallback(
                    new Wechat.PayResponse(
                            "fail",
                            null,
                            order.getMsg()
                    )
            );
        } else {
            //跳转微信并拉起支付
            PayReq request = new PayReq();
            Wechat.OrderItem orderItem = order.getData().getItem();
            request.appId = orderItem.getAppid();
            request.partnerId = orderItem.getPartnerid();
            request.prepayId = orderItem.getPrepayid();
            request.packageValue = orderItem.getPackage();
            request.nonceStr = orderItem.getNoncestr();
            request.timeStamp = orderItem.getTimestamp();
            request.sign = orderItem.getSign();
            wx_api.sendReq(request);
        }
    }

    /**
     * 微信授权登录
     *
     * @param login 用户登录响应
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void weixinAuthLogin(AuthLogin login) {
        gameWebView.loginCallback(login.getResponse());
        if (login.getResponse().getErr() == 0) {
            /**
             * 设置性别，不设置默认为0
             */
            TMSdk.Companion.setGender(login.getResponse().getData().getGender());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = getApplication();
        context = this;

        //注册Event事件
        EventBus.getDefault().register(this);

        //注册微信Activity
//        weixinInit(this);

        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCachePath(context.getCacheDir().getAbsolutePath());
        webSettings.setAppCacheEnabled(true);
        webView.setWebViewClient(gameWebView);
        //暴露给js对象为android，可自己定义
        webView.addJavascriptInterface(new AndroidWithJS(this), "android");
//        webView.loadUrl("http://h5fssdk.98wan.cn/login/xinghanh5/4060");
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TMSdk.Companion.appExit(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}