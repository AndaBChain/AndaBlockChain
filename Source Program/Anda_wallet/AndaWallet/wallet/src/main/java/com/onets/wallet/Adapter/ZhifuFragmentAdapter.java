package com.onets.wallet.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017/6/21.
 */

public class ZhifuFragmentAdapter extends FragmentPagerAdapter {
    String Titles[]={"发送","请求","交易记录"};
    List<Fragment> list;

    public ZhifuFragmentAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.list = list;
    }

    public ZhifuFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return list.get(i);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }
}
