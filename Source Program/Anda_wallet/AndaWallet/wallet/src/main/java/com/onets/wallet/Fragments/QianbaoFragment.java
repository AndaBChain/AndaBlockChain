package com.onets.wallet.Fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onets.wallet.Adapter.QianbaoFragmentAdapter;
import com.onets.wallet.Fragment_Qianbao.LianxirenFragment;
import com.onets.wallet.Fragment_Qianbao.WalletFunctionFragment;
import com.onets.wallet.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class QianbaoFragment extends Fragment {
     public TabLayout tabLayout;
     public ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qianbao, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        initView();
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    private void initView() {
        //初始化Fragments
        ArrayList<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(WalletFunctionFragment.newInstance());
        fragments.add(LianxirenFragment.newInstance());

        //实例化ViewPager的适配器
        QianbaoFragmentAdapter qfa = new QianbaoFragmentAdapter(getChildFragmentManager(),fragments);
        //ViewPager设置适配器
        viewPager.setAdapter(qfa);
    }


}
