package com.shhb.gd.shop.tools;

/**
 * Created by Kiven on 16/12/1.
 */
public class Constants {

    /**
     * SD卡权限返回的code
     */
    public static final int SD_CODE = 101;
    /**
     * 手机权限返回的code
     */
    public static final int PHONE_CODE = 102;
    /**
     * 定位权限返回的code
     */
    public static final int LOCATION_CODE = 103;

    public static final String APP_FILE_URL = "HuiAmoy";

    public static final String APATCH_PATH = "/out.apatch";

    /**
     * 服务器地址
     */
    public static final String REQUEST = "http://es1.laizhuan.com/shopping/";//正式服务器

    /**
     * 下载补丁的地址
     */
    public static final String PATCH_URL = "http://es1.laizhuan.com/shopping/Shop/android?name=out.apatch";

    /**
     *页面的地址
     */
    public static final String HTML_URL = "http://es1.laizhuan.com/views/html/index.html";
//    public static final String HTML_URL = "http://192.168.1.167:5222/html/index.html";//测试页面的地址

    /**
     * 断网的loading页面
     */
    public static final String LOAD_URL = "file:///android_asset/html/loading.html";

    public static final String APP_TYPE = "android";

    /**
     * 查找商品详情和购物券地址
     */
    public static final String FIND_BY_DETAILS = REQUEST + "detail";

    /**
     * 打开QQ客服
     */
    public static final String OPEN_QQ = "mqqwpa://im/chat?chat_type=wpa&uin=";

    /**
     * 付款后发送给服务器
     */
    public static final String SEND_ORDERS = REQUEST + "TaoBaoKe/addOrderId";

    /**
     * 商品详情分享成功的回调
     */
    public static final String DETAILS_SHARE = REQUEST + "share_goods";

    /**
     * 发送推送所需的DeviceToken
     */
    public static final String SEND_DEVICE_TOKEN = REQUEST + "user/addPushAssocForApp";

    /**
     *发送失效的购物卷
     */
    public static final String SEND_VOLUME = REQUEST + "/goods_switch";

}
