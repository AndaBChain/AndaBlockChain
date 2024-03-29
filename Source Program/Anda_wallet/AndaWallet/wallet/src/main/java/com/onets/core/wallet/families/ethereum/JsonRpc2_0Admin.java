package com.onets.core.wallet.families.ethereum;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;

/**
 * JSON-RPC 2.0 factory implementation for common Parity and Geth.
 */
public class JsonRpc2_0Admin extends JsonRpc2_0Web3j implements Admin {

    public JsonRpc2_0Admin(Web3jService web3jService) {
        super(web3jService);
    }

    public JsonRpc2_0Admin(Web3jService web3jService, long pollingInterval,
                           ScheduledExecutorService scheduledExecutorService) {
        super(web3jService, pollingInterval, scheduledExecutorService);
    }

    @Override
    public Request<?,PersonalListAccounts> personalListAccounts() {
        return new Request<String,PersonalListAccounts>(


                //"personal_listAccounts",
               /* Collections.<String>emptyList(),
                web3jService,
                PersonalListAccounts.class*/
        );
    }

    @Override
    public Request<?, NewAccountIdentifier> personalNewAccount(String password) {
        return null;
    }

    @Override
    public Request<?, PersonalUnlockAccount> personalUnlockAccount(String address, String passphrase, BigInteger duration) {
        return null;
    }

    @Override
    public Request<?, PersonalUnlockAccount> personalUnlockAccount(String address, String passphrase) {
        return null;
    }

    @Override
    public Request<?, EthSendTransaction> personalSendTransaction(Transaction transaction, String password) {
        return null;
    }
/*

    @Override
    public Request<?, PersonalListAccounts> personalListAccounts() {
        return new Request<String, PersonalListAccounts>(
                "personal_listAccounts",
                Collections.<String>emptyList(),
                web3jService,
                PersonalListAccounts.class
                );
    }

    @Override
    public Request<?, NewAccountIdentifier> personalNewAccount(String password) {
        return new Request<>(
                "personal_newAccount",
                Arrays.asList(password),
                web3jService,
                NewAccountIdentifier.class);
    }

    @Override
    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String accountId, String password,
            BigInteger duration) {
        List<Object> attributes = new ArrayList<>(3);
        attributes.add(accountId);
        attributes.add(password);

        if (duration != null) {
            // Parity has a bug where it won't support a duration
            // See https://github.com/ethcore/parity/issues/1215
            attributes.add(duration.longValue());
        } else {
            // we still need to include the null value, otherwise Parity rejects request
            attributes.add(null);
        }

        return new Request<String,PersonalUnlockAccount>(
                "personal_unlockAccount",
                attributes,
                web3jService,
                PersonalUnlockAccount.class);
    }

    @Override
    public Request<?,PersonalUnlockAccount> personalUnlockAccount(
            String accountId, String password) {

        return personalUnlockAccount(accountId, password, null);
    }

    @Override
    public Request<?, EthSendTransaction> personalSendTransaction(
            Transaction transaction, String passphrase) {
        return new Request<>(
                "personal_sendTransaction",
                Arrays.asList(transaction, passphrase),
                web3jService,
                EthSendTransaction.class);
    }
*/

}
