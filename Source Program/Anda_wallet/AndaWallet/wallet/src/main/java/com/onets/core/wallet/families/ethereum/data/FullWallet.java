package com.onets.core.wallet.families.ethereum.data;

import com.onets.core.wallet.families.ethereum.interfaces.StorableWallet;

import java.io.Serializable;

/**
 * 完整钱包
 */
public class FullWallet implements StorableWallet, Serializable {

    private static final long serialVersionUID = 2622313531196422839L;
    private String pubKey;
    private String path;
    private long dateAdded;

    public FullWallet(String pubKey, String path) {
        this.pubKey = pubKey.toLowerCase();
        this.path = path;
        this.dateAdded = System.currentTimeMillis();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }
}
