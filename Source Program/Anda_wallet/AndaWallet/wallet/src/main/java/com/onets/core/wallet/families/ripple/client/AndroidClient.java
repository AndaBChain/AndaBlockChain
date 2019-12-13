package com.onets.core.wallet.families.ripple.client;

import com.onets.core.wallet.families.ripple.logging.AndroidHandler;
import com.ripple.client.Client;
import com.ripple.client.transport.impl.JavaWebSocketTransportImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 瑞波币android客户端
 */
public class AndroidClient extends Client {
    static {
        Logger logger = Client.logger;
        AndroidHandler handler = new AndroidHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    /**
     * 调用ripple包中的JavaWebSocketTransportImpl()方法
     */
    public AndroidClient() {
        super(new JavaWebSocketTransportImpl());
    }
}
