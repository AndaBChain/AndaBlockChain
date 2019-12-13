package com.onets.core.util;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinID;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.MyMonetary;
import com.onets.core.coins.Value;
import com.onets.core.coins.ValueType;
import com.onets.core.exceptions.AddressMalformedException;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.families.andachain.AndaAddress;
import com.onets.core.wallet.families.bitcoin.BitAddress;
import com.onets.core.wallet.families.ethereum.EthereumAddress;
import com.onets.core.wallet.families.ripple.RippleAddress;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Monetary;
import org.bitcoinj.core.VersionedChecksummedBytes;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 * 通用工具类
 */
public class GenericUtils {
    private static final Pattern charactersO0 = Pattern.compile("[0O]");
    private static final Pattern characterIl = Pattern.compile("[Il]");
    private static final Pattern notBase58 = Pattern.compile("[^123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]");

    // FIXME handle NXT addresses
    public static String fixAddress(final String input) {
//        String fixed = charactersO0.matcher(input).replaceAll("o");
//        fixed = characterIl.matcher(fixed).replaceAll("1");
//        fixed = notBase58.matcher(fixed).replaceAll("");
//        return fixed;
        return input;
    }

    public static String addressSplitToGroupsMultiline(final AbstractAddress address) {
        //修改地址支持
        if (address instanceof EthereumAddress) {
            return addressSplitToGroupsMultiline((EthereumAddress) address);
        } else if (address instanceof BitAddress) {
            return addressSplitToGroupsMultiline((BitAddress) address);
        } else if (address instanceof AndaAddress) {
            return addressSplitToGroupsMultiline((AndaAddress) address);
        } else if (address instanceof RippleAddress) {
            return addressSplitToGroupsMultiline((RippleAddress) address);
        } else {
            throw new RuntimeException("Unsupported address: " + address.getClass());
        }
    }

    public static String addressSplitToGroupsMultiline(final EthereumAddress address) {
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        /*sb.append(addressStr.substring(0, 4));
        sb.append(" ");
        sb.append(addressStr.substring(4, 8));
        sb.append(" ");
        sb.append(addressStr.substring(8, 12));
        sb.append(" ");
        sb.append(addressStr.substring(12, 16));
        sb.append(" ");
        sb.append(addressStr.substring(16, 21));
        sb.append("\n");
        sb.append(addressStr.substring(21, 25));
        sb.append(" ");
        sb.append(addressStr.substring(25, 29));
        sb.append(" ");
        sb.append(addressStr.substring(29, 33));
        sb.append(" ");
        sb.append(addressStr.substring(33, 37));
        sb.append(" ");
        sb.append(addressStr.substring(37));*/
        return sb.toString();
    }

    public static String addressSplitToGroupsMultiline(final AndaAddress address) {
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        //sb.insert(0,"A0");
        /*sb.append(addressStr.substring(0, 4));
        sb.append(" ");
        sb.append(addressStr.substring(4, 8));
        sb.append(" ");
        sb.append(addressStr.substring(8, 12));
        sb.append(" ");
        sb.append(addressStr.substring(12, 16));
        sb.append(" ");
        sb.append(addressStr.substring(16, 21));
        sb.append("\n");
        sb.append(addressStr.substring(21, 25));
        sb.append(" ");
        sb.append(addressStr.substring(25, 29));
        sb.append(" ");
        sb.append(addressStr.substring(29, 33));
        sb.append(" ");
        sb.append(addressStr.substring(33, 37));
        sb.append(" ");
        sb.append(addressStr.substring(37));*/
        return sb.toString();
    }

    public static String addressSplitToGroupsMultiline(final RippleAddress address) {
        int length = address.getHexAddress().length();
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        /*if (length < 30) {

            sb.append(addressStr.substring(0, 3));
            sb.append(" ");
            sb.append(addressStr.substring(3, 6));
            sb.append(" ");
            sb.append(addressStr.substring(6, 10));
            sb.append(" ");
            sb.append(addressStr.substring(10, 14));
            sb.append("\n");
            sb.append(addressStr.substring(14, 17));
            sb.append(" ");
            sb.append(addressStr.substring(17, 20));
            sb.append(" ");
            sb.append(addressStr.substring(20, 24));
            sb.append(" ");
            sb.append(addressStr.substring(24));

        } else {

            sb.append(addressStr.substring(0, 4));
            sb.append(" ");
            sb.append(addressStr.substring(4, 8));
            sb.append(" ");
            sb.append(addressStr.substring(8, 12));
            sb.append(" ");
            sb.append(addressStr.substring(12, 17));
            sb.append("\n");
            sb.append(addressStr.substring(17, 21));
            sb.append(" ");
            sb.append(addressStr.substring(21, 25));
            sb.append(" ");
            sb.append(addressStr.substring(25, 29));
            sb.append(" ");
            sb.append(addressStr.substring(29));
        }*/
        return sb.toString();
    }

    public static String addressSplitToGroupsMultiline(final BitAddress address) {
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        /*sb.append(addressStr.substring(0, 4));
        sb.append(" ");
        sb.append(addressStr.substring(4, 8));
        sb.append(" ");
        sb.append(addressStr.substring(8, 12));
        sb.append(" ");
        sb.append(addressStr.substring(12, 17));
        sb.append("\n");
        sb.append(addressStr.substring(17, 21));
        sb.append(" ");
        sb.append(addressStr.substring(21, 25));
        sb.append(" ");
        sb.append(addressStr.substring(25, 29));
        sb.append(" ");
        sb.append(addressStr.substring(29));*/

        return sb.toString();
    }

    public static String addressSplitToGroups(final AbstractAddress address) {
        //UI 复制地址
        if (address instanceof EthereumAddress) {
            return addressSplitToGroups((EthereumAddress) address);
        } else if (address instanceof BitAddress) {
            return addressSplitToGroups((BitAddress) address);
        } else if (address instanceof AndaAddress) {
            return addressSplitToGroups((AndaAddress) address);
        } else if (address instanceof RippleAddress) {
            return addressSplitToGroups((RippleAddress) address);
        } else {
            throw new RuntimeException("Unsupported address: " + address.getClass());
        }
    }

    /**
     * 将地址转为字符串，取消了地址分组格式
     * @param address
     * @return
     */
    public static String addressSplitToGroups(final EthereumAddress address) {
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        /*sb.append(addressStr.substring(0, 5));
        sb.append(" ");
        sb.append(addressStr.substring(5, 9));
        sb.append(" ");
        sb.append(addressStr.substring(9, 13));
        sb.append(" ");
        sb.append(addressStr.substring(13, 17));
        sb.append(" ");
        sb.append(addressStr.substring(17, 21));
        sb.append(" ");
        sb.append(addressStr.substring(21, 25));
        sb.append(" ");
        sb.append(addressStr.substring(25, 29));
        sb.append(" ");
        sb.append(addressStr.substring(29, 33));
        sb.append(" ");
        sb.append(addressStr.substring(33, 37));
        sb.append(" ");
        sb.append(addressStr.substring(37));*/
        return sb.toString();
    }

    public static String addressSplitToGroups(final AndaAddress address) {
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        sb.insert(0,"A0");
        /*sb.append(addressStr.substring(0, 5));
        sb.append(" ");
        sb.append(addressStr.substring(5, 9));
        sb.append(" ");
        sb.append(addressStr.substring(9, 13));
        sb.append(" ");
        sb.append(addressStr.substring(13, 17));
        sb.append(" ");
        sb.append(addressStr.substring(17, 21));
        sb.append(" ");
        sb.append(addressStr.substring(21, 25));
        sb.append(" ");
        sb.append(addressStr.substring(25, 29));
        sb.append(" ");
        sb.append(addressStr.substring(29, 33));
        sb.append(" ");
        sb.append(addressStr.substring(33, 37));
        sb.append(" ");
        sb.append(addressStr.substring(37));*/
        return sb.toString();
    }

    public static String addressSplitToGroups(final RippleAddress address) {
        int length = address.getHexAddress().length();
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        /*if(length < 30 ) {
            sb.append(addressStr.substring(0, 3));
            sb.append(" ");
            sb.append(addressStr.substring(3, 6));
            sb.append(" ");
            sb.append(addressStr.substring(6, 10));
            sb.append(" ");
            sb.append(addressStr.substring(10, 14));
            sb.append(" ");
            sb.append(addressStr.substring(14, 17));
            sb.append(" ");
            sb.append(addressStr.substring(17, 20));
            sb.append(" ");
            sb.append(addressStr.substring(20, 24));
            sb.append(" ");
            sb.append(addressStr.substring(24));

        }else {
            sb.append(addressStr.substring(0, 4));
            sb.append(" ");
            sb.append(addressStr.substring(4, 8));
            sb.append(" ");
            sb.append(addressStr.substring(8, 12));
            sb.append(" ");
            sb.append(addressStr.substring(12, 17));
            sb.append(" ");
            sb.append(addressStr.substring(17, 21));
            sb.append(" ");
            sb.append(addressStr.substring(21, 25));
            sb.append(" ");
            sb.append(addressStr.substring(25, 29));
            sb.append(" ");
            sb.append(addressStr.substring(29));
        }*/
        return sb.toString();
    }

    public static String addressSplitToGroups(final BitAddress address) {
        String addressStr = address.toString();
        StringBuilder sb = new StringBuilder();
        /*sb.append(addressStr.substring(0, 5));
        sb.append(" ");
        sb.append(addressStr.substring(5, 9));
        sb.append(" ");
        sb.append(addressStr.substring(9, 13));
        sb.append(" ");
        sb.append(addressStr.substring(13, 17));
        sb.append(" ");
        sb.append(addressStr.substring(17, 21));
        sb.append(" ");
        sb.append(addressStr.substring(21, 25));
        sb.append(" ");
        sb.append(addressStr.substring(25, 29));
        sb.append(" ");
        sb.append(addressStr.substring(29));*/

        return sb.toString();
    }

    public static String formatValue(@Nonnull final Value value) {
        if (value.type.getName().equals("Ethereum")) {
            return formatCoinValueEth(value.type, value, 18, 0);
        } else if (value.type.getName().equals("AndaBlockChain")){
            return formatCoinValue(value.type, value.toAndaCoin(), "", "-", 8, 0);
        } else {
            return formatCoinValue(value.type, value.toBitCoin(), "", "-", 8, 0);
        }
    }

    public static String formatCoinValue(@Nonnull final ValueType type, @Nonnull final Monetary value) {
        return formatCoinValue(type, value, "", "-", 8, 0);
    }

    public static String formatCoinValue(@Nonnull final ValueType type, @Nonnull final Monetary value,
                                         final int precision, final int shift) {
        return formatCoinValue(type, value, "", "-", precision, shift);
    }

    // ---------------------------  Ethereum -------------------------------------
    public static String formatCoinValueEth(@Nonnull final ValueType type, @Nonnull final MyMonetary value,
                                            final int precision, final int shift) {
        return formatCoinValue(type, value, "", "-", precision, shift);
    }

    public static String formatCoinValue(@Nonnull final ValueType type, @Nonnull final MyMonetary value,
                                         @Nonnull final String plusSign, @Nonnull final String minusSign,
                                         final int precision, final int shift) {
        return formatValueEth(type.getUnitExponent(), value, plusSign, minusSign, precision, shift, false);
    }

    // ---------------------------  Ethereum --------------------------------------


    public static String formatCoinValue(@Nonnull final ValueType type, @Nonnull final Monetary value,
                                         @Nonnull final String plusSign, @Nonnull final String minusSign,
                                         final int precision, final int shift) {
        return formatValue(type.getUnitExponent(), value, plusSign, minusSign, precision, shift, false);
    }

    public static String formatCoinValue(@Nonnull final ValueType type, @Nonnull final Monetary value,
                                         boolean removeFinalZeroes) {
        return formatValue(type.getUnitExponent(), value, "", "-", 8, 0, removeFinalZeroes);
    }

    private static String formatValue(final long unitExponent, @Nonnull final Monetary value,
                                      @Nonnull final String plusSign, @Nonnull final String minusSign,
                                      final int precision, final int shift, boolean removeFinalZeroes) {
        long longValue = value.getValue();


        final String sign = value.signum() == -1 ? minusSign : plusSign;

        String formatedValue;

        if (shift == 0) {
            long units = Math.round(Math.pow(10, unitExponent));
            long precisionUnits = (long) (units / Math.pow(10, precision));
            long roundingPrecisionUnits = precisionUnits / 2;

            if (precision == 2 || precision == 4 || precision == 6 || precision == 8 || precision == 10 || precision == 12 || precision == 14 || precision == 16 || precision == 18) {
                if (roundingPrecisionUnits > 0) {
                    longValue = longValue - longValue % precisionUnits + longValue % precisionUnits / roundingPrecisionUnits * precisionUnits;
                }
            } else {
                throw new IllegalArgumentException("cannot handle precision/shift: " + precision + "/" + shift);
            }

            final long absValue = Math.abs(longValue);
            final long coins = absValue / units;
            final int satoshis = (int) (absValue % units);

            if (isShiftPossible(units, satoshis, 100)) {
                formatedValue = String.format(Locale.US, "%d.%02d",
                        coins, getShiftedCents(units, satoshis, 100));

            } else if (isShiftPossible(units, satoshis, 10000)) {
                formatedValue = String.format(Locale.US, "%d.%04d",
                        coins, getShiftedCents(units, satoshis, 10000));

            } else if (isShiftPossible(units, satoshis, 1000000)) {
                formatedValue = String.format(Locale.US, "%d.%06d", coins,
                        getShiftedCents(units, satoshis, 1000000));

            } else {
                formatedValue = String.format(Locale.US, "%d.%08d", coins, satoshis);
            }

        } else {
            throw new IllegalArgumentException("cannot handle shift: " + shift);
        }

        // Relax precision if incorrectly shows value as 0.00 but is in reality not zero
        if (formatedValue.equals("0.00") && value.getValue() != 0) {
            return formatValue(unitExponent, value, plusSign, minusSign, precision + 2, shift, removeFinalZeroes);
        }

        // Remove final zeroes if requested
        while (removeFinalZeroes && formatedValue.length() > 0 &&
                formatedValue.contains(".") && formatedValue.endsWith("0")) {
            formatedValue = formatedValue.substring(0, formatedValue.length() - 1);
        }
        if (removeFinalZeroes && formatedValue.length() > 0 && formatedValue.endsWith(".")) {
            formatedValue = formatedValue.substring(0, formatedValue.length() - 1);
        }

        // Add the sign if needed
        formatedValue = String.format(Locale.US, "%s%s", sign, formatedValue);

        return formatedValue;
    }

    private static long getShiftedCents(long units, int satoshis, int centAmount) {
        return satoshis / (units / centAmount);
    }

    private static boolean isShiftPossible(long units, int satoshis, int centAmount) {
        return units / centAmount != 0 && satoshis % (units / centAmount) == 0;
    }


    public static String formatFiatValue(final Value fiat, final int precision, final int shift) {
        if (fiat.type.getName().equals("Ethereum")) {
            return formatValueEth(fiat.smallestUnitExponent(), fiat, "", "-", precision, shift, false);
        } else if (fiat.type.getName().equals("AndaBlockChain")) {
            return formatValue(fiat.smallestUnitExponent(), fiat, "", "-", precision, shift, false);
        } else {
            return formatValue(fiat.smallestUnitExponent(), fiat, "", "-", precision, shift, false);
        }
    }

    private static String formatValueEth(final long unitExponent, @Nonnull final MyMonetary value,
                                         @Nonnull final String plusSign, @Nonnull final String minusSign,
                                         final int precision, final int shift, boolean removeFinalZeroes) {
        BigInteger longValue = value.getBigValue();

        final String sign = value.signum() == -1 ? minusSign : plusSign;

        String formatedValue;

        if (shift == 0) {
            long units = Math.round(Math.pow(10, unitExponent));
            long precisionUnits = (long) (units / Math.pow(10, precision));
            long roundingPrecisionUnits = precisionUnits / 2;

            if (precision == 2 || precision == 4 || precision == 6 || precision == 8 || precision == 18) {
                if (roundingPrecisionUnits > 0) {
                    BigInteger x = longValue.mod(new BigInteger(String.valueOf(precisionUnits)));
                    BigInteger y = x.divide(new BigInteger(String.valueOf(roundingPrecisionUnits))).multiply(new BigInteger(String.valueOf(precisionUnits)));

                    longValue = longValue.subtract(x).add(y);
                }
            } else {
                throw new IllegalArgumentException("cannot handle precision/shift: " + precision + "/" + shift);
            }


            final BigInteger absValue = longValue.abs();
            final BigInteger coins = absValue.divide(new BigInteger(String.valueOf(units)));
            final BigInteger satoshis = absValue.mod(new BigInteger(String.valueOf(units)));


            if (isShiftPossible(units, satoshis, 100)) {
                formatedValue = String.format(Locale.US, "%d.%02d",
                        coins, getShiftedCents(units, satoshis, 100));

            } else if (isShiftPossible(units, satoshis, 10000)) {
                formatedValue = String.format(Locale.US, "%d.%04d",
                        coins, getShiftedCents(units, satoshis, 10000));

            } else if (isShiftPossible(units, satoshis, 1000000)) {
                formatedValue = String.format(Locale.US, "%d.%06d", coins,
                        getShiftedCents(units, satoshis, 1000000));

            } else {
                formatedValue = String.format(Locale.US, "%d.%08d", coins, satoshis);
            }

        } else {
            throw new IllegalArgumentException("cannot handle shift: " + shift);
        }

        // Relax precision if incorrectly shows value as 0.00 but is in reality not zero
        if (formatedValue.equals("0.00") && value.getBigValue().compareTo(BigInteger.ZERO) != 0) {
            return formatValueEth(unitExponent, value, plusSign, minusSign, precision + 2, shift, removeFinalZeroes);
        }

        // Remove final zeroes if requested
        while (removeFinalZeroes && formatedValue.length() > 0 &&
                formatedValue.contains(".") && formatedValue.endsWith("0")) {
            formatedValue = formatedValue.substring(0, formatedValue.length() - 1);
        }

        if (removeFinalZeroes && formatedValue.length() > 0 && formatedValue.endsWith(".")) {
            formatedValue = formatedValue.substring(0, formatedValue.length() - 1);
        }

        // Add the sign if needed
        formatedValue = String.format(Locale.US, "%s%s", sign, formatedValue);

        return formatedValue;
    }


    private static boolean isShiftPossible(long units, BigInteger satoshis, int centAmount) {
        return units / centAmount != 0 && satoshis.mod(new BigInteger(String.valueOf(units / centAmount))).compareTo(BigInteger.ZERO) == 0;
    }

    private static BigInteger getShiftedCents(long units, BigInteger satoshis, int centAmount) {
        return satoshis.divide(new BigInteger(String.valueOf(units / centAmount)));
    }


    public static String formatFiatValue(Value fiat) {
        return formatFiatValue(fiat, 2, 0);
    }

    /**
     * Parses the provided string and returns the possible supported coin types.
     * Throws an AddressFormatException if the string is not a valid address or not supported.
     */
    public static List<CoinType> getPossibleTypes(String addressStr) throws AddressMalformedException {
        ImmutableList.Builder<CoinType> builder = ImmutableList.builder();
        tryBitcoinFamilyAddresses(addressStr, builder);
        // TODO try other coin addresses
        List<CoinType> possibleTypes = builder.build();
        if (possibleTypes.size() == 0) {
            throw new AddressMalformedException("Unsupported address: " + addressStr);
        }
        return possibleTypes;
    }

    /**
     * Tries to parse the addressStr as a Bitcoin style address and find potential compatible coin types
     *
     * @param addressStr possible bitcoin type address
     * @param builder    for the types list
     */
    private static void tryBitcoinFamilyAddresses(final String addressStr, ImmutableList.Builder<CoinType> builder) {
        VersionedChecksummedBytes parsed;
        try {
            parsed = new VersionedChecksummedBytes(addressStr) {
            };
        } catch (AddressFormatException e) {
            return;
        }
        int version = parsed.getVersion();
        for (CoinType type : CoinID.getSupportedCoins()) {
            if (type.getAcceptableAddressCodes() == null) continue;
            for (int addressCode : type.getAcceptableAddressCodes()) {
                if (addressCode == version) {
                    builder.add(type);
                    break;
                }
            }
        }
    }

    public static List<CoinType> getPossibleTypes(AbstractAddress address) throws AddressMalformedException {
        return getPossibleTypes(address.toString());
    }

    public static boolean hasMultipleTypes(AbstractAddress address) {
        return hasMultipleTypes(address.toString());
    }

    public static boolean hasMultipleTypes(String addressStr) {
        try {
            return getPossibleTypes(addressStr).size() > 1;
        } catch (AddressMalformedException e) {
            return false;
        }
    }
}
