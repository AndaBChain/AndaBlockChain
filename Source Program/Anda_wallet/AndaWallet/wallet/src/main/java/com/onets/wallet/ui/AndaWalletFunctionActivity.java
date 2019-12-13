package com.onets.wallet.ui;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.onets.wallet.Adapter.AndaWalletFunctionAdapter;
import com.onets.wallet.Constants;
import com.onets.wallet.Fragment_Chongzhi.TextImageBean;
import com.onets.wallet.R;
import com.onets.wallet.ui.widget.DividerGridItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * 安达钱包Activity
 */
public class AndaWalletFunctionActivity extends AppCompatActivity {
    private static final String TAG = "AndaWalletFunctionActiv";

    private AndaWalletFunctionAdapter functionAdapter;
    private List<TextImageBean> list_function;
    private RecyclerView select_function;
    private PopupWindow popupWindow;
    private float alpha = 1f;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_function);

        getSupportActionBar().setTitle("证包使用");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        select_function = (RecyclerView) findViewById(R.id.recycle_select_wallet_function);
        list_function = new ArrayList<>();

        //支付
        list_function.add(new TextImageBean(getString(R.string.coin_payment), R.drawable.transfer_money));
        //交易
        list_function.add(new TextImageBean(getString(R.string.anda_exchange_transaction), R.drawable.anda_transcation));
        //储蓄
        list_function.add(new TextImageBean(getString(R.string.anda_deposits), R.drawable.cunkuan));
        //借贷
        list_function.add(new TextImageBean(getString(R.string.anda_borrowing), R.drawable.borrow_money));
        //兑换BTC
        list_function.add(new TextImageBean(getString(R.string.anda_exchange_bitcoin), R.drawable.anda_exchange));
        //兑换ETC
        list_function.add(new TextImageBean(getString(R.string.anda_exchange_ether), R.drawable.anda_exchange));
        //兑换法定数字货币
        list_function.add(new TextImageBean(getString(R.string.anda_exchange_legaltender), R.drawable.anda_exchange));

        functionAdapter = new AndaWalletFunctionAdapter(AndaWalletFunctionActivity.this, list_function);
        select_function.setLayoutManager(new GridLayoutManager(this, 2));
        select_function.addItemDecoration(new DividerGridItemDecoration(this));
        select_function.setAdapter(functionAdapter);

        final Intent andaToken = getIntent();
        functionAdapter.setItemClickListener(new AndaWalletFunctionAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position) {
                    case 0://支付
                        Intent intent = new Intent(AndaWalletFunctionActivity.this, WalletActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.ARG_COIN_TYPE, andaToken.getStringExtra(Constants.ARG_COIN_TYPE));
                        startActivity(intent);
                        break;
                    case 1://交易
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
                    case 2://储蓄
                        Toast.makeText(AndaWalletFunctionActivity.this, "使用" + list_function.get(position).getName() , Toast.LENGTH_SHORT).show();
                        break;
                    case 3://借贷
                        Toast.makeText(AndaWalletFunctionActivity.this, "使用" + list_function.get(position).getName() , Toast.LENGTH_SHORT).show();
                        break;
                    case 4://兑换BTC
                        Toast.makeText(AndaWalletFunctionActivity.this, "使用安达通证" + list_function.get(position).getName(), Toast.LENGTH_SHORT).show();
                        break;
                    case 5://兑换ETH
                        Toast.makeText(AndaWalletFunctionActivity.this, "使用安达通证" + list_function.get(position).getName() , Toast.LENGTH_SHORT).show();
                        break;
                    case 6://兑换法定货币
                        Toast.makeText(AndaWalletFunctionActivity.this, "使用安达通证" + list_function.get(position).getName(), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });


    }

    void bottomwindow(View view) {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.contract_service_popup, null);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                startActivity(new Intent(AndaWalletFunctionActivity.this, SelectCoinActivity.class));
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 服务部分（购买契约的事件）
     * @param layout
     */
    private void setButtonListeners(LinearLayout layout) {
        Button ai = (Button) layout.findViewById(R.id.btn_ai_service);
        Button art = (Button) layout.findViewById(R.id.btn_art_service);
        Button bigmsg = (Button) layout.findViewById(R.id.btn_bigmsg_service);
        final Button financial = (Button) layout.findViewById(R.id.btn_financial_service);
        final Button game = (Button) layout.findViewById(R.id.btn_game_service);
        Button intellctual_property = (Button) layout.findViewById(R.id.btn_intellectual_property_service);
        Button supply_chain = (Button) layout.findViewById(R.id.btn_supply_chain_service);
        Button video = (Button) layout.findViewById(R.id.btn_video_service);

        Button other = (Button) layout.findViewById(R.id.btn_other_service);

        Button cancel = (Button) layout.findViewById(R.id.cancel);

        ai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //TODO：在此处添加你的按键处理 AI
                    popupWindow.dismiss();
                }
            }
        });

        art.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //TODO：在此处添加你的按键处理 ART
                    popupWindow.dismiss();
                }
            }
        });

        bigmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //TODO：在此处添加你的按键处理 大数据
                    popupWindow.dismiss();
                }
            }
        });

        financial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //TODO：在此处添加你的按键处理 financial
                    popupWindow.dismiss();
                }
            }
        });

        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //TODO：在此处添加你的按键处理 game
                    popupWindow.dismiss();
                }
            }
        });

        intellctual_property.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //TODO:在此处添加你的按键处理 intellctual_property
                    popupWindow.dismiss();
                }
            }
        });

        supply_chain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //todo:在此处添加你的按键处理 supply_chain
                    popupWindow.dismiss();
                }
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    //todo:在此处添加你的按键处理 video
                    popupWindow.dismiss();
                }
            }
        });

        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
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
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug

        getWindow().setAttributes(lp);
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AndaWalletFunctionActivity.this, SelectCoinActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}
