package com.onets.wallet.Fragment_Qianbao;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.onets.wallet.Adapter.AndaWalletFunctionAdapter;
import com.onets.wallet.Fragment_Chongzhi.TextImageBean;
import com.onets.wallet.R;
import com.onets.wallet.ui.widget.DividerGridItemDecoration;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/6/14.
 */

public class WalletFunctionFragment extends Fragment {

    private AndaWalletFunctionAdapter functionAdapter;
    private List<TextImageBean> list_function;
    private RecyclerView select_function;
    private PopupWindow popupWindow;
    private float alpha = 1f;
    private Listener listener;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    backgroundAlpha((float) msg.obj);
                    break;
                case 2:
                    backgroundAlpha(1f);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    public static WalletFunctionFragment newInstance() {
        WalletFunctionFragment fragment = new WalletFunctionFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qiangbao_gaikuang, null);
        select_function = (RecyclerView) view.findViewById(R.id.recycle_select_wallet_function);
        list_function = new ArrayList<>();

        list_function.add(new TextImageBean(getString(R.string.coin_payment), R.drawable.transfer_money));
        list_function.add(new TextImageBean(getString(R.string.anda_exchange_transaction), R.drawable.anda_transcation));
        list_function.add(new TextImageBean(getString(R.string.anda_deposits), R.drawable.cunkuan));
        list_function.add(new TextImageBean(getString(R.string.anda_borrowing), R.drawable.borrow_money));
        list_function.add(new TextImageBean(getString(R.string.buy_contract_service), R.drawable.anda_exchange));

        functionAdapter = new AndaWalletFunctionAdapter(getContext(), list_function);
        select_function.setLayoutManager(new GridLayoutManager(getContext(),2));
        select_function.addItemDecoration(new DividerGridItemDecoration(getActivity()));
        select_function.setAdapter(functionAdapter);
        functionAdapter.setItemClickListener(new AndaWalletFunctionAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                if (position != 4) {
//                    new ExplosionField(getContext()).explode(view, null);
//                }
                Toast.makeText(getContext(), "使用" + list_function.get(position).getName() + "功能", Toast.LENGTH_SHORT).show();

                switch (position) {
                    case 0:
                        listener.selectTradeCoin();
                        break;
                    case 1:

                        break;
                    case 2:

                        break;
                    case 3:

                        break;
                    case 4:
                        bottomwindow(view);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (alpha > 0.5f) {
                                    try {
                                        //4是根据弹出动画时间和减少的透明度计算
                                        Thread.sleep(6);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = 1;
                                    //每次减少0.01，精度越高，变暗的效果越流畅
                                    alpha -= 0.01f;
                                    msg.obj = alpha;
                                    mHandler.sendMessage(msg);
                                }
                            }

                        }).start();
                        break;
                    default:
                        Toast.makeText(getContext(), "选择出错", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });


        return view;
    }


    void bottomwindow(View view) {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.contract_service_popup, null);
        popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //点击空白处时，隐藏掉pop窗口
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //添加弹出、弹入的动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.LEFT | Gravity.BOTTOM, 0, -location[1]);
        //添加按键事件监听
        setButtonListeners(layout);
        //添加pop窗口关闭事件，主要是实现关闭时改变背景的透明度
        popupWindow.setOnDismissListener(new poponDismissListener());
        backgroundAlpha(1f);
    }


    private void setButtonListeners(LinearLayout layout) {
        Button ai = (Button) layout.findViewById(R.id.btn_ai_service);
        Button art = (Button) layout.findViewById(R.id.btn_art_service);
        Button bigmsg = (Button) layout.findViewById(R.id.btn_bigmsg_service);
        Button financial = (Button) layout.findViewById(R.id.btn_financial_service);
        Button game = (Button) layout.findViewById(R.id.btn_game_service);
        Button intellctual_property = (Button) layout.findViewById(R.id.btn_intellectual_property_service);
        Button supply_chain = (Button) layout.findViewById(R.id.btn_supply_chain_service);
        Button video = (Button) layout.findViewById(R.id.btn_video_service);


        Button cancel = (Button) layout.findViewById(R.id.cancel);

        ai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        art.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        bigmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        financial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        intellctual_property.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        supply_chain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //在此处添加你的按键处理 xxx
                    popupWindow.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug

        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * 返回或者点击空白位置的时候将背景透明度改回来
     */
    class poponDismissListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //此处while的条件alpha不能<= 否则会出现黑屏
                    while (alpha < 1f) {
                        try {
                            Thread.sleep(6);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("HeadPortrait", "alpha:" + alpha);
                        Message msg = mHandler.obtainMessage();
                        msg.what = 1;
                        alpha += 0.01f;
                        msg.obj = alpha;
                        mHandler.sendMessage(msg);

                        if (alpha >= 0.99f) {
                            mHandler.sendEmptyMessage(2);
                        }
                    }
                }

            }).start();

        }
    }


   public interface Listener{
        void selectTradeCoin();
    }
}
