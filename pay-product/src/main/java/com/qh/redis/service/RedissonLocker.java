package com.qh.redis.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * 
 * @ClassName: RedissonService
 * @Description: redission
 * @date 2017年10月26日 上午10:40:18
 *
 */
public class RedissonLocker {

	private RedissonClient redissonClient;

	
	public RedissonClient getRedissonClient() {
		return redissonClient;
	}


	public void setRedissonClient(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	public RLock getLock(String lockKey){
		return redissonClient.getLock(lockKey);
	}
}