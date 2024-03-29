package com.onets.wallet.ui.widget;

/*
 * Copyright 2013-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import android.content.Context;
import android.graphics.Paint;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;

import com.onets.core.coins.Value;
import com.onets.core.coins.ValueType;
import com.onets.core.util.MonetaryFormat;
import com.onets.wallet.Constants;
import com.onets.wallet.R;
import com.onets.wallet.util.MonetarySpannable;

import javax.annotation.Nonnull;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 */
public final class CurrencyTextView extends android.support.v7.widget.AppCompatTextView {
    private ValueType type = null;
    private Value amount = null;
    private MonetaryFormat format = null;
    private boolean alwaysSigned = false;
    private RelativeSizeSpan prefixRelativeSizeSpan = null;
    private ForegroundColorSpan prefixColorSpan = null;
    private RelativeSizeSpan insignificantRelativeSizeSpan = null;

    public CurrencyTextView(final Context context) {
        super(context);
    }

    public CurrencyTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAmount(@Nonnull final Value amount) {
        this.amount = amount;
        this.type = amount.type;
        if (format == null) format = type.getMonetaryFormat().noCode();
        updateView();
    }

    public void setFormat(@Nonnull final MonetaryFormat format) {
        this.format = format.codeSeparator(Constants.CHAR_HAIR_SPACE);
        updateView();
    }

    public void setAlwaysSigned(final boolean alwaysSigned) {
        this.alwaysSigned = alwaysSigned;
        updateView();
    }

    public void setStrikeThru(final boolean strikeThru) {
        if (strikeThru)
            setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            setPaintFlags(getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void setInsignificantRelativeSize(final float insignificantRelativeSize) {
        if (insignificantRelativeSize != 1) {
            this.prefixRelativeSizeSpan = new RelativeSizeSpan(insignificantRelativeSize);
            this.insignificantRelativeSizeSpan = new RelativeSizeSpan(insignificantRelativeSize);
        } else {
            this.prefixRelativeSizeSpan = null;
            this.insignificantRelativeSizeSpan = null;
        }
    }

    public void setPrefixColor(final int prefixColor) {
        this.prefixColorSpan = new ForegroundColorSpan(prefixColor);
        updateView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setPrefixColor(getResources().getColor(R.color.fg_less_significant));
        setInsignificantRelativeSize(0.85f);
        setSingleLine();
    }

    private void updateView() {
        final MonetarySpannable text;

        //第四个参数只为区分币种类型，不做实际用途
        if (amount != null) {
//            text = new MonetarySpannable(format, alwaysSigned, amount, type)
//                    .applyMarkup(prefixRelativeSizeSpan, prefixColorSpan, insignificantRelativeSizeSpan);
            if(type.getName().equals("Ethereum")||type.getName().equals("AndaBlockChain")) {
                text = new MonetarySpannable(format, alwaysSigned, amount, true);
            }else if(type.getName().equals("Ripple") ) {
                text = new MonetarySpannable(format, alwaysSigned, amount,0);
            }else {
                text = new MonetarySpannable(format, alwaysSigned, amount);
            }
        } else {
            text = null;
        }

        setText(text);
    }
}
