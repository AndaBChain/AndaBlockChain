package com.aizone.blockchain.model;

public class TxRecord {
	/**
	 * 交易ID
	 */
	private String id ;
	/**
	 * 交易的发送者
	 */
	private String sender;
	/**
	 * 交易接收者
	 */
	private String recipient;
	/**
	 * 交易金额
	 */
	private int amount;
	/**
	 * 客户安达地址
	 */
	private String andaAddress;
	public void setAmount(int amount) {
		this.amount = amount;
	}
	/**
	 * 交易是否完成
	 */
	private String sfwc;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getAndaAddress() {
		return andaAddress;
	}
	public void setAndaAddress(String andaAddress) {
		this.andaAddress = andaAddress;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	
	public int getAmount() {
		return amount;
	}
	public String getSfwc() {
		return sfwc;
	}
	public void setSfwc(String sfwc) {
		this.sfwc = sfwc;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + ((recipient == null) ? 0 : recipient.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + ((sfwc == null) ? 0 : sfwc.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TxRecord other = (TxRecord) obj;
		if (amount != other.amount)
			return false;
		if (recipient == null) {
			if (other.recipient != null)
				return false;
		} else if (!recipient.equals(other.recipient))
			return false;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (sfwc == null) {
			if (other.sfwc != null)
				return false;
		} else if (!sfwc.equals(other.sfwc))
			return false;
		return true;
	}

	
	
	
}
