package com.starsriver.ftx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dounine.tmsdk.core.TMSdk;
import com.dounine.tmsdk.model.Wechat;

/**
 * 调用数据返回给页面
 */
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
