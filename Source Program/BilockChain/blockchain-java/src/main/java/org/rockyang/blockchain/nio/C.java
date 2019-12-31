/*
package org.rockyang.blockchain.nio;

import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.UnitTestParams;
import org.rockyang.blockchain.core.BitcoinSerializer;
import org.rockyang.blockchain.db.MySql.MySQL;
import org.rockyang.blockchain.nio.Client.NIOClientSocketC;
import org.rockyang.blockchain.nio.Client.NioClientC;
import org.rockyang.blockchain.testing.FakeTxBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class C {
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    protected static final NetworkParameters params = UnitTestParams.get();
    private static final String host = "192.168.0.11";
    public static void main(String[] args) throws IOException {
        NIOClientSocketC aa = new NIOClientSocketC();
        MySQL a = new MySQL();
        List<Object> list =a.examinePeerIp();
        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
        BitcoinSerializer deserialize = new BitcoinSerializer(params,true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Message message = tx;
        deserialize.serialize(message,out);
        String s =new String(out.toByteArray());
        //aa.validation(host,s);

        */
/*for (int i = 0;i<=list.size();i++) {
            aa.validation((String) list.get(i),s);
        }*//*

    }
}
*/
