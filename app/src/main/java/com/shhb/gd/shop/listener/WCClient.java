package com.shhb.gd.shop.listener;

import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;


/**
 * Created by Kiven on 16/12/14.
 */

public class WCClient extends WebChromeClient {
    private CircularProgressView viewStub;
    private String type;

    public WCClient(CircularProgressView viewStub,String type) {
        this.viewStub = viewStub;
        this.type = type;
    }


    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (newProgress >= 100) {
            if (null != viewStub) {
                viewStub.setVisibility(View.GONE);
            }
            if(!TextUtils.equals(type,"")){
                String js = "alert(window.native_android.coupon_msg ? 'y' : 'n');";
                String wholeJS = "(function(_time,_url){" +
                                    "setTimeout(function(){" +
                                    "var msg = document.getElementById('J-msg').innerText;" +
                                    "window.native_android.coupon_msg(JSON.stringify({msg:msg,url:_url}));" +
                                    "},_time);" +
                                 "})(2000,"+ "\"" + type + "\"" +");";
                view.loadUrl("javascript:" + wholeJS);
            }
        }
    }

//    String okJs = "document.getElementById('J-btn').addEventListener('click',function(){" +
//            "alert('y');" +
//            "});";
//    view.loadUrl("javascript:" + okJs);

}
