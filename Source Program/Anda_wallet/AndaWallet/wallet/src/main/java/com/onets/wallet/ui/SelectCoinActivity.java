package com.onets.wallet.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.onets.core.wallet.SpvStroeDown;
import com.onets.core.wallet.Wallet;
import com.onets.core.wallet.WalletAccount;
import com.onets.wallet.Adapter.TextImageAdapter;
import com.onets.wallet.Constants;
import com.onets.wallet.Fragment_Chongzhi.TextImageBean;
import com.onets.wallet.R;
import com.onets.wallet.ui.dialogs.CustomDialog;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

/**
 * 交易选择
 * 虚拟币及法定数字货币列表 建立使用证包
 */
public class SelectCoinActivity extends BaseWalletActivity {
    private static final String TAG = "SelectCoinActivity";

    private AlertDialog.Builder hintBuilder;

    TextImageAdapter textImageAdapter;
    List<TextImageBean> list_tx;
    ListView lv_choose_tx;
    @CheckForNull
    private Wallet wallet;
    private boolean sharedpreference = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_coin);
        wallet = getWalletApplication().getWallet();

        //title设置
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_transaction));

        initView();
    }

    /*加密币列表初始化及列表监听事件*/
    private void initView() {
        list_tx = new ArrayList<>();
        lv_choose_tx = findViewById(R.id.choose_minePool);

        //图片与文字相对应，加入列表 【安达通证 比特币 以太坊 Libra 央行数字货币】
        final TextImageBean beanB = new TextImageBean(getString(R.string.coin_bitcoin), R.drawable.bitcoin);
        TextImageBean beanE = new TextImageBean(getString(R.string.coins_ethereum), R.drawable.ethereum);
        TextImageBean beanA = new TextImageBean(getString(R.string.coin_andachain), R.drawable.anda);
        TextImageBean beanY = new TextImageBean(getString(R.string.coin_cbdc), R.drawable.cbdc);
        TextImageBean beanL = new TextImageBean(getString(R.string.coin_libra), R.drawable.libra);
        list_tx.add(beanA);//安达
        list_tx.add(beanB);//比特
        list_tx.add(beanE);//以太
        list_tx.add(beanL);//libra
        list_tx.add(beanY);//cbdc

        textImageAdapter = new TextImageAdapter(this, list_tx);
        lv_choose_tx.setAdapter(textImageAdapter);

        //加密币选择点击监听事件
        lv_choose_tx.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, final long l) {
                //选择时提示 “建立”or“使用” || “支付”or“交易”
                String positiveText = "建立";
                String negativeText = "使用";

                CustomDialog.Builder builder = new CustomDialog.Builder(SelectCoinActivity.this);

                //选择 “建立”时
                builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //设置你的操作事项 未建立钱包时跳转界面
                        if (getWalletApplication().getWallet() == null) {
                            Log.d(TAG, Constants.LOG_LABLE + "onClick PositiveButton: 首次创建证包");
                            Intent introIntent = new Intent(SelectCoinActivity.this, IntroActivity.class);
                            introIntent.putExtra("SelectCoin", "AndaBlockChain");
                            startActivity(introIntent);
                            finish();
                            return;
                        }

                        //判断是否存在加密币
                        boolean isExistBitcoin = false;
                        boolean isExistEthereum = false;
                        boolean isExistAndachain = false;

                        List<String> exist_account = new ArrayList<>();
                        List<WalletAccount> list = getAllAccounts();
                        for (WalletAccount account : list) {
                            String name = account.getCoinType().getName();
                            exist_account.add(name);
                            if (name.equals("Bitcoin")) {
                                isExistBitcoin = true;
                            } else if (name.equals("Ethereum")) {
                                isExistEthereum = true;
                            } else if (name.equals("AndaBlockChain")) {
                                isExistAndachain = true;
                            }
                        }

                        Intent intent = new Intent(SelectCoinActivity.this, Select_AddCoinActivity.class);
                        switch (i) {
                            case 0://安达链
                                if (!isExistAndachain) {
                                    intent.putExtra("coin_choose", "AndaBlockChain");
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(SelectCoinActivity.this, R.string.wallet_had_created, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 1://比特币
                                if (!isExistBitcoin) {
                                    intent.putExtra("coin_choose", "Bitcoin");
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(SelectCoinActivity.this, R.string.wallet_had_created, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 2://以太坊
                                if (!isExistEthereum) {
                                    intent.putExtra("coin_choose", "Ethereum");
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(SelectCoinActivity.this, R.string.wallet_had_created, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 3://libra
                                noOpenInstruction();
                                break;
                            case 4://CBDC
                                noOpenInstruction();
                                break;
                            default:
                                break;
                        }

                    }
                });

                //选择“使用”时
                builder.setNegativeButton(negativeText,
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                boolean isExistBitcoin = false;
                                boolean isExistEthereum = false;
                                boolean isExistAndachain = false;

                                List<WalletAccount> list = getAllAccounts();
                                for (WalletAccount account : list) {
                                    String name = account.getCoinType().getName();
                                    if (name.equals("Bitcoin")) {
                                        isExistBitcoin = true;
                                    } else if (name.equals("Ethereum")) {
                                        isExistEthereum = true;
                                    } else if (name.equals("AndaBlockChain")) {
                                        isExistAndachain = true;
                                    }
                                }

                                AlertDialog.Builder passDialog = new AlertDialog.Builder(SelectCoinActivity.this);
                                final EditText passEdit = new EditText(SelectCoinActivity.this);
                                //设置为密码输入格式
                                int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                                passEdit.setRawInputType(inputType);
                                passDialog.setTitle("请输入密码：");
                                passDialog.setIcon(android.R.drawable.btn_star);
                                passDialog.setCancelable(false);
                                passDialog.setView(passEdit);

                                switch (i) {
                                    case 0://安达链
                                        if (isExistAndachain) {
                                            passDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String password = passEdit.getText().toString();
                                                    SharedPreferences walletPassword = getSharedPreferences("Anda_password", MODE_PRIVATE);
                                                    String AndaPassword = walletPassword.getString("Anda_password", "");

                                                    if (!password.equals(AndaPassword)){
                                                        Toast.makeText(SelectCoinActivity.this, "密码错误，请重新输入！", Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        Intent intent = new Intent(SelectCoinActivity.this, AndaWalletFunctionActivity.class);
                                                        intent.putExtra(Constants.ARG_COIN_TYPE, "AndaBlockChain");
                                                        startActivity(intent);
                                                        finish();
                                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                    }
                                                }
                                            });
                                            passDialog.show();

                                        } else {
                                            Toast.makeText(SelectCoinActivity.this, "请先建立钱包", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case 1://比特币
                                        if (isExistBitcoin) {
                                            passDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String password = passEdit.getText().toString();
                                                    SharedPreferences walletPassword = getSharedPreferences("Bitcoin_password", MODE_PRIVATE);
                                                    String BitcoinPassword = walletPassword.getString("Bitcoin_password", "");
                                                    if (!password.equals(BitcoinPassword)){
                                                        Toast.makeText(SelectCoinActivity.this, "密码错误，请重新输入！", Toast.LENGTH_SHORT).show();
                                                    }else {

                                                        if(SpvStroeDown.peerGroup == null){
                                                            //启动SPV区块下载
                                                            SpvStroeDown.SPVstoreDown();
                                                        }
                                                        Intent intent = new Intent(SelectCoinActivity.this, WalletActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        intent.putExtra(Constants.ARG_COIN_TYPE, "Bitcoin");
                                                        startActivity(intent);
                                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                                                    }
                                                }
                                            });
                                            passDialog.show();
                                        } else {
                                            Toast.makeText(SelectCoinActivity.this, "请先建立钱包", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case 2://以太坊
                                        if (isExistEthereum) {

                                            passDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String password = passEdit.getText().toString();
                                                    SharedPreferences walletPassword = getSharedPreferences("Ethereum_password", MODE_PRIVATE);
                                                    String EthereumPassword = walletPassword.getString("Ethereum_password", "");
                                                    if (!password.equals(EthereumPassword)){
                                                        Toast.makeText(SelectCoinActivity.this, "密码错误，请重新输入！", Toast.LENGTH_SHORT).show();
                                                    }else {
                                                        Intent intent = new Intent(SelectCoinActivity.this, WalletActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        intent.putExtra(Constants.ARG_COIN_TYPE, "Ethereum");
                                                        startActivity(intent);
                                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                    }
                                                }
                                            });
                                            passDialog.show();

                                        } else {
                                            Toast.makeText(SelectCoinActivity.this, "请先建立钱包", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case 3://libra
                                        noOpenInstruction();
                                        break;
                                    case 4://央行币
                                        noOpenInstruction();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                builder.create().show();

            }
        });

    }

    /**
     * 设置dialog，“暂不开通该功能提示”
     */
    private void noOpenInstruction(){
        hintBuilder = new AlertDialog.Builder(SelectCoinActivity.this)
                .setMessage(R.string.no_open_instruction)
                .setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        hintBuilder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                startActivity(new Intent(SelectCoinActivity.this, First_activity.class));
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
        startActivity(new Intent(SelectCoinActivity.this, First_activity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
