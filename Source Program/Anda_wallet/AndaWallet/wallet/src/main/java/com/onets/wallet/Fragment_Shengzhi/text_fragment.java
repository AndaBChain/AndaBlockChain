package com.onets.wallet.Fragment_Shengzhi;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onets.wallet.R;


/**
 * Created by Administrator on 2017/6/21.
 */

public class text_fragment extends Fragment {


    public static text_fragment newInstance() {
        Bundle args = new Bundle();
        text_fragment fragment = new text_fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shengzhi_text, null);
        return view;
    }

}

