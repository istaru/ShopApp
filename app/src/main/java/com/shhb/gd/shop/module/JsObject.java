package com.shhb.gd.shop.module;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ali.auth.third.core.model.Session;
import com.ali.auth.third.login.callback.LogoutCallback;
import com.alibaba.baichuan.android.trade.adapter.login.AlibcLogin;
import com.alibaba.baichuan.android.trade.callback.AlibcLoginCallback;
import com.alibaba.fastjson.JSONObject;
import com.shhb.gd.shop.activity.AlibcActivity;
import com.shhb.gd.shop.activity.DetailsActivity;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.Constants;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PhoneInfo;
import com.shhb.gd.shop.tools.PrefShared;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kiven on 16/12/9.
 */

public class JsObject {
    private WebView webView;
    private Activity context;
    private String appType = "";
    private String shareCallBack = "";

    public JsObject(WebView webView, Activity context) {
        this.webView = webView;
        this.context = context;
    }

    /**
     * 用户登录
     */
    @JavascriptInterface
    public void platform(String result){
        String callBack = JSONObject.parseObject(result).getString("callback");
        JSONObject jsonObject = new JSONObject();
        PhoneInfo phoneInfo = new PhoneInfo(context);
        Map<String,Object> map = phoneInfo.getPhoneMsg();
        map.put("address", PrefShared.getString(context,"position"));
        map.put("type","1");
        for(Map.Entry<String, Object> m : map.entrySet()){
            jsonObject.put(m.getKey(),m.getValue());
        }
        callBack(callBack,jsonObject.toString());
    }

    /**
     * 淘宝登录
     */
    @JavascriptInterface
    public void bind_taobao(String result){
        final String callBack = JSONObject.parseObject(result).getString("callback");
        final JSONObject jsonObject = new JSONObject();
        AlibcLogin.getInstance().showLogin(context, new AlibcLoginCallback() {
            @Override
            public void onSuccess() {
                Session user = AlibcLogin.getInstance().getSession();
                jsonObject.put("status",1);
                jsonObject.put("nick",user.nick);
                PhoneInfo phoneInfo = new PhoneInfo(context);
                jsonObject.put("imei",phoneInfo.getIMEI());
                PrefShared.saveString(context,"nick",user.nick);
                jsonObject.put("avatarUrl",user.avatarUrl);
                jsonObject.put("openId",user.openId);
                jsonObject.put("openSid",user.openSid);
                callBack(callBack,jsonObject.toString());
            }

            @Override
            public void onFailure(int code, String msg) {
                jsonObject.put("status",0);
                callBack(callBack,jsonObject.toString());
            }
        });
    }

    /**
     * 退出淘宝登录
     * @param result
     */
    @JavascriptInterface
    public void unbind_taobao(String result){
        try {
            final String callBack = JSONObject.parseObject(result).getString("callback");
            final JSONObject jsonObject = new JSONObject();
            AlibcLogin.getInstance().logout(context, new LogoutCallback() {
                @Override
                public void onSuccess() {
                    jsonObject.put("status",1);
                    callBack(callBack,jsonObject.toString());
                }

                @Override
                public void onFailure(int code, String msg) {
                    jsonObject.put("status",0);
                    callBack(callBack,jsonObject.toString());
                }
            });
        } catch (Exception e){
            AlibcLogin.getInstance().logout(context, new LogoutCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int code, String msg) {
                }
            });
        }
    }

    /**
     * 获取用户信息
     */
    @JavascriptInterface
    public void set_user_id(String result){
        String userId = JSONObject.parseObject(result).getString("user_id");
        PrefShared.saveString(context,"userId",userId);
    }

    /**
     * 淘宝购物车
     *
     * @param result
     */
    @JavascriptInterface
    public void shopping_cart(final String result) {
        Intent intent = new Intent(context, AlibcActivity.class);
        intent.putExtra("type", "cart");
        context.startActivity(intent);
    }

    /**
     * JS注入拿到购物卷是否失效的值
     * @param result
     */
    @JavascriptInterface
    public void coupon_msg(String result){
        JSONObject json = JSONObject.parseObject(result);
        result = json.getString("msg");
        if(TextUtils.equals(result,"")){//优惠券可以领取
            Log.e("优惠卷","可用");
        } else {//优惠券不能领取

            Log.e("优惠卷","失效");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("num_iid", json.getString("url"));
            jsonObject.put("status", "2");
            String parameter = BaseTools.encodeJson(jsonObject.toString());
            OkHttpUtils okHttpUtils = new OkHttpUtils(20);
            okHttpUtils.postEnqueue(Constants.SEND_VOLUME, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("发送失效优惠券成功回调","网络或接口异常");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String json = BaseTools.decryptJson(response.body().string());
                        Log.e("发送失效优惠券成功回调",json);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            },parameter);
        }
    }

    /**
     * JS注入拿到用户点击确认领卷的动作
     * @param result
     */
    @JavascriptInterface
    public void coupon_ok(String result){
        Log.e("确认领卷",result);
    }

    /**
     * 商品详情
     * @param result
     */
    @JavascriptInterface
    public void goods_detail(String result) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String goodsId = jsonObject.getString("goods_id");
            String couponUrl = jsonObject.getString("vocher_url");
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("goodsId", goodsId);
            intent.putExtra("couponUrl", couponUrl);
            context.startActivity(intent);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 分享
     * @param result
     */
    @JavascriptInterface
    public void share(String result) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String type = jsonObject.getString("type");
            String link = jsonObject.getString("link");
            String title = jsonObject.getString("title");
            String content = jsonObject.getString("content");
            String img = jsonObject.getString("img");
            shareCallBack = jsonObject.getString("callback");
            Map<String,Object> map = new HashMap<>();
            map.put("link",link);
            map.put("title",title);
            map.put("content",content);
            map.put("img",img);
            if (TextUtils.equals(type, "qq_space")) {
                appType = "QQ客户端";
                share(SHARE_MEDIA.QZONE,map);
            } else if (TextUtils.equals(type, "sina")) {
                share(SHARE_MEDIA.SINA,map);
            } else if (TextUtils.equals(type, "wechat")) {
                appType = "微信客户端";
                share(SHARE_MEDIA.WEIXIN,map);
            } else if (TextUtils.equals(type, "qq")) {
                appType = "QQ客户端";
                share(SHARE_MEDIA.QQ,map);
            } else {
                appType = "微信客户端";
                share(SHARE_MEDIA.WEIXIN_CIRCLE,map);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 打开客服
     * @param result
     */
    @JavascriptInterface
    public void service(String result) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String type = jsonObject.getString("type");
            if (TextUtils.equals(type, "qq")) {
                try {
                    String value = jsonObject.getString("qq_num");
                    if(null != value){
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.OPEN_QQ + value)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.toString().contains("ActivityNotFoundException")) {
                        showPrompt("请先安装QQ客户端");
                    } else {
                        showPrompt("无法打开QQ客户端");
                    }
                }
            } else {
                try {
                    Intent intent = new Intent();
                    ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");// 报名该有activity
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(cmp);
                    context.startActivityForResult(intent, 0);
                } catch (Exception e){
                    e.printStackTrace();
                    if (e.toString().contains("ActivityNotFoundException")) {
                        showPrompt("请先安装微信客户端");
                    } else {
                        showPrompt("无法打开微信客户端");
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 复制邀请码
     * @param result
     */
    @JavascriptInterface
    public void copy(String result){
        JSONObject jsonObject = JSONObject.parseObject(result);
        String content = jsonObject.getString("content");
        String callBack = jsonObject.getString("callback");
        JSONObject jsonError = new JSONObject();
        String resultJson = "";
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(content);
            jsonError.put("status",1);
            resultJson = jsonError.toString();
        } catch (Exception e){
            jsonError.put("status",0);
            resultJson = jsonError.toString();
        }
        callBack(callBack,resultJson);
    }

    /**
     * 淘宝订单
     * @param result
     */
    @JavascriptInterface
    public void orders(String result) {
        Intent intent = new Intent(context, AlibcActivity.class);
        intent.putExtra("type", "order");
        context.startActivity(intent);
    }

    /**
     * 加密
     * @param result
     */
    @JavascriptInterface
    public void encode(String result){
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String content = jsonObject.getString("data");
            String callBack = jsonObject.getString("callback");
            callBack(callBack,BaseTools.encodeJson(content));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 解密
     * @param result
     */
    @JavascriptInterface
    public void decode(String result){
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            String content = jsonObject.getString("data");
            String callBack = jsonObject.getString("callback");
            callBack(callBack,BaseTools.decryptJson(content));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 断网之后重新请求页面
     */
    @JavascriptInterface
    public void refresh(){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(Constants.HTML_URL);
                    }
                });
            }
        });
    }

    /**
     * 分享
     *
     * @param shareType
     * @param map
     */
    private void share(final SHARE_MEDIA shareType, final Map<String, Object> map) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UMImage imageurl = new UMImage(context, map.get("img")+"");//网络图片
                new ShareAction(context).setPlatform(shareType)
                        .withTitle(map.get("title")+"")//标题
                        .withText(map.get("content")+"")//内容
                        .withMedia(imageurl)//图片
                        .withTargetUrl(map.get("link")+"")//链接
                        .setCallback(umAuthListener)
                        .share();
            }
        });
    }

    /**
     * 监听分享的回调
     */
    private UMShareListener umAuthListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA share_media) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status",1);
            callBack(shareCallBack,jsonObject.toString());
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            if(throwable.getMessage().contains("没有安装")){//没有安装应用
                showPrompt("请先安装"+appType);
            } else {
                showPrompt("分享失败啦");
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            showPrompt("分享取消了");
        }
    };

    /**
     * 所有方法的回调
     */
    public void callBack(final String callBack,final String result){
        if(null != webView){
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:"+callBack+"(" + result + ");");
                }
            });
        }
    }

    /**
     * 显示alert弹窗
     *
     * @param result
     */
    private void showPrompt(final String result) {
        if(null != webView) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:$.toast('" + result + "')");
                }
            });
        }
    }

//    /**
//     * 显示Log信息
//     * @param key
//     * @param value
//     */
//    private void showLog(String key,String value) {
//        if(null == value){
//            Log.e(key,"null");
//        } else {
//            Log.e(key,value);
//        }
//    }

}
