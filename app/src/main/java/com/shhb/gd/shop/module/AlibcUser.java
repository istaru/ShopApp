package com.shhb.gd.shop.module;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ali.auth.third.core.model.Session;
import com.ali.auth.third.login.callback.LogoutCallback;
import com.alibaba.baichuan.android.trade.callback.AlibcLoginCallback;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by Kiven on 16/12/19.
 */

public class AlibcUser {

    /**
     * 淘宝登录
     * @param context
     */
    public static void login(final Activity context){
        com.alibaba.baichuan.android.trade.adapter.login.AlibcLogin.getInstance().showLogin(context, new AlibcLoginCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "登录成功 ",Toast.LENGTH_LONG).show();
                Session user = com.alibaba.baichuan.android.trade.adapter.login.AlibcLogin.getInstance().getSession();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("nick",user.nick);
                jsonObject.put("avatarUrl",user.avatarUrl);
                jsonObject.put("openId",user.openId);
                jsonObject.put("openSid",user.openSid);
                Log.e("用户信息",jsonObject.toString());
            }

            @Override
            public void onFailure(int code, String msg) {
                Toast.makeText(context, "登录失败 ",Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 退出淘宝登录
     * @param context
     */
    public static void unLogin(final Activity context){
        com.alibaba.baichuan.android.trade.adapter.login.AlibcLogin.getInstance().logout(context, new LogoutCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "退出登录成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String msg) {
                Toast.makeText(context, "退出登录失败 " + code + msg,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 判断是否淘宝登录过
     * @return
     */
    public static boolean isLogin(){
        Session session = com.alibaba.baichuan.android.trade.adapter.login.AlibcLogin.getInstance().getSession();
        if(TextUtils.equals(session.openId,"") && TextUtils.equals(session.nick,"") && TextUtils.equals(session.avatarUrl,"")){
            return false;
        } else {
            return true;
        }
    }
}
