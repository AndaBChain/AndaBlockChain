package com.onets.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.onets.wallet.Constants;
import com.onets.wallet.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AndaExchange extends AppCompatActivity {
    @Bind(R.id.wallet__address1)
    EditText bitcoinAddress;
    @Bind(R.id.anda__address1)
    EditText andaAddress;
    @Bind(R.id.send_amount)
    EditText amount;
    @Bind(R.id.coin_type1)
    TextView coinType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anda_exchange);
        ButterKnife.bind(this);

    }
    @OnClick(R.id.exchange_button1)
    public void onClick() {
      //带值跳转比特币交易界面
        Intent intent=new Intent(AndaExchange.this,WalletActivity.class);
        intent.putExtra(Constants.ARG_COIN_TYPE, "Bitcoin");
        intent.putExtra("id", 1);
        intent.putExtra("Type","BTC");
        intent.putExtra("AndaAddress",andaAddress.getText().toString());
        intent.putExtra("Amount",amount.getText().toString());
        intent.putExtra("exchange",true);

        startActivity(intent);


    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=getIntent();
        String type=intent.getStringExtra("Type");
        //coinType.setText(type);
    }
}
