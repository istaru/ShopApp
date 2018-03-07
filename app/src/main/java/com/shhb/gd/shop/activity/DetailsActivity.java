package com.shhb.gd.shop.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ali.auth.third.ui.context.CallbackContext;
import com.alibaba.baichuan.android.trade.AlibcTradeSDK;
import com.alibaba.baichuan.android.trade.page.AlibcDetailPage;
import com.alibaba.fastjson.JSONObject;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.shhb.gd.shop.R;
import com.shhb.gd.shop.application.MainApplication;
import com.shhb.gd.shop.listener.WCClient;
import com.shhb.gd.shop.module.AlibcShow;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.module.CouponPopupWindow;
import com.shhb.gd.shop.tools.Constants;
import com.shhb.gd.shop.tools.NetUtil;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.ShareBoardConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Kiven on 16/12/9.
 */

public class DetailsActivity extends BaseActivity implements View.OnClickListener {
    private WebView webView;
    private WCClient wcClient;
    private CircularProgressView viewStub;
    private LinearLayout tailLinear;
    private TextView title;
    private LinearLayout onShare;

    private CouponPopupWindow couponPopupWindow;
    private String numId = "";
    private String couponUrl = "";
    private String shareUrl = "";
    private String shareImg = "";
    private String shareTitle = "";
    private List<Activity> activitys;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        numId = getIntent().getStringExtra("goodsId");
        couponUrl = getIntent().getStringExtra("couponUrl");
        activitys = MainApplication.getActivitys();
        iniWebView();
        update();
    }

    private void update() {
        File dir = BaseTools.makeFile("patch");
        if (!dir.exists()) dir.mkdirs();
        String patchFileString = dir.getPath() + Constants.APATCH_PATH;
        File patchFile = new File(patchFileString);
        if(patchFile.exists()) {
            Log.e("补丁文件", "存在");
            try {
                MainApplication.mPatchManager.addPatch(patchFileString);//打补丁
                Log.e("补丁文件路径", patchFileString);
                if (patchFile.exists()) {//加载完成之后删除补丁文件
                    patchFile.delete();
                }
            } catch (Exception e) {
                Log.e("补丁文件", "出错");
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化宝贝详情
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void iniWebView() {
        title = (TextView) findViewById(R.id.webView_title);
        title.setText("宝贝详情");
        onShare = (LinearLayout)findViewById(R.id.onShare);
        onShare.setOnClickListener(this);

        viewStub = (CircularProgressView) findViewById(R.id.viewStub);
        viewStub.setVisibility(View.VISIBLE);
        wcClient = new WCClient(viewStub,"");

        tailLinear = (LinearLayout) findViewById(R.id.tail);
        tailLinear.setOnClickListener(this);

        webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(getResources().getColor(R.color.webBg));
        webView.setWebChromeClient(wcClient);
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
//        webView.addJavascriptInterface(new JsObject(webView, this), "native_android");
        webView.setWebViewClient(new WebViewClient());

        //关闭缩放
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setDisplayZoomControls(false);
        loadData();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void loadData() {
        if(isNetConnect()){
            AlibcShow.showH5(this,webView,wcClient,new AlibcDetailPage(numId));
            findByShareMsg();
        }
        couponPopupWindow = new CouponPopupWindow(this,
                couponUrl,
                numId,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (BaseTools.getWindowsHeight(this) / 2.09));

        webView.post(new Runnable() {
            @Override
            public void run() {
                showPopupWindow();
            }
        });
    }

    @Override
    public void onNetChange(int netMobile) {
        super.onNetChange(netMobile);
        if (netMobile== NetUtil.NETWORK_NONE){
            viewStub.setVisibility(View.VISIBLE);
        } else {
            viewStub.setVisibility(View.GONE);
            AlibcShow.showH5(this,webView,wcClient,new AlibcDetailPage(numId));
            if(null != couponPopupWindow.webView2){
                couponPopupWindow.webView2.loadUrl(couponUrl);
            }
            findByShareMsg();
        }
    }

    /**
     * 分享按钮
     * @param v
     */

    /**
     * 监听分享的回调
     */
    private UMShareListener umAuthListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA share_media) {
            Toast.makeText(DetailsActivity.this,"分享成功", Toast.LENGTH_SHORT).show();
            sendOnResult();
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            if(throwable.getMessage().contains("没有安装")){//没有安装应用
                Toast.makeText(DetailsActivity.this,"请先安装应用", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DetailsActivity.this,"分享失败啦！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            Toast.makeText(DetailsActivity.this,"分享取消", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 分享成功的回调
     */
    private void sendOnResult(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id", PrefShared.getString(context,"userId"));
        jsonObject.put("num_iid", numId);
        jsonObject.put("type",1);//表android分享
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.DETAILS_SHARE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Log.e("分享成功回调信息","网络或接口异常");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = BaseTools.decryptJson(response.body().string());
                    Log.e("分享成功回调信息",json);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        },parameter);
    }

    /**
     * 查找商品详情的数据
     */
    private void findByShareMsg() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id", PrefShared.getString(context,"userId"));
        jsonObject.put("num_iid", numId);
        jsonObject.put("app", Constants.APP_TYPE);
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.FIND_BY_DETAILS, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                showLog("商品详情页分享内容","网络或接口异常");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
//                showLog("分享信息",json);
                try {
                    JSONObject jsonObject = JSONObject.parseObject(BaseTools.decryptJson(json));
                    int status = jsonObject.getInteger("status");
                    if(status == 1){
                        jsonObject = jsonObject.getJSONObject("data");
                        shareUrl = jsonObject.getString("share_url")+"?goods_id=" + numId;
                        shareTitle = jsonObject.getString("name");
                        shareImg = jsonObject.getString("icon_url");
                    } else {

                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        },parameter);
    }

    /**
     * 显示弹窗
     */
    private void showPopupWindow() {
        // 显示窗口
        couponPopupWindow.showAtLocation(webView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        // 打开窗口时设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
    }

    /**
     * 显示、关闭劵的弹窗
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tail:
                if (couponPopupWindow.isShowing()) {
                    couponPopupWindow.dismiss();
                } else {
                    showPopupWindow();
                }
                break;
            case R.id.onShare:
                if(!TextUtils.equals(shareUrl,"") &&
                        !TextUtils.equals(shareImg,"") &&
                        !TextUtils.equals(shareTitle,"")){
                    ShareAction shareAction = new ShareAction(DetailsActivity.this);
                    UMImage imageurl = new UMImage(context, shareImg);//网络图片
                    shareAction.withTitle(shareTitle);//标题
        //          shareAction.withText();//内容
                    shareAction.withMedia(imageurl);//图片
                    shareAction.withTargetUrl(shareUrl);//链接
                    shareAction.setDisplayList(SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.QQ);
                    shareAction.setCallback(umAuthListener);
                    ShareBoardConfig config = new ShareBoardConfig();
                    config.setShareboardPostion(ShareBoardConfig.SHAREBOARD_POSITION_CENTER);//分享的面板居中显示
                    config.setMenuItemBackgroundShape(ShareBoardConfig.BG_SHAPE_NONE);//设置面板的透明度和圆角
                    config.setTitleVisibility(true);//隐藏标题
                    config.setTitleText("成功分享有返利哟");
                    config.setCancelButtonVisibility(false);//隐藏底部取消按钮
                    shareAction.open(config);
                } else {
                    Toast.makeText(DetailsActivity.this,"信息获取中", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext.onActivityResult(requestCode, resultCode, data);//阿里的回调
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);//友盟精简版的回调
    }

    /**
     * 返回按钮
     * @param v
     */
    public void back(View v) {
        if(webView.canGoBack()){
            webView.goBack();
        }else {
            for(int i = 0;i < activitys.size();i++){
                String activityName = activitys.get(i).toString();
                activityName = activityName.substring(activityName.lastIndexOf(".")+1,activityName.indexOf("@"));
                if(TextUtils.equals(activityName,"MainActivity")){
                    this.finish();
                    break;
                } else {
                    if(i == activitys.size()-1){
                        startActivity(new Intent(context,MainActivity.class));
                        this.finish();
                        break;
                    }
                }
            }
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
                        for(int i = 0;i < activitys.size();i++){
                            String activityName = activitys.get(i).toString();
                            activityName = activityName.substring(activityName.lastIndexOf(".")+1,activityName.indexOf("@"));
                            if(TextUtils.equals(activityName,"MainActivity")){
                                this.finish();
                                break;
                            } else {
                                if(i == activitys.size()-1){
                                    startActivity(new Intent(context,MainActivity.class));
                                    this.finish();
                                    break;
                                }
                            }
                        }
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
        if(null != couponPopupWindow){
            if(null != couponPopupWindow.webView2){//销毁WebView
                couponPopupWindow.webView2.onPause();
                couponPopupWindow.webView2.destroy();
                couponPopupWindow.webView2 = null;
            }
        }
        if(null != webView){//销毁WebView
            webView.onPause();
            webView.destroy();
            webView = null;
        }
        AlibcTradeSDK.destory();
        super.onDestroy();
    }
}
