package com.onets.wallet.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onets.wallet.Adapter.ChongzhiFragmentAdapter;
import com.onets.wallet.Constants;
import com.onets.wallet.Fragment_Chongzhi.GoumaiFragment;
import com.onets.wallet.Fragment_Chongzhi.RechargeFragment;
import com.onets.wallet.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/12.
 */

public class ChongzhiFragment extends Fragment {
    TabLayout tableLayout;
    ViewPager viewPager;

    public static ChongzhiFragment newInstance(String accountId){
        ChongzhiFragment fragment = new ChongzhiFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_chongzhi, container, false);
        tableLayout= (TabLayout) view.findViewById(R.id.tabLayout_chongzhi);
        viewPager= (ViewPager) view.findViewById(R.id.viewpager_chongzhi);
        initView();
        tableLayout.setupWithViewPager(viewPager);
        return view;
    }

    private void initView() {
        //初始化Fragments
        ArrayList<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(RechargeFragment.newInstance());
        fragments.add(GoumaiFragment.newInstance());
        //实例化ViewPager的适配器
        ChongzhiFragmentAdapter  cfa = new ChongzhiFragmentAdapter(getChildFragmentManager(),fragments);
        //ViewPager设置适配器
        viewPager.setAdapter(cfa);
    }
}
