package com.onets.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.onets.wallet.Adapter.TextImageAdapter;
import com.onets.wallet.Fragment_Chongzhi.TextImageBean;
import com.onets.wallet.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 安达服务列表
 */
public class AndaServiceActivity extends AppCompatActivity {

    TextImageAdapter textImageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anda_service);

        //title设置
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_service));

        initView();
    }

    /*地图初始化，服务选项与图片一一对应*/
    private void initView() {
        List<TextImageBean> list = new ArrayList<>();
        ListView list_anda_services = (ListView) findViewById(R.id.list_anda_services);
        //服务名称数组
        String[] beanName = {
                "游戏","电商","ABS","公证","AI",
                "大数据","通证信用卡","艺术作品","金融服务","知识产权",
                "影视作品","供应链回溯","打赏","博彩","其它"
        };
        //服务图片数组
        int[] beanIcon = {
                R.drawable.game,R.drawable.e_ommerce,R.drawable.abs,R.drawable.notary,R.drawable.ai,
                R.drawable.bigdata,R.drawable.token_credit_card,R.drawable.artwork,R.drawable.banking,
                R.drawable.knowledge, R.drawable.video,R.drawable.supplychainback,
                R.drawable.supplychainback,R.drawable.supplychainback,R.drawable.other
        };
        //服务的名称和图片对应绑定
        for (int i = 0; i < beanName.length; i++) {
            TextImageBean list_bean = new TextImageBean(beanName[i],beanIcon[i]);
            list.add(list_bean);
        }

        textImageAdapter = new TextImageAdapter(this,list);
        list_anda_services.setAdapter(textImageAdapter);
        textImageAdapter.setIconSize(R.dimen.service_icon);
        //添加点击事件
        list_anda_services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(AndaServiceActivity.this, list.get(i).getName() + getResources().getString(R.string.service_explain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 添加返回键实现
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                startActivity(new Intent(AndaServiceActivity.this,First_activity.class));
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 物理键返回
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(AndaServiceActivity.this,First_activity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
