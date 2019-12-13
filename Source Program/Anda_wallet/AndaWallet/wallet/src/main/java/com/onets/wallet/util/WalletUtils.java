package com.onets.wallet.util;

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
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;

import com.onets.core.coins.CoinID;
import com.onets.core.coins.CoinType;
import com.onets.core.util.Currencies;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractTransaction.AbstractOutput;
import com.onets.core.wallet.AbstractWallet;
import com.onets.core.wallet.WalletAccount;
import com.onets.wallet.Constants;

import org.bitcoinj.core.Sha256Hash;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.onets.core.Preconditions.checkState;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 * 钱包工具类
 */
public class WalletUtils {
    public static int getIconRes(CoinType type) {
        return Constants.COINS_ICONS.get(type);
    }

    /**
     * 获取图标资源
     * @param account
     * @return
     */
    public static int getIconRes(WalletAccount account) {
        return getIconRes(account.getCoinType());
    }

    /**
     * hash转为long
     * @param hash
     * @return
     */
    public static long longHash(@Nonnull final Sha256Hash hash) {
        return longHash(hash.getBytes());
    }

    /**
     * hash字符数组转为long
     * @param bytes
     * @return
     */
    public static long longHash(@Nonnull final byte[] bytes) {
        int len = bytes.length;
        checkState(len >= 8);

        return (bytes[len - 1] & 0xFFl) |
                ((bytes[len - 2] & 0xFFl) << 8) |
                ((bytes[len - 3] & 0xFFl) << 16) |
                ((bytes[len - 4] & 0xFFl) << 24) |
                ((bytes[len - 5] & 0xFFl) << 32) |
                ((bytes[len - 6] & 0xFFl) << 40) |
                ((bytes[len - 7] & 0xFFl) << 48) |
                ((bytes[len - 8] & 0xFFl) << 56);
    }

    /**
     * 获取要发送到的钱包地址
     * @param tx
     * @param pocket
     * @return
     */
    @CheckForNull
    public static List<AbstractAddress> getSendToAddress(@Nonnull final AbstractTransaction tx,
                                                         @Nonnull final AbstractWallet pocket) {
        return getToAddresses(tx, pocket, false);
    }


    /**
     * 获取接收
     * @param tx
     * @param pocket
     * @return
     */
    @CheckForNull
    public static List<AbstractAddress> getReceivedWithOrFrom(@Nonnull final AbstractTransaction tx,
                                                              @Nonnull final AbstractWallet pocket) {
        // TODO a better approach is to use a "features" enum list and check agaist that
//        if (pocket.getCoinType() instanceof EthereumFamily) {
//            return tx.getReceivedFrom();
//        } else if(pocket.getCoinType() instanceof AnDaChainFamily) {
//            return tx.getReceivedFrom();
//        }else{
        return getToAddresses(tx, pocket, true);
//        }
    }

    static String TAG = "WalletUtils";

    /**
     * 获取发送到的地址组
     * @param tx
     * @param pocket
     * @param toMe
     * @return
     */
    @CheckForNull
    private static List<AbstractAddress> getToAddresses(@Nonnull final AbstractTransaction tx,
                                                        @Nonnull final AbstractWallet pocket,
                                                        boolean toMe) {
        Log.i(TAG, "getToAddresses: -----------   1    ");

        List<AbstractAddress> addresses = new ArrayList<>();
        List<AbstractOutput> outputs = tx.getSentTo();
        Log.i(TAG, "getToAddresses: -----------   2    ");

        for (AbstractOutput output : outputs) {
            Log.i(TAG, "getToAddresses: -----------   for(AbstractOutput output : outputs)    ");

            boolean isMine = pocket.isAddressMine(output.getAddress());
            Log.i(TAG, "getToAddresses: -----------   isMine  : " + isMine);

            if (isMine == toMe) {
                Log.i(TAG, "getToAddresses: -----------  isMine == toMe");
                addresses.add(output.getAddress());
            }
        }
        return addresses;
    }

    private static final Pattern P_SIGNIFICANT = Pattern.compile("^([-+]" + Constants.CHAR_THIN_SPACE + ")?\\d*(\\.\\d{0,2})?");
    private static final Object SIGNIFICANT_SPAN = new StyleSpan(Typeface.BOLD);
    public static final RelativeSizeSpan SMALLER_SPAN = new RelativeSizeSpan(0.85f);


    /**
     * 格式化签名
     * @param spannable
     * @param insignificantRelativeSizeSpan
     */
    public static void formatSignificant(@Nonnull final Spannable spannable, @Nullable final RelativeSizeSpan insignificantRelativeSizeSpan) {
        spannable.removeSpan(SIGNIFICANT_SPAN);
        if (insignificantRelativeSizeSpan != null)
            spannable.removeSpan(insignificantRelativeSizeSpan);

        final Matcher m = P_SIGNIFICANT.matcher(spannable);
        if (m.find()) {
            final int pivot = m.group().length();
            if (pivot > 0)
                spannable.setSpan(SIGNIFICANT_SPAN, 0, pivot, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (spannable.length() > pivot && insignificantRelativeSizeSpan != null)
                spannable.setSpan(insignificantRelativeSizeSpan, pivot, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 当前本地模式
     * @return
     */
    public static String localeCurrencyCode() {
        try {
            return Currency.getInstance(Locale.getDefault()).getCurrencyCode();
        } catch (final IllegalArgumentException x) {
            return null;
        }
    }

    /**
     * 当前名字
     * @param code
     * @return
     */
    @Nullable
    public static String getCurrencyName(String code) {
        String currencyName = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Currency currency = Currency.getInstance(code);
                currencyName = currency.getDisplayName(Locale.getDefault());
            } catch (final IllegalArgumentException x) { /* ignore */ }
        } else {
            currencyName = Currencies.CURRENCY_NAMES.get(code);
        }

        // Try cryptocurrency codes
        if (currencyName == null) {
            try {
                CoinType cryptoCurrency = CoinID.typeFromSymbol(code);
                currencyName = cryptoCurrency.getName();
            } catch (final IllegalArgumentException x) { /* ignore */ }
        }

        return currencyName;
    }


    //从assets 文件夹中获取文件并读取数据
    public static byte[] getFromAssets(String fileName, Context context) {
        byte[] result = {};
        try {
            InputStream in = context.getResources().getAssets().open(fileName);
            //获取文件的字节数
            int lenght = in.available();
            //创建byte数组
            byte[] buffer = new byte[lenght];
            //将文件中的数据读到byte数组中
            in.read(buffer);
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据byte数组生成文件
     *
     * @param bytes
     *            生成文件用到的byte数组
     */
    public static void createFileWithByte(byte[] bytes,String fileName) {
        // TODO Auto-generated method stub
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(Environment.getExternalStorageDirectory(),
                fileName);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
    //把一个字符串中的大写转为小写，小写转换为大写
    public static String exChange(String str){
        StringBuffer sb = new StringBuffer();
        if(str!=null){
            for(int i=0;i<str.length();i++){
                char c = str.charAt(i);
                if(Character.isDigit(c)){
                    sb.append(c);
                }else {
                    if(Character.isUpperCase(c)){
                        sb.append(Character.toLowerCase(c));
                    }else if(Character.isLowerCase(c)){
                        sb.append(Character.toUpperCase(c));
                    }
                }

            }
        }

        return sb.toString();
    }

    public static Spanned formatHash(final String address, final int groupSize, final int lineSize){
        return formatHash(null, address, groupSize, lineSize, Constants.CHAR_THIN_SPACE);
    }

    private static Spanned formatHash(@Nullable final String prefix, final String address, final int groupSize, final int lineSize, final char groupSeparetor) {
        final SpannableStringBuilder builder = prefix != null ? new SpannableStringBuilder(prefix) : new SpannableStringBuilder();

        final int len = address.length();
        for (int i = 0; i < len; i++) {
            final int end = i + groupSize;
            final String part = address.substring(i, end < len ? end : len);

            builder.append(part);
            builder.setSpan(new MonospaceSpan(), builder.length() - part.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < len){
                final boolean endOfLine = lineSize >0 && end % lineSize ==0;
                builder.append(endOfLine ? '\n' : groupSeparetor);
            }
        }
        return SpannableString.valueOf(builder);
    }

    private static class MonospaceSpan extends TypefaceSpan{

        public MonospaceSpan() {
            super("monospace");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this){
                return true;
            }
            if (obj == null || obj.getClass() != getClass()){
                return false;
            }
            return true;
        }
    }
}
