package com.starsriver.ftx;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import com.dounine.tmsdk.core.TMSdk;
import com.dounine.tmsdk.model.Logs;
import com.dounine.tmsdk.model.Wechat;
import com.dounine.tmsdk.util.DeviceUtil;
import com.dounine.tmsdk.util.StaticConfig;
import com.starsriver.ftx.events.WeixinPay;
import com.tencent.vasdolly.helper.ChannelReaderUtil;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.proxy.IUpdateHttpService;
import com.xuexiang.xupdate.utils.UpdateUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 接受来自页面数据
 */
public class AndroidWithJS {

    private Context context;
    private String TAG = "TMSdk";

    public AndroidWithJS(Context context) {
        this.context = context;
    }

    /**
     * 初始化appid,programId,channel
     */
    @JavascriptInterface
    public void init(String appid, String programId, String channel, String weixinLoginCallbackName, String weixinPayCallbackName) {
        Log.i(TAG, "init appid:" + appid + " , programId:" + programId + " , channel:" + channel + " , weixinLoginCallbackName:" + weixinLoginCallbackName + " , weixinPayCallbackName:" + weixinPayCallbackName);
        StaticConfig.Companion.setAPPID(appid);
        StaticConfig.Companion.setProgramId(programId);
        StaticConfig.Companion.setCHANNEL(ChannelUtil.channel(context));
        MainActivity.weixinInit(appid);
        MainActivity.weixinLoginCallbackName = weixinLoginCallbackName;
        MainActivity.weixinPayCallbackName = weixinPayCallbackName;
    }

    @JavascriptInterface
    public void update(String updateUrl) {
        try {
            Log.i(TAG, "update url -> " + updateUrl);
            XUpdate.get()
                    .debug(false)
                    .isWifiOnly(false)
                    .isGet(true)
                    .isAutoMode(false)
                    .param("versionCode", UpdateUtils.getVersionCode(context))         // Set default public request parameters
                    .param("appKey", context.getPackageName())
                    .setIUpdateHttpService(new IUpdateHttpService() {
                        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                                .build();

                        @Override
                        public void asyncGet(@NonNull String url, @NonNull Map<String, Object> map, @NonNull Callback callback) {
                            Log.i(TAG, "update url:" + url);
                            Request request = new Request.Builder().url(url)
                                    .build();
                            okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    String body = response.body().string();
                                    Log.i(TAG, "update response:" + body);
                                    callback.onSuccess(body);
                                }
                            });
                        }

                        @Override
                        public void asyncPost(@NonNull String s, @NonNull Map<String, Object> map, @NonNull Callback callback) {

                        }

                        @Override
                        public void download(@NonNull String fileUrl, @NonNull String path, @NonNull String fileName, @NonNull DownloadCallback downloadCallback) {
                            Log.i(TAG, "------download：" + fileUrl + ":" + path + ":" + fileName);
                            downloadCallback.onStart();
                            Request request = new Request.Builder().get().url(
                                    fileUrl.replace("${appid}", StaticConfig.Companion.getAPPID())
                                            .replace("${channel}", ChannelUtil.channel(context))
                            ).build();
                            okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    long fileSize = response.body().contentLength();
                                    InputStream is = response.body().byteStream();
                                    new File(path).mkdirs();
                                    File apk = new File(path + "/" + fileName);
                                    FileOutputStream fos = new FileOutputStream(apk);
                                    int len = 0;
                                    long readSize = 0;
                                    byte[] buffer = new byte[2048];
                                    while (-1 != (len = is.read(buffer))) {
                                        readSize += len;
                                        fos.write(buffer, 0, len);
                                        float progress = readSize * 1f / fileSize * 1f;
                                        downloadCallback.onProgress(progress, 1);
                                    }
                                    fos.flush();
                                    fos.close();
                                    is.close();
                                    downloadCallback.onSuccess(apk);
                                }
                            });
                        }

                        @Override
                        public void cancelDownload(@NonNull String s) {

                        }
                    })
                    .init(MainActivity.application);


            XUpdate.newBuild(context)
                    .updateUrl(updateUrl)
                    .supportBackgroundUpdate(false)
                    .update();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 手动控制SDK初始化，主要是为了使用授权后的用户ID
     *
     * @param userId 用户自定义id,使用open_id,或者union_id
     */
    @JavascriptInterface
    public String loginReport(String userId) {
        Log.i(TAG, "loginReport userId:" + userId);
        TMSdk.Companion.init(context, StaticConfig.Companion.getAPPID(), StaticConfig.Companion.getProgramId(), userId, StaticConfig.Companion.getCHANNEL());
        TMSdk.Companion.appStart(UpdateUtils.getVersionName(context));
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
    public void weixinPay(int coin, String userId, String programParam, String goodsName, String zone, String gameUid, String gameNickname) {
        Log.i(TAG, "weixinPay coin:" + coin + " , userId:" + userId + " , programParam:" + programParam + " , goodsName:" + goodsName + " , zone:" + zone + " , gameNickname:" + gameNickname);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Wechat.OrderResponse response = TMSdk.Companion.weixinPayCreateOrder(
                        StaticConfig.Companion.getProgramId(),
                        coin,
                        userId,
                        programParam,
                        DeviceUtil.Companion.getBrand(),
                        DeviceUtil.Companion.getModel(),
                        goodsName,
                        zone,
                        gameUid,
                        gameNickname
                );
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
        Log.i(TAG, "queryOrder orderId:" + orderId);
        return TMSdk.Companion.objectToJson(MainActivity.weixinOrderQuery(orderId));
    }

    /**
     * 获取信息
     *
     * @return
     */
    @JavascriptInterface
    public String info() {
        return String.format(
                "{\"version\":\"%s\",\"versionCode\":%d,\"channel\":\"%s\",\"brand\":\"%s\",\"model\":\"%s\"}",
                UpdateUtils.getVersionName(context),
                UpdateUtils.getVersionCode(context),
                ChannelUtil.channel(context),
                DeviceUtil.Companion.getBrand(),
                DeviceUtil.Companion.getModel()
        );
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
        Log.i(TAG, "identifyQuery userId:" + userId);
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
        Log.i(TAG, "identify userId:" + userId + " , name:" + name + " , id:" + id);
        return TMSdk.Companion.objectToJson(MainActivity.identify(userId, name, id));
    }

}
