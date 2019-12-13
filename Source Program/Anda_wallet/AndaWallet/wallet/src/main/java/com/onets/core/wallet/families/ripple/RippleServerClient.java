package com.onets.core.wallet.families.ripple;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.onets.core.coins.CoinType;
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
import com.onets.core.wallet.families.ethereum.utils.ExchangeCalculator;
import com.onets.core.wallet.families.ethereum.utils.RequestCache;
import com.onets.core.wallet.families.ripple.bean.RippleHistoryTxBean;
import com.onets.core.wallet.families.ripple.client.AndroidClient;
import com.onets.core.wallet.families.ripple.utils.RippleAPI;
import com.onets.stratumj.ServerAddress;
import com.onets.wallet.WalletApplication;
import com.onets.wallet.util.WalletUtils;
import com.ripple.client.Account;
import com.ripple.client.transactions.ManagedTxn;
import com.ripple.client.transactions.TransactionManager;
import com.ripple.config.Config;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.types.known.tx.result.TransactionResult;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.crypto.ecdsa.Seed;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.utils.ListenerRegistration;
import org.bitcoinj.utils.Threading;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
public class RippleServerClient implements BlockchainConnection<RippleTransaction> {

    private static final int POLL_INTERVAL_SEC = 30;

    private static final Logger log = LoggerFactory.getLogger(RippleServerClient.class);

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

    private static AndroidClient client = WalletApplication.getRippleClient();
    //记录交易时错误信息
    SharedPreferences error = WalletApplication.getInstance().getSharedPreferences("error", Context.MODE_PRIVATE);


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


    public RippleServerClient(CoinAddress coinAddress, ConnectivityHelper connectivityHelper) {
        this.connectivityHelper = connectivityHelper;
        eventListeners = new CopyOnWriteArrayList<ListenerRegistration<ConnectionEventListener>>();
        type = coinAddress.getType();
    }

    private static JSONObject parseReply(Response response) throws IOException, JSONException {
        return new JSONObject(response.body().string());
    }


    @Override
    public void subscribeToBlockchain(final TransactionEventListener listener) {

    }


    @Override
    public void getBlock(int height, TransactionEventListener<RippleTransaction> listener) {
        throw new RuntimeException("RippleServerClient::getBlock not implemented");
    }

    /*
        Method that monitors account's unconfirmed balance.
        Raises onAddressStatusUpdate if changed.
         */
    @Override
    public void subscribeToAddresses(List<AbstractAddress> addresses,
                                     final TransactionEventListener<RippleTransaction> listener) {
        if (addressSubscription != null) {
            addressSubscription.shutdownNow();
        }
        log.info("刷新Ripple balance  ");
        addressSubscription = Executors.newSingleThreadScheduledExecutor();
        log.info("   Ripple   balance    1  ");
        for (final AbstractAddress address : addresses) {
            log.info("Going to subscribe to {}", address);

            addressSubscription.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {

                    balanceDouble = new BigDecimal("0");
                    try {

                        //rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ  todo:  测试Address
                        RippleAPI.getInstance().getBalance("rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                log.info("Failed to communicate with server:  " + call.toString());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                BigDecimal rippleBalance;
                                try {
                                    String jsonData = response.body().string();
                                    Log.i("RippleServerClient", " Balance onResponse: " + jsonData);
                                    JSONObject object = new JSONObject(jsonData);
                                    String result = object.getString("result");
                                    if (result.equals("success")) {
                                        JSONArray balancesArray = object.getJSONArray("balances");
                                        JSONObject xrpBalance = (JSONObject) balancesArray.get(0);
                                        String balance = xrpBalance.getString("value");
                                        rippleBalance = new BigDecimal(balance);
                                    } else {
                                        rippleBalance = new BigDecimal(0);
                                    }

                                    balanceDouble = balanceDouble.add(rippleBalance.multiply(new BigDecimal(1000000)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                final CurrencyEntry cur = ExchangeCalculator.getInstance().getCurrent();
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

        log.info("Going to fetch txs for {}", status.getAddress().toString());
        address = status.getAddress().toString();

        ImmutableList.Builder<ServerClient.HistoryTx> historyTxs = ImmutableList.builder();
        listener.onTransactionHistory(status, historyTxs.build());

    }

    private String address;

    @Override
    public void getTransaction(final Sha256Hash txHash,
                               final TransactionEventListener<RippleTransaction> listener) {

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {

                    //todo:测试地址
                    RippleAPI.getInstance().getNormalTransactions("rNmE9PtZhUcBWXesx2Xs9nwGsXKTfeBKaQ", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            log.info("Failed to communicate with server:  " + call.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            if (restring != null && restring.length() > 2 && address != null)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_NORMAL, address, restring);

                            Gson gson = new Gson();
                            RippleHistoryTxBean txBean = gson.fromJson(restring, RippleHistoryTxBean.class);

                            ImmutableList.Builder<ServerClient.HistoryTx> historyTxs = ImmutableList.builder();

                            try {
                                for (int i = 0; i < txBean.getCount(); i++) {
                                    RippleHistoryTxBean.PaymentsBean paymentsBean = txBean.getPayments().get(i);

                                    JSONObject histTx = new JSONObject();
                                    histTx.put("tx_hash", WalletUtils.exChange(paymentsBean.getTx_hash()));
                                    histTx.put("height", paymentsBean.getLedger_index());
                                    historyTxs.add(new ServerClient.HistoryTx(histTx));
                                    log.info("added to historyTx: {}", paymentsBean.getTx_hash());

                                    String value = paymentsBean.getDestination_balance_changes().get(0).getValue();

                                    BigDecimal b1 = new BigDecimal(value);
                                    BigDecimal b2 = new BigDecimal(1000000);
                                    String amount = String.valueOf(b1.multiply(b2).longValue());
                                    Log.i(TAG, " ---------------------------------\n\n\n\n\n\nonResponse: "+amount);
                                    RippleTransactionImpl.BuilderImpl builder = new RippleTransactionImpl.BuilderImpl(
                                            paymentsBean.getSource(), amount, paymentsBean.getDestination()
                                    );
                                    builder.txHash(paymentsBean.getTx_hash());
                                    RippleTransaction tx = new RippleTransaction(type, builder.build());
                                    tx.setDepthInBlocks(5);
                                    tx.setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
                                    listener.onTransactionUpdate(tx);

                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, true);

                } catch (IOException e) {
                    log.info("IOException: " + e.getMessage());
                }


            }
        }, 0, 15, TimeUnit.SECONDS);
    }


    @Override
    public void broadcastTx(RippleTransaction tx, final TransactionEventListener listener) {

    }

    private String TAG = "------------RippleServerClient";
    private boolean[] isSuccess = {false};
    private RippleTransactionImpl successRippleTx;

    @Override
    public boolean broadcastTxSync(final RippleTransaction tx) {
        boolean[] netComplete = {false};
        Log.i(TAG, "broadcastTxSync..........");


        RippleTransactionImpl impl = tx.getRawTransaction();

        final String toAddress = impl.getResult().getTx_json().getDestination();

        Log.i(TAG, "broadcastTxSync: toAddress : " + toAddress);

        final String amount = impl.getResult().getTx_json().getAmount();

        String addr = impl.getResult().getTx_json().getTxnSignature();
        Config.initBouncy();
        byte[] seedBytes = Arrays.copyOfRange(addr.getBytes(), 0, 16);
        Seed seed = new Seed(seedBytes);

        Account account = client.accountFromSeed(seed + "");
        if (!account.getAccountRoot().primed()) {
            error.edit().putString("error", "Awaiting account_info").apply();
            Log.i(TAG, "Awaiting account_info");
            return false;
        } else {
            payOneDrop(account, toAddress, amount, netComplete);
        }

        while (true) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (netComplete[0]) {
                if (successRippleTx != null) {
                    tx.setRawTransaction(successRippleTx);
                }
                break;
            }
        }

        return isSuccess[0];
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                isSuccess[0] = true;
            }

        }
    };

    /**
     * Thread: ui thread
     */
    private void payOneDrop(final Account account, String destinationAddress, final String value, final boolean[] netComplete) {
        final AccountID destination = AccountID.fromString(destinationAddress);

        client.run(new Runnable() {
            @Override
            public void run() {
                makePayment(account, destination, value, netComplete);
            }
        });
    }

    /**
     * Thread: Client thread
     */
    private void makePayment(final Account account, Object destination, Object amt, final boolean[] netComplete) {
        TransactionManager tm = account.transactionManager();

        Payment payment = new Payment();
        ManagedTxn tx = tm.manage(payment);

        payment.putTranslated(AccountID.Destination, destination);
        payment.putTranslated(Amount.Amount, amt);

        tx.once(ManagedTxn.OnSubmitSuccess.class, new ManagedTxn.OnSubmitSuccess() {
            @Override
            public void called(com.ripple.client.responses.Response response) {
                Log.i(TAG, "------------------------  " +
                        "called: OnSubmitSuccess   response :" + response.message
                        + "\n response:" + response.result
                );

                Gson gson = new Gson();
                successRippleTx = gson.fromJson(response.message.toString(), RippleTransactionImpl.class);
                mHandler.sendEmptyMessage(0);
                error.edit().putString("error","").apply();
                Log.i(TAG, "Transaction submitted "
                        + awaitingTransactionsParenthetical(account));
                netComplete[0] = true;
            }
        });
        tx.once(ManagedTxn.OnSubmitFailure.class, new ManagedTxn.OnSubmitFailure() {
            @Override
            public void called(com.ripple.client.responses.Response response) {

                Log.i(TAG, "------------------------  " +
                        "called: OnSubmitFailure   response :" + response.engineResult().toJSON());

                error.edit().putString("error", "Transaction submission failed"
                        + response.engineResult().toString()).apply();

                Log.i(TAG, "Transaction submission failed"
                        + awaitingTransactionsParenthetical(account));

                netComplete[0] = true;
            }
        });
        tx.once(ManagedTxn.OnTransactionValidated.class,
                new ManagedTxn.OnTransactionValidated() {
                    @Override
                    public void called(TransactionResult result) {


                        Log.i(TAG, "------------------------  " +
                                "called: OnTransactionValidated   result :" + result.toString());
                        error.edit().putString("error", result.engineResult.toString()).apply();

                        Log.i(TAG, "Transaction submission failed" +
                                "Transaction finalized "
                                + awaitingTransactionsParenthetical(account));

                        netComplete[0] = true;
                    }
                });
        tm.queue(tx);

        Log.i(TAG, "-----------done ------------  Transaction submission failed " +
                "Transaction queued " + awaitingTransactionsParenthetical(account));
    }

    /**
     * Thread: client thread
     */
    private String awaitingTransactionsParenthetical(Account account) {
        int awaiting = account.transactionManager().txnsPending();
        if (awaiting == 0) {
            return "";
        } else {
            ArrayList<ManagedTxn> queued = account.transactionManager().pendingSequenceSorted();
            String s = "";

            for (ManagedTxn fields : queued) {
                s = s + fields.transactionType() + ",";
            }

            return String.format("(awaiting %s %d)", s, awaiting);
        }
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
                    registration.listener.onConnection(RippleServerClient.this);
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
