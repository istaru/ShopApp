package com.shhb.gd.shop.listener;

import android.app.Activity;
import android.util.Log;

import com.alibaba.baichuan.android.trade.callback.AlibcTradeCallback;
import com.alibaba.baichuan.android.trade.model.TradeResult;
import com.alibaba.fastjson.JSONObject;
import com.shhb.gd.shop.tools.BaseTools;
import com.shhb.gd.shop.tools.Constants;
import com.shhb.gd.shop.tools.OkHttpUtils;
import com.shhb.gd.shop.tools.PrefShared;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 购物回调接口
 * Created by Kiven on 16/12/1.
 */
public class TradeCallback implements AlibcTradeCallback {
    private Activity context;

    public TradeCallback(Activity context) {
        this.context = context;
    }

    @Override
    public void onTradeSuccess(TradeResult tradeResult) {
        //当addCartPage加购成功和其他page支付成功的时候会回调
//        if(tradeResult.resultType.equals(ResultType.TYPECART)){//加入购物车成功
//            Log.e("购物回调成功","加入购物车成功");
//        }else if (tradeResult.resultType.equals(ResultType.TYPEPAY)){//支付成功
//            Log.e("购物回调成功",JSON.toJSONString(tradeResult.payResult.paySuccessOrders));
//        } else {
//            Log.e("购物回调成功","其他操作成功");
//        }
        sendByOrders(tradeResult.payResult.paySuccessOrders);
    }

    @Override
    public void onFailure(int errCode, String errMsg) {
        Log.e("购物回调出错","电商SDK出错,错误码="+errCode+" / 错误消息="+errMsg);
    }

    /**
     * 查找商品详情的数据
     * @param orders
     */
    private void sendByOrders(List orders) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", PrefShared.getString(context,"userId"));
        jsonObject.put("order_id", orders);
        jsonObject.put("taobao_nick", PrefShared.getString(context,"nick"));
        jsonObject.put("type",1);//表示android设备
        String parameter = BaseTools.encodeJson(jsonObject.toString());
        OkHttpUtils okHttpUtils = new OkHttpUtils(20);
        okHttpUtils.postEnqueue(Constants.SEND_ORDERS, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Log.e("服务器返回",BaseTools.decryptJson(json));
            }
        },parameter);
    }
}
