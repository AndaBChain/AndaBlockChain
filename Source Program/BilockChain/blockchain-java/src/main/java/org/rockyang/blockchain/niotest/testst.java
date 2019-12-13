package org.rockyang.blockchain.niotest;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;

import java.io.IOException;
import java.nio.ByteBuffer;

public class testst {
    ByteBuffer tx ;
    public ByteBuffer gettx() {
        return tx;
     }
    public void settx(ByteBuffer buffer)  {
       this.tx = buffer;
    }
}


