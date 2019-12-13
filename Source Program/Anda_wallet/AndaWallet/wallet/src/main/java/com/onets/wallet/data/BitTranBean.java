package com.onets.wallet.data;

import java.util.List;

/**
 * 比特币交易json实体类，使用btc区块浏览器
 * 比特币解析交易json字符串，放在服务器中
 */
public class BitTranBean {


    //String urlPath = "https://chain.api.btc.com/v3/tx/" + tx.getHash().toString() + "?verbose=2";
    /**
     * err_no : 0
     * data : {"confirmations":111,"block_height":551683,"block_hash":"0000000000000000001123605c83c20ba19a17e72a494fe1028b5c311a05f293","block_time":1543309546,"created_at":1543308704,"fee":12000,"hash":"7d54675b5d67a0a639f31506c59d81ae90158b3d13bdac9abe0f82e79c4988dc","inputs_count":1,"inputs_value":293300,"is_coinbase":false,"is_double_spend":false,"is_sw_tx":false,"weight":900,"vsize":225,"witness_hash":"7d54675b5d67a0a639f31506c59d81ae90158b3d13bdac9abe0f82e79c4988dc","lock_time":0,"outputs_count":2,"outputs_value":281300,"size":225,"sigops":8,"version":1,"inputs":[{"prev_addresses":["1KrguJtYqYg5TNBZYpRwBNjDVkvWpP16UK"],"prev_position":0,"prev_tx_hash":"89ea98edf7fd8760d40cdbfe394b8be7dd4cbcb9eb61be7ce44d76b17fea545c","prev_type":"P2PKH","prev_value":293300,"sequence":4294967295}],"outputs":[{"addresses":["196kVGhR2PGXnorwWBEJFcWvhXgMUJx2YR"],"value":251300,"type":"P2PKH","spent_by_tx":null,"spent_by_tx_position":-1},{"addresses":["19zDwLrGCkJtnpZBb8o6bTx3BZzeM6cYfU"],"value":30000,"type":"P2PKH","spent_by_tx":null,"spent_by_tx_position":-1}]}
     */

    private int err_no;
    private DataBean data;

    public int getErr_no() {
        return err_no;
    }

    public void setErr_no(int err_no) {
        this.err_no = err_no;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * confirmations : 111
         * block_height : 551683
         * block_hash : 0000000000000000001123605c83c20ba19a17e72a494fe1028b5c311a05f293
         * block_time : 1543309546
         * created_at : 1543308704
         * fee : 12000
         * hash : 7d54675b5d67a0a639f31506c59d81ae90158b3d13bdac9abe0f82e79c4988dc
         * inputs_count : 1
         * inputs_value : 293300
         * is_coinbase : false
         * is_double_spend : false
         * is_sw_tx : false
         * weight : 900
         * vsize : 225
         * witness_hash : 7d54675b5d67a0a639f31506c59d81ae90158b3d13bdac9abe0f82e79c4988dc
         * lock_time : 0
         * outputs_count : 2
         * outputs_value : 281300
         * size : 225
         * sigops : 8
         * version : 1
         * inputs : [{"prev_addresses":["1KrguJtYqYg5TNBZYpRwBNjDVkvWpP16UK"],"prev_position":0,"prev_tx_hash":"89ea98edf7fd8760d40cdbfe394b8be7dd4cbcb9eb61be7ce44d76b17fea545c","prev_type":"P2PKH","prev_value":293300,"sequence":4294967295}]
         * outputs : [{"addresses":["196kVGhR2PGXnorwWBEJFcWvhXgMUJx2YR"],"value":251300,"type":"P2PKH","spent_by_tx":null,"spent_by_tx_position":-1},{"addresses":["19zDwLrGCkJtnpZBb8o6bTx3BZzeM6cYfU"],"value":30000,"type":"P2PKH","spent_by_tx":null,"spent_by_tx_position":-1}]
         */

        private int confirmations;
        private int block_height;
        private String block_hash;
        private int block_time;
        private int created_at;
        private int fee;
        private String hash;
        private int inputs_count;
        private int inputs_value;
        private boolean is_coinbase;
        private boolean is_double_spend;
        private boolean is_sw_tx;
        private int weight;
        private int vsize;
        private String witness_hash;
        private int lock_time;
        private int outputs_count;
        private int outputs_value;
        private int size;
        private int sigops;
        private int version;
        private List<InputsBean> inputs;
        private List<OutputsBean> outputs;

        public int getConfirmations() {
            return confirmations;
        }

        public void setConfirmations(int confirmations) {
            this.confirmations = confirmations;
        }

        public int getBlock_height() {
            return block_height;
        }

        public void setBlock_height(int block_height) {
            this.block_height = block_height;
        }

        public String getBlock_hash() {
            return block_hash;
        }

        public void setBlock_hash(String block_hash) {
            this.block_hash = block_hash;
        }

        public int getBlock_time() {
            return block_time;
        }

        public void setBlock_time(int block_time) {
            this.block_time = block_time;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }

        public int getFee() {
            return fee;
        }

        public void setFee(int fee) {
            this.fee = fee;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public int getInputs_count() {
            return inputs_count;
        }

        public void setInputs_count(int inputs_count) {
            this.inputs_count = inputs_count;
        }

        public int getInputs_value() {
            return inputs_value;
        }

        public void setInputs_value(int inputs_value) {
            this.inputs_value = inputs_value;
        }

        public boolean isIs_coinbase() {
            return is_coinbase;
        }

        public void setIs_coinbase(boolean is_coinbase) {
            this.is_coinbase = is_coinbase;
        }

        public boolean isIs_double_spend() {
            return is_double_spend;
        }

        public void setIs_double_spend(boolean is_double_spend) {
            this.is_double_spend = is_double_spend;
        }

        public boolean isIs_sw_tx() {
            return is_sw_tx;
        }

        public void setIs_sw_tx(boolean is_sw_tx) {
            this.is_sw_tx = is_sw_tx;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getVsize() {
            return vsize;
        }

        public void setVsize(int vsize) {
            this.vsize = vsize;
        }

        public String getWitness_hash() {
            return witness_hash;
        }

        public void setWitness_hash(String witness_hash) {
            this.witness_hash = witness_hash;
        }

        public int getLock_time() {
            return lock_time;
        }

        public void setLock_time(int lock_time) {
            this.lock_time = lock_time;
        }

        public int getOutputs_count() {
            return outputs_count;
        }

        public void setOutputs_count(int outputs_count) {
            this.outputs_count = outputs_count;
        }

        public int getOutputs_value() {
            return outputs_value;
        }

        public void setOutputs_value(int outputs_value) {
            this.outputs_value = outputs_value;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getSigops() {
            return sigops;
        }

        public void setSigops(int sigops) {
            this.sigops = sigops;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public List<InputsBean> getInputs() {
            return inputs;
        }

        public void setInputs(List<InputsBean> inputs) {
            this.inputs = inputs;
        }

        public List<OutputsBean> getOutputs() {
            return outputs;
        }

        public void setOutputs(List<OutputsBean> outputs) {
            this.outputs = outputs;
        }

        public static class InputsBean {
            /**
             * prev_addresses : ["1KrguJtYqYg5TNBZYpRwBNjDVkvWpP16UK"]
             * prev_position : 0
             * prev_tx_hash : 89ea98edf7fd8760d40cdbfe394b8be7dd4cbcb9eb61be7ce44d76b17fea545c
             * prev_type : P2PKH
             * prev_value : 293300
             * sequence : 4294967295
             */

            private int prev_position;
            private String prev_tx_hash;
            private String prev_type;
            private int prev_value;
            private long sequence;
            private List<String> prev_addresses;

            public int getPrev_position() {
                return prev_position;
            }

            public void setPrev_position(int prev_position) {
                this.prev_position = prev_position;
            }

            public String getPrev_tx_hash() {
                return prev_tx_hash;
            }

            public void setPrev_tx_hash(String prev_tx_hash) {
                this.prev_tx_hash = prev_tx_hash;
            }

            public String getPrev_type() {
                return prev_type;
            }

            public void setPrev_type(String prev_type) {
                this.prev_type = prev_type;
            }

            public int getPrev_value() {
                return prev_value;
            }

            public void setPrev_value(int prev_value) {
                this.prev_value = prev_value;
            }

            public long getSequence() {
                return sequence;
            }

            public void setSequence(long sequence) {
                this.sequence = sequence;
            }

            public List<String> getPrev_addresses() {
                return prev_addresses;
            }

            public void setPrev_addresses(List<String> prev_addresses) {
                this.prev_addresses = prev_addresses;
            }
        }

        public static class OutputsBean {
            /**
             * addresses : ["196kVGhR2PGXnorwWBEJFcWvhXgMUJx2YR"]
             * value : 251300
             * type : P2PKH
             * spent_by_tx : null
             * spent_by_tx_position : -1
             */

            private int value;
            private String type;
            private Object spent_by_tx;
            private int spent_by_tx_position;
            private List<String> addresses;

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Object getSpent_by_tx() {
                return spent_by_tx;
            }

            public void setSpent_by_tx(Object spent_by_tx) {
                this.spent_by_tx = spent_by_tx;
            }

            public int getSpent_by_tx_position() {
                return spent_by_tx_position;
            }

            public void setSpent_by_tx_position(int spent_by_tx_position) {
                this.spent_by_tx_position = spent_by_tx_position;
            }

            public List<String> getAddresses() {
                return addresses;
            }

            public void setAddresses(List<String> addresses) {
                this.addresses = addresses;
            }
        }
    }
}
