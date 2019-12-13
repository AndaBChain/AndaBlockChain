package com.onets.wallet.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onets.wallet.Adapter.ShengzhiFragmentAdapter;
import com.onets.wallet.Fragment_Shengzhi.text_fragment;
import com.onets.wallet.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/12.
 */

public class ShengzhiFragment extends Fragment {
    TabLayout tableLayout;
    ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_shengzhi, container, false);
        tableLayout = (TabLayout) view.findViewById(R.id.tabLayout_shengzhi);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager_shengzhi);
        initView();
        tableLayout.setupWithViewPager(viewPager);
        return view;
    }
//    void hideKeyBoard() {
//        View view = getActivity().getCurrentFocus();
//        if (view != null) {
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
//    }
//    private void setupToolbar() {
//        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
//        toolbar.setTitle(getResources().getString(R.string.toolbar_name));
//        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
//        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arcbit_menu_white));
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideKeyBoard();
//                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
//                if (drawer.isDrawerOpen(GravityCompat.START)) {
//                    drawer.closeDrawer(GravityCompat.START);
//                } else {
//                    drawer.openDrawer(GravityCompat.START);
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        setupToolbar();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        setupToolbar();
//    }
    private void initView() {
        //初始化Fragments
        ArrayList<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(text_fragment.newInstance());
        //实例化ViewPager的适配器
        ShengzhiFragmentAdapter szf = new ShengzhiFragmentAdapter(getChildFragmentManager(),fragments);
        //ViewPager设置适配器
        viewPager.setAdapter(szf);
    }
}

