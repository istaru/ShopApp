package com.shhb.gd.shop.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.baichuan.android.trade.callback.AlibcTradeInitCallback;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.wireless.security.jaq.JAQException;
import com.alibaba.wireless.security.jaq.SecurityInit;
import com.alipay.euler.andfix.patch.PatchManager;
import com.shhb.gd.shop.activity.DetailsActivity;
import com.shhb.gd.shop.activity.MainActivity;
import com.shhb.gd.shop.tools.PrefShared;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.utils.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Moon on 2016/12/1.
 */
public class MainApplication extends Application {
    private static Context context;

    private static MainApplication instance;
    private static List<Activity> activityList = new LinkedList<Activity>();
    public static PatchManager mPatchManager;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initUMConfig();
        initAlib();
        initX5();
        getStatusBarHeight();
    }

    /**
     * 友盟SDK中配置三方平台的appkey
     */
    private void initUMConfig() {
        PlatformConfig.setWeixin("wx14f57378c2bf71ab", "5116790b9c1250727c692ec6c75bc9e5");
        PlatformConfig.setSinaWeibo("613051778", "6666dd1474995c706a03dbc09d731ced");
        PlatformConfig.setQQZone("1105875200", "rorKP9K2hG30ta4b");
//        Config.REDIRECT_URL = "您新浪后台的回调地址";//设置新浪微博回调地址
//        Config.DEBUG = false;//关闭友盟debug模式
//        Log.LOG = false;//关闭友盟的Log
//        Config.IsToastTip = false;//关闭友盟的Toast
        initUM();
    }

    /**
     * 初始化阿里相关组件
     */
    private void initAlib() {
        initJAQ();
        initBC();
        initAndFix();
    }

    /**
     * 初始化聚安全
     */
    private void initJAQ() {
        try {
            if(SecurityInit.Initialize(getApplicationContext()) == 0){
                Log.e("1", "成功");
            } else {
                Log.e("1", "失败");
            }
        } catch (JAQException e) {
            Log.e("1", "errorCode =" + e.getErrorCode());
        }
    }

    /**
     * 初始化百川
     */
    private void initBC() {
        AlibcTradeSDK.asyncInit(this, new AlibcTradeInitCallback() {
            @Override
            public void onSuccess() {
                //初始化成功，设置相关的全局配置参数
                Log.e("2", "成功");
            }

            @Override
            public void onFailure(int code, String msg) {
                //初始化失败，可以根据code和msg判断失败原因，详情参见错误说明
                Log.e("2", "失败");
            }
        });
    }

    /**
     * 初始化AndFix
     */
    private void initAndFix() {
        mPatchManager = new PatchManager(this);
        String appversion = "";
        try {
            appversion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            mPatchManager.init(appversion);
            mPatchManager.loadPatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        /**
         * 初始化友盟分享
         */
    private void initUM() {
        UMShareAPI.get(this);
        initUMPush();
        customActivity();
    }

    /**
     * 初始化友盟推送
     */
    private void initUMPush() {
        try{
            //注册推送服务，每次调用register方法都会回调该接口
            PushAgent mPushAgent = PushAgent.getInstance(this);//初始化消息推送对象
            mPushAgent.register(new IUmengRegisterCallback() {
                @Override
                public void onSuccess(String deviceToken) {
                    Log.e("UmPush","success");
                }

                @Override
                public void onFailure(String s, String s1) {
                    Log.e("UmPush","failure");
                }
            });
        }catch (Exception e){
            Log.e("UmPush","error");
            e.printStackTrace();
        }
    }

    /**
     * 消息推送自定义行为
     */
    private void customActivity() {
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setNotificationClickHandler(new UmengNotificationClickHandler(){
            @Override
            public void dealWithCustomAction(Context context, UMessage uMessage) {
                super.dealWithCustomAction(context, uMessage);
                try {
                    JSONObject jsonObject = JSONObject.parseObject(uMessage.custom);
                    Intent intent = new Intent(context, DetailsActivity.class);
                    String id = jsonObject.getString("goods_id");
                    String url = jsonObject.getString("coupon_url");
                    intent.putExtra("goodsId",id);
                    intent.putExtra("couponUrl",url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e){
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }

    private void initX5() {
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                android.util.Log.e("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                android.util.Log.e("app","onDownloadFinish is " + i);
            }

            @Override
            public void onInstallFinish(int i) {
                android.util.Log.e("app","onInstallFinish is " + i);
            }

            @Override
            public void onDownloadProgress(int i) {
                android.util.Log.e("app","onDownloadProgress:"+i);
            }
        });

        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }

    /**
     * 获取状态栏高度
     */
    private void getStatusBarHeight() {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        PrefShared.saveInt(context,"statusBarHeight",context.getResources().getDimensionPixelSize(resourceId));
    }

    /**
     * 单例模式中获取唯一的MyApplication实例
     *
     * @return
     */
    public static MainApplication getInstance() {
        if (null == instance) {
            instance = new MainApplication();
        }
        return instance;
    }

    /**
     * 添加Activity到容其中
     *
     * @param activity
     */
    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public static List<Activity> getActivitys(){
        return activityList;
    }

    /**
     * 遍历所有Activity并finish
     */
    public static void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        System.exit(0);
    }
}