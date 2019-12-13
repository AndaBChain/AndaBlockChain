package com.onets.core.wallet.families.ripple.bean;

import java.util.List;

/**
 * 瑞波币历史交易json基类
 * Created by Hasee on 2018/2/2.
 */

public class RippleHistoryTxBean {


    /**
     * result : success
     * count : 3
     * marker : rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ|20131209043020|000003784802|00002
     * payments : [{"amount":"20.0","delivered_amount":"20.0","destination_balance_changes":[{"counterparty":"","currency":"XRP","value":"20"}],"source_balance_changes":[{"counterparty":"","currency":"XRP","value":"-20"}],"tx_index":2,"currency":"XRP","destination":"rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ","executed_time":"2013-12-07T03:25:20Z","invoice_id":"11E889E6FFBC443595E9A11DB60D8A3C00000000000000000000000000000000","ledger_index":3747650,"source":"rNKXT1p7jpt5WxSRSzmUV6ZB6kY7jdqHmx","source_currency":"XRP","tx_hash":"8EA468B43D00747E6A35178593DE42E30F0C8CD19F1945AE6FE2C92B1A0F5646","transaction_cost":"2.5E-4"},{"amount":"0.1","delivered_amount":"0.1","destination_balance_changes":[{"counterparty":"","currency":"XRP","value":"0.1"}],"source_balance_changes":[{"counterparty":"","currency":"XRP","value":"-0.1"}],"tx_index":3,"currency":"XRP","destination":"rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ","executed_time":"2013-12-07T11:00:10Z","ledger_index":3753771,"source":"r2hrqyjdLCBSkB6ADLpWkWtYFK96w1xMn","source_currency":"XRP","tx_hash":"AF0124801437DE40F72A11C963B2040F02B4972258C7A720747431408E55D58B","transaction_cost":"1.5E-5"},{"amount":"86.0","delivered_amount":"86.0","destination_balance_changes":[{"counterparty":"","currency":"XRP","value":"86"}],"source_balance_changes":[{"counterparty":"","currency":"XRP","value":"-86"}],"currency":"XRP","destination":"rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ","executed_time":"2013-12-07T15:47:20Z","invoice_id":"F53BE3D8AA2648E2A3AA40C536F7F70100000000000000000000000000000000","ledger_index":3757848,"source":"rNKXT1p7jpt5WxSRSzmUV6ZB6kY7jdqHmx","source_currency":"XRP","tx_hash":"9CDAC20371037CF718DB3DA4CEDAB55F2259CA77FF735AEBC127D76C28852591","tx_index":0,"transaction_cost":"2.5E-4"}]
     */

    private String result;
    private int count;
    private String marker;
    private List<PaymentsBean> payments;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public List<PaymentsBean> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentsBean> payments) {
        this.payments = payments;
    }

    public static class PaymentsBean {
        /**
         * amount : 20.0
         * delivered_amount : 20.0
         * destination_balance_changes : [{"counterparty":"","currency":"XRP","value":"20"}]
         * source_balance_changes : [{"counterparty":"","currency":"XRP","value":"-20"}]
         * tx_index : 2
         * currency : XRP
         * destination : rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ
         * executed_time : 2013-12-07T03:25:20Z
         * invoice_id : 11E889E6FFBC443595E9A11DB60D8A3C00000000000000000000000000000000
         * ledger_index : 3747650
         * source : rNKXT1p7jpt5WxSRSzmUV6ZB6kY7jdqHmx
         * source_currency : XRP
         * tx_hash : 8EA468B43D00747E6A35178593DE42E30F0C8CD19F1945AE6FE2C92B1A0F5646
         * transaction_cost : 2.5E-4
         */

        private String amount;
        private String delivered_amount;
        private int tx_index;
        private String currency;
        private String destination;
        private String executed_time;
        private String invoice_id;
        private int ledger_index;
        private String source;
        private String source_currency;
        private String tx_hash;
        private String transaction_cost;
        private List<DestinationBalanceChangesBean> destination_balance_changes;
        private List<SourceBalanceChangesBean> source_balance_changes;

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getDelivered_amount() {
            return delivered_amount;
        }

        public void setDelivered_amount(String delivered_amount) {
            this.delivered_amount = delivered_amount;
        }

        public int getTx_index() {
            return tx_index;
        }

        public void setTx_index(int tx_index) {
            this.tx_index = tx_index;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getExecuted_time() {
            return executed_time;
        }

        public void setExecuted_time(String executed_time) {
            this.executed_time = executed_time;
        }

        public String getInvoice_id() {
            return invoice_id;
        }

        public void setInvoice_id(String invoice_id) {
            this.invoice_id = invoice_id;
        }

        public int getLedger_index() {
            return ledger_index;
        }

        public void setLedger_index(int ledger_index) {
            this.ledger_index = ledger_index;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getSource_currency() {
            return source_currency;
        }

        public void setSource_currency(String source_currency) {
            this.source_currency = source_currency;
        }

        public String getTx_hash() {
            return tx_hash;
        }

        public void setTx_hash(String tx_hash) {
            this.tx_hash = tx_hash;
        }

        public String getTransaction_cost() {
            return transaction_cost;
        }

        public void setTransaction_cost(String transaction_cost) {
            this.transaction_cost = transaction_cost;
        }

        public List<DestinationBalanceChangesBean> getDestination_balance_changes() {
            return destination_balance_changes;
        }

        public void setDestination_balance_changes(List<DestinationBalanceChangesBean> destination_balance_changes) {
            this.destination_balance_changes = destination_balance_changes;
        }

        public List<SourceBalanceChangesBean> getSource_balance_changes() {
            return source_balance_changes;
        }

        public void setSource_balance_changes(List<SourceBalanceChangesBean> source_balance_changes) {
            this.source_balance_changes = source_balance_changes;
        }

        public static class DestinationBalanceChangesBean {
            /**
             * counterparty :
             * currency : XRP
             * value : 20
             */

            private String counterparty;
            private String currency;
            private String value;

            public String getCounterparty() {
                return counterparty;
            }

            public void setCounterparty(String counterparty) {
                this.counterparty = counterparty;
            }

            public String getCurrency() {
                return currency;
            }

            public void setCurrency(String currency) {
                this.currency = currency;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class SourceBalanceChangesBean {
            /**
             * counterparty :
             * currency : XRP
             * value : -20
             */

            private String counterparty;
            private String currency;
            private String value;

            public String getCounterparty() {
                return counterparty;
            }

            public void setCounterparty(String counterparty) {
                this.counterparty = counterparty;
            }

            public String getCurrency() {
                return currency;
            }

            public void setCurrency(String currency) {
                this.currency = currency;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
