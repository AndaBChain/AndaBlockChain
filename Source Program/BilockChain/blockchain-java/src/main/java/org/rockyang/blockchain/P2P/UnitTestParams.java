package org.rockyang.blockchain.P2P;

import org.bitcoinj.core.Block;
import org.bitcoinj.params.AbstractBitcoinNetParams;

import java.math.BigInteger;

public class UnitTestParams extends AbstractBitcoinNetParams {
    public static final int UNITNET_MAJORITY_WINDOW = 8;
    public static final int TESTNET_MAJORITY_REJECT_BLOCK_OUTDATED = 6;
    public static final int TESTNET_MAJORITY_ENFORCE_BLOCK_UPGRADE = 4;
    private static UnitTestParams instance;

    protected String id;
    protected long packetMagic;
    protected int addressHeader;
    protected int p2shHeader;
    protected BigInteger maxTarget;
    protected Block genesisBlock;
    protected int port;
    protected int interval;
    protected int dumpedPrivateKeyHeader;
    protected String segwitAddressHrp;
    protected int targetTimespan;
    protected int spendableCoinbaseDepth;
    protected int subsidyDecreaseBlockCount;
    protected String[] dnsSeeds;
    protected int[] addrSeeds;
    protected int bip32HeaderP2PKHpub;
    protected int bip32HeaderP2PKHpriv;
    protected int bip32HeaderP2WPKHpub;
    protected int bip32HeaderP2WPKHpriv;
    protected int majorityEnforceBlockUpgrade;
    protected int majorityRejectBlockOutdated;
    protected int majorityWindow;


    public UnitTestParams() {
        this.id = "org.bitcoinj.unittest";
        this.packetMagic = 185665799L;
        this.addressHeader = 111;
        this.p2shHeader = 196;
        this.maxTarget = new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
        this.genesisBlock.setTime(System.currentTimeMillis() / 1000L);
        this.genesisBlock.setDifficultyTarget(545259519L);
        this.genesisBlock.solve();
        this.port = 18333;
        this.interval = 10;
        this.dumpedPrivateKeyHeader = 239;
        this.segwitAddressHrp = "tb";
        this.targetTimespan = 200000000;
        this.spendableCoinbaseDepth = 5;
        this.subsidyDecreaseBlockCount = 100;
        this.dnsSeeds = null;
        this.addrSeeds = null;
        this.bip32HeaderP2PKHpub = 70617039;
        this.bip32HeaderP2PKHpriv = 70615956;
        this.bip32HeaderP2WPKHpub = 73342198;
        this.bip32HeaderP2WPKHpriv = 73341116;
        this.majorityEnforceBlockUpgrade = 3;
        this.majorityRejectBlockOutdated = 4;
        this.majorityWindow = 7;
    }

    public static synchronized UnitTestParams set() {
        if (instance == null) {
            instance = new UnitTestParams();
        }

        return instance;
    }

    public String getPaymentProtocolId() {
        return "unittest";
    }
    public String toString() {
        return " " +id +"" + packetMagic+"" + addressHeader+"" +p2shHeader +"" +maxTarget +"" +
                genesisBlock+"" + port+"" + interval+"" +dumpedPrivateKeyHeader +"" +
                segwitAddressHrp+"" +targetTimespan +"" + spendableCoinbaseDepth+"" + subsidyDecreaseBlockCount+"" +
                dnsSeeds+"" +addrSeeds +""+bip32HeaderP2PKHpub+"" + bip32HeaderP2PKHpriv+"" +bip32HeaderP2WPKHpub +""+
                bip32HeaderP2WPKHpriv+""+majorityEnforceBlockUpgrade+""+majorityRejectBlockOutdated+""+majorityWindow;
    }
}
