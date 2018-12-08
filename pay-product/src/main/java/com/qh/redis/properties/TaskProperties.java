package com.qh.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @ClassName TaskProperties
 * @Description 任务列表配置
 * @Date 2017年11月22日 上午10:19:38
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "spring.task.pool")
public class TaskProperties {
	private int corePoolSize;
	private int maxPoolSize;
	private int keepAliveSeconds;
	private int queueCapacity;
	private String threadNamePrefix;
	public int getCorePoolSize() {
		return corePoolSize;
	}
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	public int getMaxPoolSize() {
		return maxPoolSize;
	}
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}
	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}
	public int getQueueCapacity() {
		return queueCapacity;
	}
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}
	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}
	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}
	
}
