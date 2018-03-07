package com.shhb.gd.shop.listener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Kiven on 16/12/15.
 */

public class WVClient extends WebViewClient{
    private Activity context;

    public WVClient(Activity context){
        this.context = context;
    }

    //页面开始加载
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    //页面开始加载前
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    //页面加载出错
    @Override
    public void onReceivedError(final WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.loadUrl("file:///android_asset/html/loading.html");
            }
        });
    }

    //页面加载完毕
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }
}
