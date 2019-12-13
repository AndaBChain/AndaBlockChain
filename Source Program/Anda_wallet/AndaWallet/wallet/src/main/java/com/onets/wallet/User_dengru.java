package com.onets.wallet;

import org.litepal.crud.DataSupport;

/**
 * @author Yu K.Q.
 * Created by Administrator on 2017/6/26.
 */

public class User_dengru extends DataSupport {
    String name,emai,password,phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmai() {
        return emai;
    }

    public void setEmai(String emai) {
        this.emai = emai;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
