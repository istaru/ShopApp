package com.shhb.gd.shop.module;

import android.app.Activity;
import android.webkit.WebView;

import com.alibaba.baichuan.android.trade.AlibcTrade;
import com.alibaba.baichuan.android.trade.constants.AlibcConstants;
import com.alibaba.baichuan.android.trade.model.AlibcShowParams;
import com.alibaba.baichuan.android.trade.model.AlibcTaokeParams;
import com.alibaba.baichuan.android.trade.model.OpenType;
import com.alibaba.baichuan.android.trade.page.AlibcBasePage;
import com.shhb.gd.shop.listener.TradeCallback;
import com.shhb.gd.shop.listener.WCClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kiven on 16/12/16.
 */

public class AlibcShow {

    public static void showH5(Activity activity,WebView webView,WCClient wcClient,AlibcBasePage alibcPage){
        AlibcShowParams alibcShowParams = new AlibcShowParams(OpenType.H5, false);
        Map<String, String> exParams = new HashMap<>();
        exParams.put(AlibcConstants.ISV_CODE, "appisvcode");
        AlibcTaokeParams alibcTaokeParams = new AlibcTaokeParams("mm_120894240_21124824_71244797", null, null); // 若非淘客taokeParams设置为null即可
        AlibcTrade.show(activity, webView, null, wcClient, alibcPage,alibcShowParams, alibcTaokeParams, exParams, new TradeCallback(activity));
    }


//    public static void showH5(Activity activity, WebView webView, WVClient wvClient,WCClient wcClient, AlibcBasePage alibcPage){
//        AlibcShowParams alibcShowParams = new AlibcShowParams(OpenType.H5, false);
//        Map<String, String> exParams = new HashMap<>();
//        exParams.put(AlibcConstants.ISV_CODE, "appisvcode");
//        AlibcTaokeParams alibcTaokeParams = new AlibcTaokeParams("mm_120894240_21124824_71244797", null, null); // 若非淘客taokeParams设置为null即可
//        AlibcTrade.show(activity, webView, wvClient, wcClient, alibcPage,alibcShowParams, alibcTaokeParams, exParams, new TradeCallback(activity));
//    }

}
