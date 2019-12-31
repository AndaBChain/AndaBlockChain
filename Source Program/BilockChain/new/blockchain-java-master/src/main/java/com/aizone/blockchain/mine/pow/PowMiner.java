package com.aizone.blockchain.mine.pow;

import com.aizone.blockchain.core.Block;
import com.aizone.blockchain.core.BlockBody;
import com.aizone.blockchain.core.BlockHeader;
import com.aizone.blockchain.core.Transaction;
import com.aizone.blockchain.db.DBAccess;
import com.aizone.blockchain.mine.Miner;
import com.aizone.blockchain.sm.SM2;
import com.aizone.blockchain.utils.ECKey;
import com.aizone.blockchain.utils.HashUtil;
import com.aizone.blockchain.wallet.Account;
import com.google.common.base.Optional;

import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * PoW 挖矿算法实现
 * @author yangjian
 * @since 18-4-13
 */
@Component
public class PowMiner implements Miner {

	@Autowired
	private DBAccess dbAccess;

	@Override
	public Block newBlock(Optional<Block> block) throws Exception {

		//获取挖矿账户
		Account account;
		Optional<Account> coinBaseAccount = dbAccess.getCoinBaseAccount();
		if (!coinBaseAccount.isPresent()) {
			ECKey eckey = new ECKey(new SM2());
			//ECKey eckey = ECKey.fromPrivate(HashUtil.sm3_Hash(addr.getBytes()));
//			KeyPair keyPair = WalletUtils.generateKeyPair();
			Account account1 = new Account(HashUtil.toHexString(eckey.getPrivKeyBytes()), eckey.getPubKey(),eckey.getAddress());
			dbAccess.putCoinBaseAccount(account1);
			throw new RuntimeException("没有找到挖矿账户，请先创建挖矿账户.新建");
		}
		Block newBlock;
		if (block.isPresent()) {
			Block prev = block.get();
			BlockHeader header = new BlockHeader(prev.getHeader().getIndex()+1, prev.getHeader().getHash());
			BlockBody body = new BlockBody();
			newBlock = new Block(header, body);
		} else {
			//创建创世区块
			newBlock = createGenesisBlock();
		}
		//创建挖矿奖励交易
		Transaction transaction = new Transaction();

		account = coinBaseAccount.get();
		transaction.setRecipient(account.getAddress());
		transaction.setPublicKey(Hex.toHexString(account.getPublicKey()));
		transaction.setData("Miner Reward.");
		transaction.setTxHash(transaction.hash());
		transaction.setAmount(Miner.MINING_REWARD);

		//如果不是创世区块，则使用工作量证明挖矿
		if (block.isPresent()) {
			ProofOfWork proofOfWork = ProofOfWork.newProofOfWork(newBlock);
			PowResult result = proofOfWork.run();
			newBlock.getHeader().setDifficulty(result.getTarget());
			newBlock.getHeader().setNonce(result.getNonce());
			newBlock.getHeader().setHash(result.getHash());
		}
		newBlock.getBody().addTransaction(transaction);

		//更新最后一个区块索引
		dbAccess.putLastBlockIndex(newBlock.getHeader().getIndex());
		return newBlock;
	}

	/**
	 * 创建创世区块
	 * @return
	 */
	private Block createGenesisBlock() {

		BlockHeader header = new BlockHeader(1, null);
		header.setNonce(PowMiner.GENESIS_BLOCK_NONCE);
		header.setDifficulty(ProofOfWork.getTarget());
		header.setHash(header.toHash());
		BlockBody body = new BlockBody();
		return new Block(header, body);
	}

	@Override
	public boolean validateBlock(Block block) {
		ProofOfWork proofOfWork = ProofOfWork.newProofOfWork(block);
		return proofOfWork.validate();
	}
}