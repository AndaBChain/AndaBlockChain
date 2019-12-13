package com.onets.wallet;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/6/23.
 */

public class Contacts extends DataSupport {
    String name         //姓名
            ,email        //电子邮箱
            ,coin_addr    //奖币地址
            ,flag         //标签，备注
            ,phone1       //固定电话
            ,phone2       //手机
            ,address      //地址
            ,qq           //QQ
            ,weixin       //微信
            ,company;     //公司

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCoin_addr() {
        return coin_addr;
    }

    public void setCoin_addr(String coin_addr) {
        this.coin_addr = coin_addr;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
