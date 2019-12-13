/*
package org.rockyang.blockchain.nio;
import org.rockyang.blockchain.nio.Peer;

import java.io.*;

public class Peerz {
    public void serialize(String PeerAddress,String PeerIp,String Time) throws Exception {
        serializeFlyPig();
        Peer flyPig = deserializeFlyPig();
        System.out.println(flyPig.toString());
    }

    */
/**
     * 序列化
     *//*

    private static void serializeFlyPig(String PeerAddress,String PeerIp,String Time) throws IOException {
        Peer peer = new Peer();
        peer.setPeerAddress(PeerAddress);
        peer.setPeerIp(PeerIp);
        peer.setTime(Time);
        // ObjectOutputStream 对象输出流，将 flyPig 对象存储到E盘的 flyPig.txt 文件中，完成对 flyPig 对象的序列化操作
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("d:/flyPig.txt")));
        oos.writeObject(peer);
        System.out.println("FlyPig 对象序列化成功！");

        oos.close();
    }

    */
/**
     * 反序列化
     *//*

    private static Peer deserializeFlyPig() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("d:/flyPig.txt")));
        Peer person = (Peer) ois.readObject();
        System.out.println("FlyPig 对象反序列化成功！");
        return person;
    }
}*/
