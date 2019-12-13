/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rockyang.blockchain.testing;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.rockyang.blockchain.P2P.TransactionConfidence;
import org.bitcoinj.wallet.Wallet;

/**
 * <p>Implementors are called when the contents of the wallet changes, for instance due to receiving/sending money
 * or a block chain re-organize.</p>
 * <p>实现者在钱包内容发生变化时被调用，例如由于接收/发送金钱
 * *或区块链重组。</p>
 */
public interface WalletCoinsReceivedEventListener {
    /**
     * This is called when a transaction is seen that sends coins <b>to</b> this wallet, either because it
     * was broadcast across the network or because a block was received. If a transaction is seen when it was broadcast,
     * onCoinsReceived won't be called again when a block containing it is received. If you want to know when such a
     * transaction receives its first confirmation, register a {@link TransactionConfidence} event listener using
     * the object retrieved via {@link Transaction#getConfidence()}. It's safe to modify the
     * wallet in this callback, for example, by spending the transaction just received
     * 当看到一个交易将硬币<b>发送到这个钱包</b>时，就会调用这个函数，原因可能是它
     * 通过网络广播，或者因为接收到一个块。如果事务在广播时被看到，
     * 当接收到包含癌素的块时，将不会再次调用癌素。如果你想知道什么时候这样
     * transaction收到它的第一个确认，注册一个{@link TransactionConfidence}事件监听器
     * 通过{@link Transaction#getConfidence()}检索的对象。修改它是安全的
     * 钱包在这个回调中，例如，通过花费刚刚收到的事务。.
     *
     * @param wallet      The wallet object that received the coins接收硬币的钱包对象
     * @param tx          The transaction which sent us the coins.给我们寄来硬币的那笔交易。
     * @param prevBalance Balance before the coins were received.收到硬币前的余额。
     * @param newBalance  Current balance of the wallet. This is the 'estimated' balance.钱包的当前余额。这是“估计”余额。
     */
    void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance);
}
