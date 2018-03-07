package com.shhb.gd.shop.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.jaeger.library.StatusBarUtil;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.listener.NetBroadcastReceiver;
import com.shhb.gd.shop.tools.NetUtil;
import com.shhb.gd.shop.view.BlackStatusBar;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

/**
 * Created by Moon on 2016/12/1.
 */
public class BaseActivity extends AppCompatActivity implements NetBroadcastReceiver.NetEvevt{
    public static Context context;
    public boolean processFlag = true; //默认可以点击

    public static NetBroadcastReceiver.NetEvevt evevt;
    /**
     * 网络类型
     */
    public static int netMobile;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setBlackStatusBar();
        setStatusBar();
    }

    /**
     * 设置状态栏为黑色图标
     */
    protected void setBlackStatusBar() {
        BlackStatusBar.StatusBarLightMode(this);
    }

    /**
     * 设置状态栏的背景颜色
     */
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        evevt = this;
        context = this;
        PushAgent.getInstance(context).onAppStart();//统计应用启动数据
        MainApplication.getInstance().addActivity(this);
        inspectNet();
    }


    /**
     * 初始化时判断有没有网络
     */

    public boolean inspectNet() {
        this.netMobile = NetUtil.getNetWorkState(BaseActivity.this);
        return isNetConnect();
    }

    /**
     * 网络变化之后的类型
     */
    public void onNetChange(int netMobile) {
        // TODO Auto-generated method stub
        this.netMobile = netMobile;
        isNetConnect();

    }

    /**
     * 判断有无网络 。
     *
     * @return true 有网, false 没有网络.
     */
    public boolean isNetConnect() {
        if (netMobile == NetUtil.NETWORK_WIFI) {
            return true;
        } else if (netMobile == NetUtil.NETWORK_MOBILE) {
            return true;
        } else if (netMobile == NetUtil.NETWORK_NONE) {
            return false;

        }
        return false;
    }

    /**
     * 设置按钮在短时间内被重复点击的有效标识（true表示点击有效，false表示点击无效）
     */
    protected synchronized void setProcessFlag() {
        processFlag = false;
    }

    /**
     * 计时线程（防止在一定时间段内重复点击按钮）
     */
    protected class TimeThread extends Thread {
        public void run() {
            try {
                sleep(1000);
                processFlag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 友盟session的统计
     */
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    /**
     * 友盟session的统计
     */
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
