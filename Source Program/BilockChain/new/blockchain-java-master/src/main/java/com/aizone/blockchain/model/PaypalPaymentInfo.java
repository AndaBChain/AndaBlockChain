package com.aizone.blockchain.model;


/**
 * paypal支付信息实体类（未使用）
 * @author Kelly
 *
 */
public class PaypalPaymentInfo
{
  private String paymentDate;
  

  private String paymentStatus;
  

  private String paymentAmount;
  

  private String paymentCurrency;
  

  private String txnId;
  
  private String receiverEmail;
  
  private String payerEmail;
  
  private String isExchanged;
  
  private String andaAddress;
  

  public PaypalPaymentInfo() {}
  

  public PaypalPaymentInfo(String paymentDate, String paymentStatus, String paymentAmount, String paymentCurrency, String txnId, String receiverEmail, String payerEmail, String isExchanged, String andaAddress)
  {
    this.paymentDate = paymentDate;
    this.paymentStatus = paymentStatus;
    this.paymentAmount = paymentAmount;
    this.paymentCurrency = paymentCurrency;
    this.txnId = txnId;
    this.receiverEmail = receiverEmail;
    this.payerEmail = payerEmail;
    this.isExchanged = isExchanged;
    this.andaAddress = andaAddress;
  }
  
  public String getPaymentDate() {
    return this.paymentDate;
  }
  
  public void setPaymentDate(String paymentDate) {
    this.paymentDate = paymentDate;
  }
  
  public String getPaymentStatus() {
    return this.paymentStatus;
  }
  
  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }
  
  public String getPaymentAmount() {
    return this.paymentAmount;
  }
  
  public void setPaymentAmount(String paymentAmount) {
    this.paymentAmount = paymentAmount;
  }
  
  public String getPaymentCurrency() {
    return this.paymentCurrency;
  }
  
  public void setPaymentCurrency(String paymentCurrency) {
    this.paymentCurrency = paymentCurrency;
  }
  
  public String getTxnId() {
    return this.txnId;
  }
  
  public void setTxnId(String txnId) {
    this.txnId = txnId;
  }
  
  public String getReceiverEmail() {
    return this.receiverEmail;
  }
  
  public void setReceiverEmail(String receiverEmail) {
    this.receiverEmail = receiverEmail;
  }
  
  public String getPayerEmail() {
    return this.payerEmail;
  }
  
  public void setPayerEmail(String payerEmail) {
    this.payerEmail = payerEmail;
  }
  
  public String getIsExchanged() {
    return this.isExchanged;
  }
  
  public void setIsExchanged(String isExchanged) {
    this.isExchanged = isExchanged;
  }
  
  public String getAndaAddress() {
    return this.andaAddress;
  }
  
  public void setAndaAddress(String andaAddress) {
    this.andaAddress = andaAddress;
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (this.andaAddress == null ? 0 : this.andaAddress.hashCode());
    result = 31 * result + (this.isExchanged == null ? 0 : this.isExchanged.hashCode());
    result = 31 * result + (this.payerEmail == null ? 0 : this.payerEmail.hashCode());
    result = 31 * result + (this.paymentAmount == null ? 0 : this.paymentAmount.hashCode());
    result = 31 * result + (this.paymentCurrency == null ? 0 : this.paymentCurrency.hashCode());
    result = 31 * result + (this.paymentDate == null ? 0 : this.paymentDate.hashCode());
    result = 31 * result + (this.paymentStatus == null ? 0 : this.paymentStatus.hashCode());
    result = 31 * result + (this.receiverEmail == null ? 0 : this.receiverEmail.hashCode());
    result = 31 * result + (this.txnId == null ? 0 : this.txnId.hashCode());
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PaypalPaymentInfo other = (PaypalPaymentInfo)obj;
    if (this.andaAddress == null) {
      if (other.andaAddress != null)
        return false;
    } else if (!this.andaAddress.equals(other.andaAddress))
      return false;
    if (this.isExchanged == null) {
      if (other.isExchanged != null)
        return false;
    } else if (!this.isExchanged.equals(other.isExchanged))
      return false;
    if (this.payerEmail == null) {
      if (other.payerEmail != null)
        return false;
    } else if (!this.payerEmail.equals(other.payerEmail))
      return false;
    if (this.paymentAmount == null) {
      if (other.paymentAmount != null)
        return false;
    } else if (!this.paymentAmount.equals(other.paymentAmount))
      return false;
    if (this.paymentCurrency == null) {
      if (other.paymentCurrency != null)
        return false;
    } else if (!this.paymentCurrency.equals(other.paymentCurrency))
      return false;
    if (this.paymentDate == null) {
      if (other.paymentDate != null)
        return false;
    } else if (!this.paymentDate.equals(other.paymentDate))
      return false;
    if (this.paymentStatus == null) {
      if (other.paymentStatus != null)
        return false;
    } else if (!this.paymentStatus.equals(other.paymentStatus))
      return false;
    if (this.receiverEmail == null) {
      if (other.receiverEmail != null)
        return false;
    } else if (!this.receiverEmail.equals(other.receiverEmail))
      return false;
    if (this.txnId == null) {
      if (other.txnId != null)
        return false;
    } else if (!this.txnId.equals(other.txnId))
      return false;
    return true;
  }
  
  public String toString()
  {
    return "PaypalPaymentInfo [paymentDate=" + this.paymentDate + ", paymentStatus=" + this.paymentStatus + ", paymentAmount=" + this.paymentAmount + ", paymentCurrency=" + this.paymentCurrency + ", txnId=" + this.txnId + ", receiverEmail=" + this.receiverEmail + ", payerEmail=" + this.payerEmail + ", isExchanged=" + this.isExchanged + ", andaAddress=" + this.andaAddress + "]";
  }
}

