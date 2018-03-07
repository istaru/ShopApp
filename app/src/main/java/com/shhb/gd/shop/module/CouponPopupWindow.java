package com.shhb.gd.shop.module;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.baichuan.android.trade.page.AlibcPage;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.listener.WCClient;

import static com.shhb.gd.shop.activity.BaseActivity.netMobile;

/**
 * Created by Kiven on 16/12/9.
 */

public class CouponPopupWindow extends PopupWindow {
    private TextView coupon;
    public WebView webView2;
    private View viewmenu;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public CouponPopupWindow(final Activity context, String url, String numId, int width, int height) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        viewmenu = inflater.inflate(R.layout.window_coupon, null);

        webView2 = (WebView) viewmenu.findViewById(R.id.webView);
        coupon = (TextView) viewmenu.findViewById(R.id.coupon);
        coupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //精致长按事件
        webView2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        webView2.getSettings().setJavaScriptEnabled(true);//支持JavaScript
        webView2.getSettings().setAllowFileAccess(true);//允许访问文件
        webView2.getSettings().setAllowFileAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript读取其他的本地文件
        webView2.getSettings().setAllowUniversalAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源
        //图片显示
        webView2.getSettings().setLoadsImagesAutomatically(true);
        //自适应屏幕
        webView2.getSettings().setUseWideViewPort(true);
        webView2.getSettings().setLoadWithOverviewMode(true);
        webView2.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        WCClient wcClient = new WCClient(null,numId);
        webView2.setWebChromeClient(wcClient);
        webView2.addJavascriptInterface(new JsObject(webView2,context), "native_android");
        webView2.setWebViewClient(new WebViewClient());
        //关闭缩放
        webView2.getSettings().setBuiltInZoomControls(false);
        webView2.getSettings().setSupportZoom(false);
        webView2.getSettings().setDisplayZoomControls(false);

        if(netMobile != -1) {
            AlibcShow.showH5(context,webView2,wcClient,new AlibcPage(url));
        }

        this.setContentView(viewmenu);
        this.setWidth(width);// 设置SelectPicPopupWindow弹出窗体的宽
        this.setHeight(height);// 设置SelectPicPopupWindow弹出窗体的高
        this.setOutsideTouchable(true);// 点击外部可关闭窗口
        this.setBackgroundDrawable(new ColorDrawable());
        this.setFocusable(true);//设置窗体可点击
        this.setTouchable(true);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        this.setInputMethodMode(android.widget.PopupWindow.INPUT_METHOD_NEEDED);//不被输入法挡住
        this.update();
        //关闭窗体时
        this.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                // 在dismiss中恢复透明度
                WindowManager.LayoutParams lp = context.getWindow().getAttributes();
                lp.alpha = 1f;
                context.getWindow().setAttributes(lp);
            }
        });
    }
}