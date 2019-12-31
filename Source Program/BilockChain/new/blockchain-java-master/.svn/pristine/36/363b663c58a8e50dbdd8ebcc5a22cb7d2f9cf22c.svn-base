package com.aizone.blockchain.dao;

import com.aizone.blockchain.dao.CoreUtils;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.store.UnreadableWalletException;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.DeterministicSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.base.Splitter;

/**
 * @author John L. Jegutanis
 * 钱包
 */
final public class Wallet {
    private static final Logger log = LoggerFactory.getLogger(Wallet.class);
    private static final String TAG = "Wallet";
    public static int ENTROPY_SIZE_DEBUG = -1;
    

    private final ReentrantLock lock = Threading.lock("KeyChain");

    @Nullable
    private DeterministicSeed seed;
    private DeterministicKey masterKey;
    
    

    public void setSeed(DeterministicSeed seed) {
		this.seed = seed;
	}

	public void setMasterKey(DeterministicKey masterKey) {
		this.masterKey = masterKey;
	}



	// FIXME, make multi account capable
    //多账户
    private final static int ACCOUNT_ZERO = 0;

    private int version = 2;

    public Wallet(String mnemonic) throws MnemonicException {
        this(CoreUtils.parseMnemonic(mnemonic), null);

    }

    public Wallet(List<String> mnemonic) throws MnemonicException {
        this(mnemonic, null);
    }

    public Wallet(List<String> mnemonic, @Nullable String seedPassword) throws MnemonicException {
        //Log.e(TAG, "Wallet: --------------  mnemonic  : " + mnemonic.size() + "   |  seedPassword :" + seedPassword);
       // MnemonicCode.INSTANCE.check(mnemonic);
        seedPassword = seedPassword == null ? "" : seedPassword;

        seed = new DeterministicSeed(mnemonic, null, seedPassword, 0);
        this.masterKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
        System.out.println(masterKey.toString());
        System.out.println(masterKey.getPrivKeyBytes());
        System.out.println(masterKey.getPrivKey());
       // accountsByType = new LinkedHashMap<CoinType, ArrayList<WalletAccount>>();
        //accounts = new LinkedHashMap<String, WalletAccount>();
    }
    
    public static void main(String[] args) {
    	String seed = "esrhgzthbsyz";
		try {
			ArrayList<String> seenwords = new ArrayList<>();
			for (String word : seed.trim().split(" ")) {
				if (word.isEmpty()) continue;
				seenwords.add(word);
			}
			
			Wallet wallet = new Wallet(seenwords, "aa123456789aa");
		} catch (MnemonicException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

    public Wallet(DeterministicKey masterKey, @Nullable DeterministicSeed seed) {
       // Log.e(TAG, "Wallet: --------------  masterKey   |  DeterministicSeed :");

        this.seed = seed;
        this.masterKey = masterKey;
        //accountsByType = new LinkedHashMap<CoinType, ArrayList<WalletAccount>>();
        //accounts = new LinkedHashMap<String, WalletAccount>();
    }

    /**
     * 生成助记符
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

        return CoreUtils.bytesToMnemonic(entropy);
    }

    /**
     * 生成助记符
     * @param entropyBitsSize
     * @return
     */
    public static String generateMnemonicString(int entropyBitsSize) {
        List<String> mnemonicWords = Wallet.generateMnemonic(entropyBitsSize);
        return mnemonicToString(mnemonicWords);
    }

    /**
     * 助记符转换为字符串
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

   

    private static List<String> decodeMnemonicCode(byte[] mnemonicCode) throws UnreadableWalletException {
        try {
            return Splitter.on(" ").splitToList(new String(mnemonicCode, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new UnreadableWalletException(e.toString());
        }
    }

}
