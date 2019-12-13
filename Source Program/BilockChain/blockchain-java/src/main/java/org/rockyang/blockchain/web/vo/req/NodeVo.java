package org.rockyang.blockchain.web.vo.req;

/**
 * Node vo params for request
 * @author Wang HaiTian
 */
public class NodeVo {

	// ip address
	private String ip;
	// connector port
	private int port;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
