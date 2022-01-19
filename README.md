# tmsdk

## 天幕安卓SDK

## 说明

1. 用户ID，默认使用系统生成，用户卸载再安装，既视为一个新用户，可以以使用自己的ID。

## 使用方法

gradle添加依赖

```
implementation 'com.dounine:tmsdk:1.0.0'
implementation 'org.greenrobot:eventbus:3.3.1'
implementation 'com.tencent.mm.opensdk:wechat-sdk-android:6.8.0'
```

TMSDK初始化

第一种：先微信授权登录，再初始化SDK
```
TMSdk.Companion.init(context, "应用APPID", "天幕ID", "用户ID", "渠道ID");//建议使用openid或者unionid，请自行保证用户id唯一
TMSdk.Companion.appStart();
```
第二种：单机应用，不需要登录
```
//onCreate方法添加
TMSdk.Companion.init(this, "游戏ID", "天幕ID", TMSdk.Companion.createUserId(this), "渠道ID");//如果是单机游戏且不需要登录，可使用系统生成的临时id
TMSdk.Companion.appStart();
```

混淆设置，如果您的应用使用了代码混淆，请添加如下配置，以避免【天幕SDK】被错误混淆导致SDK不可用。

```
-keep class com.dounine.tmsdk.** {*;}
```

## 使用方法
1、MainActivity 注册微信SDK
```
IWXAPI wx_api = WXAPIFactory.createWXAPI(context, "appid", true);
wx_api.registerApp(appid);
```

2、activity_main.xml中添加webview控件
```
<WebView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/webview"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

3、初始化webview
```
setContentView(R.layout.activity_main);
webView = findViewById(R.id.webview);
WebSettings webSettings = webView.getSettings();
webSettings.setJavaScriptEnabled(true);
webView.setWebViewClient(gameWebView);
//暴露给js对象为android，可自己定义
webView.addJavascriptInterface(new AndroidWithJS(this), "android");
//webView.loadUrl("https://baidu.com");//上线修改为真实地扯即可
webView.loadUrl("file:///android_asset/index.html");//此为测试demo的页面
```
`AndroidWithJS.java` 此类接受来自页面的数据
```
public class AndroidWithJS {

    private Context context;

    public AndroidWithJS(Context context) {
        this.context = context;
    }

    /**
     * 手动控制SDK初始化，主要是为了使用授权后的用户ID
     *
     * @param appid     应用appid
     * @param programId 天幕平台配置programId
     * @param userId    用户自定义id,使用open_id,或者union_id
     * @param channel   渠道
     */
    @JavascriptInterface
    public String initSdk(String userId, String channel) {
        TMSdk.Companion.init(context, MainActivity.appid, MainActivity.programId, userId, channel);
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
                Wechat.OrderResponse response = TMSdk.Companion.weixinPayCreateOrder(MainActivity.programId, coin, userId, programParam);
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
```
`GameWebView.java` 此类可回调数据给页面
```
public class GameWebView extends WebViewClient {

    private WebView webView;

    @Override
    public void onPageFinished(WebView view, String url) {
        this.webView = view;
        view.evaluateJavascript("javascript:pageFinished()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
            }
        });
    }

    //回调网页公开接受用户信息的function
    public void loginCallback(Wechat.LoginResponse loginResponse) {
        if (this.webView != null) {
            this.webView.evaluateJavascript("javascript:weixinLoginCallback(" + TMSdk.Companion.objectToJson(loginResponse) + ")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                }
            });
        }
    }

    //回调网页公开接受用户支付回调的function
    public void payCallback(Wechat.PayResponse payResponse) {
        if (this.webView != null) {
            this.webView.evaluateJavascript("javascript:weixinPayCallback(" + TMSdk.Companion.objectToJson(payResponse) + ")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                }
            });
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setMessage("SSL认证失败，是否继续访问？");
        builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
```
4、编写游戏页面方法调用
```
/**
     * 微信登录
     */
    function weixinLogin() {
        window.android.weixinLogin();
    }

    /**
     * 微信登录回调
     * @param data
     */
    function weixinLoginCallback(data) {
        loginData = data;
        let dataStr = JSON.stringify(data);
        document.querySelector("#loginResult").innerHTML = dataStr;
        console.log("微信授权登录回调 " + dataStr);
        if (loginData.data && loginData.data.open_id) {
            document.querySelector("#sdkInitBtn").removeAttribute("disabled");
            document.querySelector("#weixinPayBtn").removeAttribute("disabled");
            document.querySelector("#identifyBtn").removeAttribute("disabled");
            document.querySelector("#identifyQueryBtn").removeAttribute("disabled");
        }
    }

    /**
     * sdk初始化
     */
    function sdkInit() {
        let result = window.android.initSdk(loginData.data.open_id, "channel");
        document.querySelector("#initResult").innerHTML = result;
        console.log("初始化结果", result);
    }

    /**
     * 调用微信支付
     */
    function weixinPay() {
        //参数1：金额(单位角)
        //参数2：用户id
        //参数3：参数，用于服务支付成功回调识别
        window.android.weixinPay(1, loginData.data.open_id, "test")
    }

    /**
     * 支付回调结果
     * @param data
     */
    function weixinPayCallback(data) {
        payOrder = data;
        let dataStr = JSON.stringify(data);
        document.querySelector("#payResult").innerHTML = dataStr;
        if (data.orderId) {
            document.querySelector("#weixinOrderQueryBtn").removeAttribute("disabled");
        }
        console.log("微信支付回调 " + dataStr);
    }

    /**
     * 微信支付定单查询
     */
    function weixinOrderQuery() {
        let result = window.android.queryOrder(payOrder.orderId);
        document.querySelector("#payQueryResult").innerHTML = result;
        console.log("微信定单查询结果 " + result);
    }

    /**
     * 实时认证查询
     */
    function identifyQuery() {
        let result = window.android.identifyQuery(loginData.data.open_id);
        document.querySelector("#identifyQueryResult").innerHTML = result;
        console.log("实时认证查询结果 " + result);
    }

    /**
     * 实时认证
     */
    function identify() {
        let result = window.android.identify(loginData.data.open_id, "李德松", "452525197305054416");
        document.querySelector("#identifyResult").innerHTML = result;
        console.log("实时认证结果 " + result);
    }
```
AndroidManifest.xml
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.starsriver.ftx">

    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    <queries>
        <package android:name="com.tencent.mm" />
    </queries>


    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:networkSecurityConfig="@xml/network_security_config"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">

        <activity
            android:name= ".wxapi.WXEntryActivity"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.Translucent"
            android:exported="true" />

        <activity
            android:name= ".wxapi.WXPayEntryActivity"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:theme="@android:style/Theme.Translucent"
            android:exported="true" />

        <activity
            android:name="com.starsriver.ftx.MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication.NoActionBar">


            <intent-filter>
                <action android:name="android.intent.action.MAIN" />


                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

## 注意事项
1、登录后的用户数据，请游戏自行保存，如果不想保存可每次让用户授权登录，但游戏体验不好
2、天幕SDK初始化，请勿必在授权后拿到用户信息再调用(不需要用户登录除外)
3、用户支付成功后会有回调，但不可信任，请以服务器的支付回调为准
4、在运行此项目demo的时候，需要修改获取测试机的应用签名，联系运营人员修改签名即可运行
### 签名生成工具
用于获取安装到手机的第三方应用签名的apk包。点击下载 [签名生成工具](https://res.wx.qq.com/open/zh_CN/htmledition/res/dev/download/sdk/Gen_Signature_Android2.apk)
安装到手机上，输入`com.starsriver.ftx`即可获得
