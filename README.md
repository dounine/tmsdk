![](https://github.com/dounine/tmsdk/workflows/Publish%20package%20to%20the%20Maven%20Central%20Repository/badge.svg) ![](https://img.shields.io/github/license/dounine/tmsdk)

## 天幕安卓SDK

## 打包
```
./gradlew clean channelRelease
#或
./gradlew clean build
```
修改打包后的渠道
```
java -jar ./tools/VasDolly.jar put -c "hello" ./app/build/channel/app-1.0-1-default-release.apk ./app/build/channel/app-1.0-1-default-release.apk
#或
java -jar ./tools/VasDolly.jar put -c "hello" ./app/release/app-release.apk ./app/release/app-release.apk
```
[多渠道参考](https://github.com/Tencent/VasDolly)

## 说明

1. 用户ID，可使用系统生成，用户卸载再安装，既视为一个新用户。
2. 建议使用openid或者union_id，也可以使用自定ID。

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
```
`GameWebView.java` 此类可回调数据给页面
```
public class GameWebView extends WebViewClient {

    private WebView webView;

    @Override
    public void onPageFinished(WebView view, String url) {
        this.webView = view;
    }

    //回调网页公开接受用户信息的function
    public void loginCallback(Wechat.LoginResponse loginResponse) {
        if (this.webView != null) {
            this.webView.evaluateJavascript("javascript:" + MainActivity.weixinLoginCallbackName + "(" + TMSdk.Companion.objectToJson(loginResponse) + ")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                }
            });
        }
    }

    //回调网页公开接受用户支付回调的function
    public void payCallback(Wechat.PayResponse payResponse) {
        if (this.webView != null) {
            this.webView.evaluateJavascript("javascript:" + MainActivity.weixinPayCallbackName + "(" + TMSdk.Companion.objectToJson(payResponse) + ")", new ValueCallback<String>() {
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
4、H5页面使用
```
先在页面引入h5 sdk
<script src="https://cdn.kuaiyugo.com/SDK/h5_sdk/tmsdk.js"></script>
<script>
    let loginData;
    let payOrder;

    /**
     * 配置
     */
    TMSdk.config({
        appid: "wx1968f4cbe8ebfe5d",
        programId: "92c7461171c211ecaba983a259950266",
        channel: ""
    });

    /**
     * 微信登录
     * {
     *  "data": {
     *      "app_id": "wx1968f4cbe8ebfe5d",
     *      "avatar_url": "https://thirdwx.qlogo.cn/mmopen/vi_32/Vegkzdr6BasIicicyjic1JJNtick8qO03oGc7Go6t0Jhm4cqiaNdqNYgElPCqiaqsIQvPTv8Rm85yPA1fMiaaRWmSsqLA/132",
     *      "gender": 0, //0未知,1男,2女
     *      "last_login_time": 1642746317,
     *      "login_city": "广州市",
     *      "login_district": "海珠区",
     *      "login_province": "广东省",
     *      "new": false,
     *      "nick_name": "lake",
     *      "open_id": "oeCdT6vf30VwgQla7X_WcxxLneVc",
     *      "recharged": 0,
     *      "recharged_times": 0,
     *      "union_id": "oj7QN1cI4TCKVcSB0cXYj6q-GoPk"
     *  },
     *  "err": 0, //非0授权失败
     *  "msg": "请求成功" //用户取消,用户拒绝授权,返回
     *  }
     */
    function weixinLogin() {
        TMSdk.weixinLogin().then(function (data) {
            loginData = data;
            let dataStr = JSON.stringify(data);
            document.querySelector("#loginResult").innerHTML = dataStr;
            console.log("微信授权登录回调 " + dataStr);
            if (loginData.data && loginData.data.open_id) {
                TMSdk.loginReport({userId: loginData.data.open_id});//数据登录上报
                document.querySelector("#weixinPayBtn").removeAttribute("disabled");
                document.querySelector("#identifyBtn").removeAttribute("disabled");
                document.querySelector("#identifyQueryBtn").removeAttribute("disabled");
            }
        });
    }

    /**
     * 调用微信支付
     *  {
     *     "action":"ok",
     *     "msg":"支付成功",//或者支付失败
     *     "orderId":"xxxxxxxxx"
     * }
     */
    function weixinPay() {
        TMSdk.weixinPay({
            coin: 1,//1支付1角
            userId: loginData.data.open_id,//用户id
            programParam: 'test'//参数(用于服务器回调识别用)
        }).then(function (data) {
            payOrder = data;
            let dataStr = JSON.stringify(data);
            document.querySelector("#payResult").innerHTML = dataStr;
            if (data.orderId) {
                document.querySelector("#weixinOrderQueryBtn").removeAttribute("disabled");
            }
            console.log("微信支付回调 " + dataStr);
        });
    }

    /**
     * 微信支付定单查询
     * {
     *     "data":{
     *         "order_status":1, //订单状态：0下单，1待发货，2取消订单，3发货成功
     *         "pay_status":1 //支付状态：0待支付，1支付成功，2支付失败
     *     },
     *     "err":0, //0成功，非0请求异常
     *     "msg":"请求成功"
     * }
     */
    function weixinOrderQuery() {
        TMSdk.weixinOrderQuery({orderId: payOrder.orderId})
            .then(function (data) {
                document.querySelector("#payQueryResult").innerHTML = data;
                console.log("微信定单查询结果 " + data);
            })
    }

    /**
     * 实名认证查询
     * {
     *     "data":{
     *         "result":true //true是已经认证,false未认证
     *     },
     *     "err":0, //0成功，非0请求异常
     *     "msg":"请求成功"
     * }
     */
    function identifyQuery() {
        TMSdk.identifyQuery({userId: loginData.data.open_id}).then(function (data) {
            document.querySelector("#identifyQueryResult").innerHTML = data;
            console.log("实名认证查询结果 " + data);
        })
    }

    /**
     * 实名认证
     * {
     *     "data":{
     *         "result":true //true是已经认证,false未认证
     *     },
     *     "err":0, //0成功，非0请求异常
     *     "msg":"请求成功"
     * }
     */
    function identify() {
        TMSdk.identify({
            userId: loginData.data.open_id,
            name: "李德松",
            id: "452525197305054416"
        }).then(function (data) {
            document.querySelector("#identifyResult").innerHTML = data;
            console.log("实名认证结果 " + data);
        })
    }

</script>  
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
1. 登录后的用户数据，请游戏自行保存，如果不想保存可每次让用户授权登录，但游戏体验不好
2. 天幕SDK初始化，请勿必在授权后拿到用户信息再调用(不需要用户登录除外)
3. 用户支付成功后会有回调，但不可信任，请以服务器的支付回调为准
4. 在运行此项目demo的时候，需要修改获取测试机的应用签名，联系运营人员修改签名即可运行
### 签名生成工具
用于获取安装到手机的第三方应用签名的apk包。点击下载 [签名生成工具](https://res.wx.qq.com/open/zh_CN/htmledition/res/dev/download/sdk/Gen_Signature_Android2.apk)
安装到手机上，输入`com.starsriver.ftx`即可获得
