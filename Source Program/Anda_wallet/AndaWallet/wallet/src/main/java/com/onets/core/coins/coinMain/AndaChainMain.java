package com.onets.core.coins.coinMain;

import com.onets.core.coins.SoftDustPolicy;
import com.onets.core.coins.families.AndaFamily;

/**
 * @author Yu K.Q.
 * 安达链主
 */
public class AndaChainMain extends AndaFamily {
    private AndaChainMain() {
        id = "andachain.main";

        //todo:SET addressHeader p2shHeader spendableCoinbaseDepth dumpedPrivateKeyHeader in AndaChainMain
        addressHeader = 121;
        p2shHeader = 256;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        spendableCoinbaseDepth = 100;
        dumpedPrivateKeyHeader = 256;

        name = "AndaBlockChain";
        symbol = "ABT";
        uriScheme = "andachain";
        bip44Index = 98;
        unitExponent = 8;   //0.0001
        feeValue = value(0);     //交易费
        minNonDust = value(5460);
        softDustLimit = value(1000000);
        softDustPolicy = SoftDustPolicy.AT_LEAST_BASE_FEE_IF_SOFT_DUST_TXO_PRESENT;
        signedMessageHeader = toBytes("AndaChain Signed Message:\n");

    }

    private static AndaChainMain instance = new AndaChainMain();
    public static synchronized AndaChainMain get() {
        return instance;
    }
}
