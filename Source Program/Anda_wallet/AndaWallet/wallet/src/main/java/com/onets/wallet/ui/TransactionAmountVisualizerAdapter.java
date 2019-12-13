package com.onets.wallet.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.families.AndaFamily;
import com.onets.core.coins.families.EthereumFamily;
import com.onets.core.coins.families.RippleFamily;
import com.onets.core.util.GenericUtils;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractTransaction.AbstractOutput;
import com.onets.core.wallet.AbstractWallet;
import com.onets.wallet.R;
import com.onets.wallet.ui.widget.SendOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yu K.Q.
 */
public class TransactionAmountVisualizerAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;

    private final AbstractWallet pocket;
    private boolean isSending;
    private CoinType type;
    private String symbol;
    private List<AbstractOutput> outputs;
    private boolean hasFee;
    private Value feeAmount;

    private int itemCount;

    public TransactionAmountVisualizerAdapter(final Context context, final AbstractWallet walletPocket) {
        this.context = context.getApplicationContext();
        inflater = LayoutInflater.from(context);
        pocket = walletPocket;
        type = pocket.getCoinType();
        symbol = type.getSymbol();
        outputs = new ArrayList<>();
    }

    public void setTransaction(AbstractTransaction tx) {
        outputs.clear();
        final Value value = tx.getValue(pocket);
        isSending = value.signum() < 0;
        // if sending and all the outputs point inside the current pocket it is an internal transfer
        boolean isInternalTransfer = isSending;

        for (AbstractOutput output : tx.getSentTo()) {
            if (isSending) {
                // When sending hide change outputs
                if (pocket.isAddressMine(output.getAddress())) continue;
                isInternalTransfer = false;
                outputs.add(output);
            } else {
                boolean isReceived = pocket.isAddressMine(output.getAddress());
                if (pocket.getCoinType() instanceof EthereumFamily) {
                    if(isReceived) {
                        outputs.add(new AbstractOutput(tx.getReceivedFrom().get(0), tx.getValue(pocket)));
                        isSending = false;
                    }
                }else if (pocket.getCoinType() instanceof AndaFamily) {
                    Log.i(TAG, "setTransaction: output.getAddress:"+output.getAddress());
                    if(isReceived) {
                        outputs.add(new AbstractOutput(tx.getReceivedFrom().get(0), tx.getValue(pocket)));
                        isSending = false;

                    }
                }else if (pocket.getCoinType() instanceof RippleFamily) {
                    Log.i(TAG, "setTransaction: output.getAddress:"+output.getAddress());
                    if(isReceived) {
                        outputs.add(new AbstractOutput(tx.getReceivedFrom().get(0), tx.getValue(pocket)));
                        isSending = false;

                    }
                }
                // When receiving hide outputs that are not ours
                if (pocket.isAddressMine(output.getAddress())) continue;
                outputs.add(new AbstractOutput(tx.getSentTo().get(0).getAddress(), tx.getValue(pocket)));
                isSending = true;

            }

        }

        feeAmount = tx.getFee();
        hasFee = feeAmount != null && !feeAmount.isZero();

        itemCount = isInternalTransfer ? 1 : outputs.size();
        itemCount += hasFee ? 1 : 0;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemCount;
    }

    @Override
    public AbstractOutput getItem(int position) {
        if (position < outputs.size()) {
            return outputs.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        if (row == null) {
            row = inflater.inflate(R.layout.transaction_details_output_row, null);

            ((SendOutput) row).setSendLabel(context.getString(R.string.sent));
            ((SendOutput) row).setReceiveLabel(context.getString(R.string.received));
        }

        final SendOutput output = (SendOutput) row;
        final AbstractOutput txo = getItem(position);

        if (txo == null) {
            if (position == 0) {
                output.setLabel(context.getString(R.string.internal_transfer));
                output.setSending(isSending);
                output.setAmount(null);
                output.setSymbol(null);
            } else if (hasFee) {
                if(type.getName().equals("Ethereum") || type.getName().equals("AndaBlockChain")){
                    output.setAmount(GenericUtils.formatValue(feeAmount));
                }else {
                    output.setAmount(GenericUtils.formatCoinValue(type, feeAmount));
                }
                output.setSymbol(symbol);
                output.setIsFee(true);
            } else { // Should not happen
                output.setLabel("???");
                output.setAmount(null);
                output.setSymbol(null);
            }
        } else {
            Value outputAmount = txo.getValue();
            if(type.getName().equals("Ethereum") || type.getName().equals("AndaBlockChain")){
                output.setAmount(GenericUtils.formatValue(outputAmount));
            }else {
                output.setAmount(GenericUtils.formatCoinValue(type, outputAmount));
            }
            output.setSending(isSending);
            output.setSymbol(symbol);
            output.setLabelAndAddress(txo.getAddress());

        }

        return row;
    }

    String TAG = "TransactionAmountVisualizerAdapter";
}
