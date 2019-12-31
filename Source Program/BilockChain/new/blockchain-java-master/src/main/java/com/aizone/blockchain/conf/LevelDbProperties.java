package com.aizone.blockchain.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LevelDB 配置参数
 * @author wss
 * 
 */
@Configuration
@ConfigurationProperties(prefix = "leveldb")
public class LevelDbProperties {

	private String dataDir;

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}
}
