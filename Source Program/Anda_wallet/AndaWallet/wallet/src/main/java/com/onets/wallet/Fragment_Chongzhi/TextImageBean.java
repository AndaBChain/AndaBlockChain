package com.onets.wallet.Fragment_Chongzhi;

/**
 * 文字图片基类
 * Created by Hasee on 2018/1/14.
 */

public class TextImageBean {
    private String name;
    private int icon;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public TextImageBean(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }
}
