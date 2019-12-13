package com.onets.core.coins.families;

/**
 * @author Yu K.Q.
 */
public enum Families {
    ETHEREUM("ethereum"),
    ANDACHAIN("andachain"),
    // same as in org.bitcoinj.params.Networks
    BITCOIN("bitcoin"),
    NUBITS("nubits"),
    PEERCOIN("peercoin"),
    REDDCOIN("reddcoin"),
    VPNCOIN("vpncoin"),
    CLAMS("clams"),
    NXT("nxt"),
    RIPPLE("ripple"),
    ;

    public final String family;

    Families(String family) {
        this.family = family;
    }

    @Override
    public String toString() {
        return family;
    }
}
