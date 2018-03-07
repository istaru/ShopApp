package com.shhb.gd.shop.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ali.auth.third.ui.context.CallbackContext;
import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.baichuan.android.trade.page.AlibcMyCartsPage;
import com.alibaba.baichuan.android.trade.page.AlibcMyOrdersPage;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.listener.WCClient;
import com.shhb.gd.shop.module.AlibcShow;
import com.shhb.gd.shop.module.JsObject;
import com.shhb.gd.shop.tools.NetUtil;

/**
 * Created by Kiven on 16/12/16.
 */

public class AlibcActivity extends BaseActivity {
    private String type = "";
    private WebView webView;
    private WCClient wcClient;
    private CircularProgressView viewStub;
    private TextView title;
    private LinearLayout onShare;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alibc_activity);
        type = getIntent().getStringExtra("type");
        initWebView();
        showAlibcH5();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initWebView(){

        title = (TextView) findViewById(R.id.webView_title);
        onShare = (LinearLayout) findViewById(R.id.onShare);
        onShare.setVisibility(View.GONE);

        viewStub = (CircularProgressView) findViewById(R.id.viewStub);
        viewStub.setVisibility(View.VISIBLE);
        wcClient = new WCClient(viewStub,"");

        webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(getResources().getColor(R.color.webBg));
        //禁止长按事件
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);//支持JavaScript
        webView.getSettings().setAllowFileAccess(true);//允许访问文件
        webView.getSettings().setAllowFileAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript读取其他的本地文件
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源
        //图片显示
        webView.getSettings().setLoadsImagesAutomatically(true);
        //自适应屏幕
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.addJavascriptInterface(new JsObject(webView, this), "native_android");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(wcClient);
        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
    }

    /**
     * 显示淘宝购物车、淘宝订单
     */
    private void showAlibcH5() {
        if(TextUtils.equals("cart",type)){
            title.setText("淘宝购物车");
            if(isNetConnect()) {
                AlibcShow.showH5(this, webView, wcClient, new AlibcMyCartsPage());
            }
        } else {
            title.setText("淘宝订单");
            if(isNetConnect()){
                AlibcShow.showH5(this, webView, wcClient, new AlibcMyOrdersPage(0, true));
            }
        }
    }

    @Override
    public void onNetChange(int netMobile) {
        super.onNetChange(netMobile);
        if (netMobile== NetUtil.NETWORK_NONE){

        } else {
            if(TextUtils.equals("cart",type)){
                AlibcShow.showH5(this, webView, wcClient, new AlibcMyCartsPage());
            } else {
                AlibcShow.showH5(this, webView, wcClient, new AlibcMyOrdersPage(0, true));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 返回按钮
     * @param v
     */
    public void back(View v) {
        if(webView.canGoBack()){
            webView.goBack();
        }else {
            finish();
        }
    }

    /**
     * 返回键结束当前Activity
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(null != webView){
                    if(webView.canGoBack()){
                        webView.goBack();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if(null != webView){//销毁WebView
            webView.onPause();
            webView.destroy();
            webView = null;
        }
        AlibcTradeSDK.destory();
        super.onDestroy();
    }
}
