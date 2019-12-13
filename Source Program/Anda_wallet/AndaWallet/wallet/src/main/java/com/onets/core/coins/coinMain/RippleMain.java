package com.onets.core.coins.coinMain;

import com.onets.core.coins.families.RippleFamily;

/**
 * @author Yu K.Q.
 */
public class RippleMain extends RippleFamily {
    private RippleMain() {
        id = "ripple.main";

        name = "Ripple";
        symbol = "XRP";
        uriScheme = "ripple";
        bip44Index = 33;
        unitExponent = 6;      //单位换算  指数  1 ether =  10^unitExponent wei
        feeValue = value(10);     //交易费
        minNonDust = value(1);


//        addressPrefix = "NXT-";
//        feeValue = oneCoin();
//        minNonDust = value(1);
//        feePolicy = FeePolicy.FLAT_FEE;

    }

    private static RippleMain instance = new RippleMain();
    public static synchronized RippleMain get() {
        return instance;
    }
}
