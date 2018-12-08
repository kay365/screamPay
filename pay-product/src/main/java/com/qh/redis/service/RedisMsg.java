package com.qh.redis.service;

import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.qh.redis.RedisConstants;

/**
 * @ClassName RedisMsg
 * @Description redis消息传送
 * @Date 2017年11月17日 下午3:20:16
 * @version 1.0.0
 */
public class RedisMsg {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisMsg.class);
	
	private static RedisTemplate<String, Object> redisTemplate;
	
	
	/**
	 * @Description 订单 回调通知
	 * @param order
	 */
	public static void orderNotifyMsg(String merchNo, String orderNo) {
		logger.info("redisMsg：订单 回调通知 - " + merchNo + RedisConstants.link_symbol + orderNo);
		redisTemplate.convertAndSend(RedisConstants.channel_order_notify, merchNo + RedisConstants.link_symbol + orderNo);
	}
	
	/**
	 * @Description 订单 通知保存
	 */
	public static void orderDataMsg(String merchNo, String orderNo) {
		logger.info("redisMsg：订单 通知保存 - " + merchNo + RedisConstants.link_symbol + orderNo);
		redisTemplate.convertAndSend(RedisConstants.channel_order_data, merchNo + RedisConstants.link_symbol + orderNo);
	}
	
	/**
	 * @Description 商户充值保存 通知保存
	 */
	public static void chargeDataMsg(String merchNo, String businessNo) {
		logger.info("redisMsg：商户充值 通知保存 - " + merchNo + RedisConstants.link_symbol + businessNo);
		redisTemplate.convertAndSend(RedisConstants.channel_charge_data, merchNo + RedisConstants.link_symbol + businessNo);
	}
	
	/**
	 * @Description 代付订单 审核未通过
	 * @param string
	 */
	public static void orderAcpNopassMsg(String merchNo, String orderNo) {
		logger.info("redisMsg：代付订单 审核未通过 - " + merchNo + RedisConstants.link_symbol + orderNo);
		redisTemplate.convertAndSend(RedisConstants.channel_order_acp_nopass, merchNo + RedisConstants.link_symbol + orderNo);
	}
	
	/**
	 * @Description 代付订单下单通知
	 * @param order
	 */
	public static void orderAcpMsg(String merchNo, String orderNo) {
		logger.info("redisMsg：代付订单下单通知 - " + merchNo + RedisConstants.link_symbol + orderNo);
		redisTemplate.convertAndSend(RedisConstants.channel_order_acp, merchNo + RedisConstants.link_symbol + orderNo);
	}
	
	/**
	 * @Description 代付订单回调通知
	 * @param order
	 */
	public static void orderAcpNotifyMsg(String merchNo, String orderNo) {
		logger.info("redisMsg：代付订单回调通知 - " + merchNo + RedisConstants.link_symbol + orderNo);
		redisTemplate.convertAndSend(RedisConstants.channel_order_acp_notify, merchNo + RedisConstants.link_symbol + orderNo);
	}
	
	/**
	 * @Description 代付订单通知保存
	 */
	public static void orderAcpDataMsg(String merchNo, String orderNo) {
		logger.info("redisMsg：代付订单通知保存 - " + merchNo + RedisConstants.link_symbol + orderNo);
		redisTemplate.convertAndSend(RedisConstants.channel_order_acp_data, merchNo + RedisConstants.link_symbol + orderNo);
	}
	
	
	
	public static RedisTemplate<String, Object> getRedisTemplate(){
		return redisTemplate;
	}
	public static void setRedisTemplate(RedisTemplate<String, Object> template) {
		redisTemplate = template;
	}
}
