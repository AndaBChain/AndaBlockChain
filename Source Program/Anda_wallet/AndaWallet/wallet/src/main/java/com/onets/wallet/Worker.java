package com.onets.wallet;

import org.litepal.crud.DataSupport;

/**
 * @author Yu K.Q.
 * Created by Administrator on 2017/6/26.
 * 数据支持工具类
 */

public class Worker extends DataSupport {
    String name;
    String password;
    User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
