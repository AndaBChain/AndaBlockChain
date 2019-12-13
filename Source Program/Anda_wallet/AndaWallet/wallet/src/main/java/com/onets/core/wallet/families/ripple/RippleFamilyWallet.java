package com.onets.core.wallet.families.ripple;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.nxt.Convert;
import com.onets.core.exceptions.TransactionBroadcastException;
import com.onets.core.network.AddressStatus;
import com.onets.core.network.BlockHeader;
import com.onets.core.network.ServerClient;
import com.onets.core.network.interfaces.BlockchainConnection;
import com.onets.core.network.interfaces.TransactionEventListener;
import com.onets.core.protos.Protos;
import com.onets.core.util.KeyUtils;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.AbstractTransaction;
import com.onets.core.wallet.AbstractWallet;
import com.onets.core.wallet.SendRequest;
import com.onets.core.wallet.SignedMessage;
import com.onets.core.wallet.Wallet;
import com.onets.core.wallet.WalletAccountEventListener;
import com.onets.core.wallet.WalletConnectivityStatus;
import com.onets.wallet.WalletApplication;
import com.ripple.client.Account;
import com.ripple.config.Config;
import com.ripple.crypto.ecdsa.Seed;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.utils.ListenerRegistration;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.RedeemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.onets.core.Preconditions.checkNotNull;
import static com.onets.core.Preconditions.checkState;

/**
 * @author Yu K.Q.
 */
public class RippleFamilyWallet extends AbstractWallet<RippleTransaction, RippleAddress>
        implements TransactionEventListener<RippleTransaction> {

    private static final Logger log = LoggerFactory.getLogger(RippleFamilyWallet.class);

    private final RippleAddress address;

    private Value balance;
    private int lastEcBlockHeight;
    private long lastEcBlockId;
    // Wallet that this account belongs
    @Nullable
    private transient Wallet wallet = null;
    private RippleServerClient blockchainConnection;
    @Nullable
    private Sha256Hash lastBlockSeenHash;
    private int lastBlockSeenHeight = -1;
    private long lastBlockSeenTimeSecs = 0;
    private List<ListenerRegistration<WalletAccountEventListener>> listeners;

    RippleFamilyKey rootkey;

    @com.google.common.annotations.VisibleForTesting
    final HashMap<AbstractAddress, String> addressesStatus;
    @com.google.common.annotations.VisibleForTesting
    final transient ArrayList<AbstractAddress> addressesSubscribed;
    @com.google.common.annotations.VisibleForTesting
    final transient ArrayList<AbstractAddress> addressesPendingSubscription;
    @com.google.common.annotations.VisibleForTesting
    final transient HashMap<AbstractAddress, AddressStatus> statusPendingUpdates;


    public RippleFamilyWallet(DeterministicKey entropy, CoinType type) {
        this(entropy, type, null, null);
    }

    public RippleFamilyWallet(DeterministicKey entropy, CoinType type,
                              @Nullable KeyCrypter keyCrypter, @Nullable KeyParameter key) {
        this(new RippleFamilyKey(entropy, keyCrypter, key), type);
    }

    public RippleFamilyWallet(RippleFamilyKey key, CoinType type) {
        this(KeyUtils.getPublicKeyId(type, key.getAddress().getBytes()), key, type);
    }

    public RippleFamilyWallet(String id, RippleFamilyKey key, CoinType type) {
        super(type, id);

        rawtransactions = new HashMap<>();

        addressesStatus = new HashMap<>();
        addressesSubscribed = new ArrayList<>();
        addressesPendingSubscription = new ArrayList<>();
        statusPendingUpdates = new HashMap<>();


        rootkey = key;
        String addr = new String(key.getPrivateKey());
        Config.initBouncy();
//        SecureRandom random = new SecureRandom();
//        byte[] seedBytes = new byte[16];
//        random.nextBytes(seedBytes);
        byte[] seedBytes = Arrays.copyOfRange(addr.getBytes(),0,16);
        Seed seed = new Seed(seedBytes);
        Account account = WalletApplication.getRippleClient().accountFromSeed(seed + "");
        if (!account.getAccountRoot().primed()) {
            Log.i(TAG, "RippleFamilyWallet:     init  account  ");
        }
        address = new RippleAddress(type, key.getAddress());
        balance = type.value(0);
        Log.e("------------------", "RippleFamilyWallet: balance:" + balance.toFriendlyString());
        listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public byte[] getPublicKey() {
        return rootkey.getAddress().getBytes();
    }

    @Override
    public String getPublicKeyMnemonic() {
        return address.getHexAddress();
    }

    @Override
    public SendRequest getEmptyWalletRequest(AbstractAddress destination) throws WalletAccountException {
        checkAddress(destination);
        return RippleSendRequest.emptyWallet(this, (RippleAddress) destination);
    }

    @Override
    public SendRequest getSendToRequest(AbstractAddress destination, Value amount) throws WalletAccountException {
        checkAddress(destination);
        return RippleSendRequest.to(this, (RippleAddress) destination, amount);
    }

    private void checkAddress(AbstractAddress destination) throws WalletAccountException {
        if (!(destination instanceof RippleAddress)) {
            throw new WalletAccountException("Incompatible address" +
                    destination.getClass().getName() + ", expected " + RippleAddress.class.getName());
        }
    }


    @Override
    public void completeTransaction(SendRequest request) throws WalletAccountException {
        //todo :  completeTransaction
        checkSendRequest(request);
        completeTransaction((RippleSendRequest) request);
    }

    @Override
    public void signTransaction(SendRequest request) throws WalletAccountException {
        checkSendRequest(request);
        signTransaction((RippleSendRequest) request);
    }

    private void checkSendRequest(SendRequest request) throws WalletAccountException {
        if (!(request instanceof RippleSendRequest)) {
            throw new WalletAccountException("Incompatible request " +
                    request.getClass().getName() + ", expected " + RippleSendRequest.class.getName());
        }
    }

    public void completeTransaction(RippleSendRequest request) throws WalletAccountException {
        checkArgument(!request.isCompleted(), "Given SendRequest has already been completed.");
        
        request.tx = new RippleTransaction(type, request.rippleTxBuilder.build());
        request.setCompleted(true);
        
        if (request.signTransaction) {
            signTransaction(request);
        }
    }

    public void signTransaction(RippleSendRequest request) {
        checkArgument(request.isCompleted(), "Send request is not completed");
        checkArgument(request.tx != null, "No transaction found in send request");
        RippleTransactionImpl tx = request.tx.getRawTransaction();

        byte[] privateKey;
        if (rootkey.isEncrypted()) {
            checkArgument(request.aesKey != null, "Wallet is encrypted but no decryption key provided");
            privateKey = rootkey.toDecrypted(request.aesKey).getPrivateKey();
        } else {
            privateKey = rootkey.getPrivateKey();
        }

        //todo:  Ripple交易签名
        try {
            String addr = new String(privateKey);
            tx.getResult().getTx_json().setTxnSignature(addr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Arrays.fill(privateKey, (byte) 0); // clear private key

    }

    @Override
    public void signMessage(SignedMessage unsignedMessage, @Nullable KeyParameter aesKey) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void verifyMessage(SignedMessage signedMessage) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public String getPublicKeySerialized() {
        return Convert.toHexString(getPublicKey());
    }

    @Override
    public SendRequest sendCoins(AbstractAddress address, CoinType coinType, Value Amount, String s, String password) throws WalletAccountException {
        return null;
    }

    @Override
    public boolean isNew() {
        // TODO implement, how can we check if this account is new?
        return true;
    }

    @Override
    public Value getBalance() {
        return balance;
    }

    @Override
    public void refresh() {
        lock.lock();
        try {
            log.info("Refreshing wallet pocket {}", type);
            lastBlockSeenHash = null;
            lastBlockSeenHeight = -1;
            lastBlockSeenTimeSecs = 0;
            lastEcBlockHeight = 0;
            lastEcBlockId = 0;

            rawtransactions.clear();
            addressesStatus.clear();
            clearTransientState();
        } finally {
            lock.unlock();
        }
    }

    private void clearTransientState() {
        addressesSubscribed.clear();
        addressesPendingSubscription.clear();
        statusPendingUpdates.clear();
    }

    @Override
    public boolean isConnected() {
        return blockchainConnection != null;
    }

    @Override
    public boolean isLoading() {
//        TODO implement
        return false;
    }

    @Override
    public void disconnect() {
        if (blockchainConnection != null) {
            blockchainConnection.stopAsync();
        }
    }

    @Override
    public AbstractAddress getChangeAddress() {
        return address;
    }

    @Override
    public AbstractAddress getReceiveAddress() {
        return address;
    }

    @Override
    public RippleAddress getReceiveAddress(boolean isManualAddressManagement) {
        return this.address;
    }


    @Override
    public boolean hasUsedAddresses() {
        return false;
    }

    @Override
    public boolean canCreateNewAddresses() {
        return false;
    }

    @Override
    public boolean broadcastTxSync(AbstractTransaction tx) throws TransactionBroadcastException {
        Log.i(TAG, "broadcastTxSync: 广播发送交易");
        if (tx instanceof RippleTransaction) {
            return broadcastEthTxSync((RippleTransaction) tx);
        } else {
            throw new TransactionBroadcastException("Unsupported transaction class: " +
                    tx.getClass().getName() + ", need: " + RippleTransaction.class.getName());
        }
    }


    public boolean broadcastEthTxSync(RippleTransaction tx) throws TransactionBroadcastException {
        Log.i(TAG, "broadcastEthTxSync: 网络是否通畅：" + isConnected());
        if (isConnected()) {
            boolean success = blockchainConnection.broadcastTxSync(tx);
            if (success) {
                onTransactionBroadcast(tx);
            } else {
                onTransactionBroadcastError(tx);
            }
            return success;
        } else {
            throw new TransactionBroadcastException("No connection available");
        }
    }


    @Override
    public void broadcastTx(AbstractTransaction tx) throws TransactionBroadcastException {
        Log.i(TAG, "broadcastTx: 接口------广播发送交易");
        if (tx instanceof RippleTransaction) {
            broadcastTx((RippleTransaction) tx, this);
        } else {
            throw new TransactionBroadcastException("Unsupported transaction class: " +
                    tx.getClass().getName() + ", need: " + RippleTransaction.class.getName());
        }

    }

    private void broadcastTx(RippleTransaction tx, TransactionEventListener<RippleTransaction> listener)
            throws TransactionBroadcastException {
        if (isConnected()) {
            lock.lock();
            try {
                blockchainConnection.broadcastTx(tx, listener != null ? listener : this);
            } finally {
                lock.unlock();
            }
        } else {
            throw new TransactionBroadcastException("No connection available");
        }
    }

    @Override
    public AbstractAddress getRefundAddress(boolean isManualAddressManagement) {
        return address;
    }


    protected final Map<Sha256Hash, RippleTransaction> rawtransactions;

    @Override
    public Map<Sha256Hash, RippleTransaction> getTransactions() {
        lock.lock();
        try {
            return ImmutableMap.copyOf(rawtransactions);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public RippleTransaction getTransaction(String transactionId) {
        lock.lock();
        try {
            return rawtransactions.get(new Sha256Hash(transactionId));
        } finally {
            lock.unlock();
        }

    }

    @Override
    public Map<Sha256Hash, RippleTransaction> getPendingTransactions() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AbstractAddress> getActiveAddresses() {
        return ImmutableList.of((AbstractAddress) address);
    }

    @Override
    public void markAddressAsUsed(AbstractAddress address) { /* does not apply */ }

    @Override
    public Wallet getWallet() {
        return wallet;
    }

    @Override
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public void walletSaveLater() {
//        throw new RuntimeException("Not implemented");
    }

    @Override
    public void walletSaveNow() {
//        throw new RuntimeException("Not implemented");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Serialization support
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    List<Protos.Key> serializeKeychainToProtobuf() {
        lock.lock();
        try {
            return rootkey.toProtobuf();
        } finally {
            lock.unlock();
        }
    }


    @Override
    public boolean isEncryptable() {
        return true;
    }

    @Override
    public boolean isEncrypted() {
        lock.lock();
        try {
            return rootkey.isEncrypted();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public KeyCrypter getKeyCrypter() {
        lock.lock();
        try {
            return rootkey.getKeyCrypter();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void encrypt(KeyCrypter keyCrypter, KeyParameter aesKey) {
        checkNotNull(keyCrypter);
        checkNotNull(aesKey);

        lock.lock();
        try {
            this.rootkey = this.rootkey.toEncrypted(keyCrypter, aesKey);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void decrypt(KeyParameter aesKey) {
        throw new RuntimeException("Not implemented");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Transaction signing support
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Other stuff TODO implement
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addEventListener(WalletAccountEventListener listener) {
        addEventListener(listener, Threading.USER_THREAD);
    }

    @Override
    public void addEventListener(WalletAccountEventListener listener, Executor executor) {
        listeners.add(new ListenerRegistration<>(listener, executor));
    }

    @Override
    public boolean removeEventListener(WalletAccountEventListener listener) {
        return ListenerRegistration.removeFromList(listener, listeners);
    }

    @Override
    public boolean isAddressMine(AbstractAddress address) {
        String localAddress = this.address.getHexAddress();
        String remoteAddress = address.toString();
        if (localAddress.equals(remoteAddress)) {
            Log.i(TAG, "isAddressMine:   return   true");
            return true;
        } else {
            Log.i(TAG, "isAddressMine:   return   false");
            return false;
        }
    }

    @Override
    public void maybeInitializeAllKeys() { /* Doesn't need initialization */ }

    @Override
    public void onConnection(BlockchainConnection blockchainConnection) {
        this.blockchainConnection = (RippleServerClient) blockchainConnection;

        subscribeToBlockchain();
        subscribeIfNeeded();

    }

    void subscribeIfNeeded() {
        lock.lock();
        try {
            if (blockchainConnection != null) {
                List<AbstractAddress> addressesToWatch = getAddressesToWatch();
                if (addressesToWatch.size() > 0) {
                    addressesPendingSubscription.addAll(addressesToWatch);
                    blockchainConnection.subscribeToAddresses(addressesToWatch, this);
                }
            }
        } catch (Exception e) {
            log.error("Error subscribing to addresses", e);
        } finally {
            lock.unlock();
        }
    }

    @VisibleForTesting
    List<AbstractAddress> getAddressesToWatch() {
        ImmutableList.Builder<AbstractAddress> addressesToWatch = ImmutableList.builder();
        for (AbstractAddress address : getActiveAddresses()) {
            // If address not already subscribed or pending subscription
            if (!addressesSubscribed.contains(address) && !addressesPendingSubscription.contains(address)) {
                addressesToWatch.add(address);
            }
        }
        return addressesToWatch.build();
    }

    private void subscribeToBlockchain() {
        lock.lock();
        try {
            if (blockchainConnection != null) {
                blockchainConnection.subscribeToBlockchain(this);
            }
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void onDisconnect() {
        blockchainConnection = null;
        queueOnConnectivity();
    }

    @Nullable
    @Override
    public ECKey findKeyFromPubHash(byte[] pubkeyHash) {
        throw new RuntimeException("Not implemented");
    }

    @Nullable
    @Override
    public ECKey findKeyFromPubKey(byte[] pubkey) {
        throw new RuntimeException("Not implemented");
    }

    @Nullable
    @Override
    public RedeemData findRedeemDataFromScriptHash(byte[] scriptHash) {
        throw new RuntimeException("Not implemented");
    }

//    @Nullable
//    public synchronized Transaction getRawTransaction(Sha256Hash hash) {
//        lock.lock();
//        try {
//            RippleTransaction tx = rawtransactions.get(hash);
//            if (tx != null) {
//                return tx.getRawTransaction();
//            } else {
//                return null;
//            }
//        } finally {
//            lock.unlock();
//        }
//    }

    void queueOnConnectivity() {
        final WalletConnectivityStatus connectivity = getConnectivityStatus();
        for (final ListenerRegistration<WalletAccountEventListener> registration : listeners) {
            registration.executor.execute(new Runnable() {
                @Override
                public void run() {
                    registration.listener.onConnectivityStatus(connectivity);
                    registration.listener.onWalletChanged(RippleFamilyWallet.this);
                }
            });
        }
    }

    @Override
    public void onNewBlock(BlockHeader header) {

    }

    @Override
    public void onBlockUpdate(BlockHeader header) {

    }

    @Override
    public void onAddressStatusUpdate(AddressStatus status) {
        Log.e("------  监听  ------", "onAddressStatusUpdate: " + status.getStatus());
        log.debug("Got a status {}", status);
        lock.lock();
        try {
            if (status.getStatus() != null) {
                if (isAddressStatusChanged(status)) {

                    this.balance = Value.valueOf(this.type, status.getStatus().replaceAll(",", ""));
                    Log.e(TAG, "onAddressStatusUpdate: balance :  " + balance);
                    log.info("Must get transactions for address {}, status {}",
                            status.getAddress(), status.getStatus());

                    if (blockchainConnection != null) {
                        blockchainConnection.getHistoryTx(status, this);
                    }
                }

            } else {
                Log.e(TAG, "commitAddressStatus(status) ");
                commitAddressStatus(status);
            }
        } finally {
            lock.unlock();
        }

    }

    private String TAG = "-------RippleFamilyWallet";

    private boolean isAddressStatusChanged(AddressStatus addressStatus) {
        Log.e(TAG, "isAddressStatusChanged:      1   ");
        lock.lock();
        try {
            AbstractAddress address = addressStatus.getAddress();
            String newStatus = addressStatus.getStatus();
            Log.e(TAG, "isAddressStatusChanged:      address  " + address.getType().getName()
                    + "\nnewStatus:" + newStatus);
            if (addressesStatus.containsKey(address)) {
                Log.e(TAG, "isAddressStatusChanged:      true   ");
                String previousStatus = addressesStatus.get(address);
                if (previousStatus == null) {
                    return newStatus != null; // Status changed if newStatus is not null
                } else {
                    return !previousStatus.equals(newStatus);
                }
            } else {
                Log.e(TAG, "isAddressStatusChanged:      false   ");

                // Unused address, just mark it that we watch it
                if (newStatus == null) {
                    Log.e(TAG, "newStatus:  null  ");

                    commitAddressStatus(addressStatus);
                    return false;
                } else {
                    Log.e(TAG, "newStatus:  return  true  ");

                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    void commitAddressStatus(AddressStatus newStatus) {
        lock.lock();
        try {
            /*AddressStatus updatingStatus = statusPendingUpdates.get(newStatus.getAddress());
            if (updatingStatus != null && updatingStatus.equals(newStatus)) {
                statusPendingUpdates.remove(newStatus.getAddress());
            }*/
            addressesStatus.put(newStatus.getAddress(), newStatus.getStatus());
        } finally {
            lock.unlock();
        }
        // Skip saving null statuses
        if (newStatus.getStatus() != null) {
            walletSaveLater();
        }
    }


    @Override
    public void onTransactionHistory(AddressStatus status, List<ServerClient.HistoryTx> historyTxes) {
        log.info("onTransactionHistory");
        lock.lock();
        try {
            //AddressStatus updatingStatus = statusPendingUpdates.get(status.getAddress());
            // Check if this updating status is valid
            status.queueHistoryTransactions(historyTxes);
            log.info("Fetching txs");
            fetchTransactions(historyTxes);
            queueOnNewBalance();
            //tryToApplyState(updatingStatus);
        } finally {
            lock.unlock();
        }
    }


    private void fetchTransactions(List<? extends ServerClient.HistoryTx> txes) {
        checkState(lock.isHeldByCurrentThread(), "Lock is held by another thread");
//        for (ServerClient.HistoryTx tx : txes) {
//            Log.i(TAG, "-----------------------  fetchTransactions:     ");
//            fetchTransactionIfNeeded(tx.getTxHash());
//        }
        fetchTransactionIfNeeded(new Sha256Hash("1111111111111111111111111111111111111111111111111111111111111111"));
    }

    private void fetchTransactionIfNeeded(Sha256Hash txHash) {
        checkState(lock.isHeldByCurrentThread(), "Lock is held by another thread");
        // Check if need to fetch the transaction
//        log.info("Trying to fetch transaction with hash ");
//        if (!isTransactionAvailableOrQueued(txHash)) {
//            log.info("Going to fetch transaction with hash {}", txHash);
//            //fetchingTransactions.add(txHash);
            if (blockchainConnection != null) {
                blockchainConnection.getTransaction(txHash, this);
            }
//        } else {
//            log.info("cannot fetch tx with hash {}", txHash);
//        }
    }

    private boolean isTransactionAvailableOrQueued(Sha256Hash txHash) {
        checkState(lock.isHeldByCurrentThread(), "Lock is held by another thread");
        return rawtransactions.containsKey(txHash);
    }


    void queueOnNewBalance() {
        checkState(lock.isHeldByCurrentThread(), "Lock is held by another thread");
        final Value balance = getBalance();
        for (final ListenerRegistration<WalletAccountEventListener> registration : listeners) {
            registration.executor.execute(new Runnable() {
                @Override
                public void run() {
                    registration.listener.onNewBalance(balance);
                    registration.listener.onWalletChanged(RippleFamilyWallet.this);
                }
            });
        }
    }


    @Override
    public void onTransactionUpdate(RippleTransaction tx) {
        if (log.isInfoEnabled()) log.info("Got a new transaction {}", tx.getHashAsString());
        lock.lock();
        try {
            addNewTransactionIfNeeded(tx);
        } finally {
            lock.unlock();
        }
    }


    @com.google.common.annotations.VisibleForTesting
    void addNewTransactionIfNeeded(RippleTransaction tx) {
        lock.lock();
        try {
            // If was fetching this tx, remove it
            //fetchingTransactions.remove(tx.getFullHash());
            log.info("adding transaction to wallet");
            // This tx not in wallet, add it
            RippleTransaction storedTx = rawtransactions.get(tx.getHash());
            if (storedTx == null) {
                log.info("transaction added");
                rawtransactions.put(tx.getHash(), tx);
                //tx.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.PENDING);
                //addWalletTransaction(WalletTransaction.Pool.PENDING, tx, true);
                queueOnNewBalance();
            } else {
                storedTx.setDepthInBlocks(tx.getDepthInBlocks());
                storedTx.setAppearedAtChainHeight(tx.getAppearedAtChainHeight());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onTransactionBroadcast(RippleTransaction transaction) {
        lock.lock();
        try {
            log.info("Transaction sent {}", transaction);
            //FIXME, when enabled it breaks the transactions connections and we get an incorrect coin balance
            addNewTransactionIfNeeded(transaction);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onTransactionBroadcastError(RippleTransaction transaction) {
        Log.e(TAG, "onTransactionBroadcastError: -----   " + transaction.getType().getName());

    }
}
