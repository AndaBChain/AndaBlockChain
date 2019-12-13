package com.onets.wallet.ui.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.ValueType;
import com.onets.core.util.MonetaryFormat;
import com.onets.wallet.R;
import com.onets.wallet.util.MonetarySpannable;

import org.bitcoinj.core.Coin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 */
public class AmountEditView extends LinearLayout {
    private static final String AMOUNT_EDIT_VIEW_SUPER_STATE = "amount_edit_view_super_state";
    private static final String AMOUNT_EDIT_VIEW_AMOUNT_VALUE = "amount_edit_view_value";
    private static final String AMOUNT_EDIT_VIEW_HINT_VALUE = "amount_edit_view_hint_value";
    private static final String AMOUNT_EDIT_VIEW_TYPE = "amount_edit_view_type";
    private static final String AMOUNT_EDIT_VIEW_TEXT = "amount_edit_view_text";
    private static final String AMOUNT_EDIT_VIEW_AMOUNT_SIGNED = "amount_edit_view_amount_signed";
    private static final String AMOUNT_EDIT_VIEW_FORMAT = "amount_edit_view_format";

    private Listener listener;

    private TextView symbol;
    private EditText amountText;
    private boolean amountSigned = false;
    @Nullable
    private ValueType type;
    @Nullable
    private Value hint;
    private MonetaryFormat format = new MonetaryFormat().noCode();
    private final TextViewListener amountTextListener = new TextViewListener();

    public interface Listener {
        void changed();

        void focusChanged(final boolean hasFocus);
    }

    public AmountEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.amount_edit, this, true);

        LinearLayout layout = (LinearLayout) findViewById(R.id.amount_layout);
        amountText = (EditText) layout.getChildAt(0);
        amountText.addTextChangedListener(amountTextListener);
        amountText.setOnFocusChangeListener(amountTextListener);
        symbol = (TextView) layout.getChildAt(1);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AMOUNT_EDIT_VIEW_SUPER_STATE, super.onSaveInstanceState());
        bundle.putSerializable(AMOUNT_EDIT_VIEW_TYPE, type);
        bundle.putSerializable(AMOUNT_EDIT_VIEW_FORMAT, format);
        bundle.putString(AMOUNT_EDIT_VIEW_TEXT, amountText.getText().toString());
        bundle.putBoolean(AMOUNT_EDIT_VIEW_AMOUNT_SIGNED, amountSigned);
        bundle.putSerializable(AMOUNT_EDIT_VIEW_AMOUNT_VALUE, getAmount());
        bundle.putSerializable(AMOUNT_EDIT_VIEW_HINT_VALUE, hint);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            setType((ValueType) bundle.getSerializable(AMOUNT_EDIT_VIEW_TYPE));
            bundle.putSerializable(AMOUNT_EDIT_VIEW_FORMAT, format);
            amountText.setText(bundle.getString(AMOUNT_EDIT_VIEW_TEXT));
            bundle.putBoolean(AMOUNT_EDIT_VIEW_AMOUNT_SIGNED, amountSigned);
            setAmount((Value) bundle.getSerializable(AMOUNT_EDIT_VIEW_AMOUNT_VALUE), false);
            setHint((Value) bundle.getSerializable(AMOUNT_EDIT_VIEW_HINT_VALUE));
            state = bundle.getParcelable(AMOUNT_EDIT_VIEW_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    public void reset() {
        amountText.setText(null);
        symbol.setText(null);
        type = null;
        hint = null;
    }

    String  TAG ="AmountEditView";
    public void resetType(CoinType type, boolean updateView) {
        if (resetType(type) && updateView) {
            Log.i(TAG, "resetType: ");
            updateAppearance();
        }
    }

    public boolean resetType(final ValueType newType) {
        if (type == null || !type.equals(newType)) {
            type = newType;
            hint = null;
            setFormat(newType.getMonetaryFormat());
            return true;
        } else {
            return false;
        }
    }

    public void setFormat(final MonetaryFormat inputFormat) {
        this.format = inputFormat.noCode();
        Log.i(TAG, "setFormat: ");

        updateAppearance();
    }

    public void setType(@Nullable final ValueType type) {
        this.type = type;

        Log.i(TAG, "setType: ");

        updateAppearance();
    }

    public void setListener(@Nonnull final Listener listener) {
        this.listener = listener;
    }

    public void setHint(@Nullable final Value hint) {
        this.hint = hint;
        Log.i(TAG, "setHint: ");

        updateAppearance();
    }

    public void setAmountSigned(final boolean amountSigned) {
        this.amountSigned = amountSigned;
    }

    public void setSingleLine(boolean isSingleLine) {
        if (isSingleLine) {
            setOrientation(LinearLayout.HORIZONTAL);
        } else {
            setOrientation(LinearLayout.VERTICAL);
        }
    }

    @Nullable
    public Value getAmount() {
        final String str = amountText.getText().toString().trim();
        Value amount = null;

        try {
            if (!str.isEmpty()) {
                if (type != null) {
                    if(type.getName().equals("Ethereum") ){
                        amount = format.parseEth(type, str);
                        Log.i(TAG, "getBigAmount: ------------- : "+amount.getBigValue());

                    }else {
                        amount = format.parse(type, str);
                        Log.i(TAG, "getAmount: ------------- : "+amount.getValue());

                    }
                }
            }
        } catch (final Exception x) { /* ignored */ }

        return amount;
    }

    public void setAmount(@Nullable final Value value, final boolean fireListener) {
        if (!fireListener) amountTextListener.setFire(false);

        if (value != null) {
            amountText.setText(new MonetarySpannable(format, amountSigned, value));
        } else {
            amountText.setText(null);
        }

        if (!fireListener) amountTextListener.setFire(true);
    }

    public String getAmountText() {
        return amountText.getText().toString().trim();
    }

    public TextView getAmountView() {
        return amountText;
    }

    private void updateAppearance() {
        if (type != null) {
            symbol.setText(type.getSymbol());
            symbol.setVisibility(VISIBLE);
        } else {
            symbol.setText(null);
            symbol.setVisibility(GONE);
        }

        if (hint == null) {
            Log.e("---------AmountEditView", "hint: null");
        }
        if (type != null) {
            Log.e("---------AmountEditView", "getName:" + type.getName());

            if (type.getName().equals("Ethereum") || type.getName().equals("AndaBlockChain")  ) {

                final Spannable hintSpannable = new MonetarySpannable(format, amountSigned,
                        hint != null ? hint : Value.valueOfEth(type, "18"));
                amountText.setHint(hintSpannable);
                Log.e("---------AmountEditView", "Ethereum     hintSpannable:" + hintSpannable);

            } else {
                final Spannable hintSpannable = new MonetarySpannable(format, amountSigned,
                        hint != null ? hint : Coin.ZERO);
                amountText.setHint(hintSpannable);
                Log.e("---------AmountEditView", "not  Ethereum  hintSpannable:" + hintSpannable);
            }
        } else {

            final Spannable hintSpannable = new MonetarySpannable(format, amountSigned,
                    hint != null ? hint : Coin.ZERO);
            amountText.setHint(hintSpannable);
            Log.e("---------AmountEditView", "hintSpannable:" + hintSpannable);
        }


    }

    private final class TextViewListener implements TextWatcher, OnFocusChangeListener {
        private boolean fire = true;

        public void setFire(final boolean fire) {
            this.fire = fire;
        }

        @Override
        public void afterTextChanged(final Editable s) {
            // workaround for German keyboards
            final String original = s.toString();
            final String replaced = original.replace(',', '.');
            if (!replaced.equals(original)) {
                s.clear();
                s.append(replaced);
            }
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            if (listener != null && fire) listener.changed();
        }

        @Override
        public void onFocusChange(final View v, final boolean hasFocus) {
            if (!hasFocus) {
                final Value amount = getAmount();
                if (amount != null)
                    setAmount(amount, false);
            }

            if (listener != null && fire) listener.focusChanged(hasFocus);
        }
    }
}
