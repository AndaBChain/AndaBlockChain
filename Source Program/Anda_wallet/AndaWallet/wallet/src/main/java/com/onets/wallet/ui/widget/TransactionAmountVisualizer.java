package com.onets.wallet.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.messages.TxMessage;
import com.onets.core.util.ExchangeRate;
import com.onets.core.util.GenericUtils;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractTransaction.AbstractOutput;
import com.onets.core.wallet.AbstractWallet;
import com.onets.wallet.R;

import java.util.List;

import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 *
 * TODO TransactionAmountVisualizerAdapter does a similar function, keep only one
 */
public class TransactionAmountVisualizer extends LinearLayout {

    private final SendOutput output;
    private final SendOutput fee;
    private final TextView txMessageLabel;
    private final TextView txMessage;
    private Value outputAmount;
    private Value feeAmount;
    private boolean isSending;

    private AbstractAddress address;
    private CoinType type;

    public TransactionAmountVisualizer(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.transaction_amount_visualizer, this, true);

        output = (SendOutput) findViewById(R.id.transaction_output);
        output.setVisibility(View.GONE);
        fee = (SendOutput) findViewById(R.id.transaction_fee);
        fee.setVisibility(View.GONE);
        txMessageLabel = (TextView) findViewById(R.id.tx_message_label);
        txMessage = (TextView) findViewById(R.id.tx_message);

        if (isInEditMode()) {
            output.setVisibility(View.VISIBLE);
            fee.setVisibility(View.VISIBLE);
        }
    }

    private String TAG = "------------------MakeTransactionFragment";
    public void setTransaction(@Nullable AbstractWallet pocket, AbstractTransaction tx) {
        type = tx.getType();
        String symbol = type.getSymbol();

        final Value value = pocket != null ? tx.getValue(pocket) : type.value(0);
        if(pocket != null ){
            isSending  = value.signum() < 0;
            Log.i(TAG, "setTransaction: pocket != null    isSending :"+isSending);

        }else {
            Log.i(TAG, "setTransaction: isSending   true ");

            isSending = true;
        }

        // if sending and all the outputs point inside the current pocket. If received
        boolean isInternalTransfer = isSending;
        output.setVisibility(View.VISIBLE);
        List<AbstractOutput> outputs = tx.getSentTo();
        for (AbstractOutput txo : outputs) {
            if (isSending) {
                if (pocket != null && pocket.isAddressMine(txo.getAddress())) continue;
                isInternalTransfer = false;
            } else {
                if(type.getName().equals("Ethereum") || type.getName().equals("AndaBlockChain")){
                    if (pocket != null && pocket.isAddressMine(txo.getAddress())) continue;
                }else {
                    if (pocket != null && !pocket.isAddressMine(txo.getAddress())) continue;
                }
            }

            // TODO support more than one output
            outputAmount = txo.getValue();
            output.setAmount(GenericUtils.formatValue(outputAmount));
            output.setSymbol(symbol);
            address = txo.getAddress();
            output.setLabelAndAddress(address);
            break; // TODO remove when supporting more than one output
        }

        if (isInternalTransfer) {
            output.setLabel(getResources().getString(R.string.internal_transfer));
        }

        output.setSending(isSending);

        feeAmount = tx.getFee();
        if (isSending && feeAmount != null && !feeAmount.isZero()) {
            fee.setVisibility(View.VISIBLE);
            fee.setAmount(GenericUtils.formatCoinValue(type, feeAmount));
            fee.setSymbol(symbol);
        }

        if (type.canHandleMessages()) {
            setMessage(type.getMessagesFactory().extractPublicMessage(tx));
        }
    }

    public void setTransactionEth(@Nullable AbstractWallet pocket, AbstractTransaction tx,boolean isSend) {
        type = tx.getType();
        String symbol = type.getSymbol();

        if(pocket != null ){
            isSending  = isSend;
        }else {
            isSending = true;
        }

        // if sending and all the outputs point inside the current pocket. If received
        boolean isInternalTransfer = isSending;
        output.setVisibility(View.VISIBLE);
        List<AbstractOutput> outputs = tx.getSentTo();
        for (AbstractOutput txo : outputs) {
            if (isSending) {
                if (pocket != null && pocket.isAddressMine(txo.getAddress())) continue;
                isInternalTransfer = false;
            } else {
                if (pocket != null && !pocket.isAddressMine(txo.getAddress())) continue;
            }

            // TODO support more than one output
            outputAmount = txo.getValue();
            output.setAmount(GenericUtils.formatValue(outputAmount));
            output.setSymbol(symbol);
            address = txo.getAddress();
            output.setLabelAndAddress(address);
            break; // TODO remove when supporting more than one output
        }

        if (isInternalTransfer) {
            output.setLabel(getResources().getString(R.string.internal_transfer));
        }

        output.setSending(isSending);

        feeAmount = tx.getFee();
        if (isSending && feeAmount != null && !feeAmount.isZero()) {
            fee.setVisibility(View.VISIBLE);
            fee.setAmount(GenericUtils.formatCoinValue(type, feeAmount));
            fee.setSymbol(symbol);
        }

        if (type.canHandleMessages()) {
            setMessage(type.getMessagesFactory().extractPublicMessage(tx));
        }
    }

    private void setMessage(@Nullable TxMessage message) {
        if (message != null) {
            switch (message.getType()) {
                case PRIVATE:
                    txMessageLabel.setText(R.string.tx_message_private);
                    break;
                case PUBLIC:
                    txMessageLabel.setText(R.string.tx_message_public);
                    break;
            }
            txMessageLabel.setVisibility(VISIBLE);

            txMessage.setText(message.toString());
            txMessage.setVisibility(VISIBLE);
        }
    }

    public void setExchangeRate(ExchangeRate rate) {
        if (outputAmount != null) {
            Value fiatAmount = rate.convert(type, outputAmount.toBitCoin());
            output.setAmountLocal(GenericUtils.formatFiatValue(fiatAmount));
            output.setSymbolLocal(fiatAmount.type.getSymbol());
        }

        if (isSending && feeAmount != null) {
            Value fiatAmount = rate.convert(type, feeAmount.toBitCoin());
            fee.setAmountLocal(GenericUtils.formatFiatValue(fiatAmount));
            fee.setSymbolLocal(fiatAmount.type.getSymbol());
        }
    }

    /**
     * Hide the output address and label. Useful when we are exchanging, where the send address is
     * not important to the user.
     */
    public void hideAddresses() {
        output.hideLabelAndAddress();
    }

    public void resetLabels() {
        output.setLabelAndAddress(address);
    }

    public List<SendOutput> getOutputs() {
        return ImmutableList.of(output);
    }
}
