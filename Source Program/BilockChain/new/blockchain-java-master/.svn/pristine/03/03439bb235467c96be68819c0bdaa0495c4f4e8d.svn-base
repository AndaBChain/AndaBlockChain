package com.aizone.blockchain.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aizone.blockchain.conf.LevelDbProperties;
import com.aizone.blockchain.core.Block;
import com.aizone.blockchain.net.base.Node;
import com.aizone.blockchain.net.conf.TioProperties;
import com.aizone.blockchain.utils.SerializeUtils;
import com.aizone.blockchain.wallet.Account;
import com.aizone.blockchain.wallet.AnyAccount;
import com.google.common.base.Optional;

/**
 * LevelDB 操作封装
 * 
 * @author wss
 * 
 */
@Component
public class LevelDBAccess implements DBAccess {

	static Logger logger = LoggerFactory.getLogger(LevelDBAccess.class);

	private DB db;

	private DBFactory factory;
	/**
	 * 区块数据存储 hash 桶前缀
	 */
	public static final String BLOCKS_BUCKET_PREFIX = "blocks_";
	/**
	 * 钱包数据存储 hash 桶前缀
	 */
	public static final String WALLETS_BUCKET_PREFIX = "wallets_";
	/**
	 * 挖矿账户
	 */
	public static final String COIN_BASE_ADDRESS = "coinbase_address";
	/**
	 * 最后一个区块的区块高度
	 */
	public static final String LAST_BLOCK_INDEX = BLOCKS_BUCKET_PREFIX + "last_block";

	/**
	 * 客户端节点列表存储 key
	 */
	private static final String CLIENT_NODES_LIST_KEY = "client-node-list";
	
	/**
	 * IOS恢复证包用（账户数据存储的前缀）
	 */
	private static final String ANY_ACCOUNT_PREFIX = "any_account_";

	@Autowired
	private LevelDbProperties levelDbProperties;

	@Autowired
	private TioProperties tioProperties;

	public LevelDBAccess() {
		//
	}

	/**
	 * 初始化LevelDB
	 */
	@PostConstruct
	public void initLevelDB() {

		try {
			Charset.forName("utf-8");
			String path = levelDbProperties.getDataDir();
			this.factory = Iq80DBFactory.factory;
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			Options options = new Options().createIfMissing(true);
			this.db = this.factory.open(dir, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean putLastBlockIndex(Object lastBlock) {
		return this.put(LAST_BLOCK_INDEX, lastBlock);
	}

	@Override
	public Optional<Object> getLastBlockIndex() {
		return this.get(LAST_BLOCK_INDEX);
	}

	@Override
	public boolean putBlock(Block block) {
		return this.put(BLOCKS_BUCKET_PREFIX + block.getHeader().getIndex(), block);
	}

	@Override
	public Optional<Block> getBlock(Object blockIndex) {

		Optional<Object> object = this.get(BLOCKS_BUCKET_PREFIX + String.valueOf(blockIndex));
		if (object.isPresent()) {
			return Optional.of((Block) object.get());
		}
		return Optional.absent();
	}

	@Override
	public Optional<Block> getLastBlock() {
		Optional<Object> blockIndex = getLastBlockIndex();
		if (blockIndex.isPresent()) {
			return this.getBlock(blockIndex.get().toString());
		}
		return Optional.absent();
	}

	@Override
	public boolean putAccount(Account account) {
		return this.put(WALLETS_BUCKET_PREFIX + account.getAddress(), account);
	}

	@Override
	public Optional<Account> getAccount(String address) {

		Optional<Object> object = this.get(WALLETS_BUCKET_PREFIX + address);
		if (object.isPresent()) {
			return Optional.of((Account) object.get());
		}
		return Optional.absent();
	}
	
	@Override
	public boolean putAnyAccount(AnyAccount anyaccount)
	  {
	    return this.put(ANY_ACCOUNT_PREFIX + anyaccount.getEncryptedSeed(), anyaccount);
	  }
	
	@Override
    public Optional<AnyAccount> getAnyAccount(String encryptedSeed)
	 {
	    Optional<Object> object = this.get(ANY_ACCOUNT_PREFIX + encryptedSeed);
	    if (object.isPresent()) {
	      return Optional.of((AnyAccount)object.get());
	    }
	    return Optional.absent();
	 }
	  
	@Override
	public boolean putCoinBaseAddress(String address) {
		return this.put(COIN_BASE_ADDRESS, address);
	}

	@Override
	public Optional<String> getCoinBaseAddress() {
		Optional<Object> object = this.get(COIN_BASE_ADDRESS);
		if (object.isPresent()) {
			return Optional.of((String) object.get());
		}
		return Optional.absent();
	}

	@Override
	public Optional<Account> getCoinBaseAccount() {
		Optional<String> address = getCoinBaseAddress();
		if (address.isPresent()) {
			return getAccount(address.get());
		} else {
			return Optional.absent();
		}
	}

	@Override
	public boolean putCoinBaseAccount(Account account) {

		putCoinBaseAddress(account.getAddress());
		return putAccount(account);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<List<Node>> getNodeList() {
		Optional<Object> nodes = this.get(CLIENT_NODES_LIST_KEY);
		if (nodes.isPresent()) {
			return Optional.of((List<Node>) nodes.get());
		}
		return Optional.absent();
	}

	@Override
	public boolean putNodeList(List<Node> nodes) {
		return this.put(CLIENT_NODES_LIST_KEY, nodes);
	}

	@Override
	public synchronized boolean addNode(Node node) {
		Optional<List<Node>> nodeList = getNodeList();
		if (nodeList.isPresent()) {
			// 已经存在的节点跳过
			if (nodeList.get().contains(node)) {
				return true;
			}
			// 跳过自身节点
			Node self = new Node(tioProperties.getServerIp(), tioProperties.getServerPort());
			if (self.equals(node)) {
				return true;
			}
			nodeList.get().add(node);
			return putNodeList(nodeList.get());
		} else {
			ArrayList<Node> nodes = new ArrayList<>();
			nodes.add(node);
			return putNodeList(nodes);
		}
	}

	@Override
	public void clearNodes() {
		delete(CLIENT_NODES_LIST_KEY);
	}

	@Override
	public boolean put(String key, Object value) {
		try {
			db.put(key.getBytes(), SerializeUtils.serialize(value));
			return true;
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.info("ERROR for LevelDB : {}", e);
			}
			return false;
		}
	}

	@Override
	public Optional<Object> get(String key) {
		try {
			return Optional.of(SerializeUtils.unSerialize(db.get(key.getBytes())));
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.info("ERROR for LevelDB : {}", e);
			}
			return Optional.absent();
		}
	}

	@Override
	public boolean delete(String key) {
		try {
			db.delete(key.getBytes());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> seekByKey(String keyPrefix) {

		ArrayList<T> ts = new ArrayList<>();
		DBIterator iterator = db.iterator(new ReadOptions());
		byte[] key = keyPrefix.getBytes();
		for (iterator.seek(key); iterator.hasNext(); iterator.next()) {
			ts.add((T) SerializeUtils.unSerialize(iterator.peekNext().getValue()));
		}
		return ts;
	}

	@Override
	public List<Account> listAccounts() {

		List<Object> objects = seekByKey(WALLETS_BUCKET_PREFIX);
		List<Account> accounts = new ArrayList<>();
		for (Object o : objects) {
			accounts.add((Account) o);
		}
		return accounts;
	}

	@Override
	public void closeDB() {
		try {
			if (null != db) {
				db.close();
			}
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}
