package com.onets.wallet.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.onets.core.coins.CoinType;
import com.onets.core.wallet.Wallet;
import com.onets.core.wallet.WalletAccount;

import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 */
public final class AddCoinTask  extends AsyncTask<Void, Void, Void> {
    private final Listener listener;
    protected final CoinType type;
    private final Wallet wallet;
    @Nullable
    private final String description;
    @Nullable private final CharSequence password;
    private WalletAccount newAccount;
    private Exception exception;

    public interface Listener {
        void onAddCoinTaskStarted();
        void onAddCoinTaskFinished(Exception error, WalletAccount newAccount);
    }

    public AddCoinTask(Listener listener, CoinType type, Wallet wallet, @Nullable String description, @Nullable CharSequence password) {
        this.listener = listener;
        this.type = type;
        this.wallet = wallet;
        this.description = description;
        this.password = password;
    }

    @Override
    protected void onPreExecute() {
        listener.onAddCoinTaskStarted();
    }


    private static final String TAG = "AddCoinTask";

    @Override
    protected Void doInBackground(Void... params) {
        KeyParameter key = null;
        exception = null;
        try {

            if (wallet.isEncrypted() && wallet.getKeyCrypter() != null) {
                key = wallet.getKeyCrypter().deriveKey(password);
            }
            Log.d(TAG, " 2 -------20189713928 " + key);
            newAccount = wallet.createAccount(type, true, key);
            Log.i(TAG, "doInBackground:   5 ");

            if (description != null && !description.trim().isEmpty()) {
                Log.i(TAG, "doInBackground:   6 ");

                newAccount.setDescription(description);
                Log.i(TAG, "doInBackground:   7 ");

            }
            Log.i(TAG, "doInBackground:   8 ");

            wallet.saveNow();
            Log.i(TAG, "doInBackground:   9 ");

        } catch (Exception e) {
            exception = e;
        }

        return null;
    }

    @Override
    final protected void onPostExecute(Void aVoid) {
        listener.onAddCoinTaskFinished(exception, newAccount);
    }
}
