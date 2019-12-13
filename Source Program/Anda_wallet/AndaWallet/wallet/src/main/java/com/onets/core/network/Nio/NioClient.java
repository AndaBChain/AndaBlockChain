package com.onets.core.network.Nio;

import org.bitcoinj.net.AbstractTimeoutHandler;
import org.bitcoinj.net.MessageWriteTarget;
import org.bitcoinj.net.StreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * 使用流解析器处理数据，创建到服务器的简单连接。
 */
public class NioClient implements MessageWriteTarget {
    private static final Logger log = LoggerFactory.getLogger(NioClient.class);

    private final Handler handler;
    private final NioClientManager manager = new NioClientManager();//Nio客户端管理器

    class Handler extends AbstractTimeoutHandler implements StreamParser {
        private final StreamParser upstreamParser;
        private MessageWriteTarget writeTarget;
        private boolean closeOnOpen = false;//关闭已打开的
        private boolean closeCalled = false;//关闭调用的

        Handler(StreamParser upstreamParser, int connectTimeoutMillis) {
            this.upstreamParser = upstreamParser;
            setSocketTimeout(connectTimeoutMillis);//设置socket超时
            setTimeoutEnabled(true);//设置已超时
        }

        @Override
        protected synchronized void timeoutOccurred() {//是否发生过超时
            this.closeOnOpen = true;
            connectionClosed();
        }

        @Override
        public synchronized void connectionClosed() {//连接关闭
            NioClient.this.manager.stopAsync();
            if (!this.closeCalled) {
                closeCalled = true;
                upstreamParser.connectionClosed();
            }
        }

        @Override
        public synchronized void connectionOpened() {
            if (!this.closeOnOpen) {
                upstreamParser.connectionOpened();
            }
        }

        @Override
        public int receiveBytes(ByteBuffer buff) throws Exception {
            return upstreamParser.receiveBytes(buff);
        }

        @Override
        public synchronized void setWriteTarget(MessageWriteTarget writeTarget) {
            if (this.closeOnOpen) {
                writeTarget.closeConnection();
            } else {
                setTimeoutEnabled(false);
                this.writeTarget = writeTarget;
                upstreamParser.setWriteTarget(writeTarget);
            }
        }

        @Override
        public int getMaxMessageSize() {
            return upstreamParser.getMaxMessageSize();
        }
    }

    public NioClient(SocketAddress serverAddress, StreamParser parser, int connectTimeoutMillis) throws IOException {
        manager.startAsync();
        manager.awaitRunning();
        handler = new Handler(parser, connectTimeoutMillis);
        manager.openConnection(serverAddress, this.handler);
    }

    public void closeConnection() {
        handler.writeTarget.closeConnection();
    }

    public synchronized void writeBytes(byte[] message) throws IOException {
        handler.writeTarget.writeBytes(message);
    }
}
