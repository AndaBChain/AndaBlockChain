package org.rockyang.blockchain.net.base;

import java.io.Serializable;

/**
 * @author Wang HaiTian
 */
public class Node extends org.tio.core.Node implements Serializable {

	public Node(String ip, int port) {
		super(ip, port);
	}

	public Node() {
		super(null, 0);
	}
}
