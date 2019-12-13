package com.onets.wallet.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.text.IDNA;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.onets.wallet.WalletApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * 手机信息工具类
 */
public class PhoneInfoUtils {
    private static final String TAG = "PhoneInfoUtils";

    //获取TelephonyManager对象
    public TelephonyManager telephonyManager;
    //移动运营商编号
    public String NetworkOperator;
    public Context context;

    public PhoneInfoUtils(Context context) {
        this.context = context.getApplicationContext();
        telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
    }

    //获取sim卡iccid
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getIccid() {
        String iccid = "N/A";
        iccid = telephonyManager.getSimSerialNumber();
        return iccid;
    }

    //获取电话号码
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getNativePhoneNumber() {
        String nativePhoneNumber = "N/A";
        nativePhoneNumber = telephonyManager.getLine1Number();
        return nativePhoneNumber;
    }

    //获取手机服务商信息
    public String getProvidersName() {
        String providersName = "N/A";
        NetworkOperator = telephonyManager.getNetworkOperator();
        //IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
//        Flog.d(TAG,"NetworkOperator=" + NetworkOperator);
        if (NetworkOperator.equals("46000") || NetworkOperator.equals("46002")) {
            providersName = "中国移动";//中国移动
        } else if (NetworkOperator.equals("46001")) {
            providersName = "中国联通";//中国联通
        } else if (NetworkOperator.equals("46003")) {
            providersName = "中国电信";//中国电信
        }
        return providersName;

    }

    /**
     * 获取Mac地址
     * 该方法获取后，Mac地址是02:00:00:00:00:00
     * @return
     */
    public String getMacAddress(){
        String macAddress = null;
        WifiManager wifiManager = (WifiManager) WalletApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = (null == wifiManager ? null : wifiManager.getConnectionInfo());
        if (!wifiManager.isWifiEnabled()){
            Log.d(TAG, "macAddress: wifienabled " + wifiManager.isWifiEnabled());
            wifiManager.setWifiEnabled(true);
            wifiManager.setWifiEnabled(false);
        }
        if (null != wifiInfo){
            macAddress = wifiInfo.getMacAddress();
        }
        return macAddress;
    }

    public String getAddressMAC(Context context){
        String strMac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            //6.0以下mac
            strMac = getLocalMacAddressFromWifiInfo(context);
        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            //6.0以上7.0以下
            strMac = getMacfromMarshmallow();
        }else {
            //7.0以上
            strMac = getMacFromHardware();
        }
        return strMac;
    }

    /**
     * 6.0以下 ,根据wifi信息获取本地mac
     *
     * @param context
     * @return
     */
    public String getLocalMacAddressFromWifiInfo(Context context) {
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }
        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * android 6.0及以上、7.0以下 获取mac地址
     * 如果是6.0以下，直接通过wifimanager获取
     *
     * @return
     */
    public String getMacfromMarshmallow() {

        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }


    /**
     * 7.0 以上 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    private String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    /*public String getPhoneInfo() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        StringBuffer sb = new StringBuffer();
        sb.append("\nLine1Number = " + tm.getLine1Number());
        sb.append("\nNetworkOperator = " + tm.getNetworkOperator());//移动运营商编号
        sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());//移动运营商名称
        sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
        sb.append("\nSimOperator = " + tm.getSimOperator());
        sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
        sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
        sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
        return  sb.toString();
    }*/
}
