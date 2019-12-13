package com.onets.wallet.Fragment_Chongzhi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.onets.wallet.Adapter.TextImageAdapter;
import com.onets.wallet.Constants;
import com.onets.wallet.R;
import com.onets.wallet.WalletApplication;
import com.onets.wallet.ui.Exchange;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/15.
 * 充值选择界面
 */

public class RechargeFragment extends Fragment {

    // Fragment管理对象
    private FragmentManager manager;
    private FragmentTransaction fTransaction;
    WalletApplication application;

    ListView lv_choose_minePool;
    TextImageAdapter textImageAdapter;
    List<TextImageBean> list_minePool;

    public static RechargeFragment newInstance() {
        RechargeFragment wakuang = new RechargeFragment();
        return wakuang;
    }

    public static RechargeFragment newInstance(String accountId){
        RechargeFragment fragment = new RechargeFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chongzhi_recharge, null);


//        DbHelper_Workers dbHelperWorkers = new DbHelper_Workers(getActivity());
//        final SQLiteDatabase workers = dbHelperWorkers.getWritableDatabase();
//        kuanggongAddress= (EditText) view.findViewById(R.id.et_kuanggongming);
//        lv_kuanggongMsg= (ListView) view.findViewById(R.id.lv_kuanggong);
//        list_w = new ArrayList<>();
//        adp = new KuanggongAdapter(list_w,getActivity());
//        lv_kuanggongMsg.setAdapter(adp);

        lv_choose_minePool= (ListView) view.findViewById(R.id.choose_minePool);
        list_minePool = new ArrayList<>();

        TextImageBean bean1 = new TextImageBean("比特币 兑换 安达通证", R.drawable.bitcoin);
        TextImageBean bean2 = new TextImageBean("以太坊 兑换 安达通证", R.drawable.ethereum);
        TextImageBean bean3 = new TextImageBean("PayPal 兑换 安达通证", R.drawable.paypal_256px);
        list_minePool.add(bean1);
        list_minePool.add(bean2);
        list_minePool.add(bean3);


        textImageAdapter = new TextImageAdapter(getContext(), list_minePool);
        lv_choose_minePool.setAdapter(textImageAdapter);

        lv_choose_minePool.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), Exchange.class);

                switch (i){
                    case 0://比特币兑换安达币
                        intent.putExtra("Type","BTC");
                        startActivity(intent);
                        break;
                    case 1://以太坊兑换安达币
                        intent.putExtra("Type","ETH");
                        startActivity(intent);
                        break;
                    case 2://paypal兑换安达币
                        intent.putExtra("Type", "PayPal");
                        startActivity(intent);
                        break;
                    default:
                        Toast.makeText(getContext(), "充值" + list_minePool.get(i).getName(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        manager = getFragmentManager();

        return view;
    }

}
