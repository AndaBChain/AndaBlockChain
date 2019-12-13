package com.onets.wallet.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.onets.core.coins.CoinID;
import com.onets.core.coins.CoinType;
import com.onets.core.wallet.Wallet;
import com.onets.core.wallet.WalletAccount;
import com.onets.wallet.Constants;
import com.onets.wallet.R;
import com.onets.wallet.tasks.AddCoinTask;
import com.onets.wallet.ui.dialogs.ConfirmAddCoinUnlockWalletDialog;

import org.bitcoinj.crypto.KeyCrypterException;

import java.util.ArrayList;

import javax.annotation.CheckForNull;

/**
 *
 */
public class Select_AddCoinActivity extends BaseWalletActivity
        implements SelectCoinsFragment.Listener, AddCoinTask.Listener,
        ConfirmAddCoinUnlockWalletDialog.Listener {

    private static final String ADD_COIN_TASK_BUSY_DIALOG_TAG = "add_coin_task_busy_dialog_tag";
    private static final String ADD_COIN_DIALOG_TAG = "ADD_COIN_DIALOG_TAG";

    @CheckForNull
    private Wallet wallet;
    private AddCoinTask addCoinTask;
    private CoinType selectedCoin;
    private String choose_coin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select__add_coin);

        //title标题设置
        getSupportActionBar().setTitle("建立");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if (savedInstanceState == null) {

            Intent intent = getIntent();
            choose_coin = intent.getStringExtra("coin_choose");
            SelectCoinsFragment fragment = new SelectCoinsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("coin_choose",choose_coin);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        wallet = getWalletApplication().getWallet();
    }

    @Override
    public void onCoinSelection(Bundle args) {
        ArrayList<String> ids = args.getStringArrayList(Constants.ARG_MULTIPLE_COIN_IDS);

        // For new we add only one coin at a time
        selectedCoin = CoinID.typeFromId(ids.get(0));

        if (wallet.isAccountExists(selectedCoin)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.coin_already_added_title, selectedCoin.getName()))
                    .setMessage(R.string.coin_already_added)
                    .setPositiveButton(R.string.button_ok, null)
                    .create().show();
            return;
        }
        showAddCoinDialog();
    }

    private void showAddCoinDialog() {
        Dialogs.dismissAllowingStateLoss(getFM(), ADD_COIN_DIALOG_TAG);
        ConfirmAddCoinUnlockWalletDialog.getInstance(selectedCoin, true)
                .show(getFM(), ADD_COIN_DIALOG_TAG);
    }

    @Override
    public void addCoin(CoinType type, String description, CharSequence password) {
        if (type != null && addCoinTask == null) {
            addCoinTask = new AddCoinTask(this, type, wallet, description, password);
            addCoinTask.execute();
        }
    }

    @Override
    public void onAddCoinTaskStarted() {
        Dialogs.ProgressDialogFragment.show(getSupportFragmentManager(),
                getString(R.string.adding_coin_working, selectedCoin.getName()),
                ADD_COIN_TASK_BUSY_DIALOG_TAG);
    }

    @Override
    public void onAddCoinTaskFinished(Exception error, WalletAccount newAccount) {
        if (Dialogs.dismissAllowingStateLoss(getSupportFragmentManager(), ADD_COIN_TASK_BUSY_DIALOG_TAG))
            return;
        addCoinTask = null;
        final Intent result = new Intent();
        if (error != null) {
            if (error instanceof KeyCrypterException) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.unlocking_wallet_error_title))
                        .setMessage(R.string.unlocking_wallet_error_detail)
                        .setPositiveButton(R.string.button_retry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showAddCoinDialog();
                            }
                        })
                        .setNegativeButton(R.string.button_cancel, null)
                        .create().show();
            } else {
                String message = getResources().getString(R.string.add_coin_error,
                        selectedCoin.getName(), error.getMessage());
              //  Toast.makeText(Select_AddCoinActivity.this, message, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED, result);
                finish();
            }
        } else {
            result.putExtra(Constants.ARG_ACCOUNT_ID, newAccount.getId());
            result.setClass(Select_AddCoinActivity.this, AndaWalletFunctionActivity.class);
            startActivity(result);
            finish();
        }

    }


    /**
     * 返回键，返回到上一界面
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                startActivity(new Intent(Select_AddCoinActivity.this,SelectCoinActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

