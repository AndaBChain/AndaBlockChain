package com.onets.core.network.AndaNetWork;

import com.google.common.base.Stopwatch;
import com.onets.core.util.MonetaryFormat;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for Anda networks.
 * 该文件根据bitcoinj/params/AbstractBitcoinNetParams.java文件编写
 */
public class AbstractAndaNetParams extends NetworkParameters {
    /*Scheme part for Anda URIs*/
    public static final String ANDA_SCHEME = "andachain";
    public static final int REWARD_HALVING_INTERVAL = 210000;//奖励减半时间间隔

    private static final Logger log = LoggerFactory.getLogger(AbstractAndaNetParams.class);
    public AbstractAndaNetParams(){
        super();
    }

    /**
     * Checks if we are at a reward halving point.
     * 检查是否处于奖励减半点。
     * @param height height The height of the previous stored block <高度是前一个存储块的高度>
     * @return If this is a reward halving point <如果是一个奖励减半点>
     */
    public final boolean isRewardHalvingPoint(final int height){
        return ((height + 1) % REWARD_HALVING_INTERVAL) == 0;
    }

    /**
     * Checks if we are at a difficulty transition point.
     * 检查是否处于一个困难的过渡点
     * @param height height The height of the previous stored block <高度是前一个存储块的高度>
     * @return If this is a difficulty transition point
     */
    public final boolean isDifficultyTransitionPoint(final int height){
        return ((height + 1) % this.getInterval()) == 0;
    }

    /**
     * 检查转换困难
     * @param storedPrev
     * @param nextBlock
     * @param blockStore
     * @throws VerificationException
     * @throws BlockStoreException
     */
    public void checkDifficultyTransitions(final StoredBlock storedPrev, final Block nextBlock,
                                           final BlockStore blockStore) throws VerificationException, BlockStoreException {
        final Block prev = storedPrev.getHeader();

        // Is this supposed to be a difficulty transition point?
        if (!isDifficultyTransitionPoint(storedPrev.getHeight())) {

            // No ... so check the difficulty didn't actually change.
            if (nextBlock.getDifficultyTarget() != prev.getDifficultyTarget())
                throw new VerificationException("Unexpected change in difficulty at height " + storedPrev.getHeight() +
                        ": " + Long.toHexString(nextBlock.getDifficultyTarget()) + " vs " +
                        Long.toHexString(prev.getDifficultyTarget()));
            return;
        }

        // We need to find a block far back in the chain. It's OK that this is expensive because it only occurs every
        // two weeks after the initial block chain download.
        final Stopwatch watch = Stopwatch.createStarted();
        Sha256Hash hash = prev.getHash();
        StoredBlock cursor = null;
        final int interval = this.getInterval();
        for (int i = 0; i < interval; i++) {
            cursor = blockStore.get(hash);
            if (cursor == null) {
                // This should never happen. If it does, it means we are following an incorrect or busted chain.
                throw new VerificationException(
                        "Difficulty transition point but we did not find a way back to the last transition point. Not found: " + hash);
            }
            hash = cursor.getHeader().getPrevBlockHash();
        }
        checkState(cursor != null && isDifficultyTransitionPoint(cursor.getHeight() - 1),
                "Didn't arrive at a transition point.");
        watch.stop();
        if (watch.elapsed(TimeUnit.MILLISECONDS) > 50)
            log.info("Difficulty transition traversal took {}", watch);

        Block blockIntervalAgo = cursor.getHeader();
        int timespan = (int) (prev.getTimeSeconds() - blockIntervalAgo.getTimeSeconds());
        // Limit the adjustment step.
        final int targetTimespan = this.getTargetTimespan();
        if (timespan < targetTimespan / 4)
            timespan = targetTimespan / 4;
        if (timespan > targetTimespan * 4)
            timespan = targetTimespan * 4;

        BigInteger newTarget = Utils.decodeCompactBits(prev.getDifficultyTarget());
        newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
        newTarget = newTarget.divide(BigInteger.valueOf(targetTimespan));

        if (newTarget.compareTo(this.getMaxTarget()) > 0) {
            log.info("Difficulty hit proof of work limit: {}", newTarget.toString(16));
            newTarget = this.getMaxTarget();
        }

        int accuracyBytes = (int) (nextBlock.getDifficultyTarget() >>> 24) - 3;
        long receivedTargetCompact = nextBlock.getDifficultyTarget();

        // The calculated difficulty is to a higher precision than received, so reduce here.
        BigInteger mask = BigInteger.valueOf(0xFFFFFFL).shiftLeft(accuracyBytes * 8);
        newTarget = newTarget.and(mask);
        long newTargetCompact = Utils.encodeCompactBits(newTarget);

        if (newTargetCompact != receivedTargetCompact)
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    Long.toHexString(newTargetCompact) + " vs " + Long.toHexString(receivedTargetCompact));
    }

    public Coin getMaxMoney(){
        return MAX_MONEY;
    }

    public Coin getMinNonDustOutput(){
        return Transaction.MIN_NONDUST_OUTPUT;
    }

    public MonetaryFormat getMonetaryFormat(){
        return new MonetaryFormat();
    }

    /*public int getProtocolVersionNum(final ProtocolVersion version){
        return version.get
    }*/


    @Override
    public String getPaymentProtocolId() {
        return null;
    }
}
