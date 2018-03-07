package com.shhb.gd.shop.tools;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.shhb.gd.shop.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kiven on 16/10/14.
 */
public class PhoneInfo {
    private static TelephonyManager telephonyManager;
    private Context context;

    Map<String,Object> map = new HashMap<String,Object>();

    public PhoneInfo(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public Map<String, Object> getPhoneMsg(){
        map.put("phone",getNativePhoneNumber());//手机号码
        map.put("bdid","");//电池ID
        String appMsg = getAppMsg();
        if(null != appMsg){
            String[] appMsgs = appMsg.split(",");
            map.put("deviceVer",appMsgs[0]+"");//APP更新号
            map.put("appVer",appMsgs[1]+"");//APP版本号
            map.put("appBid",appMsgs[2]+"");//APP包名
            map.put("imei",appMsgs[3]+"");//IMEI
        } else {
            map.put("deviceVer","");//APP更新号
            map.put("appVer","");//APP版本号
            map.put("appBid","");//APP包名
        }
        map.put("appName",context.getResources().getString(R.string.app_name));//APP名
        String wifi = getNetworkType();
        if(null != wifi){
            String[] wifis = wifi.split(",");
            map.put("wifiSid",wifis[0]);//wifi名
            map.put("wifiBid",wifis[1]+"");//wifi编号
        } else {
            map.put("wifiSid","");//wifi名
            map.put("wifiBid","");//wifi编号
        }
        map.put("simType",getProvidersName());//运营商的名称
        map.put("onlineType","");//是否登录
        map.put("jaibreak","");//手机是否越狱
        map.put("deviceModel","android");//手机是否越狱
        map.put("deviceName",android.os.Build.MANUFACTURER);// 手机名称
        map.put("deviceW",BaseTools.getWindowsWidth((Activity) context));//手机宽度像素
        map.put("deviceH",BaseTools.getWindowsHeight((Activity) context));//手机高度像素
        map.put("ip",getOuterNetFormCmyIP());//IP地址
        return map;
    }

    /**
     * 获取电话号码
     */
    public String getNativePhoneNumber() {
        String phoneNumber = "";
        try {
            phoneNumber = telephonyManager.getLine1Number();//手机号码
        } catch (Exception e){
            e.printStackTrace();
        }
        return phoneNumber;
    }

    /**
     *获取APP信息
     * @return
     */
    public String getAppMsg(){
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;//APP更新号
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;//APP版本号
            String pkName = context.getPackageName();//APP包名
            String IMEI = telephonyManager.getDeviceId();//IMEI
            return versionCode + "," + versionName + "," + pkName + "," + IMEI;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取IMEI
     * @return
     */
    public static String getIMEI(){
        String IMEI = telephonyManager.getDeviceId();//IMEI
        return IMEI;
    }

    /**
     * 获取手机服务商信息
     */
    public String getProvidersName() {
        String providersName = "N/A";
        String IMSI = "";
        try{
            IMSI = telephonyManager.getSubscriberId();//IMSI
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            if(null != IMSI){
                if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                    providersName = "中国移动";
                } else if (IMSI.startsWith("46001")) {
                    providersName = "中国联通";
                } else if (IMSI.startsWith("46003")) {
                    providersName = "中国电信";
                }
                return providersName;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return providersName;
    }

    /**
     * 获取IP地址
     */
//    public String getIpAddress(){
//        String ipAddress = "";
//        try {
//            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = (NetworkInterface) en.nextElement();
//                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && !TextUtils.equals(inetAddress.getHostAddress(),"10.0.2.15")) {
//                        ipAddress = inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return ipAddress;
//    }

    /**
     * 通过CmyIP获取获取外网外网地址  需在异步线程中访问
     * @return 外网IP
     */
    public static String getOuterNetFormCmyIP() {
        String response = GetOuterNetIp("http://www.cmyip.com/");
        Pattern pattern = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
        Matcher matcher = pattern.matcher(response.toString());
        if (matcher.find()) {
            String group = matcher.group();
            return group;
        }
        return "";
    }

    /**
     * 获取获取外网外网地址  需在异步线程中访问
     * @param ipaddr 提供外网服务的服务器ip地址
     * @return 外网IP
     */
    public static String GetOuterNetIp(String ipaddr) {
        URL infoUrl = null;
        InputStream inStream = null;
        try {
            infoUrl = new URL(ipaddr);
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                    strber.append(line + "\n");
                inStream.close();
                return strber.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取网络类型
     * @return
     */
    public String getNetworkType() {
        String wifiSsId = "";
        String wifiBssId = "";
        try{
            NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiInfo wifiInfo = ((WifiManager) context.getSystemService(context.WIFI_SERVICE)).getConnectionInfo();
                    wifiSsId = wifiInfo.getSSID();
                    if(wifiSsId.contains("\"")){
                        wifiSsId.replace("\"","");
                    }
                    wifiBssId = wifiInfo.getBSSID();
                    return wifiSsId + "," + wifiBssId;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String getPhoneInfo(){
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneName = android.os.Build.MANUFACTURER;// 手机名称
        String phoneType = android.os.Build.MODEL; // 手机型号
        String systemVersion = android.os.Build.VERSION.RELEASE;//系统版本
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nDeviceId(IMEI) = " + telephonyManager.getDeviceId());
        stringBuilder.append("\nDeviceSoftwareVersion = " + telephonyManager.getDeviceSoftwareVersion());
        stringBuilder.append("\nLine1Number = " + telephonyManager.getLine1Number());
        stringBuilder.append("\nNetworkCountryIso = " + telephonyManager.getNetworkCountryIso());
        stringBuilder.append("\nNetworkOperator = " + telephonyManager.getNetworkOperator());
        stringBuilder.append("\nNetworkOperatorName = " + telephonyManager.getNetworkOperatorName());
        stringBuilder.append("\nNetworkType = " + telephonyManager.getNetworkType());
        stringBuilder.append("\nPhoneType = " + telephonyManager.getPhoneType());
        stringBuilder.append("\nSimCountryIso = " + telephonyManager.getSimCountryIso());
        stringBuilder.append("\nSimOperator = " + telephonyManager.getSimOperator());
        stringBuilder.append("\nSimOperatorName = " + telephonyManager.getSimOperatorName());
        stringBuilder.append("\nSimSerialNumber = " + telephonyManager.getSimSerialNumber());
        stringBuilder.append("\nSimState = " + telephonyManager.getSimState());
        stringBuilder.append("\nSubscriberId(IMSI) = " + telephonyManager.getSubscriberId());
        stringBuilder.append("\nVoiceMailNumber = " + telephonyManager.getVoiceMailNumber());
        return  stringBuilder.toString();
    }
}
