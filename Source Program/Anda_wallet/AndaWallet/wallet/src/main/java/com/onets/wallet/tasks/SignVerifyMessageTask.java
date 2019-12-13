package com.onets.wallet.tasks;

import android.os.AsyncTask;

import com.onets.core.wallet.SignedMessage;
import com.onets.core.wallet.WalletAccount;

import org.acra.ACRA;
import org.bitcoinj.crypto.KeyCrypterException;
import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 * 签名验证消息任务
 */
public abstract class SignVerifyMessageTask extends AsyncTask<SignedMessage, Void, SignedMessage> {
    private final WalletAccount account;
    private final boolean signMessage;
    @Nullable private final CharSequence password;

    /**
     * 签名验证消息任务
     * @param account
     * @param signMessage
     * @param password
     */
    public SignVerifyMessageTask(WalletAccount account, boolean signMessage, @Nullable CharSequence password) {
        this.account = account;
        this.signMessage = signMessage;
        this.password = password;
    }

    /**
     * 返回操作
     * @param params
     * @return
     */
    @Override
    protected SignedMessage doInBackground(SignedMessage... params) {
        SignedMessage message = params[0];

        try {
            if (signMessage) {
                KeyParameter key = null;
                if (account.isEncrypted() && account.getKeyCrypter() != null && password != null) {
                    key = account.getKeyCrypter().deriveKey(password);
                }
                account.signMessage(message, key);
            } else {
                account.verifyMessage(message);
            }
        } catch (KeyCrypterException e) {
            message = new SignedMessage(message, SignedMessage.Status.KeyIsEncrypted);
        } catch (Exception e) {
            // Should not happen
            ACRA.getErrorReporter().handleSilentException(e);
            // Return the message with unknown status
            message = new SignedMessage(message, SignedMessage.Status.Unknown);
        }

        return message;
    }

    /**
     * POST执行
     * @param message
     */
    @Override abstract protected void onPostExecute(SignedMessage message);
}
