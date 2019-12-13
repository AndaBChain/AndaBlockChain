package com.onets.wallet.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2016/9/18 0018.
 */
public class QianbaoFragmentAdapter extends FragmentPagerAdapter {
    private static final String[] TITLES={"功能选择","联系人"};
    private List<Fragment> fragments;
    public QianbaoFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public QianbaoFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;

    }
    /**
     * 返回对应位置的fragment
     * @param position
     * @return
     */
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }
    /**
     * 返回fragments的个数
     * @return
     */
    @Override
    public int getCount() {
        return fragments.size();
    }

    /**
     * tablayout与ViewPager进行绑定要用到这个方法来获取当前页的title
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
}
