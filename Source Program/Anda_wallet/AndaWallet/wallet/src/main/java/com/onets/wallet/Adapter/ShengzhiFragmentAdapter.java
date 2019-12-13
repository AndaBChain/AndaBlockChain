package com.onets.wallet.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017/6/15.
 */

public class ShengzhiFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public ShengzhiFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    public ShengzhiFragmentAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
