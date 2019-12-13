package com.onets.wallet.Fragment_Chongzhi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.K_xian.K_adapter.FragmentAdapter;
import com.K_xian.K_fragment.FenshiFragment;
import com.K_xian.K_fragment.KLineFragment;
import com.K_xian.K_view.NoScrollViewPager;
import com.onets.wallet.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/15.
 */

public class GoumaiFragment extends Fragment {
    public static final String[] TITLES = {"分时线", "日K"};
    private TabLayout tabLayout;
    private NoScrollViewPager viewPager;

    public static GoumaiFragment newInstance(){
        GoumaiFragment fragment = new GoumaiFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chongzhi_goumai,null);

        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (NoScrollViewPager) view.findViewById(R.id.viewpager);
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new FenshiFragment());
        fragments.add(new KLineFragment());
        FragmentAdapter adapter = new FragmentAdapter(getChildFragmentManager(), fragments, TITLES);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
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
}
