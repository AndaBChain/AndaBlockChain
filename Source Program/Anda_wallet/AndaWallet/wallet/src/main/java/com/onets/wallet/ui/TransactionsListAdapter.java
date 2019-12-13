package com.onets.wallet.ui;

/*
 * Copyright 2011-2014 the original author or authors.
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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.families.Families;
import com.onets.core.util.GenericUtils;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractWallet;
import com.onets.wallet.AddressBookProvider;
import com.onets.wallet.R;
import com.onets.wallet.ui.widget.CurrencyTextView;
import com.onets.wallet.util.Fonts;
import com.onets.wallet.util.TimeUtils;
import com.onets.wallet.util.WalletUtils;

import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * 交易列表适配器
 * @author Yu K.Q.
 */
public class TransactionsListAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final AbstractWallet walletPocket;//钱包

    private final List<AbstractTransaction> transactions = new ArrayList<>();
    private final Resources res;
    private int precision = 0;
    private int shift = 0;
    private boolean showEmptyText = false;//显示空白文本

    private final int colorSignificant;//显著重要对应的颜色
    private final int colorLessSignificant;//比较重要对应的颜色
    private final int colorInsignificant;//不重要对应的颜色
    private final int colorError;//错误对应的颜色
    private final int colorCircularBuilding = Color.parseColor("#44ff44");
    private final String minedTitle;//“挖矿”标题
    private final String fontIconMined;//“挖矿”字体图标
    private final String sentToTitle;//”目的地址“标题
    private final String fontIconSentTo;//“目的地址”字体图标
    private final String receivedWithTitle;//“源地址”标题
    private final String receivedFromTitle;
    private final String fontIconReceivedWith;//“源地址”字体图标

    private final Map<AbstractAddress, String> labelCache = new HashMap<>();//标签缓存
    private final static Object CACHE_NULL_MARKER = "";//空标记缓存

    private static final String CONFIDENCE_SYMBOL_DEAD = "\u271D"; // latin cross
    private static final String CONFIDENCE_SYMBOL_UNKNOWN = "?";

    private static final int VIEW_TYPE_TRANSACTION = 0;//交易视图类型

    @Deprecated // TODO change AbstractWallet to WalletAccount
    public TransactionsListAdapter(final Context context, @Nonnull final AbstractWallet walletPocket) {
        this.context = context.getApplicationContext();
        inflater = LayoutInflater.from(context);

        this.walletPocket = walletPocket;

        //资源赋值
        res = context.getResources();
        colorSignificant = res.getColor(R.color.gray_87_text);
        colorLessSignificant = res.getColor(R.color.gray_54_sec_text_icons);
        colorInsignificant = res.getColor(R.color.gray_26_hint_text);
        colorError = res.getColor(R.color.fg_error);
        minedTitle = res.getString(R.string.wallet_transactions_coinbase);
        fontIconMined = res.getString(R.string.font_icon_mining);
        sentToTitle = res.getString(R.string.sent_to);
        fontIconSentTo = res.getString(R.string.font_icon_send_coins);
        receivedWithTitle = res.getString(R.string.received_with);
        receivedFromTitle = res.getString(R.string.received_from);
        fontIconReceivedWith = res.getString(R.string.font_icon_receive_coins);
    }

    /**
     * 精度设置
     * @param precision
     * @param shift
     */
    public void setPrecision(final int precision, final int shift) {
        this.precision = precision;
        this.shift = shift;

        //底层数据改变，通知刷新
        notifyDataSetChanged();
    }

    /**
     * 清空交易列表
     */
    public void clear() {
        transactions.clear();

        notifyDataSetChanged();
    }

    /*public void replace(@Nonnull final org.bitcoinj.core.Transaction tx) {
        transactions.clear();
        transactions.add(new BitTransaction(tx));

        notifyDataSetChanged();
    }

    public void replace(@Nonnull final Transaction tx) {
        transactions.clear();
        transactions.add(new NxtTransaction(tx));

        notifyDataSetChanged();
    }*/

    /**
     * 清空交易列表并重新添加一条交易记录
     * @param tx
     */
    public void replace(@Nonnull final AbstractTransaction tx) {
        transactions.clear();
        transactions.add(tx);

        notifyDataSetChanged();
    }

    /**
     * 清空交易列表，添加交易列表
     * @param transactions
     */
    public void replace(@Nonnull final Collection<AbstractTransaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);

        showEmptyText = true;

        notifyDataSetChanged();
    }

    /**
     * 判断是否为空
     * @return
     */
    @Override
    public boolean isEmpty() {
        return showEmptyText && super.isEmpty();
    }

    /**
     * 获取金额
     * @return
     */
    @Override
    public int getCount() {
        return transactions.size();
    }

    /**
     * 获取列表中指定位置的交易
     * @param position
     * @return
     */
    @Override
    public AbstractTransaction getItem(final int position) {
        if (position == transactions.size())
            return null;

        return transactions.get(position);
    }

    /**
     * 获取列表中知道你个位置的ID
     * @param position
     * @return
     */
    @Override
    public long getItemId(final int position) {
        if (position == transactions.size())
            return 0;

        return WalletUtils.longHash(transactions.get(position).getHashBytes());
    }

    /*是否有稳定ID，返回true*/
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * 获取视图
     * @param position
     * @param row
     * @param parent
     * @return
     */
    @Override
    public View getView(final int position, View row, final ViewGroup parent) {
        final int type = getItemViewType(position);

        if (type == VIEW_TYPE_TRANSACTION) {//type == 0
            if (row == null)
                row = inflater.inflate(R.layout.transaction_row, null);

            final AbstractTransaction tx = getItem(position);
            bindView(row, tx);
        } else {
            throw new IllegalStateException("unknown type: " + type);
        }

        return row;
    }

    /**
     * 绑定视图
     * @param row
     * @param tx
     */
    public void bindView(@Nonnull final View row, @Nonnull final AbstractTransaction tx) {
        //从当前获取资源
        Resources res = context.getResources();
        //获取交易confidence类型
        final ConfidenceType confidenceType = tx.getConfidenceType();
        //交易是否来源于自己
        final boolean isOwn = tx.getSource().equals(TransactionConfidence.Source.SELF);
//      final boolean isInternal = WalletUtils.isInternal(tx);

        //从钱包账户中获取交易的金额
        final Value value = tx.getValue(walletPocket);
        //交易值小于0，是发送
        final boolean sent = value.signum() < 0;
        Log.i(TAG, "bindView: -----signum  :" + value.signum() + "   |   sent  :" + sent);

        //获取当前账户的钱包类型
        final CoinType type = walletPocket.getCoinType();

        // TODO set colors as theme, not here in code

        //控件资源绑定
        final TextView rowDirectionText = (TextView) row.findViewById(R.id.transaction_row_direction_text);
        final TextView rowDirectionFontIcon = (TextView) row.findViewById(R.id.transaction_row_direction_font_icon);
        Fonts.setTypeface(rowDirectionFontIcon, Fonts.Font.OPENWALLET_FONT_ICONS);
        final TextView rowConfirmationsFontIcon = (TextView) row.findViewById(R.id.transaction_row_confirmations_font_icon);
        Fonts.setTypeface(rowConfirmationsFontIcon, Fonts.Font.OPENWALLET_FONT_ICONS);
        final TextView rowMessageFontIcon = (TextView) row.findViewById(R.id.transaction_row_message_font_icon);
        Fonts.setTypeface(rowMessageFontIcon, Fonts.Font.OPENWALLET_FONT_ICONS);
        final TextView rowDate = (TextView) row.findViewById(R.id.transaction_row_time);
        final TextView rowLabel = (TextView) row.findViewById(R.id.transaction_row_label);
        final TextView rowAddress = (TextView) row.findViewById(R.id.transaction_row_address);
        final CurrencyTextView rowValue = (CurrencyTextView) row.findViewById(R.id.transaction_row_value);

        // 根据confidence的类型设置视图颜色背景等外观
        if (confidenceType == ConfidenceType.PENDING) {
            rowLabel.setTextColor(colorInsignificant);
            rowValue.setTextColor(colorInsignificant);
            rowDirectionText.setTextColor(colorInsignificant);
            rowDirectionFontIcon.setTextColor(colorInsignificant);
            rowDirectionFontIcon.setBackgroundResource(R.drawable.transaction_row_circle_bg_pending);
        } else if (confidenceType == ConfidenceType.BUILDING) {
            rowLabel.setTextColor(colorSignificant);
            rowValue.setTextColor(colorSignificant);
            rowDirectionText.setTextColor(colorLessSignificant);
            rowDirectionFontIcon.setTextColor(colorLessSignificant);
            if (value.isNegative()) {
                rowDirectionFontIcon.setBackgroundResource(R.drawable.transaction_row_circle_bg_send);
                rowValue.setTextColor(res.getColor(R.color.send_color_fg));
            } else {
                rowDirectionFontIcon.setBackgroundResource(R.drawable.transaction_row_circle_bg_receive);
                rowValue.setTextColor(res.getColor(R.color.receive_color_fg));
            }
        } else if (confidenceType == ConfidenceType.DEAD) {
            rowLabel.setTextColor(colorSignificant);
            rowValue.setTextColor(colorSignificant);
            Fonts.strikeThrough(rowLabel);
            Fonts.strikeThrough(rowValue);
        } else {
            rowDirectionText.setTextColor(colorError);
            rowLabel.setTextColor(colorInsignificant);
            rowValue.setTextColor(colorInsignificant);
            rowDirectionFontIcon.setTextColor(colorInsignificant);
            rowDirectionFontIcon.setBackgroundResource(R.drawable.transaction_row_circle_bg_pending);
        }

        // Confirmations 交易确认进度时的图标
        if (tx.getDepthInBlocks() < 4) {
            rowConfirmationsFontIcon.setVisibility(View.VISIBLE);
            rowConfirmationsFontIcon.setTextColor(colorLessSignificant);
            switch (tx.getDepthInBlocks()) {
                case 0: // No confirmations
                    rowConfirmationsFontIcon.setText(res.getString(R.string.font_icon_progress_empty));
                    rowConfirmationsFontIcon.setTextColor(colorInsignificant); // PENDING
                    break;
                case 1: // 1 out of 3 confirmations
                    rowConfirmationsFontIcon.setText(res.getString(R.string.font_icon_progress_one));
                    break;
                case 2: // 2 out of 3 confirmations
                    rowConfirmationsFontIcon.setText(res.getString(R.string.font_icon_progress_two));
                    break;
                case 3: // 3 out of 3 confirmations
                    rowConfirmationsFontIcon.setText(res.getString(R.string.font_icon_progress_full));
                    break;
            }
        } else {
            rowConfirmationsFontIcon.setVisibility(View.GONE);
        }

        Log.d(TAG, "bindView: ----- isGenerated " + tx.isGenerated());
        Log.d(TAG, "bindView: ----- Family" + walletPocket.getCoinType().getFamily());

        // Money direction and icon
        // 交易的方向和图标
        if (tx.isGenerated()) {
            rowDirectionText.setText(minedTitle);
            rowDirectionFontIcon.setText(fontIconMined);
        } else {

            if (value.isNegative()) {//小于0为真，是发送交易；
                rowDirectionText.setText(sentToTitle);
                rowDirectionFontIcon.setText(fontIconSentTo);
            } else if (walletPocket.getCoinType().getFamily() != Families.ETHEREUM
                    || walletPocket.getCoinType().getFamily() != Families.ANDACHAIN
                    || walletPocket.getCoinType().getFamily() != Families.RIPPLE) {//接受交易，而且钱包类型是比特币等类型
                List<AbstractTransaction.AbstractOutput> outputs = tx.getSentTo();//
                List<AbstractAddress> addresses = tx.getReceivedFrom();//接收的源地址
                Log.d(TAG, "bindView: ----- tx " + tx);
                Log.d(TAG, "bindView: ----- outputs " + tx.getSentTo());
                Log.d(TAG, "bindView: ----- addresses " + tx.getReceivedFrom());
                boolean isMine = false;
                for (AbstractTransaction.AbstractOutput output : outputs) {
                    Log.i(TAG, "bindView:  --------   tx.getSentTo() "+output.getAddress());//钱包地址
                    Log.i(TAG, "bindView: ----- length " + addresses.size());//地址列表长度
                    //Log.i(TAG, "bindView:  --------   tx.getReceivedFrom() "+addresses.get(0));
                    isMine = walletPocket.isAddressMine(output.getAddress());
                }

                Log.i(TAG, "bindView:         交易图标显示:   isMine :"+isMine);
                if (!isMine) {
                    rowDirectionText.setText(sentToTitle);
                    rowDirectionFontIcon.setText(fontIconSentTo);
                } else {
                    rowDirectionText.setText(receivedWithTitle);
                    rowDirectionFontIcon.setText(fontIconReceivedWith);
                }
            } else {
                rowDirectionText.setText(receivedWithTitle);
                rowDirectionFontIcon.setText(fontIconReceivedWith);
            }


        }

        // date 时间
        final long time = tx.getTimestamp();
        if (time > 0) {
            rowDate.setText(TimeUtils.toRelativeTimeString(time));
            rowDate.setVisibility(View.VISIBLE);
        } else {
            rowDate.setVisibility(View.GONE);
        }

        // value 交易值
        rowValue.setAlwaysSigned(true);
        rowValue.setAmount(value);
        Log.d(TAG, "bindView1: 账户信息tx.getValue- " + value);
        Log.d(TAG, "bindView1: 账户信息rowValue- " + rowValue.getText().toString());

        // address - label
        final AbstractAddress address;
        final String label;

        //send = true，是发送交易；反之，是接受交易
        if (sent) {
            Log.i(TAG, "bindView: -----   sent   true ");
            // we send payment to those addresses
            List<AbstractAddress> sentTo = WalletUtils.getSendToAddress(tx, walletPocket);
            for (int i = 0; i < sentTo.size(); i++) {
                Log.i(TAG, "bindView: -----------   sent   address " + sentTo.get(i));
            }
            // For now show only the first address
            address = sentTo.size() == 0 ? null : sentTo.get(0);
            Log.i(TAG, "bindView: -----------   sent   address: " + address);

        } else if (walletPocket.getCoinType().getFamily() != Families.ETHEREUM
                || walletPocket.getCoinType().getFamily() != Families.ANDACHAIN
                || walletPocket.getCoinType().getFamily() != Families.RIPPLE
                ) {
            List<AbstractTransaction.AbstractOutput> outputs = tx.getSentTo();
            boolean isMine = false;
            for (AbstractTransaction.AbstractOutput output : outputs) {
                isMine = walletPocket.isAddressMine(output.getAddress());
            }
            if (!isMine) {
                // value
                rowValue.setAlwaysSigned(false);
                //address = outputs.get(0).getAddress();
                address = null;
                Log.i(TAG, "bindView:  -----show   address   isMine  true"+address);

            } else {
                rowValue.setAlwaysSigned(true);
                //address = tx.getReceivedFrom().get(0);
                address = null;
                Log.i(TAG, "bindView:  -----show   address   isMine  false"+address);

            }

            rowValue.setAmount(value);
        } else {
            // received with those addresses
            List<AbstractAddress> receivedWith = WalletUtils.getReceivedWithOrFrom(tx, walletPocket);
            // Should be one
            address = receivedWith.size() == 0 ? null : receivedWith.get(0);
        }

        if (address != null) {
            label = resolveLabel(address);
        } else {
            if (sent) {
                // If no address found, assume it is an internal transfer
                label = res.getString(R.string.internal_transfer);
            } else {
                label = "?";
            }
        }

        if (label != null) {
            rowLabel.setText(label);
            if (address != null) {
                rowAddress.setText(GenericUtils.addressSplitToGroups(address));
                rowAddress.setVisibility(View.VISIBLE);
            } else {
                rowAddress.setVisibility(View.GONE);
            }
        } else if (address != null) {
            rowLabel.setText(GenericUtils.addressSplitToGroups(address));
            rowAddress.setVisibility(View.GONE);
        } else {
            rowLabel.setText("???"); // should not happen
        }
        rowAddress.setVisibility(View.GONE);
        rowLabel.setTypeface(label != null ? Typeface.DEFAULT : Typeface.MONOSPACE);



        // Show message label
        if (type.canHandleMessages()) {
            /*MessageFactory factory = type.getMessagesFactory();
            try {
                // TODO not efficient, should parse the message and save it to a database
                if (factory != null && factory.extractPublicMessage(tx) != null) {
                    rowMessageFontIcon.setVisibility(View.VISIBLE);
                } else {
                    rowMessageFontIcon.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                rowMessageFontIcon.setVisibility(View.GONE);
                ACRA.getErrorReporter().handleSilentException(e);
            }*/
            if (tx.getMessage() == null) {
                rowMessageFontIcon.setVisibility(View.GONE);
            } else {
                rowMessageFontIcon.setVisibility(View.VISIBLE);
            }
        } else {
            rowMessageFontIcon.setVisibility(View.GONE);
        }
    }

    String TAG = "TransactionsListAdapter";

    /*解决标签，缓存地址和标签*/
    private String resolveLabel(@Nonnull final AbstractAddress address) {
        final String cachedLabel = labelCache.get(address);
        if (cachedLabel == null) {
            final String label = AddressBookProvider.resolveLabel(context, address);
            if (label != null) {
                labelCache.put(address, label);
            } else {
                labelCache.put(address, (String) CACHE_NULL_MARKER);
            }
            return label;
        } else {
            return cachedLabel != CACHE_NULL_MARKER ? cachedLabel : null;
        }
    }

    /*清除标签缓存*/
    public void clearLabelCache() {
        labelCache.clear();

        notifyDataSetChanged();
    }
}
