package com.qh.redis.service;

import org.redisson.api.RLock;

import com.qh.redis.RedisConstants;

public class RedissonLockUtil {
	
    private static RedissonLocker redissLock;
    
    public static void setLocker(RedissonLocker locker) {
        redissLock = locker;
    }
    
    public static RLock getLock(String lockKey){
		return redissLock.getLock(lockKey);
	}
    
    public static RLock getClearLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_clear + lockKey);
	}
    
    public static RLock getOrderLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_order + lockKey);
	}
    
    public static RLock getOrderLock(String merchNo, String orderNo){
		return redissLock.getLock(RedisConstants.lock_order + merchNo + RedisConstants.link_symbol + orderNo);
	}
    
    public static RLock getChargeLock(String merchNo, String businessNo){
		return redissLock.getLock(RedisConstants.lock_charge + merchNo + RedisConstants.link_symbol + businessNo);
	}
    
    public static RLock getMonAmountLock(String merchNo,String outChannel, String monAmount){
		return redissLock.getLock(RedisConstants.lock_monAmount_occupy + merchNo + RedisConstants.link_symbol +
				outChannel + RedisConstants.link_symbol + monAmount);
	}
    
    public static RLock getEventOrderLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_event_order + lockKey);
	}
    
    public static RLock getEventLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_event + lockKey);
	}
    
    public static RLock getEventOrderLock(String merchNo, String orderNo){
		return redissLock.getLock(RedisConstants.lock_event_order + merchNo + RedisConstants.link_symbol + orderNo);
	}
    
    
    public static RLock getOrderAcpLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_order_acp + lockKey);
	}
    public static RLock getOrderAcpLock(String merchNo, String orderNo){
		return redissLock.getLock(RedisConstants.lock_order_acp + merchNo + RedisConstants.link_symbol + orderNo);
	}
    
    public static RLock getEventOrderAcpLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_event_order_acp + lockKey);
	}
    public static RLock getEventOrderAcpLock(String merchNo, String orderNo){
		return redissLock.getLock(RedisConstants.lock_event_order_acp + merchNo + RedisConstants.link_symbol + orderNo);
	}
    
    public static RLock getBalMerchLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_bal_merch + lockKey);
	}
    
    public static RLock getBalFoundAcctLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_bal_foundAcct + lockKey);
	}
    
    public static RLock getBalFoundAcctLock(){
		return redissLock.getLock(RedisConstants.lock_bal_foundAcct);
	}
    
    public static RLock getBalPayMerchLock(String lockKey){
		return redissLock.getLock(RedisConstants.lock_bal_three_payMerch + lockKey);
	}
}