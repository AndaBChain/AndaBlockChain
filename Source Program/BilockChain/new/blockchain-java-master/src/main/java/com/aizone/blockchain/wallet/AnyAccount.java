package com.aizone.blockchain.wallet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * IOS恢复证包用（账户）
 * @author Kelly
 *
 */
public class AnyAccount implements Serializable{
  private static final long serialVersionUID = 1L;
  private String encryptedSeed;
  private String encryptedPrivateKey;
  private String publicKey;
  private String encryptedKeyStore;
  private String address;
  public AnyAccount() {}
  public AnyAccount(String encryptedSeed, String encryptedPrivateKey, String publicKey, String encryptedKeyStore, String address)
  {
    this.encryptedSeed = encryptedSeed;
    this.encryptedPrivateKey = encryptedPrivateKey;
    this.publicKey = publicKey;
    this.encryptedKeyStore = encryptedKeyStore;
    this.address = address;
  }
  public String getEncryptedSeed() {
   return this.encryptedSeed;
  }
  
   public void setEncryptedSeed(String encryptedSeed) {
    this.encryptedSeed = encryptedSeed;
  }
   
  public String getEncryptedPrivateKey() {
    return this.encryptedPrivateKey;
  }
  
  public void setEncryptedPrivateKey(String encryptedPrivateKey) {
    this.encryptedPrivateKey = encryptedPrivateKey;
  }
   
  public String getPublicKey() {
    return this.publicKey;
  }
  
  public void setPublicKey(String publicKey) {
     this.publicKey = publicKey;
  }
  
  public String getEncryptedKeyStore() {
    return this.encryptedKeyStore;
  }
  
   public void setEncryptedKeyStore(String encryptedKeyStore) {
    this.encryptedKeyStore = encryptedKeyStore;
   }
   
   public String getAddress() {
     return this.address;
  }
  
   public void setAddress(String address) {
    this.address = address;
  }
  /**
   * 将账户信息转为map形式
   * @param anyAccount
   * @return
   */
  public Map<String, String> AnyAccountToMap(AnyAccount anyAccount) {
    Map<String, String> map = new HashMap<String, String>();
    map.put("address", anyAccount.getAddress());
    map.put("encryptedPrivateKey", anyAccount.getEncryptedPrivateKey());
    map.put("publicKey", anyAccount.getPublicKey());
    map.put("encryptedKeyStore", anyAccount.getEncryptedKeyStore());
    return map;
  }
}
