package com.onets.wallet.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.onets.core.network.Nio.NIOSocketClientTest;
import com.onets.test1.NioClient1;
import com.onets.wallet.R;

import java.util.List;

/*进入程序时的首页，选择：挖矿、交易、服务*/
public class First_activity extends AppCompatActivity {
    private static final String TAG = "First_activity";

    private TextView btn_trade, btn_mine, btn_service;

    /**
     * 创建
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_activity);
        getSupportActionBar().hide();

        findViewById();

        //交易选择监听，跳转到币种选择界面
        btn_trade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(First_activity.this, SelectCoinActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        });

        //挖矿选择监听
        btn_mine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(First_activity.this, MineActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        //服务选择监听
        btn_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(First_activity.this, AndaServiceActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

    }

    /*组件初始化*/
    public void findViewById(){
        btn_trade = findViewById(R.id.btn_trade);
        btn_mine = findViewById(R.id.btn_mine);
        btn_service = findViewById(R.id.btn_service);
    }

    /**
     * 双击返回键退出程序
     */
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(First_activity.this, R.string.exit_again, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
