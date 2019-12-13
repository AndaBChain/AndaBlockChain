package org.rockyang.blockchain.P2P;

//client启动类
/**
 * description:
 *
 * @author wkGui
 */
public class ClientMain {
    public static void main(String[] args) {
        new NioSocketClient().start();
    }
}
