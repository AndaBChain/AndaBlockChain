/*
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.bitcoinj.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.bitcoinj.core.NetworkParameters.ProtocolVersion;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.core.VerificationException.CoinbaseHeightMismatch;
import org.bitcoinj.core.VerificationException.CoinbaseScriptSizeOutOfRange;
import org.bitcoinj.core.VerificationException.DuplicatedOutPoint;
import org.bitcoinj.core.VerificationException.EmptyInputsOrOutputs;
import org.bitcoinj.core.VerificationException.ExcessiveValue;
import org.bitcoinj.core.VerificationException.LargerThanMaxBlockSize;
import org.bitcoinj.core.VerificationException.NegativeValueOutput;
import org.bitcoinj.core.VerificationException.UnexpectedCoinbaseInput;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptError;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.script.Script.ScriptType;
import org.bitcoinj.utils.ExchangeRate;
import org.bitcoinj.wallet.WalletTransaction.Pool;
import org.bouncycastle.crypto.params.KeyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transaction extends ChildMessage {
    public static final Comparator<Transaction> SORT_TX_BY_UPDATE_TIME = new Comparator<Transaction>() {
        public int compare(Transaction tx1, Transaction tx2) {
            long time1 = tx1.getUpdateTime().getTime();
            long time2 = tx2.getUpdateTime().getTime();
            int updateTimeComparison = -Longs.compare(time1, time2);
            return updateTimeComparison != 0 ? updateTimeComparison : tx1.getTxId().compareTo(tx2.getTxId());
        }
    };
    public static final Comparator<Transaction> SORT_TX_BY_HEIGHT = new Comparator<Transaction>() {
        public int compare(Transaction tx1, Transaction tx2) {
            TransactionConfidence confidence1 = tx1.getConfidence();
            int height1 = confidence1.getConfidenceType() == ConfidenceType.BUILDING ? confidence1.getAppearedAtChainHeight() : -1;
            TransactionConfidence confidence2 = tx2.getConfidence();
            int height2 = confidence2.getConfidenceType() == ConfidenceType.BUILDING ? confidence2.getAppearedAtChainHeight() : -1;
            int heightComparison = -Ints.compare(height1, height2);
            return heightComparison != 0 ? heightComparison : tx1.getTxId().compareTo(tx2.getTxId());
        }
    };
    private static final Logger log = LoggerFactory.getLogger(Transaction.class);
    public static final int LOCKTIME_THRESHOLD = 500000000;
    public static final BigInteger LOCKTIME_THRESHOLD_BIG = BigInteger.valueOf(500000000L);
    public static final int MAX_STANDARD_TX_SIZE = 100000;
    public static final Coin REFERENCE_DEFAULT_MIN_TX_FEE = Coin.valueOf(1000L);
    public static final Coin DEFAULT_TX_FEE = Coin.valueOf(100000L);
    public static final Coin MIN_NONDUST_OUTPUT = Coin.valueOf(546L);
    private long version;
    private ArrayList<TransactionInput> inputs;
    private ArrayList<TransactionOutput> outputs;
    private long lockTime;
    private Date updatedAt;
    private Sha256Hash cachedTxId;
    private Sha256Hash cachedWTxId;
    @Nullable
    private TransactionConfidence confidence;
    private Map<Sha256Hash, Integer> appearsInHashes;
    private int optimalEncodingMessageSize;
    private Transaction.Purpose purpose;
    @Nullable
    private ExchangeRate exchangeRate;
    @Nullable
    private String memo;
    @Nullable
    private Coin cachedValue;
    @Nullable
    private TransactionBag cachedForBag;
    */
/** @deprecated *//*

    public static final byte SIGHASH_ANYONECANPAY_VALUE = -128;

    public Transaction(NetworkParameters params) {
        super(params);
        this.purpose = Transaction.Purpose.UNKNOWN;
        this.version = 1L;
        this.inputs = new ArrayList();
        this.outputs = new ArrayList();
        this.length = 8;
    }

    public Transaction(NetworkParameters params, byte[] payloadBytes) throws ProtocolException {
        super(params, payloadBytes, 0);
        this.purpose = Transaction.Purpose.UNKNOWN;
    }

    public Transaction(NetworkParameters params, byte[] payload, int offset) throws ProtocolException {
        super(params, payload, offset);
        this.purpose = Transaction.Purpose.UNKNOWN;
    }

    public Transaction(NetworkParameters params, byte[] payload, int offset, @Nullable Message parent, MessageSerializer setSerializer, int length, @Nullable byte[] hashFromHeader) throws ProtocolException {
        super(params, payload, offset, parent, setSerializer, length);
        this.purpose = Transaction.Purpose.UNKNOWN;
        if (hashFromHeader != null) {
            this.cachedWTxId = Sha256Hash.wrapReversed(hashFromHeader);
            if (!this.hasWitnesses()) {
                this.cachedTxId = this.cachedWTxId;
            }
        }

    }

    public Transaction(NetworkParameters params, byte[] payload, @Nullable Message parent, MessageSerializer setSerializer, int length) throws ProtocolException {
        super(params, payload, 0, parent, setSerializer, length);
        this.purpose = Transaction.Purpose.UNKNOWN;
    }

    */
/** @deprecated *//*

    @Deprecated
    public Sha256Hash getHash() {
        return this.getTxId();
    }

    */
/** @deprecated *//*

    @Deprecated
    public String getHashAsString() {
        return this.getTxId().toString();
    }

    public Sha256Hash getTxId() {
        if (this.cachedTxId == null) {
            if (!this.hasWitnesses() && this.cachedWTxId != null) {
                this.cachedTxId = this.cachedWTxId;
            } else {
                UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream(this.length < 32 ? 32 : this.length + 32);

                try {
                    this.bitcoinSerializeToStream(stream, false);
                } catch (IOException var3) {
                    throw new RuntimeException(var3);
                }

                this.cachedTxId = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(stream.toByteArray()));
            }
        }

        return this.cachedTxId;
    }

    public Sha256Hash getWTxId() {
        if (this.cachedWTxId == null) {
            if (!this.hasWitnesses() && this.cachedTxId != null) {
                this.cachedWTxId = this.cachedTxId;
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                    this.bitcoinSerializeToStream(baos, this.hasWitnesses());
                } catch (IOException var3) {
                    throw new RuntimeException(var3);
                }

                this.cachedWTxId = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(baos.toByteArray()));
            }
        }

        return this.cachedWTxId;
    }

    public Coin getInputSum() {
        Coin inputTotal = Coin.ZERO;
        Iterator var2 = this.inputs.iterator();

        while(var2.hasNext()) {
            TransactionInput input = (TransactionInput)var2.next();
            Coin inputValue = input.getValue();
            if (inputValue != null) {
                inputTotal = inputTotal.add(inputValue);
            }
        }

        return inputTotal;
    }

    public Coin getValueSentToMe(TransactionBag transactionBag) {
        Coin v = Coin.ZERO;
        Iterator var3 = this.outputs.iterator();

        while(var3.hasNext()) {
            TransactionOutput o = (TransactionOutput)var3.next();
            if (o.isMineOrWatched(transactionBag)) {
                v = v.add(o.getValue());
            }
        }

        return v;
    }

    @Nullable
    public Map<Sha256Hash, Integer> getAppearsInHashes() {
        return this.appearsInHashes != null ? ImmutableMap.copyOf(this.appearsInHashes) : null;
    }

    public boolean isPending() {
        return this.getConfidence().getConfidenceType() == ConfidenceType.PENDING;
    }

    public void setBlockAppearance(StoredBlock block, boolean bestChain, int relativityOffset) {
        long blockTime = block.getHeader().getTimeSeconds() * 1000L;
        if (bestChain && (this.updatedAt == null || this.updatedAt.getTime() == 0L || this.updatedAt.getTime() > blockTime)) {
            this.updatedAt = new Date(blockTime);
        }

        this.addBlockAppearance(block.getHeader().getHash(), relativityOffset);
        if (bestChain) {
            TransactionConfidence transactionConfidence = this.getConfidence();
            transactionConfidence.setAppearedAtChainHeight(block.getHeight());
        }

    }

    public void addBlockAppearance(Sha256Hash blockHash, int relativityOffset) {
        if (this.appearsInHashes == null) {
            this.appearsInHashes = new TreeMap();
        }

        this.appearsInHashes.put(blockHash, relativityOffset);
    }

    public Coin getValueSentFromMe(TransactionBag wallet) throws ScriptException {
        Coin v = Coin.ZERO;
        Iterator var3 = this.inputs.iterator();

        while(var3.hasNext()) {
            TransactionInput input = (TransactionInput)var3.next();
            TransactionOutput connected = input.getConnectedOutput(wallet.getTransactionPool(Pool.UNSPENT));
            if (connected == null) {
                connected = input.getConnectedOutput(wallet.getTransactionPool(Pool.SPENT));
            }

            if (connected == null) {
                connected = input.getConnectedOutput(wallet.getTransactionPool(Pool.PENDING));
            }

            if (connected != null && connected.isMineOrWatched(wallet)) {
                v = v.add(connected.getValue());
            }
        }

        return v;
    }

    public Coin getOutputSum() {
        Coin totalOut = Coin.ZERO;

        TransactionOutput output;
        for(Iterator var2 = this.outputs.iterator(); var2.hasNext(); totalOut = totalOut.add(output.getValue())) {
            output = (TransactionOutput)var2.next();
        }

        return totalOut;
    }

    public Coin getValue(TransactionBag wallet) throws ScriptException {
        boolean isAndroid = Utils.isAndroidRuntime();
        if (isAndroid && this.cachedValue != null && this.cachedForBag == wallet) {
            return this.cachedValue;
        } else {
            Coin result = this.getValueSentToMe(wallet).subtract(this.getValueSentFromMe(wallet));
            if (isAndroid) {
                this.cachedValue = result;
                this.cachedForBag = wallet;
            }

            return result;
        }
    }

    public Coin getFee() {
        Coin fee = Coin.ZERO;
        if (!this.inputs.isEmpty() && !this.outputs.isEmpty()) {
            Iterator var2;
            TransactionInput input;
            for(var2 = this.inputs.iterator(); var2.hasNext(); fee = fee.add(input.getValue())) {
                input = (TransactionInput)var2.next();
                if (input.getValue() == null) {
                    return null;
                }
            }

            TransactionOutput output;
            for(var2 = this.outputs.iterator(); var2.hasNext(); fee = fee.subtract(output.getValue())) {
                output = (TransactionOutput)var2.next();
            }

            return fee;
        } else {
            return null;
        }
    }

    public boolean isAnyOutputSpent() {
        Iterator var1 = this.outputs.iterator();

        TransactionOutput output;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            output = (TransactionOutput)var1.next();
        } while(output.isAvailableForSpending());

        return true;
    }

    public boolean isEveryOwnedOutputSpent(TransactionBag transactionBag) {
        Iterator var2 = this.outputs.iterator();

        TransactionOutput output;
        do {
            if (!var2.hasNext()) {
                return true;
            }

            output = (TransactionOutput)var2.next();
        } while(!output.isAvailableForSpending() || !output.isMineOrWatched(transactionBag));

        return false;
    }

    public Date getUpdateTime() {
        if (this.updatedAt == null) {
            this.updatedAt = new Date(0L);
        }

        return this.updatedAt;
    }

    public void setUpdateTime(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    protected void unCache() {
        super.unCache();
        this.cachedTxId = null;
        this.cachedWTxId = null;
    }

    protected static int calcLength(byte[] buf, int offset) {
        int cursor = offset + 4;
        VarInt varint = new VarInt(buf, cursor);
        long txInCount = varint.value;
        cursor += varint.getOriginalSizeInBytes();

        int i;
        long scriptLen;
        for(i = 0; (long)i < txInCount; ++i) {
            cursor += 36;
            varint = new VarInt(buf, cursor);
            scriptLen = varint.value;
            cursor = (int)((long)cursor + scriptLen + 4L + (long)varint.getOriginalSizeInBytes());
        }

        varint = new VarInt(buf, cursor);
        long txOutCount = varint.value;
        cursor += varint.getOriginalSizeInBytes();

        for(i = 0; (long)i < txOutCount; ++i) {
            cursor += 8;
            varint = new VarInt(buf, cursor);
            scriptLen = varint.value;
            cursor = (int)((long)cursor + scriptLen + (long)varint.getOriginalSizeInBytes());
        }

        return cursor - offset + 4;
    }

    protected void parse() throws ProtocolException {
        this.cursor = this.offset;
        this.optimalEncodingMessageSize = 4;
        this.version = this.readUint32();
        byte marker = this.payload[this.cursor];
        boolean useSegwit = marker == 0;
        if (useSegwit) {
            this.readBytes(2);
            this.optimalEncodingMessageSize += 2;
        }

        this.parseInputs();
        this.parseOutputs();
        if (useSegwit) {
            this.parseWitnesses();
        }

        this.lockTime = this.readUint32();
        this.optimalEncodingMessageSize += 4;
        this.length = this.cursor - this.offset;
    }

    private void parseInputs() {
        long numInputs = this.readVarInt();
        this.optimalEncodingMessageSize += VarInt.sizeOf(numInputs);
        this.inputs = new ArrayList(Math.min((int)numInputs, 20));

        for(long i = 0L; i < numInputs; ++i) {
            TransactionInput input = new TransactionInput(this.params, this, this.payload, this.cursor, this.serializer);
            this.inputs.add(input);
            long scriptLen = this.readVarInt(36);
            this.optimalEncodingMessageSize = (int)((long)this.optimalEncodingMessageSize + (long)(36 + VarInt.sizeOf(scriptLen)) + scriptLen + 4L);
            this.cursor = (int)((long)this.cursor + scriptLen + 4L);
        }

    }

    private void parseOutputs() {
        long numOutputs = this.readVarInt();
        this.optimalEncodingMessageSize += VarInt.sizeOf(numOutputs);
        this.outputs = new ArrayList(Math.min((int)numOutputs, 20));

        for(long i = 0L; i < numOutputs; ++i) {
            TransactionOutput output = new TransactionOutput(this.params, this, this.payload, this.cursor, this.serializer);
            this.outputs.add(output);
            long scriptLen = this.readVarInt(8);
            this.optimalEncodingMessageSize = (int)((long)this.optimalEncodingMessageSize + (long)(8 + VarInt.sizeOf(scriptLen)) + scriptLen);
            this.cursor = (int)((long)this.cursor + scriptLen);
        }

    }

    private void parseWitnesses() {
        int numWitnesses = this.inputs.size();

        for(int i = 0; i < numWitnesses; ++i) {
            long pushCount = this.readVarInt();
            TransactionWitness witness = new TransactionWitness((int)pushCount);
            this.getInput((long)i).setWitness(witness);
            this.optimalEncodingMessageSize += VarInt.sizeOf(pushCount);

            for(int y = 0; (long)y < pushCount; ++y) {
                long pushSize = this.readVarInt();
                this.optimalEncodingMessageSize = (int)((long)this.optimalEncodingMessageSize + (long)VarInt.sizeOf(pushSize) + pushSize);
                byte[] push = this.readBytes((int)pushSize);
                witness.setPush(y, push);
            }
        }

    }

    public boolean hasWitnesses() {
        Iterator var1 = this.inputs.iterator();

        TransactionInput in;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            in = (TransactionInput)var1.next();
        } while(!in.hasWitness());

        return true;
    }

    public int getOptimalEncodingMessageSize() {
        if (this.optimalEncodingMessageSize != 0) {
            return this.optimalEncodingMessageSize;
        } else {
            this.optimalEncodingMessageSize = this.getMessageSize();
            return this.optimalEncodingMessageSize;
        }
    }

    public int getMessageSizeForPriorityCalc() {
        int size = this.getMessageSize();
        Iterator var2 = this.inputs.iterator();

        while(var2.hasNext()) {
            TransactionInput input = (TransactionInput)var2.next();
            int benefit = 41 + Math.min(110, input.getScriptSig().getProgram().length);
            if (size > benefit) {
                size -= benefit;
            }
        }

        return size;
    }

    public boolean isCoinBase() {
        return this.inputs.size() == 1 && ((TransactionInput)this.inputs.get(0)).isCoinBase();
    }

    public boolean isMature() {
        if (!this.isCoinBase()) {
            return true;
        } else if (this.getConfidence().getConfidenceType() != ConfidenceType.BUILDING) {
            return false;
        } else {
            return this.getConfidence().getDepthInBlocks() >= this.params.getSpendableCoinbaseDepth();
        }
    }

    public String toString() {
        ToStringHelper helper = MoreObjects.toStringHelper(this);
        helper.addValue(this.toString((AbstractBlockChain)null, (CharSequence)null));
        return helper.toString();
    }

    public String toString(@Nullable AbstractBlockChain chain, @Nullable CharSequence indent) {
        if (indent == null) {
            indent = "";
        }

        StringBuilder s = new StringBuilder();
        Sha256Hash txId = this.getTxId();
        Sha256Hash wTxId = this.getWTxId();
        s.append((CharSequence)indent).append(txId);
        if (!wTxId.equals(txId)) {
            s.append(", wtxid ").append(wTxId);
        }

        s.append('\n');
        if (this.updatedAt != null) {
            s.append((CharSequence)indent).append("updated: ").append(Utils.dateTimeFormat(this.updatedAt)).append('\n');
        }

        if (this.version != 1L) {
            s.append((CharSequence)indent).append("version ").append(this.version).append('\n');
        }

        if (this.isTimeLocked()) {
            s.append((CharSequence)indent).append("time locked until ");
            if (this.lockTime < 500000000L) {
                s.append("block ").append(this.lockTime);
                if (chain != null) {
                    s.append(" (estimated to be reached at ").append(Utils.dateTimeFormat(chain.estimateBlockTime((int)this.lockTime))).append(')');
                }
            } else {
                s.append(Utils.dateTimeFormat(this.lockTime * 1000L));
            }

            s.append('\n');
        }

        if (this.hasRelativeLockTime()) {
            s.append((CharSequence)indent).append("has relative lock time\n");
        }

        if (this.isOptInFullRBF()) {
            s.append((CharSequence)indent).append("opts into full replace-by-fee\n");
        }

        if (this.purpose != null) {
            s.append((CharSequence)indent).append("purpose: ").append(this.purpose).append('\n');
        }

        if (this.isCoinBase()) {
            String script;
            String script2;
            try {
                script = ((TransactionInput)this.inputs.get(0)).getScriptSig().toString();
                script2 = ((TransactionOutput)this.outputs.get(0)).getScriptPubKey().toString();
            } catch (ScriptException var14) {
                script = "???";
                script2 = "???";
            }

            s.append((CharSequence)indent).append("   == COINBASE TXN (scriptSig ").append(script).append(")  (scriptPubKey ").append(script2).append(")\n");
            return s.toString();
        } else {
            if (!this.inputs.isEmpty()) {
                int i = 0;

                for(Iterator var7 = this.inputs.iterator(); var7.hasNext(); ++i) {
                    TransactionInput in = (TransactionInput)var7.next();
                    s.append((CharSequence)indent).append("   ");
                    s.append("in   ");

                    try {
                        s.append(in.getScriptSig());
                        Coin value = in.getValue();
                        if (value != null) {
                            s.append("  ").append(value.toFriendlyString());
                        }

                        s.append('\n');
                        if (in.hasWitness()) {
                            s.append((CharSequence)indent).append("        witness:");
                            s.append(in.getWitness());
                            s.append('\n');
                        }

                        TransactionOutPoint outpoint = in.getOutpoint();
                        TransactionOutput connectedOutput = outpoint.getConnectedOutput();
                        s.append((CharSequence)indent).append("        ");
                        if (connectedOutput != null) {
                            Script scriptPubKey = connectedOutput.getScriptPubKey();
                            ScriptType scriptType = scriptPubKey.getScriptType();
                            if (scriptType != null) {
                                s.append(scriptType).append(" addr:").append(scriptPubKey.getToAddress(this.params));
                            } else {
                                s.append("unknown script type");
                            }
                        } else {
                            s.append("unconnected");
                        }

                        s.append("  outpoint:").append(outpoint).append('\n');
                        if (in.hasSequence()) {
                            s.append((CharSequence)indent).append("        sequence:").append(Long.toHexString(in.getSequenceNumber()));
                            if (in.isOptInFullRBF()) {
                                s.append(", opts into full RBF");
                            }

                            if (this.version >= 2L && in.hasRelativeLockTime()) {
                                s.append(", has RLT");
                            }

                            s.append('\n');
                        }
                    } catch (Exception var15) {
                        s.append("[exception: ").append(var15.getMessage()).append("]\n");
                    }
                }
            } else {
                s.append((CharSequence)indent).append("   ");
                s.append("INCOMPLETE: No inputs!\n");
            }

            Iterator var17 = this.outputs.iterator();

            while(var17.hasNext()) {
                TransactionOutput out = (TransactionOutput)var17.next();
                s.append((CharSequence)indent).append("   ");
                s.append("out  ");

                try {
                    Script scriptPubKey = out.getScriptPubKey();
                    s.append(scriptPubKey.getChunks().size() > 0 ? scriptPubKey.toString() : "<no scriptPubKey>");
                    s.append("  ");
                    s.append(out.getValue().toFriendlyString());
                    s.append('\n');
                    s.append((CharSequence)indent).append("        ");
                    ScriptType scriptType = scriptPubKey.getScriptType();
                    if (scriptType != null) {
                        s.append(scriptType).append(" addr:").append(scriptPubKey.getToAddress(this.params));
                    } else {
                        s.append("unknown script type");
                    }

                    if (!out.isAvailableForSpending()) {
                        s.append("  spent");
                        TransactionInput spentBy = out.getSpentBy();
                        if (spentBy != null) {
                            s.append(" by:");
                            s.append(spentBy.getParentTransaction().getTxId()).append(':').append(spentBy.getIndex());
                        }
                    }

                    if (scriptType != null || !out.isAvailableForSpending()) {
                        s.append('\n');
                    }
                } catch (Exception var16) {
                    s.append("[exception: ").append(var16.getMessage()).append("]\n");
                }
            }

            Coin fee = this.getFee();
            if (fee != null) {
                int size = this.unsafeBitcoinSerialize().length;
                s.append((CharSequence)indent).append("   fee  ").append(fee.multiply(1000L).divide((long)size).toFriendlyString()).append("/kB, ").append(fee.toFriendlyString()).append(" for ").append(size).append(" bytes\n");
            }

            return s.toString();
        }
    }

    public void clearInputs() {
        this.unCache();
        Iterator var1 = this.inputs.iterator();

        while(var1.hasNext()) {
            TransactionInput input = (TransactionInput)var1.next();
            input.setParent((Message)null);
        }

        this.inputs.clear();
        this.length = this.unsafeBitcoinSerialize().length;
    }

    public TransactionInput addInput(TransactionOutput from) {
        return this.addInput(new TransactionInput(this.params, this, from));
    }

    public TransactionInput addInput(TransactionInput input) {
        this.unCache();
        input.setParent(this);
        this.inputs.add(input);
        this.adjustLength(this.inputs.size(), input.length);
        return input;
    }

    public TransactionInput addInput(Sha256Hash spendTxHash, long outputIndex, Script script) {
        return this.addInput(new TransactionInput(this.params, this, script.getProgram(), new TransactionOutPoint(this.params, outputIndex, spendTxHash)));
    }

    public TransactionInput addSignedInput(TransactionOutPoint prevOut, Script scriptPubKey, ECKey sigKey, Transaction.SigHash sigHash, boolean anyoneCanPay) throws ScriptException {
        Preconditions.checkState(!this.outputs.isEmpty(), "Attempting to sign tx without outputs.");
        TransactionInput input = new TransactionInput(this.params, this, new byte[0], prevOut);
        this.addInput(input);
        int inputIndex = this.inputs.size() - 1;
        TransactionSignature signature;
        if (ScriptPattern.isP2PK(scriptPubKey)) {
            signature = this.calculateSignature(inputIndex, sigKey, scriptPubKey, sigHash, anyoneCanPay);
            input.setScriptSig(ScriptBuilder.createInputScript(signature));
            input.setWitness((TransactionWitness)null);
        } else if (ScriptPattern.isP2PKH(scriptPubKey)) {
            signature = this.calculateSignature(inputIndex, sigKey, scriptPubKey, sigHash, anyoneCanPay);
            input.setScriptSig(ScriptBuilder.createInputScript(signature, sigKey));
            input.setWitness((TransactionWitness)null);
        } else {
            if (!ScriptPattern.isP2WPKH(scriptPubKey)) {
                throw new ScriptException(ScriptError.SCRIPT_ERR_UNKNOWN_ERROR, "Don't know how to sign for this kind of scriptPubKey: " + scriptPubKey);
            }

            Script scriptCode = (new ScriptBuilder()).data(ScriptBuilder.createOutputScript(LegacyAddress.fromKey(this.params, sigKey)).getProgram()).build();
            TransactionSignature signature = this.calculateWitnessSignature(inputIndex, sigKey, scriptCode, input.getValue(), sigHash, anyoneCanPay);
            input.setScriptSig(ScriptBuilder.createEmpty());
            input.setWitness(TransactionWitness.redeemP2WPKH(signature, sigKey));
        }

        return input;
    }

    public TransactionInput addSignedInput(TransactionOutPoint prevOut, Script scriptPubKey, ECKey sigKey) throws ScriptException {
        return this.addSignedInput(prevOut, scriptPubKey, sigKey, Transaction.SigHash.ALL, false);
    }

    public TransactionInput addSignedInput(TransactionOutput output, ECKey signingKey) {
        return this.addSignedInput(output.getOutPointFor(), output.getScriptPubKey(), signingKey);
    }

    public TransactionInput addSignedInput(TransactionOutput output, ECKey signingKey, Transaction.SigHash sigHash, boolean anyoneCanPay) {
        return this.addSignedInput(output.getOutPointFor(), output.getScriptPubKey(), signingKey, sigHash, anyoneCanPay);
    }

    public void clearOutputs() {
        this.unCache();
        Iterator var1 = this.outputs.iterator();

        while(var1.hasNext()) {
            TransactionOutput output = (TransactionOutput)var1.next();
            output.setParent((Message)null);
        }

        this.outputs.clear();
        this.length = this.unsafeBitcoinSerialize().length;
    }

    public TransactionOutput addOutput(TransactionOutput to) {
        this.unCache();
        to.setParent(this);
        this.outputs.add(to);
        this.adjustLength(this.outputs.size(), to.length);
        return to;
    }

    public TransactionOutput addOutput(Coin value, Address address) {
        return this.addOutput(new TransactionOutput(this.params, this, value, address));
    }

    public TransactionOutput addOutput(Coin value, ECKey pubkey) {
        return this.addOutput(new TransactionOutput(this.params, this, value, pubkey));
    }

    public TransactionOutput addOutput(Coin value, Script script) {
        return this.addOutput(new TransactionOutput(this.params, this, value, script.getProgram()));
    }

    public TransactionSignature calculateSignature(int inputIndex, ECKey key, byte[] redeemScript, Transaction.SigHash hashType, boolean anyoneCanPay) {
        Sha256Hash hash = this.hashForSignature(inputIndex, redeemScript, hashType, anyoneCanPay);
        return new TransactionSignature(key.sign(hash), hashType, anyoneCanPay);
    }

    public TransactionSignature calculateSignature(int inputIndex, ECKey key, Script redeemScript, Transaction.SigHash hashType, boolean anyoneCanPay) {
        Sha256Hash hash = this.hashForSignature(inputIndex, redeemScript.getProgram(), hashType, anyoneCanPay);
        return new TransactionSignature(key.sign(hash), hashType, anyoneCanPay);
    }

    public TransactionSignature calculateSignature(int inputIndex, ECKey key, @Nullable KeyParameter aesKey, byte[] redeemScript, Transaction.SigHash hashType, boolean anyoneCanPay) {
        Sha256Hash hash = this.hashForSignature(inputIndex, redeemScript, hashType, anyoneCanPay);
        return new TransactionSignature(key.sign(hash, aesKey), hashType, anyoneCanPay);
    }

    public TransactionSignature calculateSignature(int inputIndex, ECKey key, @Nullable KeyParameter aesKey, Script redeemScript, Transaction.SigHash hashType, boolean anyoneCanPay) {
        Sha256Hash hash = this.hashForSignature(inputIndex, redeemScript.getProgram(), hashType, anyoneCanPay);
        return new TransactionSignature(key.sign(hash, aesKey), hashType, anyoneCanPay);
    }

    public Sha256Hash hashForSignature(int inputIndex, byte[] redeemScript, Transaction.SigHash type, boolean anyoneCanPay) {
        byte sigHashType = (byte)TransactionSignature.calcSigHashValue(type, anyoneCanPay);
        return this.hashForSignature(inputIndex, redeemScript, sigHashType);
    }

    public Sha256Hash hashForSignature(int inputIndex, Script redeemScript, Transaction.SigHash type, boolean anyoneCanPay) {
        int sigHash = TransactionSignature.calcSigHashValue(type, anyoneCanPay);
        return this.hashForSignature(inputIndex, redeemScript.getProgram(), (byte)sigHash);
    }

    public Sha256Hash hashForSignature(int inputIndex, byte[] connectedScript, byte sigHashType) {
        try {
            Transaction tx = this.params.getDefaultSerializer().makeTransaction(this.bitcoinSerialize());

            for(int i = 0; i < tx.inputs.size(); ++i) {
                TransactionInput input = (TransactionInput)tx.inputs.get(i);
                input.clearScriptBytes();
                input.setWitness((TransactionWitness)null);
            }

            connectedScript = Script.removeAllInstancesOfOp(connectedScript, 171);
            TransactionInput input = (TransactionInput)tx.inputs.get(inputIndex);
            input.setScriptBytes(connectedScript);
            int i;
            if ((sigHashType & 31) == Transaction.SigHash.NONE.value) {
                tx.outputs = new ArrayList(0);

                for(i = 0; i < tx.inputs.size(); ++i) {
                    if (i != inputIndex) {
                        ((TransactionInput)tx.inputs.get(i)).setSequenceNumber(0L);
                    }
                }
            } else if ((sigHashType & 31) == Transaction.SigHash.SINGLE.value) {
                if (inputIndex >= tx.outputs.size()) {
                    return Sha256Hash.wrap("0100000000000000000000000000000000000000000000000000000000000000");
                }

                tx.outputs = new ArrayList(tx.outputs.subList(0, inputIndex + 1));

                for(i = 0; i < inputIndex; ++i) {
                    tx.outputs.set(i, new TransactionOutput(tx.params, tx, Coin.NEGATIVE_SATOSHI, new byte[0]));
                }

                for(i = 0; i < tx.inputs.size(); ++i) {
                    if (i != inputIndex) {
                        ((TransactionInput)tx.inputs.get(i)).setSequenceNumber(0L);
                    }
                }
            }

            if ((sigHashType & Transaction.SigHash.ANYONECANPAY.value) == Transaction.SigHash.ANYONECANPAY.value) {
                tx.inputs = new ArrayList();
                tx.inputs.add(input);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream(tx.length);
            tx.bitcoinSerializeToStream(bos, false);
            Utils.uint32ToByteStreamLE((long)(255 & sigHashType), bos);
            Sha256Hash hash = Sha256Hash.twiceOf(bos.toByteArray());
            bos.close();
            return hash;
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        }
    }

    public TransactionSignature calculateWitnessSignature(int inputIndex, ECKey key, byte[] scriptCode, Coin value, Transaction.SigHash hashType, boolean anyoneCanPay) {
        Sha256Hash hash = this.hashForWitnessSignature(inputIndex, scriptCode, value, hashType, anyoneCanPay);
        return new TransactionSignature(key.sign(hash), hashType, anyoneCanPay);
    }

    public TransactionSignature calculateWitnessSignature(int inputIndex, ECKey key, Script scriptCode, Coin value, Transaction.SigHash hashType, boolean anyoneCanPay) {
        return this.calculateWitnessSignature(inputIndex, key, scriptCode.getProgram(), value, hashType, anyoneCanPay);
    }

    public TransactionSignature calculateWitnessSignature(int inputIndex, ECKey key, @Nullable KeyParameter aesKey, byte[] scriptCode, Coin value, Transaction.SigHash hashType, boolean anyoneCanPay) {
        Sha256Hash hash = this.hashForWitnessSignature(inputIndex, scriptCode, value, hashType, anyoneCanPay);
        return new TransactionSignature(key.sign(hash, aesKey), hashType, anyoneCanPay);
    }

    public TransactionSignature calculateWitnessSignature(int inputIndex, ECKey key, @Nullable KeyParameter aesKey, Script scriptCode, Coin value, Transaction.SigHash hashType, boolean anyoneCanPay) {
        return this.calculateWitnessSignature(inputIndex, key, aesKey, scriptCode.getProgram(), value, hashType, anyoneCanPay);
    }

    public synchronized Sha256Hash hashForWitnessSignature(int inputIndex, byte[] scriptCode, Coin prevValue, Transaction.SigHash type, boolean anyoneCanPay) {
        int sigHash = TransactionSignature.calcSigHashValue(type, anyoneCanPay);
        return this.hashForWitnessSignature(inputIndex, scriptCode, prevValue, (byte)sigHash);
    }

    public synchronized Sha256Hash hashForWitnessSignature(int inputIndex, Script scriptCode, Coin prevValue, Transaction.SigHash type, boolean anyoneCanPay) {
        return this.hashForWitnessSignature(inputIndex, scriptCode.getProgram(), prevValue, type, anyoneCanPay);
    }

    public synchronized Sha256Hash hashForWitnessSignature(int inputIndex, byte[] scriptCode, Coin prevValue, byte sigHashType) {
        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(this.length == -2147483648 ? 256 : this.length + 4);

        try {
            byte[] hashPrevouts = new byte[32];
            byte[] hashSequence = new byte[32];
            byte[] hashOutputs = new byte[32];
            int basicSigHashType = sigHashType & 31;
            boolean anyoneCanPay = (sigHashType & Transaction.SigHash.ANYONECANPAY.value) == Transaction.SigHash.ANYONECANPAY.value;
            boolean signAll = basicSigHashType != Transaction.SigHash.SINGLE.value && basicSigHashType != Transaction.SigHash.NONE.value;
            UnsafeByteArrayOutputStream bosHashOutputs;
            int i;
            if (!anyoneCanPay) {
                bosHashOutputs = new UnsafeByteArrayOutputStream(256);

                for(i = 0; i < this.inputs.size(); ++i) {
                    bosHashOutputs.write(((TransactionInput)this.inputs.get(i)).getOutpoint().getHash().getReversedBytes());
                    Utils.uint32ToByteStreamLE(((TransactionInput)this.inputs.get(i)).getOutpoint().getIndex(), bosHashOutputs);
                }

                hashPrevouts = Sha256Hash.hashTwice(bosHashOutputs.toByteArray());
            }

            if (!anyoneCanPay && signAll) {
                bosHashOutputs = new UnsafeByteArrayOutputStream(256);

                for(i = 0; i < this.inputs.size(); ++i) {
                    Utils.uint32ToByteStreamLE(((TransactionInput)this.inputs.get(i)).getSequenceNumber(), bosHashOutputs);
                }

                hashSequence = Sha256Hash.hashTwice(bosHashOutputs.toByteArray());
            }

            if (signAll) {
                bosHashOutputs = new UnsafeByteArrayOutputStream(256);

                for(i = 0; i < this.outputs.size(); ++i) {
                    Utils.uint64ToByteStreamLE(BigInteger.valueOf(((TransactionOutput)this.outputs.get(i)).getValue().getValue()), bosHashOutputs);
                    bosHashOutputs.write((new VarInt((long)((TransactionOutput)this.outputs.get(i)).getScriptBytes().length)).encode());
                    bosHashOutputs.write(((TransactionOutput)this.outputs.get(i)).getScriptBytes());
                }

                hashOutputs = Sha256Hash.hashTwice(bosHashOutputs.toByteArray());
            } else if (basicSigHashType == Transaction.SigHash.SINGLE.value && inputIndex < this.outputs.size()) {
                bosHashOutputs = new UnsafeByteArrayOutputStream(256);
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(((TransactionOutput)this.outputs.get(inputIndex)).getValue().getValue()), bosHashOutputs);
                bosHashOutputs.write((new VarInt((long)((TransactionOutput)this.outputs.get(inputIndex)).getScriptBytes().length)).encode());
                bosHashOutputs.write(((TransactionOutput)this.outputs.get(inputIndex)).getScriptBytes());
                hashOutputs = Sha256Hash.hashTwice(bosHashOutputs.toByteArray());
            }

            Utils.uint32ToByteStreamLE(this.version, bos);
            bos.write(hashPrevouts);
            bos.write(hashSequence);
            bos.write(((TransactionInput)this.inputs.get(inputIndex)).getOutpoint().getHash().getReversedBytes());
            Utils.uint32ToByteStreamLE(((TransactionInput)this.inputs.get(inputIndex)).getOutpoint().getIndex(), bos);
            bos.write(scriptCode);
            Utils.uint64ToByteStreamLE(BigInteger.valueOf(prevValue.getValue()), bos);
            Utils.uint32ToByteStreamLE(((TransactionInput)this.inputs.get(inputIndex)).getSequenceNumber(), bos);
            bos.write(hashOutputs);
            Utils.uint32ToByteStreamLE(this.lockTime, bos);
            Utils.uint32ToByteStreamLE((long)(255 & sigHashType), bos);
        } catch (IOException var14) {
            throw new RuntimeException(var14);
        }

        return Sha256Hash.twiceOf(bos.toByteArray());
    }

    protected void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        boolean useSegwit = this.hasWitnesses() && this.protocolVersion >= ProtocolVersion.WITNESS_VERSION.getBitcoinProtocolVersion();
        this.bitcoinSerializeToStream(stream, useSegwit);
    }

    protected void bitcoinSerializeToStream(OutputStream stream, boolean useSegwit) throws IOException {
        Utils.uint32ToByteStreamLE(this.version, stream);
        if (useSegwit) {
            stream.write(0);
            stream.write(1);
        }

        stream.write((new VarInt((long)this.inputs.size())).encode());
        Iterator var3 = this.inputs.iterator();

        TransactionInput in;
        while(var3.hasNext()) {
            in = (TransactionInput)var3.next();
            in.bitcoinSerialize(stream);
        }

        stream.write((new VarInt((long)this.outputs.size())).encode());
        var3 = this.outputs.iterator();

        while(var3.hasNext()) {
            TransactionOutput out = (TransactionOutput)var3.next();
            out.bitcoinSerialize(stream);
        }

        if (useSegwit) {
            var3 = this.inputs.iterator();

            while(var3.hasNext()) {
                in = (TransactionInput)var3.next();
                in.getWitness().bitcoinSerializeToStream(stream);
            }
        }

        Utils.uint32ToByteStreamLE(this.lockTime, stream);
    }

    public long getLockTime() {
        return this.lockTime;
    }

    public void setLockTime(long lockTime) {
        this.unCache();
        boolean seqNumSet = false;
        Iterator var4 = this.inputs.iterator();

        while(var4.hasNext()) {
            TransactionInput input = (TransactionInput)var4.next();
            if (input.getSequenceNumber() != 4294967295L) {
                seqNumSet = true;
                break;
            }
        }

        if (lockTime != 0L && (!seqNumSet || this.inputs.isEmpty())) {
            log.warn("You are setting the lock time on a transaction but none of the inputs have non-default sequence numbers. This will not do what you expect!");
        }

        this.lockTime = lockTime;
    }

    public long getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = (long)version;
        this.unCache();
    }

    public List<TransactionInput> getInputs() {
        return Collections.unmodifiableList(this.inputs);
    }

    public List<TransactionOutput> getOutputs() {
        return Collections.unmodifiableList(this.outputs);
    }

    public List<TransactionOutput> getWalletOutputs(TransactionBag transactionBag) {
        List<TransactionOutput> walletOutputs = new LinkedList();
        Iterator var3 = this.outputs.iterator();

        while(var3.hasNext()) {
            TransactionOutput o = (TransactionOutput)var3.next();
            if (o.isMineOrWatched(transactionBag)) {
                walletOutputs.add(o);
            }
        }

        return walletOutputs;
    }

    public void shuffleOutputs() {
        Collections.shuffle(this.outputs);
    }

    public TransactionInput getInput(long index) {
        return (TransactionInput)this.inputs.get((int)index);
    }

    public TransactionOutput getOutput(long index) {
        return (TransactionOutput)this.outputs.get((int)index);
    }

    public TransactionConfidence getConfidence() {
        return this.getConfidence(Context.get());
    }

    public TransactionConfidence getConfidence(Context context) {
        return this.getConfidence(context.getConfidenceTable());
    }

    public TransactionConfidence getConfidence(TxConfidenceTable table) {
        if (this.confidence == null) {
            this.confidence = table.getOrCreate(this.getTxId());
        }

        return this.confidence;
    }

    public boolean hasConfidence() {
        return this.getConfidence().getConfidenceType() != ConfidenceType.UNKNOWN;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && this.getClass() == o.getClass() ? this.getTxId().equals(((Transaction)o).getTxId()) : false;
        }
    }

    public int hashCode() {
        return this.getTxId().hashCode();
    }

    public int getSigOpCount() throws ScriptException {
        int sigOps = 0;

        Iterator var2;
        TransactionInput input;
        for(var2 = this.inputs.iterator(); var2.hasNext(); sigOps += Script.getSigOpCount(input.getScriptBytes())) {
            input = (TransactionInput)var2.next();
        }

        TransactionOutput output;
        for(var2 = this.outputs.iterator(); var2.hasNext(); sigOps += Script.getSigOpCount(output.getScriptBytes())) {
            output = (TransactionOutput)var2.next();
        }

        return sigOps;
    }

    public void checkCoinBaseHeight(int height) throws VerificationException {
        Preconditions.checkArgument(height >= 0);
        Preconditions.checkState(this.isCoinBase());
        TransactionInput in = (TransactionInput)this.getInputs().get(0);
        ScriptBuilder builder = new ScriptBuilder();
        builder.number((long)height);
        byte[] expected = builder.build().getProgram();
        byte[] actual = in.getScriptBytes();
        if (actual.length < expected.length) {
            throw new CoinbaseHeightMismatch("Block height mismatch in coinbase.");
        } else {
            for(int scriptIdx = 0; scriptIdx < expected.length; ++scriptIdx) {
                if (actual[scriptIdx] != expected[scriptIdx]) {
                    throw new CoinbaseHeightMismatch("Block height mismatch in coinbase.");
                }
            }

        }
    }

    public Sha256Hash findWitnessCommitment() {
        Preconditions.checkState(this.isCoinBase());
        Iterator var1 = Lists.reverse(this.outputs).iterator();

        Script scriptPubKey;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            TransactionOutput out = (TransactionOutput)var1.next();
            scriptPubKey = out.getScriptPubKey();
        } while(!ScriptPattern.isWitnessCommitment(scriptPubKey));

        return ScriptPattern.extractWitnessCommitmentHash(scriptPubKey);
    }

    public void verify() throws VerificationException {
        if (this.inputs.size() != 0 && this.outputs.size() != 0) {
            if (this.getMessageSize() > 1000000) {
                throw new LargerThanMaxBlockSize();
            } else {
                Coin valueOut = Coin.ZERO;
                HashSet<TransactionOutPoint> outpoints = new HashSet();
                Iterator var3 = this.inputs.iterator();

                TransactionInput input;
                while(var3.hasNext()) {
                    input = (TransactionInput)var3.next();
                    if (outpoints.contains(input.getOutpoint())) {
                        throw new DuplicatedOutPoint();
                    }

                    outpoints.add(input.getOutpoint());
                }

                try {
                    var3 = this.outputs.iterator();

                    while(var3.hasNext()) {
                        TransactionOutput output = (TransactionOutput)var3.next();
                        if (output.getValue().signum() < 0) {
                            throw new NegativeValueOutput();
                        }

                        valueOut = valueOut.add(output.getValue());
                        if (this.params.hasMaxMoney() && valueOut.compareTo(this.params.getMaxMoney()) > 0) {
                            throw new IllegalArgumentException();
                        }
                    }
                } catch (IllegalStateException var5) {
                    throw new ExcessiveValue();
                } catch (IllegalArgumentException var6) {
                    throw new ExcessiveValue();
                }

                if (this.isCoinBase()) {
                    if (((TransactionInput)this.inputs.get(0)).getScriptBytes().length < 2 || ((TransactionInput)this.inputs.get(0)).getScriptBytes().length > 100) {
                        throw new CoinbaseScriptSizeOutOfRange();
                    }
                } else {
                    var3 = this.inputs.iterator();

                    while(var3.hasNext()) {
                        input = (TransactionInput)var3.next();
                        if (input.isCoinBase()) {
                            throw new UnexpectedCoinbaseInput();
                        }
                    }
                }

            }
        } else {
            throw new EmptyInputsOrOutputs();
        }
    }

    public boolean isTimeLocked() {
        if (this.getLockTime() == 0L) {
            return false;
        } else {
            Iterator var1 = this.getInputs().iterator();

            TransactionInput input;
            do {
                if (!var1.hasNext()) {
                    return false;
                }

                input = (TransactionInput)var1.next();
            } while(!input.hasSequence());

            return true;
        }
    }

    public boolean hasRelativeLockTime() {
        if (this.version < 2L) {
            return false;
        } else {
            Iterator var1 = this.getInputs().iterator();

            TransactionInput input;
            do {
                if (!var1.hasNext()) {
                    return false;
                }

                input = (TransactionInput)var1.next();
            } while(!input.hasRelativeLockTime());

            return true;
        }
    }

    public boolean isOptInFullRBF() {
        Iterator var1 = this.getInputs().iterator();

        TransactionInput input;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            input = (TransactionInput)var1.next();
        } while(!input.isOptInFullRBF());

        return true;
    }

    public boolean isFinal(int height, long blockTimeSeconds) {
        long time = this.getLockTime();
        return time < (time < 500000000L ? (long)height : blockTimeSeconds) || !this.isTimeLocked();
    }

    public Date estimateLockTime(AbstractBlockChain chain) {
        return this.lockTime < 500000000L ? chain.estimateBlockTime((int)this.getLockTime()) : new Date(this.getLockTime() * 1000L);
    }

    public Transaction.Purpose getPurpose() {
        return this.purpose;
    }

    public void setPurpose(Transaction.Purpose purpose) {
        this.purpose = purpose;
    }

    @Nullable
    public ExchangeRate getExchangeRate() {
        return this.exchangeRate;
    }

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Nullable
    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public static enum SigHash {
        ALL(1),
        NONE(2),
        SINGLE(3),
        ANYONECANPAY(128),
        ANYONECANPAY_ALL(129),
        ANYONECANPAY_NONE(130),
        ANYONECANPAY_SINGLE(131),
        UNSET(0);

        public final int value;

        private SigHash(int value) {
            this.value = value;
        }

        public byte byteValue() {
            return (byte)this.value;
        }
    }

    public static enum Purpose {
        UNKNOWN,
        USER_PAYMENT,
        KEY_ROTATION,
        ASSURANCE_CONTRACT_CLAIM,
        ASSURANCE_CONTRACT_PLEDGE,
        ASSURANCE_CONTRACT_STUB,
        RAISE_FEE;

        private Purpose() {
        }
    }
}
*/
