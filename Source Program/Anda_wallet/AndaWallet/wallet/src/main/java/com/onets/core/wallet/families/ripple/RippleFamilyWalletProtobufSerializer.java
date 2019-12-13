/**
 * Copyright 2012 Google Inc.
 * Copyright 2014 Yu K.Q.
 * Copyright 2014 Yu K.Q.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.onets.core.wallet.families.ripple;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.onets.core.coins.CoinID;
import com.onets.core.coins.CoinType;
import com.onets.core.protos.Protos;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.store.UnreadableWalletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * @author Yu K.Q.
 */
final public class RippleFamilyWalletProtobufSerializer {
    private static final Logger log = LoggerFactory.getLogger(RippleFamilyWalletProtobufSerializer.class);

    // Used for de-serialization
    protected Map<ByteString, Transaction> txMap = new HashMap<ByteString, Transaction>();

    public static Protos.WalletPocket toProtobuf(RippleFamilyWallet account) {

        Protos.WalletPocket.Builder walletBuilder = Protos.WalletPocket.newBuilder();
        walletBuilder.setNetworkIdentifier(account.getCoinType().getId());
        if (account.getDescription() != null) {
            walletBuilder.setDescription(account.getDescription());
        }
        if (account.getId() != null) {
            walletBuilder.setId(account.getId());
        }

        walletBuilder.addAllKey(account.serializeKeychainToProtobuf());

        return walletBuilder.build();
    }


    public RippleFamilyWallet readWallet(Protos.WalletPocket walletProto, @Nullable KeyCrypter keyCrypter) throws UnreadableWalletException {
        CoinType coinType;
        try {
            coinType = CoinID.typeFromId(walletProto.getNetworkIdentifier());
        } catch (IllegalArgumentException e) {
            throw new UnreadableWalletException("Unknown network parameters ID " + walletProto.getNetworkIdentifier());
        }
        // Read the scrypt parameters that specify how encryption and decryption is performed.
        RippleFamilyKey rootKey;
        if (keyCrypter != null) {
            rootKey = RippleFamilyKey.fromProtobuf(walletProto.getKeyList(), keyCrypter);
        } else {
            rootKey = RippleFamilyKey.fromProtobuf(walletProto.getKeyList());
        }

        RippleFamilyWallet pocket;
        if (walletProto.hasId()) {
            pocket = new RippleFamilyWallet(walletProto.getId(), rootKey, coinType);
        } else {
            pocket = new RippleFamilyWallet(rootKey, coinType);
        }

        if (walletProto.hasDescription()) {
            pocket.setDescription(walletProto.getDescription());
        }

        // TODO ready transactions? Check com.openwallet.core.wallet WalletPocketProtobufSerializer
        Log.e("-------RippleFamily..", "readWallet: "+pocket.getBalance());
        return pocket;
    }
}
