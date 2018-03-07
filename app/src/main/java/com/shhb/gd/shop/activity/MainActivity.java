package com.shhb.gd.shop.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.auth.third.ui.context.CallbackContext;
import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jaeger.library.StatusBarUtil;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.adapter.ViewPagerAdapter;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.listener.WVClient;
import com.shhb.gd.shop.module.JsObject;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.Constants;
import com.shhb.gd.shop.tools.NetUtil;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;
import com.shhb.gd.shop.view.CustomViewPager;
import com.umeng.message.PushAgent;
import com.umeng.socialize.UMShareAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kiven on 16/12/1.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private AlphaAnimation start_anima;
    private long mExitTime;
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
    private AMapLocationClientOption mLocationOption = null;//声明AMapLocationClientOption对象
    private String addres = "";//手机位置信息
    private View mViewNeedOffset;
    private WebView webView;
    private RelativeLayout mainView;
    private TextView skipView;
    private CustomViewPager viewPager;
    private List<ImageView> imgs;
    private ImageView img1,img2,img3;
    private ViewPagerAdapter viewPagerAdapter;
    private boolean flag;
    private int banner3;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initWebView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//Android6.0以上的系统
            int sdPermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);//获取手机信息的权限
            if(sdPermission != PackageManager.PERMISSION_GRANTED){//还没有获取获取手机信息的权限
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, Constants.PHONE_CODE);
            } else {
                loadData();
            }
        } else {
            loadData();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initWebView() {
        mViewNeedOffset = findViewById(R.id.view_need_offset);

        mainView = (RelativeLayout) findViewById(R.id.mainView);
        skipView = (TextView) findViewById(R.id.skipView);
        skipView.setOnClickListener(this);
        viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        viewPager.setScanScroll(true);
        webView = (WebView) findViewById(R.id.webView);

        banner3 = PrefShared.getInt(context,"banner3");
        if(banner3 < 3){
            skipView.setVisibility(View.VISIBLE);
            initIBanner();
        } else {
            skipView.setVisibility(View.GONE);
            mainView.setBackground(getResources().getDrawable(R.mipmap.welcome1_1));
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    if (newProgress == 100) {// 加载完成
                        initImg();
                    }
                    super.onProgressChanged(view, newProgress);
                }
            });
        }

        webView.setBackgroundColor(getResources().getColor(R.color.webBg));
        //禁止长按事件
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);//支持JavaScript
        webView.getSettings().setAllowFileAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript读取其他的本地文件
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//通过此API可以设置是否允许通过file url加载的Javascript可以访问其他的源，包括其他的文件和http,https等其他的源
        //图片显示
        webView.getSettings().setLoadsImagesAutomatically(true);
        //自适应屏幕
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.addJavascriptInterface(new JsObject(webView, this), "native_android");
        webView.setWebViewClient(new WVClient(this));
        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        //给H5读写内存的权限
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);//允许访问文件
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
    }

    /**
     * 初始化启动图片
     */
    private void initIBanner() {
        imgs = new ArrayList<>();
        img1 = new ImageView(this);
        img1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        img1.setImageResource(R.mipmap.welcome1);
        img1.setScaleType(ImageView.ScaleType.FIT_XY);

        img2 = new ImageView(this);
        img2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        img2.setImageResource(R.mipmap.welcome2);
        img2.setScaleType(ImageView.ScaleType.FIT_XY);

        img3 = new ImageView(this);
        img3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        img3.setImageResource(R.mipmap.welcome3);
        img3.setScaleType(ImageView.ScaleType.FIT_XY);

        imgs.add(img1);
        imgs.add(img2);
        imgs.add(img3);

        viewPagerAdapter = new ViewPagerAdapter(imgs);
        viewPager.addOnPageChangeListener(pageChangeListener);
        viewPager.setAdapter(viewPagerAdapter);
    }

    /**
     * 发送推送所需的DeviceToken
     */
    private void sendPushToken(){
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id",PrefShared.getString(context,"userId"));
        jsonObject.put("device_token", PushAgent.getInstance(this).getRegistrationId());
        jsonObject.put("type",1);
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        okHttpUtils.postEnqueue(Constants.SEND_DEVICE_TOKEN, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = BaseTools.decryptJson(response.body().string());
                    Log.e("DeviceToken成功回调信息",json);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        },parameter);
    }

    /**
     * ImageView滑动事件
     */
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    flag= false;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    flag = true;
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !flag) {
                        initImg();
                    }
                    flag = true;
                    break;
            }
        }
    };

    /**
     * 隐藏viewPager
     */
    private void initImg() {
        start_anima = new AlphaAnimation(1.0f, 0f);
        start_anima.setDuration(500);
        start_anima.setFillAfter(true);
        viewPager.startAnimation(start_anima);
        start_anima.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                mainView.setVisibility(View.GONE);
                if(banner3 < 3){
                    banner3 ++;
                    PrefShared.saveInt(context,"banner3",banner3);
                    sendPushToken();
                }
            }
        });
    }

    /**
     * 加载数据
     */
    private void loadData(){
        if(isNetConnect()) {
            initLocation();
            webView.loadUrl(Constants.HTML_URL);
        } else {
            webView.loadUrl(Constants.LOAD_URL);
        }
    }

    @Override
    public void onNetChange(int netMobile) {
        super.onNetChange(netMobile);
        if (netMobile== NetUtil.NETWORK_NONE){

        } else {
            webView.loadUrl(Constants.HTML_URL);
        }
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageView(this, 0, mViewNeedOffset);
    }

    /***
     * 如果是6.0的系统就获取权限
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PHONE_CODE:
                if(permissions[0].equals(Manifest.permission.READ_PHONE_STATE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意读取手机信息权限
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.SD_CODE);//SD权限
                } else {
                    showAlertDialog(requestCode);
                }
                break;
            case Constants.SD_CODE:
                if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意SD读写权限
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},Constants.LOCATION_CODE);//定位权限
                    loadData();
                } else {
                    showAlertDialog(requestCode);
                }
                break;
            case Constants.LOCATION_CODE:
                initLocation();
                break;
            default:
                break;
        }
    }

    /**
     * 弹出提示框
     * @param requestCode
     */
    private void showAlertDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意");
        builder.setMessage("在您同意的情况下，可以使用本应用的所有服务。");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (requestCode){
                    case Constants.SD_CODE:
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
                        break;
                    case Constants.PHONE_CODE:
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},requestCode);
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

//    /**
//     * 开启高德定位
//     */
//    private void initAMap() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//Android6.0以上的系统
//            int phonePermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);//手机信息权限
//            if(phonePermission != PackageManager.PERMISSION_GRANTED){//还没有获取到读取手机信息的权限
//                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},Constants.PHONE_CODE);
//            } else {//已获取到读取手机信息权限
//                requestLocation();
//            }
//        } else {//Android6.0以下的系统
//            initLocation();
//        }
//    }

//    /**
//     * 当获取到读取手机信息的权限时开始获取定位权限
//     */
//    @TargetApi(Build.VERSION_CODES.M)
//    private void requestLocation() {
//        int locationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);//定位权限
//        if(locationPermission != PackageManager.PERMISSION_GRANTED){//还没有获取到定位权限
//            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},Constants.LOCATION_CODE);//定位权限
//        } else {//已获取到定位权限
//            initLocation();
//        }
//    }

    /**
     * 开始定位
     */
    private void initLocation(){
        long yqTime = PrefShared.getLong(context,"yqTime");
        String current = (System.currentTimeMillis())+"";
        current = current.substring(0,10);
        long xzTime = Long.parseLong(current);
        long s = (xzTime - yqTime) / 60;
        if(s > 10){//十分钟进行一次定位
            mLocationClient = new AMapLocationClient(getApplicationContext());//初始化定位
            mLocationClient.setLocationListener(mLocationListener);//设置定位回调监听
            mLocationOption = new AMapLocationClientOption();//初始化AMapLocationClientOption对象
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式。
            mLocationOption.setOnceLocation(true);//获取一次定位结果：该方法默认为false。
            mLocationOption.setOnceLocationLatest(true);//获取最近3s内精度最高的一次定位结果
            mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
            mLocationClient.startLocation();//启动定位
        }
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    double latitude = aMapLocation.getLatitude();//获取纬度
                    double longitude = aMapLocation.getLongitude();//获取经度
                    float accuracy = aMapLocation.getAccuracy();//获取精度信息
                    String address = aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    String country = aMapLocation.getCountry();//国家信息
                    String province = aMapLocation.getProvince();//省信息
                    String city = aMapLocation.getCity();//城市信息
                    String distric = aMapLocation.getDistrict();//城区信息
                    String street = aMapLocation.getStreet();//街道信息
                    String streetNum = aMapLocation.getStreetNum();//街道门牌号信息
                    String cityCode = aMapLocation.getCityCode();//城市编码
                    String adCode = aMapLocation.getAdCode();//地区编码
                    String aoiName = aMapLocation.getAoiName();//获取当前定位点的AOI信息
                    addres = country + province + city + distric + street + streetNum + aoiName;
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    addres = aMapLocation.getErrorCode()+"";
                }
                PrefShared.saveString(context,"position",addres);
                String current = (System.currentTimeMillis())+"";
                current = current.substring(0,10);
                long xzTime = Long.parseLong(current);
                PrefShared.saveLong(context,"yqTime",xzTime);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.skipView:
                initImg();
            break;
            default:
                break;
        }
    }

    /**
     * 淘宝、友盟等回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);//阿里的回调
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);//友盟精简版的回调
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(null != webView){
                String webViewUrl = webView.getUrl();
                if(null != webViewUrl){
                    webViewUrl = webViewUrl.substring(webViewUrl.lastIndexOf("/")+1);
                    if((TextUtils.equals(webViewUrl,"my.html") || TextUtils.equals(webViewUrl,"my.html#my")) ||
                            (TextUtils.equals(webViewUrl,"share.html") || TextUtils.equals(webViewUrl,"share.html#share")) ||
                            (TextUtils.equals(webViewUrl,"9_9.html") || TextUtils.equals(webViewUrl,"9_9.html#9_9")) ||
                            (TextUtils.equals(webViewUrl,"index.html") || TextUtils.equals(webViewUrl,"index.html#index")) ||
                             TextUtils.equals(webViewUrl,"loading.html")){
                        SySGc();
                    } else {
                        webView.goBack();
                    }
                } else {
                    SySGc();
                }
            } else {
                SySGc();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出系统
     */
    private void SySGc(){
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            MainApplication.exit();
        }
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
