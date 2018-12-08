package com.qh.redis.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @ClassName: RedisServcie
 * @Description: redisService
 * @date 2017年10月25日 下午9:06:54
 *
 */
@Component
public class RedisService {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisService.class);
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	/***
	 * 加锁
	 * 
	 * @param key
	 * @param value
	 *            当前时间+超时时间
	 * @return 锁住返回true
	 */
	public boolean lock(String key, String value) {
		if (stringRedisTemplate.opsForValue().setIfAbsent(key, value)) {// setNX
			return true;
		}
		// 如果锁超时 ***
		String currentValue = stringRedisTemplate.opsForValue().get(key);
		if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()) {
			// 获取上一个锁的时间
			String oldvalue = stringRedisTemplate.opsForValue().getAndSet(key, value);
			if (!StringUtils.isEmpty(oldvalue) && oldvalue.equals(currentValue)) {
				return true;
			}
		}
		return false;
	}

	
	/***
	 * 解锁
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public void unlock(String key, String value) {
		try {
			String currentValue = stringRedisTemplate.opsForValue().get(key);
			if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
				stringRedisTemplate.opsForValue().getOperations().delete(key);
			}
		} catch (Exception e) {
			logger.error("key + 解锁异常");
		}
	}
}
