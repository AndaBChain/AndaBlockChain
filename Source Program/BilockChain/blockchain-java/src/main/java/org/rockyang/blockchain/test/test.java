package org.rockyang.blockchain.test;


import org.apache.commons.lang3.SerializationUtils;
import org.bitcoinj.core.*;
import org.rockyang.blockchain.core.BitcoinSerializer;
import org.bitcoinj.params.UnitTestParams;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.*;
import org.rockyang.blockchain.testing.FakeTxBuilder;
import org.rockyang.blockchain.utils.SerializeUtils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.bitcoinj.params.MainNetParams;

public class test {
    protected static final NetworkParameters UNITTEST = UnitTestParams.get();
    /*主要网络参数*/
    protected static final NetworkParameters params = MainNetParams.get();
    public static void main(String[] args) throws IOException {
        Transaction tx = FakeTxBuilder.createFakeTx(UNITTEST);
        /*作为一个中间储存类，在序列化中将其序列化后的二进制码写入其中*/
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        /*将tx赋予message，作为序列化的参数需要这种格式*/
        Message message = tx;
        /*为BitcoinSerializer赋予params参数，序列化tx需要这种参数*/
        BitcoinSerializer deserialize = new BitcoinSerializer(params,true);
        org.bitcoinj.core.BitcoinSerializer deserialize2;
        deserialize2 = new org.bitcoinj.core.BitcoinSerializer(params,true);
        /*将其tx里的内容序列化后赋予out*/
            deserialize.serialize(message,out);
           ByteBuffer buffer = ByteBuffer.allocate(4096);
           /*通过调用out，即可取出储存其中的tx序列化后的二进制码*/
           buffer.put(out.toByteArray());

           /*通过反序列化程序重建tx*/
           Transaction transaction = (Transaction) deserialize2.deserialize(ByteBuffer.wrap(out.toByteArray()));
           String tx1 =transaction.toString();
           System.out.println(tx1);
    }
}
