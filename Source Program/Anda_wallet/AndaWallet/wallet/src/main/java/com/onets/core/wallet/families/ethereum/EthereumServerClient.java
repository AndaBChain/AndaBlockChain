package com.onets.core.wallet.families.ethereum;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.onets.core.coins.CoinType;
import com.onets.core.coins.nxt.NxtException;
import com.onets.core.network.AddressStatus;
import com.onets.core.network.BlockHeader;
import com.onets.core.network.CoinAddress;
import com.onets.core.network.ConnectivityHelper;
import com.onets.core.network.ServerClient;
import com.onets.core.network.interfaces.BlockchainConnection;
import com.onets.core.network.interfaces.ConnectionEventListener;
import com.onets.core.network.interfaces.TransactionEventListener;
import com.onets.core.wallet.AbstractAddress;
import com.onets.core.wallet.families.ethereum.data.CurrencyEntry;
import com.onets.core.wallet.families.ethereum.data.TokenDisplay;
import com.onets.core.wallet.families.ethereum.data.TransactionDisplay;
import com.onets.core.wallet.families.ethereum.utils.EtherscanAPI;
import com.onets.core.wallet.families.ethereum.utils.ExchangeCalculator;
import com.onets.core.wallet.families.ethereum.utils.RequestCache;
import com.onets.core.wallet.families.ethereum.utils.ResponseParser;
import com.onets.core.wallet.families.ethereum.utils.WalletStorage;
import com.onets.stratumj.ServerAddress;
import com.onets.wallet.WalletApplication;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.utils.ListenerRegistration;
import org.bitcoinj.utils.Threading;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * @author Yu K.Q.
 * @author Yu K.Q.
 */
public class EthereumServerClient implements BlockchainConnection<EthereumTransaction> {

    private static final int POLL_INTERVAL_SEC = 30;

    private static final Logger log = LoggerFactory.getLogger(EthereumServerClient.class);

    private static final ScheduledThreadPoolExecutor connectionExec;

    static {
        connectionExec = new ScheduledThreadPoolExecutor(1);
        // FIXME, causing a crash in old Androids
//        connectionExec.setRemoveOnCancelPolicy(true);
    }

    private final ConnectivityHelper connectivityHelper;

    private CoinType type;
    private long retrySeconds = 0;
    private boolean stopped = false;
    private ServerAddress lastServerAddress;

    private String lastBalance = "";
    private BlockHeader lastBlockHeader = new BlockHeader(type, 0, 0);

    private List<TokenDisplay> token = new ArrayList<>();
    BigDecimal balanceDouble = new BigDecimal("0");



    // TODO, only one is supported at the moment. Change when accounts are supported.
    private transient CopyOnWriteArrayList<ListenerRegistration<ConnectionEventListener>> eventListeners;
    private ScheduledExecutorService blockchainSubscription;
    private ScheduledExecutorService ecSubscription;
    private ScheduledExecutorService addressSubscription;


    private Runnable reconnectTask = new Runnable() {
        public boolean isPolling = true;

        @Override
        public void run() {
            if (!stopped) {
                if (connectivityHelper.isConnected()) {
                    isPolling = false;
                } else {
                    // Start polling for connection to become available
                    if (!isPolling) log.info("No connectivity, starting polling.");
                    connectionExec.remove(reconnectTask);
                    connectionExec.schedule(reconnectTask, 10, TimeUnit.SECONDS);
                    isPolling = true;
                }
            } else {
                log.info("{} client stopped, aborting reconnect.", type.getName());
                isPolling = false;
            }
        }
    };


    public EthereumServerClient(CoinAddress coinAddress, ConnectivityHelper connectivityHelper) {
        this.connectivityHelper = connectivityHelper;
        eventListeners = new CopyOnWriteArrayList<ListenerRegistration<ConnectionEventListener>>();
        type = coinAddress.getType();
    }

    private static JSONObject parseReply(Response response) throws IOException, JSONException {
        return new JSONObject(response.body().string());
    }


    @Override
    public void subscribeToBlockchain(final TransactionEventListener listener) {

        log.info("Going to subscribe to block chain headers");
        if (blockchainSubscription != null) {
            blockchainSubscription.shutdownNow();
        }

        blockchainSubscription = Executors.newSingleThreadScheduledExecutor();
        blockchainSubscription.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {


                try {
                    EtherscanAPI.getInstance().getBlockNumber(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            log.info("Failed to communicate with server:  " + call.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String restring = ResponseParser.parseBlockNumber(response.body().string(), 16);
                                long timestamp = 0l;
                                int height = Integer.valueOf(restring);
                                BlockHeader blockheader = new BlockHeader(type, timestamp, height);

                                if (!lastBlockHeader.equals(blockheader)) {
                                    lastBlockHeader = blockheader;
                                    listener.onNewBlock(blockheader);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, POLL_INTERVAL_SEC, TimeUnit.SECONDS);

    }


    @Override
    public void getBlock(int height, TransactionEventListener<EthereumTransaction> listener) {
        throw new RuntimeException("AndaServerClient::getBlock not implemented");
    }

    /*
        Method that monitors account's unconfirmed balance.
        Raises onAddressStatusUpdate if changed.
         */
    @Override
    public void subscribeToAddresses(List<AbstractAddress> addresses,
                                     final TransactionEventListener<EthereumTransaction> listener) {
        if (addressSubscription != null) {
            addressSubscription.shutdownNow();
        }
        log.info("刷新以太坊 balance  ");
        addressSubscription = Executors.newSingleThreadScheduledExecutor();
        log.info("   以太坊   balance    1  ");
        for (final AbstractAddress address : addresses) {
            log.info("Going to subscribe to {}", address);
            Log.d(TAG, "subscribeToAddresses: -----address:"+address);
            String EthAddress=address.toString();
            addressSubscription.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {

                    balanceDouble = new BigDecimal("0");
                    try {

                       // EtherscanAPI.getInstance().getBalance("0x829BD824B016326A401d083B33D092293333A830", new Callback() {
                       EtherscanAPI.getInstance().getBalance(EthAddress, new Callback() {

                                @Override
                            public void onFailure(Call call, IOException e) {
                                log.info("Failed to communicate with server:  " + call.toString());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                BigDecimal ethbal;
                                try {
                                    String jsonData = response.body().string();
                                    Log.i("RippleServerClient", " Balance onResponse: " + jsonData);
                                    JSONObject object = new JSONObject(jsonData);
                                    String balance = object.getString("result");
                                    Log.d(TAG, "onResponse: ----20189251635balance"+balance);
                                    if (balance.equals("")) {
                                        ethbal = new BigDecimal(0);
                                    } else {
                                        ethbal = new BigDecimal(balance);
                                    }
                                    Log.d(TAG, "onResponse: ----20189251639balance"+balance);
                                    Log.d(TAG, "onResponse: ----20189251640ethbal"+ethbal);
                                    token.add(0, new TokenDisplay("Ether", "ETH", ethbal, 3, 1, "", "", 0, 0));
                                    Log.d(TAG, "onResponse: ----20189251641ethbal"+ethbal);
                                    balanceDouble = balanceDouble.add(ethbal);
                                    Log.d(TAG, "onResponse: ----20189251641balanceDouble"+balanceDouble);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                final CurrencyEntry cur = ExchangeCalculator.getInstance().getCurrent();
                                Log.d(TAG, "onResponse: -----balanceDouble"+balanceDouble);
                                String status = ExchangeCalculator.getInstance().convertRateExact(balanceDouble, cur.getRate());
                                Log.i("---------------", "Convert  balance onResponse: " + status
                                        + "\ndoublebalance:" + balanceDouble);
                                AddressStatus addressStatus = new AddressStatus(address, status);

                                Log.i("---------------balance ", "onResponse: lastBalance" + lastBalance
                                        + "\nstatus :" + status);

                                if (!lastBalance.equals(status)) {
                                    Log.i("---------------balance ", "onResponse: 钱包AddressStatus 更新");
                                    lastBalance = status;
                                    listener.onAddressStatusUpdate(addressStatus);
                                }
                                //listener.onAddressStatusUpdate(addressStatus);
                            }
                        });
                    } catch (IOException e) {
                        log.info("IOException: " + e.getMessage());
                    }

                }
            }, 0, POLL_INTERVAL_SEC, TimeUnit.SECONDS);

        }
        log.info("-----------------", "      balance    2  ");

    }

    /*
        Method that fetches all account's transactions.
        Only call it when account's balance has changed
     */
    @Override
    public void getHistoryTx(final AddressStatus status, final TransactionEventListener listener) {

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("Going to fetch txs for {}", status.getAddress().toString());
                final String address = status.getAddress().toString();

                try {

                    EtherscanAPI.getInstance().getNormalTransactions("0xf3d6be49d35930421c28d65Be5274FE1C7733e5c", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            log.info("Failed to communicate with server:  " + call.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            if (restring != null && restring.length() > 2)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_NORMAL, address, restring);

                            final List<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", address, TransactionDisplay.NORMAL));

                            ImmutableList.Builder<ServerClient.HistoryTx> historyTxs = ImmutableList.builder();

                            try {

                                //限制了显示的交易记录上限
                                if (w.size() <= 200000) {
                                    for (int j = 0; j < w.size(); j++) {

                                        JSONObject histTx = new JSONObject();
                                        histTx.put("tx_hash", w.get(j).getTxHash().substring(2));
                                        histTx.put("height", w.get(j).getBlock());
                                        historyTxs.add(new ServerClient.HistoryTx(histTx));
                                        log.info("added to historyTx: {}", w.get(j).getTxHash());

                                    }
                                } else {
                                    for (int j = 0; j < 200000; j++) {

                                        JSONObject histTx = new JSONObject();
                                        histTx.put("tx_hash", w.get(j).getTxHash().substring(2));
                                        histTx.put("height", w.get(j).getBlock());
                                        historyTxs.add(new ServerClient.HistoryTx(histTx));
                                        log.info("added to historyTx: {}", w.get(j).getTxHash());

                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onTransactionHistory(status, historyTxs.build());
                        }
                    }, true);

                    EtherscanAPI.getInstance().getInternalTransactions(address, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            log.info("Failed to communicate with server:  " + call.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            if (restring != null && restring.length() > 2)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_INTERNAL, address, restring);
                            final List<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", address, TransactionDisplay.CONTRACT));
                            ImmutableList.Builder<ServerClient.HistoryTx> historyTxs = ImmutableList.builder();

                            try {
                                for (int j = 0; j < w.size(); j++) {

                                    JSONObject histTx = new JSONObject();
                                    histTx.put("tx_hash", w.get(j).getTxHash());
                                    histTx.put("height", w.get(j).getBlock());
                                    historyTxs.add(new ServerClient.HistoryTx(histTx));
                                    log.info("added to historyTx: {}", w.get(j).getTxHash());

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onTransactionHistory(status, historyTxs.build());
                        }
                    }, true);

                } catch (IOException e) {
                    log.info("IOException: " + e.getMessage());
                }

            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public void getTransaction(final Sha256Hash txHash,
                               final TransactionEventListener<EthereumTransaction> listener) {

        try {
            final int[] height = {0};
            try {
                EtherscanAPI.getInstance().getBlockNumber(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        log.info("Failed to communicate with server:  " + call.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String restring = ResponseParser.parseBlockNumber(response.body().string(), 16);

                            height[0] = Integer.valueOf(restring);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            EtherscanAPI.getInstance().getTransactionByHash(txHash.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.info("Failed to communicate with server:  " + call.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String content = response.body().string();
                        Log.e("-----------", " getTransactionByHash Response: " + content);

                        if (content.substring(0, 1).equals("{")) {
                            JSONObject jsonObject = new JSONObject(content);
                            JSONObject result = jsonObject.getJSONObject("result");

                            String fromAddress = result.getString("from");
                            String toAddress = result.getString("to");
                            BigInteger amount = new BigInteger(result.getString("value").substring(2), 16);
                            long date = 0l;
                            String walletName = "ethereum";
                            byte typeTx = 0;
                            String txHash = result.getString("hash");
                            String nonce = result.getString("nonce");
                            long block = Long.parseLong(new BigInteger(result.getString("blockNumber").substring(2), 16).toString());
                            int gasUsed = Integer.parseInt(new BigInteger(result.getString("gas").substring(2), 16).toString());
                            long gasprice = Long.parseLong(new BigInteger(result.getString("gasPrice").substring(2), 16).toString());
                            boolean error = false;
                            int confirmationStatus = (int) (height[0] - block);
//                            EthereumTransaction tx = new EthereumTransaction(type,

//                                    new TransactionDisplay(
//                                            fromAddress, toAddress, amount, confirmationStatus, date,
//                                            walletName, typeTx, txHash, nonce, block, gasUsed, gasprice, error),
//                                    height[0]);
                            EthereumTransaction tx = new EthereumTransaction(type,
                                    TransactionImp.parseTransaction(fromAddress, toAddress, amount,gasUsed,gasprice ,txHash,
                                            "0", height[0], block, confirmationStatus));
                            listener.onTransactionUpdate(tx);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NxtException.NotValidException e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void broadcastTx(EthereumTransaction tx, final TransactionEventListener listener) {
//        try {
//            BigInteger gaslimit = new BigInteger("21000");
//            String gasPrice = new BigDecimal(10 + "").multiply(new BigDecimal("1000000000")).toBigInteger().toString();
//            TransactionImp impl = tx.getRawTransaction();
//            String fromAddress = impl.getSenderAddress();
//            final String toAddress = impl.getTo();
//            final String amount = impl.getAmountETH().toString();
//            final String gas_price = gasPrice;
//            final String gas_limit = gaslimit.toString();
//            final String data = impl.getData();
//            String password = "123456789";
//
//            final Credentials keys = WalletStorage.getInstance(WalletApplication.getInstance())
//                    .getFullWallet(WalletApplication.getInstance(), password, fromAddress);
//
//            EtherscanAPI.getInstance().getNonceForAddress(fromAddress, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Log.e(TAG, "onFailure: Can't connect to network, retry it later");
//                }
//
//                @Override
//                public void onResponse(Call call, final Response response) throws IOException {
//                    try {
//                        JSONObject o = new JSONObject(response.body().string());
//                        BigInteger nonce = new BigInteger(o.getString("result").substring(2), 16);
//
//                        RawTransaction tx = RawTransaction.createTransaction(
//                                nonce,
//                                new BigInteger(gas_price),
//                                new BigInteger(gas_limit),
//                                toAddress,
//                                new BigDecimal(amount).multiply(ExchangeCalculator.ONE_ETHER).toBigInteger(),
//                                data
//                        );
//
//                        Log.d("txx",
//                                "Nonce: " + tx.getNonce() + "\n" +
//                                        "gasPrice: " + tx.getGasPrice() + "\n" +
//                                        "gasLimit: " + tx.getGasLimit() + "\n" +
//                                        "To: " + tx.getTo() + "\n" +
//                                        "Amount: " + tx.getValue() + "\n" +
//                                        "Data: " + tx.getData()
//                        );
//
//                        byte[] signed = TransactionEncoder.signMessage(tx, (byte) 1, keys);
//
//                        forwardTX(signed, listener);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Log.e(TAG, "onResponse: Can't connect to network, retry it later");
//                    }
//                }
//            });
//
//        } catch (Exception e) {
//            listener.onTransactionBroadcastError("Invalid Wallet Password!");
//            Log.e(TAG, "Invalid Wallet Password!");
//            e.printStackTrace();
//        }
    }


    private void forwardTX(byte[] signed, final TransactionEventListener listener) throws IOException {
        EtherscanAPI.getInstance().forwardTransaction("0x" + Hex.toHexString(signed), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Can't connect to network, retry it later");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String received = response.body().string();
                try {
                    Log.e(TAG, "TXHash :  0  " + new JSONObject(received).getString("result"));
                } catch (Exception e) {
                    // Advanced error handling. If etherscan returns error message show the shortened version in notification. Else abbort with unknown error
                    try {
                        String errormsg = new JSONObject(received).getJSONObject("error").getString("message");
                        if (errormsg.indexOf(".") > 0)
                            errormsg = errormsg.substring(0, errormsg.indexOf("."));
                        Log.e(TAG, errormsg); // f.E Insufficient funds
                    } catch (JSONException e1) {
                        Log.e(TAG, "Unknown error occured");
                    }
                }
            }
        });
    }


    private String TAG = "------------EthereumServerClient";

    @Override
    public boolean broadcastTxSync(final EthereumTransaction tx) {
        final boolean[] netComplete = {false};
        Log.i(TAG, "broadcastTxSync..........");
        final SharedPreferences error = WalletApplication.getInstance().getSharedPreferences("error", Context.MODE_PRIVATE);
        final boolean[] isSuccess = {false};

        try {
            BigInteger gaslimit = new BigInteger("21000");
            String gasPrice = new BigDecimal(10 + "").multiply(new BigDecimal("1000000000")).toBigInteger().toString();
            TransactionImp impl = tx.getRawTransaction();
            final String fromAddress = impl.getSenderAddress();

            final String toAddress = impl.getTo();

            Log.i(TAG, "broadcastTxSync: toAddress : " + toAddress);

            final String amount = impl.getAmountETH().toString();
            final String gas_price = gasPrice;
            final String gas_limit = gaslimit.toString();
            String dataconver = "";
            if(impl.getData() != null){
                dataconver = impl.getData();
            }
            final String data = dataconver;

            String password = new String(impl.getSignature());

            final Credentials keys = WalletStorage.getInstance(WalletApplication.getInstance())
                    .getFullWallet(WalletApplication.getInstance(), password, fromAddress);


            EtherscanAPI.getInstance().getNonceForAddress(fromAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "onFailure: Can't connect to network, retry it later");
                    netComplete[0] = true;
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    try {
                        String responseStr = response.body().string();
                        Log.i(TAG, "onResponse:  ----------  fromAddress :"+fromAddress+"  |   response :" + responseStr);
                        JSONObject o = new JSONObject(responseStr);
                        BigInteger nonce = new BigInteger(o.getString("result").substring(2), 16);
                        Log.i(TAG, "onResponse:  ----------  nonce :" + nonce);


                        RawTransaction tx = RawTransaction.createTransaction(
                                nonce,
                                new BigInteger(gas_price),
                                new BigInteger(gas_limit),
                                toAddress,
                                new BigDecimal(amount).multiply(ExchangeCalculator.ONE_ETHER).toBigInteger(),
                                data
                        );
                        Log.i(TAG, "onResponse:  ----------  create RawTransaction success ");


                        Log.d("txx",
                                "Nonce: " + tx.getNonce() + "\n" +
                                        "gasPrice: " + tx.getGasPrice() + "\n" +
                                        "gasLimit: " + tx.getGasLimit() + "\n" +
                                        "To: " + tx.getTo() + "\n" +
                                        "Amount: " + tx.getValue() + "\n" +
                                        "Data: " + tx.getData()
                        );

                        byte[] signed = TransactionEncoder.signMessage(tx, (byte) 1, keys);
                        Log.i(TAG, "onResponse:  ----------   RawTransaction signed ");

                        EtherscanAPI.getInstance().forwardTransaction("0x" + Hex.toHexString(signed), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e(TAG, "Can't connect to network, retry it later");
                                netComplete[0] = true;
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                String received = response.body().string();
                                Log.i(TAG, "onResponse:  ---------forwardTransaction  received : "+received);

                                try {
                                    String txHash = new JSONObject(received).getString("result");
                                    Log.e(TAG, "TXHash :  " + txHash);
                                    isSuccess[0] = true;
                                    netComplete[0] = true;
                                } catch (Exception e) {
                                    // Advanced error handling. If etherscan returns error message show the shortened version in notification. Else abbort with unknown error
                                    try {
                                        String errormsg = new JSONObject(received).getJSONObject("error").getString("message");
                                        if (errormsg.indexOf(".") > 0)
                                            errormsg = errormsg.substring(0, errormsg.indexOf("."));
                                        netComplete[0] = true;
                                        boolean saveError = error.edit().putString("error",errormsg).commit();
                                        Log.e(TAG, "错误信息是否保存："+saveError+"  |  error : "+errormsg); // f.E Insufficient funds
                                    } catch (JSONException e1) {
                                        error.edit().putString("error","Unknown error occured").apply();
                                        netComplete[0] = true;
                                        Log.e(TAG, "Unknown error occured");
                                    }
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        netComplete[0] = true;
                        Log.e(TAG, "onResponse: Can't connect to network, retry it later");
                    }
                }
            });

        } catch (Exception e) {
            error.edit().putString("error","Invalid Wallet Password!").apply();
            netComplete[0] = true;
            Log.e(TAG, "Invalid Wallet Password!");
            e.printStackTrace();
        }

        while (true){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (netComplete[0]){
                break;
            }
        }

        return isSuccess[0];
    }




    @Override
    public void ping(String versionString) {

    }

    @Override
    public void addEventListener(ConnectionEventListener listener) {
        addEventListener(listener, Threading.USER_THREAD);

    }

    private void addEventListener(ConnectionEventListener listener, Executor executor) {
        boolean isNew = !ListenerRegistration.removeFromList(listener, eventListeners);
        eventListeners.add(new ListenerRegistration<ConnectionEventListener>(listener, executor));
        if (isNew && isActivelyConnected()) {
            broadcastOnConnection();
        }
    }

    private void broadcastOnConnection() {
        for (final ListenerRegistration<ConnectionEventListener> registration : eventListeners) {
            registration.executor.execute(new Runnable() {
                @Override
                public void run() {
                    registration.listener.onConnection(EthereumServerClient.this);
                }
            });
        }
    }

    private void broadcastOnDisconnect() {
        for (final ListenerRegistration<ConnectionEventListener> registration : eventListeners) {
            registration.executor.execute(new Runnable() {
                @Override
                public void run() {
                    registration.listener.onDisconnect();
                }
            });
        }
    }

    @Override
    public void resetConnection() {

    }

    @Override
    public void stopAsync() {
        if (stopped) return;
        stopped = true;
        if (isActivelyConnected()) broadcastOnDisconnect();
        eventListeners.clear();
        connectionExec.remove(reconnectTask);
        if (blockchainSubscription != null) {
            blockchainSubscription.shutdownNow();
            blockchainSubscription = null;
        }
        if (ecSubscription != null) {
            ecSubscription.shutdownNow();
            ecSubscription = null;
        }
        if (addressSubscription != null) {
            addressSubscription.shutdownNow();
            addressSubscription = null;
        }

    }

    @Override
    public boolean isActivelyConnected() {
        // TODO implement
        return true;
    }

    @Override
    public void startAsync() {
        // TODO implement
    }

}
