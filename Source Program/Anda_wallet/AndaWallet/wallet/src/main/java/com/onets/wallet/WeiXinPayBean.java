package com.onets.wallet;

/**
 * @author Yu K.Q.
 * Created by Hasee on 2018/3/21.
 * 微信支付工具类
 */

public class WeiXinPayBean {


    /**
     * appid : wxb4ba3c02aa476ea1
     * partnerid : 1900006771
     * packageValue : Sign=WXPay
     * noncestr : 9ca8f1d6194e9f35f82ed95b3e0b1798
     * timestamp : 1521613560
     * prepayid : wx20180321142600553b3a06cd0361943529
     * sign : E6516F83D70D67D84E96D8FF3E3FB7CD
     */

    private String appid;
    private String partnerid;
    private String packageValue;
    private String noncestr;
    private int timestamp;
    private String prepayid;
    private String sign;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getPackageValue() {
        return packageValue;
    }

    public void setPackageValue(String packageValue) {
        this.packageValue = packageValue;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
