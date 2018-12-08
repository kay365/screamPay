package com.qh.pay.api.utils;

import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.ConfigDO;

/**
 * @ClassName QhPayUtil
 * @Description 支付工具类
 * @Date 2017年10月31日 上午11:44:01
 * @version 1.0.0
 */
public class QhPayUtil {
	/**
	 * qh密钥
	 */
	private static final String qhpayKey = "NsXpt407QKN2zq/5x4gK/Q==";

	/**
	 * 商户号前缀
	 */
	private static String merchNoPrefix = ""; 
	/**
	 * 代理商户号前缀
	 */
	private static String agentNoPrefix = ""; 
	
	/***
	 * qh 公钥
	 */
	private static String qhPublicKey = "";
	
	/**
	 * qh私钥
	 */
	private static String qhPrivateKey = "";
	
	/**
	 * 
	 * @Description 获取商户号前缀
	 * @return
	 */
	public static String getMerchNoPrefix(){
		if("".equals(QhPayUtil.merchNoPrefix)) {
			ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, "merchNoPrefix");
			if(config!=null) {
				QhPayUtil.setMerchNoPrefix(config.getConfigValue());
			}
		}
		return QhPayUtil.merchNoPrefix;
	}

	public static void setMerchNoPrefix(String merchNoPrefix){
		QhPayUtil.merchNoPrefix = merchNoPrefix;
	}
	
	/**
	 * 
	 * @Description 获取代理前缀
	 * @return
	 */
	public static String getAgentNoPrefix(){
		if("".equals(QhPayUtil.agentNoPrefix)) {
			ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, "agentNoPrefix");
			if(config!=null) {
				QhPayUtil.setAgentNoPrefix(config.getConfigValue());
			}
		}
		return QhPayUtil.agentNoPrefix;
	}

	public static void setAgentNoPrefix(String agentNoPrefix){
		QhPayUtil.agentNoPrefix = agentNoPrefix;
	}
	
	/**
	 * 
	 * @Description 加密
	 * @param content
	 * @return
	 */
	public static String encrypt(String content){
		return AesUtil.encrypt(content, qhpayKey);
	}
	
	/**
	 * 
	 * @Description 解密
	 * @param result
	 * @return
	 */
	public static String decrypt(String result){
		return AesUtil.decrypt(result, qhpayKey);
	};
	
	/**
	 * 
	 * @Description 获取聚富公钥
	 * @return
	 */
	public static String getQhPublicKey(){
		if("".equals(QhPayUtil.qhPublicKey)) {
			ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, "publicKeyPath");
			if(config!=null) {
				QhPayUtil.setQhPublicKey(config.getConfigValue());
			}
		}
		return QhPayUtil.qhPublicKey;
	}

	public static void setQhPublicKey(String publicKey){
		QhPayUtil.qhPublicKey = publicKey;
	}
	/**
	 * 
	 * @Description 获取聚富私钥
	 * @return
	 */
	public static String getQhPrivateKey(){
		
		if("".equals(QhPayUtil.qhPrivateKey)) {
			ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, "privateKeyPath");
			if(config!=null) {
				QhPayUtil.setQhPrivateKey(config.getConfigValue());
			}
		}
		return QhPayUtil.qhPrivateKey;
	}

	public static void setQhPrivateKey(String privateKey){
		QhPayUtil.qhPrivateKey = privateKey;
	}

}
