package com.onets.core.wallet.families.ripple;

/**
 * Created by Hasee on 2018/2/1.
 */

public class RippleTransactionImpl {

    public static final class BuilderImpl{

        private String account;
        private String txHash;
        private String destination;
        private String amount;


        public BuilderImpl(String account, String amount, String destination) {
            this.account = account;
            this.amount = amount;
            this.destination = destination;
        }

        public RippleTransactionImpl build() {
            return new RippleTransactionImpl(this);
        }


        public RippleTransactionImpl.BuilderImpl txHash(String txHash) {
            this.txHash = txHash;
            return this;
        }

        public RippleTransactionImpl.BuilderImpl account(String account) {
            this.account = account;
            return this;
        }
        public RippleTransactionImpl.BuilderImpl destination(String destination) {
            this.destination = destination;
            return this;
        }
        public RippleTransactionImpl.BuilderImpl amount(String amount) {
            this.amount = amount;
            return this;
        }


    }


    private static final String TAG = "RippleTransactionImpl";
    private RippleTransactionImpl(BuilderImpl builder) {

        ResultBean bean = new ResultBean();
        ResultBean.TxJsonBean txJsonBean = bean.new TxJsonBean();
        bean.setTx_json(txJsonBean);

        txJsonBean.setAccount(builder.account);
        txJsonBean.setAmount(builder.amount);
        txJsonBean.setDestination(builder.destination);
        txJsonBean.setHash(builder.txHash);

        this.result = bean;

    }




    public RippleTransactionImpl() {

    }
//
//  public RippleTransactionImpl(int id, ResultBean result, String status, String type) {
//        this.id = id;
//        this.result = result;
//        this.status = status;
//        this.type = type;
//    }

    /**
     * id : 4
     * result : {"engine_result":"tecUNFUNDED_PAYMENT","engine_result_code":104,"engine_result_message":"Insufficient XRP balance to send.","tx_blob":"12000022800000002400000003201B0060B1F2614000000001312D0068400000000000000B73210326606A9C2BC57F2D43A4C68085FF34092DFC2455F9D8A72C15D065B78A1CB04974473045022100CB19C95922947B91344B448DA4720F50E5683643BF88C2E7EEDCCFB61F44014102206E69BFB1AC2245FB5713D78FBC343C4D4ACDD8B90521626BE7EB55F7A280B6D781144ACD0F3E16CD53A8A8D79601C3AF490BE26084448314D41FC5FF784EAAA1D676BF9A052E7DD00FD6FEC1","tx_json":{"Account":"rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW","Amount":"20000000","Destination":"rLLcKKqsgvmPK5tbwsqraZTTqirgoC4X8E","Fee":"11","Flags":2147483648,"LastLedgerSequence":6337010,"Sequence":3,"SigningPubKey":"0326606A9C2BC57F2D43A4C68085FF34092DFC2455F9D8A72C15D065B78A1CB049","TransactionType":"Payment","TxnSignature":"3045022100CB19C95922947B91344B448DA4720F50E5683643BF88C2E7EEDCCFB61F44014102206E69BFB1AC2245FB5713D78FBC343C4D4ACDD8B90521626BE7EB55F7A280B6D7","hash":"043C5A46A045D2807E7DE2DF2611212CE169027C4E0D91450E42F4E1BCDB7A8E"}}
     * status : success
     * type : response
     */

    private int id;
    private ResultBean result;
    private String status;
    private String type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class ResultBean {
        /**
         * engine_result : tecUNFUNDED_PAYMENT
         * engine_result_code : 104
         * engine_result_message : Insufficient XRP balance to send.
         * tx_blob : 12000022800000002400000003201B0060B1F2614000000001312D0068400000000000000B73210326606A9C2BC57F2D43A4C68085FF34092DFC2455F9D8A72C15D065B78A1CB04974473045022100CB19C95922947B91344B448DA4720F50E5683643BF88C2E7EEDCCFB61F44014102206E69BFB1AC2245FB5713D78FBC343C4D4ACDD8B90521626BE7EB55F7A280B6D781144ACD0F3E16CD53A8A8D79601C3AF490BE26084448314D41FC5FF784EAAA1D676BF9A052E7DD00FD6FEC1
         * tx_json : {"Account":"rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW","Amount":"20000000","Destination":"rLLcKKqsgvmPK5tbwsqraZTTqirgoC4X8E","Fee":"11","Flags":2147483648,"LastLedgerSequence":6337010,"Sequence":3,"SigningPubKey":"0326606A9C2BC57F2D43A4C68085FF34092DFC2455F9D8A72C15D065B78A1CB049","TransactionType":"Payment","TxnSignature":"3045022100CB19C95922947B91344B448DA4720F50E5683643BF88C2E7EEDCCFB61F44014102206E69BFB1AC2245FB5713D78FBC343C4D4ACDD8B90521626BE7EB55F7A280B6D7","hash":"043C5A46A045D2807E7DE2DF2611212CE169027C4E0D91450E42F4E1BCDB7A8E"}
         */

        private String engine_result;
        private int engine_result_code;
        private String engine_result_message;
        private String tx_blob;
        private TxJsonBean tx_json;

        public String getEngine_result() {
            return engine_result;
        }

        public void setEngine_result(String engine_result) {
            this.engine_result = engine_result;
        }

        public int getEngine_result_code() {
            return engine_result_code;
        }

        public void setEngine_result_code(int engine_result_code) {
            this.engine_result_code = engine_result_code;
        }

        public String getEngine_result_message() {
            return engine_result_message;
        }

        public void setEngine_result_message(String engine_result_message) {
            this.engine_result_message = engine_result_message;
        }

        public String getTx_blob() {
            return tx_blob;
        }

        public void setTx_blob(String tx_blob) {
            this.tx_blob = tx_blob;
        }

        public TxJsonBean getTx_json() {
            return tx_json;
        }

        public void setTx_json(TxJsonBean tx_json) {
            this.tx_json = tx_json;
        }

        public class TxJsonBean {
            /**
             * Account : rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW
             * Amount : 20000000
             * Destination : rLLcKKqsgvmPK5tbwsqraZTTqirgoC4X8E
             * Fee : 11
             * Flags : 2147483648
             * LastLedgerSequence : 6337010
             * Sequence : 3
             * SigningPubKey : 0326606A9C2BC57F2D43A4C68085FF34092DFC2455F9D8A72C15D065B78A1CB049
             * TransactionType : Payment
             * TxnSignature : 3045022100CB19C95922947B91344B448DA4720F50E5683643BF88C2E7EEDCCFB61F44014102206E69BFB1AC2245FB5713D78FBC343C4D4ACDD8B90521626BE7EB55F7A280B6D7
             * hash : 043C5A46A045D2807E7DE2DF2611212CE169027C4E0D91450E42F4E1BCDB7A8E
             */

            private String Account;
            private String Amount;
            private String Destination;
            private String Fee;
            private long Flags;
            private int LastLedgerSequence;
            private int Sequence;
            private String SigningPubKey;
            private String TransactionType;
            private String TxnSignature;
            private String hash;

            public String getAccount() {
                return Account;
            }

            public void setAccount(String Account) {
                this.Account = Account;
            }

            public String getAmount() {
                return Amount;
            }

            public void setAmount(String Amount) {
                this.Amount = Amount;
            }

            public String getDestination() {
                return Destination;
            }

            public void setDestination(String Destination) {
                this.Destination = Destination;
            }

            public String getFee() {
                return Fee;
            }

            public void setFee(String Fee) {
                this.Fee = Fee;
            }

            public long getFlags() {
                return Flags;
            }

            public void setFlags(long Flags) {
                this.Flags = Flags;
            }

            public int getLastLedgerSequence() {
                return LastLedgerSequence;
            }

            public void setLastLedgerSequence(int LastLedgerSequence) {
                this.LastLedgerSequence = LastLedgerSequence;
            }

            public int getSequence() {
                return Sequence;
            }

            public void setSequence(int Sequence) {
                this.Sequence = Sequence;
            }

            public String getSigningPubKey() {
                return SigningPubKey;
            }

            public void setSigningPubKey(String SigningPubKey) {
                this.SigningPubKey = SigningPubKey;
            }

            public String getTransactionType() {
                return TransactionType;
            }

            public void setTransactionType(String TransactionType) {
                this.TransactionType = TransactionType;
            }

            public String getTxnSignature() {
                return TxnSignature;
            }

            public void setTxnSignature(String TxnSignature) {
                this.TxnSignature = TxnSignature;
            }

            public String getHash() {
                return hash;
            }

            public void setHash(String hash) {
                this.hash = hash;
            }
        }
    }


//
//    /**
//     * engine_result : tecUNFUNDED_PAYMENT
//     * engine_result_code : 104
//     * engine_result_message : Insufficient XRP balance to send.
//     * ledger_hash : 08025A9F794577FFB0DAE7282EB5AABB3BEB997CEEA84F15F081FC0EC5C41877
//     * ledger_index : 6337004
//     * meta : {"AffectedNodes":[{"ModifiedNode":{"FinalFields":{"Account":"rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW","Balance":"39999967","Flags":0,"OwnerCount":0,"Sequence":4},"LedgerEntryType":"AccountRoot","LedgerIndex":"4B210C6D0CA779BC7061BD4E38D22C0103E2E66AE727A0DA4F75C5E3F8B93039","PreviousFields":{"Balance":"39999978","Sequence":3},"PreviousTxnID":"DE3F9DE9C1AF07AD92C5D9CC8A7D59427CC12CBE7B2BD91741501BB995A4C10F","PreviousTxnLgrSeq":6335949}}],"TransactionIndex":4,"TransactionResult":"tecUNFUNDED_PAYMENT"}
//     * status : closed
//     * transaction : {"Account":"rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW","Amount":"20000000","Destination":"rLLcKKqsgvmPK5tbwsqraZTTqirgoC4X8E","Fee":"11","Flags":2147483648,"LastLedgerSequence":6337010,"Sequence":3,"SigningPubKey":"0326606A9C2BC57F2D43A4C68085FF34092DFC2455F9D8A72C15D065B78A1CB049","TransactionType":"Payment","TxnSignature":"3045022100CB19C95922947B91344B448DA4720F50E5683643BF88C2E7EEDCCFB61F44014102206E69BFB1AC2245FB5713D78FBC343C4D4ACDD8B90521626BE7EB55F7A280B6D7","date":570769082,"hash":"043C5A46A045D2807E7DE2DF2611212CE169027C4E0D91450E42F4E1BCDB7A8E"}
//     * type : transaction
//     * validated : true
//     */
//
//    private String engine_result;
//    private int engine_result_code;
//    private String engine_result_message;
//    private String ledger_hash;
//    private int ledger_index;
//    private MetaBean meta;
//    private String status;
//    private TransactionBean transaction;
//    private String type;
//    private boolean validated;
//
//    public String getEngine_result() {
//        return engine_result;
//    }
//
//    public void setEngine_result(String engine_result) {
//        this.engine_result = engine_result;
//    }
//
//    public int getEngine_result_code() {
//        return engine_result_code;
//    }
//
//    public void setEngine_result_code(int engine_result_code) {
//        this.engine_result_code = engine_result_code;
//    }
//
//    public String getEngine_result_message() {
//        return engine_result_message;
//    }
//
//    public void setEngine_result_message(String engine_result_message) {
//        this.engine_result_message = engine_result_message;
//    }
//
//    public String getLedger_hash() {
//        return ledger_hash;
//    }
//
//    public void setLedger_hash(String ledger_hash) {
//        this.ledger_hash = ledger_hash;
//    }
//
//    public int getLedger_index() {
//        return ledger_index;
//    }
//
//    public void setLedger_index(int ledger_index) {
//        this.ledger_index = ledger_index;
//    }
//
//    public MetaBean getMeta() {
//        return meta;
//    }
//
//    public void setMeta(MetaBean meta) {
//        this.meta = meta;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public TransactionBean getTransaction() {
//        return transaction;
//    }
//
//    public void setTransaction(TransactionBean transaction) {
//        this.transaction = transaction;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public boolean isValidated() {
//        return validated;
//    }
//
//    public void setValidated(boolean validated) {
//        this.validated = validated;
//    }
//
//    public static class MetaBean {
//        /**
//         * AffectedNodes : [{"ModifiedNode":{"FinalFields":{"Account":"rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW","Balance":"39999967","Flags":0,"OwnerCount":0,"Sequence":4},"LedgerEntryType":"AccountRoot","LedgerIndex":"4B210C6D0CA779BC7061BD4E38D22C0103E2E66AE727A0DA4F75C5E3F8B93039","PreviousFields":{"Balance":"39999978","Sequence":3},"PreviousTxnID":"DE3F9DE9C1AF07AD92C5D9CC8A7D59427CC12CBE7B2BD91741501BB995A4C10F","PreviousTxnLgrSeq":6335949}}]
//         * TransactionIndex : 4
//         * TransactionResult : tecUNFUNDED_PAYMENT
//         */
//
//        private int TransactionIndex;
//        private String TransactionResult;
//        private List<AffectedNodesBean> AffectedNodes;
//
//        public int getTransactionIndex() {
//            return TransactionIndex;
//        }
//
//        public void setTransactionIndex(int TransactionIndex) {
//            this.TransactionIndex = TransactionIndex;
//        }
//
//        public String getTransactionResult() {
//            return TransactionResult;
//        }
//
//        public void setTransactionResult(String TransactionResult) {
//            this.TransactionResult = TransactionResult;
//        }
//
//        public List<AffectedNodesBean> getAffectedNodes() {
//            return AffectedNodes;
//        }
//
//        public void setAffectedNodes(List<AffectedNodesBean> AffectedNodes) {
//            this.AffectedNodes = AffectedNodes;
//        }
//
//        public static class AffectedNodesBean {
//            /**
//             * ModifiedNode : {"FinalFields":{"Account":"rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW","Balance":"39999967","Flags":0,"OwnerCount":0,"Sequence":4},"LedgerEntryType":"AccountRoot","LedgerIndex":"4B210C6D0CA779BC7061BD4E38D22C0103E2E66AE727A0DA4F75C5E3F8B93039","PreviousFields":{"Balance":"39999978","Sequence":3},"PreviousTxnID":"DE3F9DE9C1AF07AD92C5D9CC8A7D59427CC12CBE7B2BD91741501BB995A4C10F","PreviousTxnLgrSeq":6335949}
//             */
//
//            private ModifiedNodeBean ModifiedNode;
//
//            public ModifiedNodeBean getModifiedNode() {
//                return ModifiedNode;
//            }
//
//            public void setModifiedNode(ModifiedNodeBean ModifiedNode) {
//                this.ModifiedNode = ModifiedNode;
//            }
//
//            public static class ModifiedNodeBean {
//                /**
//                 * FinalFields : {"Account":"rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW","Balance":"39999967","Flags":0,"OwnerCount":0,"Sequence":4}
//                 * LedgerEntryType : AccountRoot
//                 * LedgerIndex : 4B210C6D0CA779BC7061BD4E38D22C0103E2E66AE727A0DA4F75C5E3F8B93039
//                 * PreviousFields : {"Balance":"39999978","Sequence":3}
//                 * PreviousTxnID : DE3F9DE9C1AF07AD92C5D9CC8A7D59427CC12CBE7B2BD91741501BB995A4C10F
//                 * PreviousTxnLgrSeq : 6335949
//                 */
//
//                private FinalFieldsBean FinalFields;
//                private String LedgerEntryType;
//                private String LedgerIndex;
//                private PreviousFieldsBean PreviousFields;
//                private String PreviousTxnID;
//                private int PreviousTxnLgrSeq;
//
//                public FinalFieldsBean getFinalFields() {
//                    return FinalFields;
//                }
//
//                public void setFinalFields(FinalFieldsBean FinalFields) {
//                    this.FinalFields = FinalFields;
//                }
//
//                public String getLedgerEntryType() {
//                    return LedgerEntryType;
//                }
//
//                public void setLedgerEntryType(String LedgerEntryType) {
//                    this.LedgerEntryType = LedgerEntryType;
//                }
//
//                public String getLedgerIndex() {
//                    return LedgerIndex;
//                }
//
//                public void setLedgerIndex(String LedgerIndex) {
//                    this.LedgerIndex = LedgerIndex;
//                }
//
//                public PreviousFieldsBean getPreviousFields() {
//                    return PreviousFields;
//                }
//
//                public void setPreviousFields(PreviousFieldsBean PreviousFields) {
//                    this.PreviousFields = PreviousFields;
//                }
//
//                public String getPreviousTxnID() {
//                    return PreviousTxnID;
//                }
//
//                public void setPreviousTxnID(String PreviousTxnID) {
//                    this.PreviousTxnID = PreviousTxnID;
//                }
//
//                public int getPreviousTxnLgrSeq() {
//                    return PreviousTxnLgrSeq;
//                }
//
//                public void setPreviousTxnLgrSeq(int PreviousTxnLgrSeq) {
//                    this.PreviousTxnLgrSeq = PreviousTxnLgrSeq;
//                }
//
//                public static class FinalFieldsBean {
//                    /**
//                     * Account : rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW
//                     * Balance : 39999967
//                     * Flags : 0
//                     * OwnerCount : 0
//                     * Sequence : 4
//                     */
//
//                    private String Account;
//                    private String Balance;
//                    private int Flags;
//                    private int OwnerCount;
//                    private int Sequence;
//
//                    public String getAccount() {
//                        return Account;
//                    }
//
//                    public void setAccount(String Account) {
//                        this.Account = Account;
//                    }
//
//                    public String getBalance() {
//                        return Balance;
//                    }
//
//                    public void setBalance(String Balance) {
//                        this.Balance = Balance;
//                    }
//
//                    public int getFlags() {
//                        return Flags;
//                    }
//
//                    public void setFlags(int Flags) {
//                        this.Flags = Flags;
//                    }
//
//                    public int getOwnerCount() {
//                        return OwnerCount;
//                    }
//
//                    public void setOwnerCount(int OwnerCount) {
//                        this.OwnerCount = OwnerCount;
//                    }
//
//                    public int getSequence() {
//                        return Sequence;
//                    }
//
//                    public void setSequence(int Sequence) {
//                        this.Sequence = Sequence;
//                    }
//                }
//
//                public static class PreviousFieldsBean {
//                    /**
//                     * Balance : 39999978
//                     * Sequence : 3
//                     */
//
//                    private String Balance;
//                    private int Sequence;
//
//                    public String getBalance() {
//                        return Balance;
//                    }
//
//                    public void setBalance(String Balance) {
//                        this.Balance = Balance;
//                    }
//
//                    public int getSequence() {
//                        return Sequence;
//                    }
//
//                    public void setSequence(int Sequence) {
//                        this.Sequence = Sequence;
//                    }
//                }
//            }
//        }
//    }
//
//    public static class TransactionBean {
//        /**
//         * Account : rfFWeyxxRupJnWJMUdDvJv6wYcsfsgj4YW
//         * Amount : 20000000
//         * Destination : rLLcKKqsgvmPK5tbwsqraZTTqirgoC4X8E
//         * Fee : 11
//         * Flags : 2147483648
//         * LastLedgerSequence : 6337010
//         * Sequence : 3
//         * SigningPubKey : 0326606A9C2BC57F2D43A4C68085FF34092DFC2455F9D8A72C15D065B78A1CB049
//         * TransactionType : Payment
//         * TxnSignature : 3045022100CB19C95922947B91344B448DA4720F50E5683643BF88C2E7EEDCCFB61F44014102206E69BFB1AC2245FB5713D78FBC343C4D4ACDD8B90521626BE7EB55F7A280B6D7
//         * date : 570769082
//         * hash : 043C5A46A045D2807E7DE2DF2611212CE169027C4E0D91450E42F4E1BCDB7A8E
//         */
//
//        private String Account;
//        private String Amount;
//        private String Destination;
//        private String Fee;
//        private long Flags;
//        private int LastLedgerSequence;
//        private int Sequence;
//        private String SigningPubKey;
//        private String TransactionType;
//        private String TxnSignature;
//        private int date;
//        private String hash;
//
//        public String getAccount() {
//            return Account;
//        }
//
//        public void setAccount(String Account) {
//            this.Account = Account;
//        }
//
//        public String getAmount() {
//            return Amount;
//        }
//
//        public void setAmount(String Amount) {
//            this.Amount = Amount;
//        }
//
//        public String getDestination() {
//            return Destination;
//        }
//
//        public void setDestination(String Destination) {
//            this.Destination = Destination;
//        }
//
//        public String getFee() {
//            return Fee;
//        }
//
//        public void setFee(String Fee) {
//            this.Fee = Fee;
//        }
//
//        public long getFlags() {
//            return Flags;
//        }
//
//        public void setFlags(long Flags) {
//            this.Flags = Flags;
//        }
//
//        public int getLastLedgerSequence() {
//            return LastLedgerSequence;
//        }
//
//        public void setLastLedgerSequence(int LastLedgerSequence) {
//            this.LastLedgerSequence = LastLedgerSequence;
//        }
//
//        public int getSequence() {
//            return Sequence;
//        }
//
//        public void setSequence(int Sequence) {
//            this.Sequence = Sequence;
//        }
//
//        public String getSigningPubKey() {
//            return SigningPubKey;
//        }
//
//        public void setSigningPubKey(String SigningPubKey) {
//            this.SigningPubKey = SigningPubKey;
//        }
//
//        public String getTransactionType() {
//            return TransactionType;
//        }
//
//        public void setTransactionType(String TransactionType) {
//            this.TransactionType = TransactionType;
//        }
//
//        public String getTxnSignature() {
//            return TxnSignature;
//        }
//
//        public void setTxnSignature(String TxnSignature) {
//            this.TxnSignature = TxnSignature;
//        }
//
//        public int getDate() {
//            return date;
//        }
//
//        public void setDate(int date) {
//            this.date = date;
//        }
//
//        public String getHash() {
//            return hash;
//        }
//
//        public void setHash(String hash) {
//            this.hash = hash;
//        }
//    }
}
