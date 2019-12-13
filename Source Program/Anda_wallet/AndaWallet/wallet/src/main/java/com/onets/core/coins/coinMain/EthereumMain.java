package com.onets.core.coins.coinMain;

import com.onets.core.coins.families.EthereumFamily;

/**
 * @author Yu K.Q.
 */
public class EthereumMain extends EthereumFamily {
    private EthereumMain() {
        id = "ethereum.main";

        name = "Ethereum";
        symbol = "ETH";
        uriScheme = "ethereum";
        bip44Index = 80;
        unitExponent = 18;      //单位换算  指数  1 ether =  10^unitExponent wei
        feeValue = value(1);     //交易费
        minNonDust = value(1);
    }

    private static EthereumMain instance = new EthereumMain();
    public static synchronized EthereumMain get() {
        return instance;
    }
}
