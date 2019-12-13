package com.onets.core.wallet;

import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.onets.core.CoreUtils;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.Value;
import com.onets.core.coins.families.AndaFamily;
import com.onets.core.coins.families.BitFamily;
import com.onets.core.coins.families.EthereumFamily;
import com.onets.core.coins.families.RippleFamily;
import com.onets.core.exceptions.UnsupportedCoinTypeException;
import com.onets.core.protos.Protos;
import com.onets.core.wallet.families.andachain.utils.ECKey;
import com.onets.core.wallet.families.andachain.utils.HashUtil;
import com.onets.core.wallet.families.ethereum.EthereumFamilyWallet;
import com.onets.core.wallet.families.ripple.RippleFamilyWallet;
import com.onets.wallet.Constants;

import org.bitcoinj.core.TransactionBroadcaster;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.onets.core.CoreUtils.bytesToMnemonic;

/**
 * @author Yu K.Q.
 * 钱包
 */
final public class Wallet {
    private static final Logger log = LoggerFactory.getLogger(Wallet.class);
    private static final String TAG = "Wallet";
    public static int ENTROPY_SIZE_DEBUG = -1;

    private final ReentrantLock lock = Threading.lock("KeyChain");


    @GuardedBy("lock")
    private final LinkedHashMap<CoinType, ArrayList<WalletAccount>> accountsByType;
    @GuardedBy("lock")
    private final LinkedHashMap<String, WalletAccount> accounts;

    //种子密码
    @Nullable
    private DeterministicSeed seed;
    private DeterministicSeed bit_seed;

    private DeterministicKey masterKey;
    public static DeterministicKey rootKey;

    protected volatile WalletFiles vFileManager;

    // FIXME, make multi account capable
    //多账户
    private final static int ACCOUNT_ZERO = 0;

    private int version = 2;

    public static String pass;

    public Wallet(String mnemonic) throws MnemonicException {
        this(CoreUtils.parseMnemonic(mnemonic), null);

    }

    public Wallet(List<String> mnemonic) throws MnemonicException {
        this(mnemonic, null);
    }

    /**
     * 通过助记词和种子密码生成种子
     * @param mnemonic
     * @param seedPassword
     * @throws MnemonicException
     */
    public Wallet(List<String> mnemonic, @Nullable String seedPassword) throws MnemonicException {
        Log.d(TAG, Constants.LOG_LABLE + "Wallet: ----mnemonic  : " + mnemonic.size() + "   |  seedPassword :" + seedPassword);
        MnemonicCode.INSTANCE.check(mnemonic);
        seedPassword = seedPassword == null ? "" : seedPassword;

        seed = new DeterministicSeed(mnemonic, null, seedPassword, 0);
        Log.d(TAG, Constants.LOG_LABLE + "Wallet: seed " + seed.toHexString());
        Log.d(TAG, Constants.LOG_LABLE + "Wallet: seed " + seed.toString());

        try {
            bit_seed =new DeterministicSeed(Joiner.on(" ").join(seed.getMnemonicCode()),null,seedPassword,seed.getCreationTimeSeconds());
            Log.d(TAG, Constants.LOG_LABLE + "Wallet: bit_seed" + bit_seed.toHexString());
            Log.d(TAG, Constants.LOG_LABLE + "Wallet: bit_seed" + bit_seed.toString());
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }

        ECKey eckey = ECKey.fromPrivate(HashUtil.sm3_Hash(seedPassword.getBytes()));
        masterKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());

        Log.d(TAG, Constants.LOG_LABLE + "wallet---masterKey--私钥: "+masterKey.serializePrivB58());
        Log.d(TAG, Constants.LOG_LABLE + "wallet----masterKey-----公钥: "+masterKey.serializePubB58());
        Log.d(TAG, Constants.LOG_LABLE + "wallet masterKey pri " + masterKey.getPrivKey());
        Log.d(TAG, Constants.LOG_LABLE + "wallet masterKey pub " + Hex.toHexString(masterKey.getPubKey()));

        accountsByType = new LinkedHashMap<CoinType, ArrayList<WalletAccount>>();
        accounts = new LinkedHashMap<String, WalletAccount>();
    }

    public Wallet(DeterministicKey masterKey, @Nullable DeterministicSeed seed) {
        this.seed = seed;
        this.masterKey = masterKey;
        accountsByType = new LinkedHashMap<CoinType, ArrayList<WalletAccount>>();
        accounts = new LinkedHashMap<String, WalletAccount>();
    }

    /**
     * 生成助记符字符串列表
     * @param entropyBitsSize
     * @return
     */
    public static List<String> generateMnemonic(int entropyBitsSize) {
        byte[] entropy;
        if (ENTROPY_SIZE_DEBUG > 0) {
            entropy = new byte[ENTROPY_SIZE_DEBUG];
        } else {
            entropy = new byte[entropyBitsSize / 8];
        }

        SecureRandom sr = new SecureRandom();
        sr.nextBytes(entropy);

        return bytesToMnemonic(entropy);
    }

    /**
     * 生成助记符字符串
     * @param entropyBitsSize
     * @return
     */
    public static String generateMnemonicString(int entropyBitsSize) {
        List<String> mnemonicWords = Wallet.generateMnemonic(entropyBitsSize);
        return mnemonicToString(mnemonicWords);
    }

    /**
     * 助记符自妇产列表转换为字符串
     * @param mnemonicWords
     * @return
     */
    public static String mnemonicToString(List<String> mnemonicWords) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mnemonicWords.size(); i++) {
            if (i != 0) sb.append(' ');
            sb.append(mnemonicWords.get(i));
        }
        return sb.toString();
    }

    /**
     * 生成随机数ID
     * @return
     */
    static String generateRandomId() {
        byte[] randomIdBytes = new byte[32];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(randomIdBytes);
        return Hex.toHexString(randomIdBytes);
    }

    /**
     * 创建账户
     * @param coin
     * @param key
     * @return
     */
    public WalletAccount createAccount(CoinType coin, @Nullable KeyParameter key) {
        return createAccount(coin, false, key);
    }

    /**
     * 创建账户
     * @param coin
     * @param generateAllKeys
     * @param key
     * @return
     */
    public WalletAccount createAccount(CoinType coin, boolean generateAllKeys,
                                       @Nullable KeyParameter key) {
        Log.d(TAG, "测试createAccount " + key);
        return createAccounts(Lists.newArrayList(coin), generateAllKeys, key).get(0);
    }

    /**
     * 创建账户
     * @param coins
     * @param generateAllKeys
     * @param key
     * @return
     */
    public List<WalletAccount> createAccounts(List<CoinType> coins, boolean generateAllKeys,
                                              @Nullable KeyParameter key) {
        lock.lock();
        Log.i(TAG, "createAccounts: 开始创建账户");
        try {
            ImmutableList.Builder<WalletAccount> newAccounts = ImmutableList.builder();
            for (CoinType coin : coins) {
                log.info("-------  Creating coin pocket for {}", coin);
                WalletAccount newAccount = createAndAddAccount(coin, key);
                if (generateAllKeys) {
                    newAccount.maybeInitializeAllKeys();
                }
                newAccounts.add(newAccount);
            }
            return newAccounts.build();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check if at least one account exists for a specific coin
     * 检测账户是否存在
     */
    public boolean isAccountExists(CoinType coinType) {
        lock.lock();
        try {
            return accountsByType.containsKey(coinType);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check if account exists
     * 检测账户是否存在
     */
    public boolean isAccountExists(@Nullable String accountId) {
        if (accountId == null) return false;
        lock.lock();
        try {
            return accounts.containsKey(accountId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get a specific account, null if does not exist
     * 获取账户
     */
    @Nullable
    public WalletAccount getAccount(@Nullable String accountId) {
        if (accountId == null) return null;
        lock.lock();
        try {
            return accounts.get(accountId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get accounts for a specific coin type. Returns empty list if no account exists
     * 获取账户－根据币类型
     */
    public List<WalletAccount> getAccounts(CoinType coinType) {
        return getAccounts(Lists.newArrayList(coinType));
    }

    /**
     * Get accounts for a specific coin type. Returns empty list if no account exists
     * 获取账户列表－根据币类型
     */
    public List<WalletAccount> getAccounts(List<CoinType> types) {
        lock.lock();
        try {
            ImmutableList.Builder<WalletAccount> builder = ImmutableList.builder();
            for (CoinType type : types) {
                if (isAccountExists(type)) {
                    builder.addAll(accountsByType.get(type));
                }
            }
            return builder.build();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get accounts that watch a specific address. Returns empty list if no account exists
     * 获取账户－根据地址
     */
    public List<WalletAccount> getAccounts(final AbstractAddress address) {
        lock.lock();
        try {
            ImmutableList.Builder<WalletAccount> builder = ImmutableList.builder();
            CoinType type = address.getType();
            if (isAccountExists(type)) {
                for (WalletAccount account : accountsByType.get(type)) {
                    if (account.isAddressMine(address)) {
                        builder.add(account);
                    }
                }
            }
            return builder.build();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取所有的账户
     * @return
     */
    public List<WalletAccount> getAllAccounts() {
        lock.lock();
        try {
            return ImmutableList.copyOf(accounts.values());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取账户ID列表
     * @return
     */
    public List getAccountIds() {
        lock.lock();
        try {
            return ImmutableList.copyOf(accounts.keySet());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generate and add a new BIP44 account for a specific coin type
     * 创建和添加账户
     */
    private WalletAccount createAndAddAccount(CoinType coinType, @Nullable KeyParameter key) {
        checkState(lock.isHeldByCurrentThread(), "Lock is held by another thread");
        checkNotNull(coinType, "Attempting to create a pocket for a null coin");

        // TODO, currently we support a single account so return the existing account
        //当前账户
        List<WalletAccount> currentAccount = getAccounts(coinType);

        if (currentAccount.size() > 0) {
            return currentAccount.get(0);
        }
        // TODO ///////////////

        DeterministicHierarchy hierarchy;

        Log.d(TAG, "测试--masterkey  是否加密: "+isEncrypted());
        //检测是否加密
        if (isEncrypted()) {
            hierarchy = new DeterministicHierarchy(masterKey.decrypt(getKeyCrypter(), key));
        } else {
            hierarchy = new DeterministicHierarchy(masterKey);
        }

        //新的索引
        int newIndex = getLastAccountIndex(coinType) + 1;
        this.rootKey = hierarchy.get(coinType.getBip44Path(newIndex), false, true);
        this.pass = new String(rootKey.getPrivKeyBytes());
        Log.d(TAG, "测试--rootkey公钥: "+rootKey.serializePrivB58());
        Log.d(TAG, "测试--rootkey 私钥: "+rootKey.serializePubB58());
        Log.d(TAG, "测试-createAndAddAccount: getKeyCrypter() " + getKeyCrypter());
        WalletAccount newPocket;
        Log.d(TAG, "测试-createAndAddAccount: key " + key);
        //检测新钱包类型
        if (coinType instanceof BitFamily) {
            newPocket = new WalletPocketHD(rootKey, coinType, getKeyCrypter(), key);
        } else if (coinType instanceof EthereumFamily) {
            newPocket = new EthereumFamilyWallet(rootKey, coinType, getKeyCrypter(), key);
        } else if (coinType instanceof AndaFamily) {
            newPocket = new WalletPocketHD(rootKey, coinType, getKeyCrypter(), key);
        } else if (coinType instanceof RippleFamily) {
            newPocket = new RippleFamilyWallet(rootKey, coinType, getKeyCrypter(), key);
        } else {
            throw new UnsupportedCoinTypeException(coinType);
        }
        Log.d(TAG, "测试-createAndAddAccount: Address " + newPocket.getReceiveAddress().toString());


        if (isEncrypted() && !newPocket.isEncrypted()) {
            newPocket.encrypt(getKeyCrypter(), key);

        }
        addAccount(newPocket);
        return newPocket;
    }


    /**
     * Get the last BIP44 account index of an account in this wallet. If no accounts found return -1
     * 获取最后一个账户的index
     */
    private int getLastAccountIndex(CoinType coinType) {
        if (!isAccountExists(coinType)) {
            return -1;
        }
        int lastIndex = -1;
        for (WalletAccount account : accountsByType.get(coinType)) {
            if (account instanceof WalletPocketHD) {
                int index = ((WalletPocketHD) account).getAccountIndex();
                if (index > lastIndex) {
                    lastIndex = index;
                }
            }
        }
        return lastIndex;
    }

    /**
     * 添加账户
     * @param pocket
     */
    public void addAccount(WalletAccount pocket) {
        lock.lock();

        try {
            String id = pocket.getId();
            CoinType type = pocket.getCoinType();

            checkState(!accounts.containsKey(id), "Cannot replace an existing wallet pocket");

            if (!accountsByType.containsKey(type)) {
                accountsByType.put(type, new ArrayList<WalletAccount>());
            }
            accountsByType.get(type).add(pocket);
            accounts.put(pocket.getId(), pocket);
            //if()
            pocket.setWallet(this);
        }
        catch (Exception e){
        e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * 删除账户
     * @param id
     * @return
     */
    public WalletAccount deleteAccount(String id) {
        lock.lock();
        try {
            if (!accounts.containsKey(id)) {
                return null;
            }

            WalletAccount deletedAccount = accounts.remove(id);
            CoinType type = deletedAccount.getCoinType();
            ArrayList<WalletAccount> sameTypeAccounts = accountsByType.get(type);
            if (sameTypeAccounts != null) {
                if (!sameTypeAccounts.remove(deletedAccount)) {
                    log.warn("Could not find account in accounts by type index");
                }
                if (sameTypeAccounts.size() == 0) {
                    accountsByType.remove(type);
                }
            }
            deletedAccount.setWallet(null);
            deletedAccount.disconnect();
            saveNow();
            return deletedAccount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Make the wallet generate all the needed lookahead keys if needed
     */
    public void maybeInitializeAllPockets() {
        lock.lock();
        try {
            for (WalletAccount account : accounts.values()) {
                if (account instanceof WalletPocketHD) {
                    account.maybeInitializeAllKeys();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    //TODO remove public and implement seed password protection check/

    /**
     * 获取主密钥
     * @return
     */
    public DeterministicKey getMasterKey() {
        lock.lock();
        try {
            return masterKey;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a list of words that represent the seed or null if this chain is a watching chain.
     */
    @Nullable
    public List<String> getMnemonicCode() {
        if (seed == null) return null;
        lock.lock();
        try {
            return seed.getMnemonicCode();
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public DeterministicSeed getSeed() {
        lock.lock();
        try {
            return seed;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the {@link KeyCrypter} in use or null if the key chain is not encrypted.
     */
    @Nullable
    public KeyCrypter getKeyCrypter() {
        lock.lock();
        try {
            return masterKey.getKeyCrypter();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 设置版本
     * @param version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * 获取版本
     * @return
     */
    public int getVersion() {
        return version;
    }

    /**
     * 刷新
     * @param accountIdToReset
     * @return
     */
    public WalletAccount refresh(String accountIdToReset) {
        lock.lock();
        try {
            WalletAccount account = getAccount(accountIdToReset);
            if (account != null) {
                account.refresh();
                saveLater();
            }
            return account;
        } finally {
            lock.unlock();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Serialization support
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @VisibleForTesting
    Protos.Wallet toProtobuf() {
        lock.lock();
        try {
            return WalletProtobufSerializer.toProtobuf(this);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a wallet deserialized from the given file.
     * 从文件加载账户
     */
    public static Wallet loadFromFile(File f) throws UnreadableWalletException {
        try {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(f);
                return loadFromFileStream(stream);
            } finally {
                if (stream != null) stream.close();
            }
        } catch (IOException e) {
            throw new UnreadableWalletException("Could not open file", e);
        }
    }

    /**
     * Returns a wallet deserialized from the given input stream.
     * 从文件流加载账户
     */
    public static Wallet loadFromFileStream(InputStream stream) throws UnreadableWalletException {
        return WalletProtobufSerializer.readWallet(stream);
    }

    /**
     * Uses protobuf serialization to save the wallet to the given file stream. To learn more about this file format, see
     * {@link WalletProtobufSerializer}.
     * 保存到文件流
     */
    public void saveToFileStream(OutputStream f) throws IOException {
        lock.lock();
        try {
            WalletProtobufSerializer.writeWallet(this, f);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Saves the wallet first to the given temp file, then renames to the dest file.
     * 保存到文件
     */
    public void saveToFile(File temp, File destFile) throws IOException {
        FileOutputStream stream = null;
        lock.lock();
        try {
            stream = new FileOutputStream(temp);
            saveToFileStream(stream);
            // Attempt to force the bits to hit the disk. In reality the OS or hard disk itself may still decide
            // to not write through to physical media for at least a few seconds, but this is the best we can do.
            stream.flush();
            stream.getFD().sync();
            stream.close();
            stream = null;
            Log.d(TAG, "saveToFile: ---------success");
            if (!temp.renameTo(destFile)) {
                throw new IOException("Failed to rename " + temp + " to " + destFile);
            }
        } catch (RuntimeException e) {
            log.error("Failed whilst saving wallet", e);
            throw e;
        } finally {
            lock.unlock();
            if (stream != null) {
                stream.close();
            }
            if (temp.exists()) {
                log.warn("Temp file still exists after failed save.");
            }
        }
    }
    public void saveToFile(File f) throws IOException {
        File directory = f.getAbsoluteFile().getParentFile();
        File temp = File.createTempFile("wallet", null, directory);
        saveToFile(temp, f);
    }
    /**
     * <p>Sets up the wallet to auto-save itself to the given file, using temp files with atomic renames to ensure
     * consistency. After connecting to a file, you no longer need to save the wallet manually, it will do it
     * whenever necessary. Protocol buffer serialization will be used.</p>
     * <p>
     * <p>If delayTime is set, a background thread will be created and the wallet will only be saved to
     * disk every so many time units. If no changes have occurred for the given time period, nothing will be written.
     * In this way disk IO can be rate limited. It's a good idea to set this as otherwise the wallet can change very
     * frequently, eg if there are a lot of transactions in it or during block sync, and there will be a lot of redundant
     * writes. Note that when a new key is added, that always results in an immediate save regardless of
     * delayTime. <b>You should still save the wallet manually when your program is about to shut down as the JVM
     * will not wait for the background thread.</b></p>
     * <p>
     * <p>An event listener can be provided. If a delay >0 was specified, it will be called on a background thread
     * with the wallet locked when an auto-save occurs. If delay is zero or you do something that always triggers
     * an immediate save, like adding a key, the event listener will be invoked on the calling threads.</p>
     *
     * @param f             The destination file to save to.
     * @param delayTime     How many time units to wait until saving the wallet on a background thread.
     * @param timeUnit      the unit of measurement for delayTime.
     * @param eventListener callback to be informed when the auto-save thread does things, or null
     * 自动保存到文件
     */
    public WalletFiles autosaveToFile(File f, long delayTime, TimeUnit timeUnit,
                                      @Nullable WalletFiles.Listener eventListener) {
        lock.lock();
        try {
            checkState(vFileManager == null, "Already auto saving this wallet.");
            WalletFiles manager = new WalletFiles(this, f, delayTime, timeUnit);
            if (eventListener != null) {
                manager.setListener(eventListener);
            }
            vFileManager = manager;
            return manager;
        } finally {
            lock.unlock();
        }
    }

    /**
     * <p>
     * Disables auto-saving, after it had been enabled with
     * {@link Wallet#autosaveToFile(File, long, TimeUnit, com.onets.core.wallet.WalletFiles.Listener)}
     * before. This method blocks until finished.
     * 关闭自动保存
     * </p>
     */
    public void shutdownAutosaveAndWait() {
        lock.lock();
        try {
            WalletFiles files = vFileManager;
            vFileManager = null;
            if (files != null) {
                files.shutdownAndWait();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Requests an asynchronous save on a background thread
     * 稍后保存
     */
    public void saveLater() {
        lock.lock();
        try {
            WalletFiles files = vFileManager;
            if (files != null) {
                files.saveLater();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * If auto saving is enabled, do an immediate sync write to disk ignoring any delays.
     * 现在保存
     */
    public void saveNow() {
        lock.lock();
        try {
            WalletFiles files = vFileManager;
            if (files != null) {
                try {
                    files.saveNow();  // This calls back into saveToFile().
                } catch (IOException e) {
                    // Can't really do much at this point, just let the API user know.
                    log.error("Failed to save wallet to disk!", e);
                    Thread.UncaughtExceptionHandler handler = Threading.uncaughtExceptionHandler;
                    if (handler != null)
                        handler.uncaughtException(Thread.currentThread(), e);
                }
            }
        } finally {
            lock.unlock();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Encryption support
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 判断是否加密
     * @return
     */
    public boolean isEncrypted() {
        lock.lock();
        try {
            return masterKey.isEncrypted();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Encrypt the keys in the group using the KeyCrypter and the AES key. A good default KeyCrypter to use is
     * {@link org.bitcoinj.crypto.KeyCrypterScrypt}.
     *
     * @throws org.bitcoinj.crypto.KeyCrypterException Thrown if the wallet encryption fails for some reason,
     *                                                 leaving the group unchanged.
     * 加密
     */
    public void encrypt(KeyCrypter keyCrypter, KeyParameter aesKey) {
        checkNotNull(keyCrypter, "Attempting to encrypt with a null KeyCrypter");
        checkNotNull(aesKey, "Attempting to encrypt with a null KeyParameter");

        lock.lock();
        try {
            if (seed != null) seed = seed.encrypt(keyCrypter, aesKey);
            masterKey = masterKey.encrypt(keyCrypter, aesKey, null);

            for (WalletAccount account : accounts.values()) {
                if (account.isEncryptable()) {
                    account.encrypt(keyCrypter, aesKey);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void decrypt(KeyParameter aesKey) {
        checkNotNull(aesKey, "Attemting to decrypt with a null KeyParameter");

        lock.lock();
        try {
            checkState(isEncrypted(), "Wallet is already decrypted");

            if (seed != null) {
                checkState(seed.isEncrypted(), "Seed is already decrypted");
                List<String> mnemonic = null;
                try {
                    mnemonic = decodeMnemonicCode(getKeyCrypter().decrypt(seed.getEncryptedData(), aesKey));
                } catch (UnreadableWalletException e) {
                    throw new RuntimeException(e);
                }
                seed = new DeterministicSeed(new byte[16], mnemonic, 0);
            }

            masterKey = masterKey.decrypt(getKeyCrypter(), aesKey);

            for (WalletAccount account : accounts.values()) {
                if (account.isEncryptable()) {
                    account.decrypt(aesKey);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private static List<String> decodeMnemonicCode(byte[] mnemonicCode) throws UnreadableWalletException {
        try {
            return Splitter.on(" ").splitToList(new String(mnemonicCode, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new UnreadableWalletException(e.toString());
        }
    }

    public List<Value> getBalances() {
        ImmutableList.Builder<Value> builder = ImmutableList.builder();
        lock.lock();
        try {
            for (WalletAccount account : accounts.values()) {
                builder.add(account.getBalance());
            }
            return builder.build();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 判断是否已经加载
     * @return
     */
    public boolean isLoading() {
        for (WalletAccount account : accounts.values()) {
            if (account.isLoading()) {
                return true;
            }
        }
        return false;
    }

    public DeterministicKey getRootKey() {
        return rootKey;
    }

    public void setRootKey(DeterministicKey rootKey) {
        this.rootKey = rootKey;
    }

    //比特币发送sendCoins
    // Object that is used to send transactions asynchronously when the wallet requires it.
    protected volatile TransactionBroadcaster vTransactionBroadcaster;

    /*public org.bitcoinj.core.Wallet.SendResult sendCoins(org.bitcoinj.core.Wallet.SendRequest request){
        TransactionBroadcaster broadcaster = vTransactionBroadcaster;
        checkState(broadcaster != null, "No transaction broadcaster is configured");
        retur
    }*/

    public  String getPass(){
        Log.d(TAG, "getPrivateKey: -----privateKey"+pass);
        return pass;
    }
}
