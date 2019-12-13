package com.onets.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.onets.wallet.Adapter.TextImageAdapter;
import com.onets.wallet.Constants;
import com.onets.wallet.Fragment_Chongzhi.TextImageBean;
import com.onets.wallet.R;
import com.onets.wallet.util.HttpClientPost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*币种挖矿选择：安达链、比特币、以太坊、法定数字货币*/
public class MineActivity extends AppCompatActivity {
    private static final String TAG = "MineActivity";

    TextImageAdapter textImageAdapter;
    List<TextImageBean> list_minePool;
    ListView lv_choose_minePool;//挖矿选择列表
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);

        //设置挖矿界面的title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle(R.string.title_mining);

        //初始化挖矿选择表并填充内容
        list_minePool= new ArrayList<>();
        lv_choose_minePool = findViewById(R.id.choose_minePool);

        TextImageBean bean1 = new TextImageBean(getString(R.string.coin_bitcoin),R.drawable.bitcoin);
        TextImageBean bean3 = new TextImageBean(getString(R.string.coin_andachain),R.drawable.anda);
        list_minePool.add(bean3);
        list_minePool.add(bean1);

        //矿池选择器
        //TODO: 该部分是挖矿，只存在界面，1.0版本里面的功能未添加。
        textImageAdapter =new TextImageAdapter(this,list_minePool);
        lv_choose_minePool.setAdapter(textImageAdapter);
        lv_choose_minePool.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               // new ExplosionField(MineActivity.this).explode(view, null);
                Toast.makeText(MineActivity.this, list_minePool.get(i).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        //测试
        //使用TimerTask实现延迟操作
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                try {
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put("AndaAddress","qnN994o65jEsxZNCngk3zjwumbHyHQ65Wr");
                    map.put("Amount", (long) 20000);
                    map.put("id", "89310722db30cbf8bfb04e85dd375d8a02eea220e7325947e3a8ea18dcf0ed75");
                    map.put("TxHash","89310722db30cbf8bfb04e85dd375d8a02eea220e7325947e3a8ea18dcf0ed75");

                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        Log.d(TAG, Constants.LOG_LABLE + "handleSendConfirmB: Key = " + entry.getKey() + ",Value= " + entry.getValue());
                    }

                    HttpClientPost httpclient = new HttpClientPost();
                    String result_ = httpclient.post(Constants.SERVER_ADDRESS_BITCOIN_NEW_TX,map);
                    Log.d(TAG, Constants.LOG_LABLE + "handleSendConfirmB： result_ "+result_);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 1000);//1秒后执行TimerTask的run方法

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                startActivity(new Intent(MineActivity.this,First_activity.class));
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MineActivity.this,First_activity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
