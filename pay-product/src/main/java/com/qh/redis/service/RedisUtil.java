package com.qh.redis.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.qh.common.config.CfgKeyConst;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.PayConfigType;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.utils.AesUtil;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.MerchCharge;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayPropertyDO;
import com.qh.redis.RedisConstants;
import com.qh.redis.constenum.ConfigParent;
import com.qh.system.domain.ConfigDO;

/**
 * @ClassName: RedisUtil
 * @Description: redis用到的常用操作
 * @date 2017年10月27日 上午10:26:01
 */
public class RedisUtil {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    private static RedisTemplate<String, Object> redisTemplate;
    private static RedisTemplate<String, Object> redisNotifyTemplate;

    public static void setValue(String key, Object obj) {
        redisTemplate.opsForValue().set(key, obj);
    }

    public static Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

	public static Object getHashValue(String key,Object obj){
		return redisTemplate.opsForHash().get(key,obj);
	}

	public static void setHashValue(String key,Object key2,Object obj){
		 redisTemplate.opsForHash().put(key,key2,obj);
	}

	public static Set getHashValueKeys(String key){
		return redisTemplate.opsForHash().keys(key);
	}
	public static List<Object> getHashValueList(String key){

    	return redisTemplate.opsForHash().values(key);
	}

	public static int getHashValueCount(String key){
    	return redisTemplate.opsForHash().size(key).intValue();
	}

	public static List<Object> getHashValueListForStringObjBlur(String key,String patten){
    	Set<Object> keySet = redisTemplate.opsForHash().keys(key);

    	List<Object> keyList = new ArrayList<>();
    	for(Object obj:keySet){
    		String tmpKey = obj.toString();
    		if(tmpKey.matches(".*"+patten+".*")){
				keyList.add(tmpKey);
			}
		}
    	return redisTemplate.opsForHash().multiGet(key,keyList);
	}

    /**
     * @Description 设置聚富支付网管最近连接时间
     */
    public static void setQrGatewayLastSyncTime(String merchNo, String outChannel, Object obj) {
        redisTemplate.opsForHash().put(RedisConstants.cache_qr_last_login_time, merchNo + RedisConstants.link_symbol + outChannel, obj);
    }

    /**
     * @Description 获取聚富支付网管最近连接时间
     */
    public static Object getQrGatewayLastSyncTime(String merchNo, String outChannel) {
        return redisTemplate.opsForHash().get(RedisConstants.cache_qr_last_login_time, merchNo + RedisConstants.link_symbol + outChannel);
    }


    /**
     * @param order
     * @Description 支付订单
     */
    public static void setOrder(Order order) {
        redisTemplate.opsForHash().put(RedisConstants.cache_order + order.getMerchNo(), order.getOrderNo(), order);
        redisTemplate.opsForZSet().add(RedisConstants.cache_sort_order + order.getMerchNo(), order.getOrderNo(), order.getCrtDate());
    }

    /**
     * @param merchNo
     * @param orderNo
     * @return
     * @Description 获取支付订单
     */
    public static Order getOrder(String merchNo, String orderNo) {
        return (Order) redisTemplate.opsForHash().get(RedisConstants.cache_order + merchNo, orderNo);
    }


    /**
     * @Description 删除支付订单
     */
    public static void removeOrder(String merchNo, String orderNo) {
        redisTemplate.opsForHash().delete(RedisConstants.cache_order + merchNo, orderNo);
        redisTemplate.opsForZSet().remove(RedisConstants.cache_sort_order + merchNo, orderNo);
    }


	/**
	 * @Description 代付订单
	 * @param order
	 */
	public static void setOrderAcp(Order order) {
		redisTemplate.opsForHash().put(RedisConstants.cache_order_acp +  order.getMerchNo(), order.getOrderNo(), order);
		redisTemplate.opsForZSet().add(RedisConstants.cache_sort_acp_order + order.getMerchNo(), order.getOrderNo(), order.getCrtDate());
	}
	
	/**
	 * @Description 获取代付订单
	 * @param merchNo
	 * @param orderNo
	 * @return
	 */
	public static Order getOrderAcp(String merchNo, String orderNo) {
		return 	(Order) redisTemplate.opsForHash().get(RedisConstants.cache_order_acp + merchNo, orderNo);
	}
	
	/**
	 * @Description 删除代付订单
	 * @param msgKey
	 */
	public static void removeOrderAcp(String merchNo, String orderNo) {
		redisTemplate.opsForHash().delete(RedisConstants.cache_order_acp + merchNo, orderNo);
		redisTemplate.opsForZSet().remove(RedisConstants.cache_sort_acp_order + merchNo, orderNo);
	}

	/**
	 * @param orderKey 
	 * @param merchNo 
	 * @Description 
	 * @param orderNo
	 * @return
	 */
	public static boolean setKeyEventExpired(String orderKey, String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getEventLock(orderKey + RedisConstants.link_symbol + merchNo + RedisConstants.link_symbol +  orderNo);
		if (lock.tryLock()) {
			try {
				Integer minute = (Integer) redisNotifyTemplate.opsForHash().get(orderKey + merchNo, orderNo);
				logger.info("新的订单过期时间：{}，{}，{}", merchNo,orderNo,minute);
				if(minute == null){
					minute = RedisConstants.keyevent_10;
				}else if(minute == 0){
					redisNotifyTemplate.opsForHash().delete(orderKey + merchNo, orderNo);
					return false;
				}else if(minute==RedisConstants.keyevent_160 || minute==RedisConstants.keyevent_320) {
					minute = RedisConstants.keyevent_80;
				}
				Long timeLive = redisNotifyTemplate.opsForValue().getOperations().getExpire(orderKey + merchNo + RedisConstants.link_symbol +  orderNo);
				if(timeLive == null || timeLive < 30){
					redisNotifyTemplate.opsForHash().put(orderKey + merchNo, orderNo, RedisConstants.evtMinuteMap.get(minute));
					redisNotifyTemplate.opsForValue().set(orderKey + merchNo + RedisConstants.link_symbol +  orderNo, minute, minute, TimeUnit.MINUTES);
				}
				return true;
			} finally {
				lock.unlock();
			}
		}
		return false;
	}
	
	/**
	 * @param orderKey 
	 * @param merchNo 
	 * @Description 
	 * @param orderNo
	 * @return
	 */
	public static boolean setKeyEventExpiredByAutoSync(String orderKey, String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getEventLock(orderKey + RedisConstants.link_symbol + merchNo + RedisConstants.link_symbol +  orderNo);
		if (lock.tryLock()) {
			try {
				Integer minute = (Integer) redisNotifyTemplate.opsForHash().get(orderKey + merchNo, orderNo);
				logger.info("自动同步 订单过期时间：{}，{}，{}", merchNo,orderNo,minute);
				if(minute == null){
					minute = RedisConstants.keyevent_5;
				}else if(minute == 0){
					redisNotifyTemplate.opsForHash().delete(orderKey + merchNo, orderNo);
					return false;
				}
				Long timeLive = redisNotifyTemplate.opsForValue().getOperations().getExpire(orderKey + merchNo + RedisConstants.link_symbol +  orderNo);
				if(timeLive == null || timeLive < 30){
					redisNotifyTemplate.opsForHash().put(orderKey + merchNo, orderNo, RedisConstants.autoSyncMinuteMap.get(minute));
					redisNotifyTemplate.opsForValue().set(orderKey + merchNo + RedisConstants.link_symbol +  orderNo, minute, minute, TimeUnit.MINUTES);
				}
				return true;
			} finally {
				lock.unlock();
			}
		}
		return false;
	}
	
	/**
	 * @param orderKey 
	 * @param merchNo 
	 * @Description 
	 * @param orderNo
	 * @return
	 */
	public static void delKeyEventExpired(String orderKey, String merchNo, String orderNo) {
		redisNotifyTemplate.opsForHash().delete(orderKey + merchNo, orderNo);
	}
	
	/**
	 * @Description 获取商户余额
	 * @param merchNo
	 */
	public static PayAcctBal getMerchBal(String merchNo) {
		return (PayAcctBal) redisTemplate.opsForHash().get(RedisConstants.cache_bal_merch, merchNo);
	}
	
	public static void setMerchBal(PayAcctBal payAcctBal){
		 redisTemplate.opsForHash().put(RedisConstants.cache_bal_merch, payAcctBal.getUsername(), payAcctBal);
	}
	
	/**
     * @Description 获取支付通道余额
     * @param merchNo
     */
    public static PayAcctBal getPayMerchBal(String payCompany,String payMerch,String outChannel) {
    	PayAcctBal payAcctBal = (PayAcctBal) redisTemplate.opsForHash().get(RedisConstants.cache_bal_payMerch, 
                payCompany + RedisConstants.link_symbol + payMerch);
        if(payAcctBal == null) {
        	payAcctBal = createPayAcctBal(payMerch);
        }
        return payAcctBal;
    }
    
    /**
     * @Description 获取支付通道余额
     * @param merchNo
     */
    public static PayAcctBal getPayMerchBal(String payCompany,String payMerch) {
        PayAcctBal payAcctBal = (PayAcctBal) redisTemplate.opsForHash().get(RedisConstants.cache_bal_payMerch, 
                payCompany + RedisConstants.link_symbol + payMerch);
        if(payAcctBal == null) {
        	payAcctBal = createPayAcctBal(payMerch);
        }
        return payAcctBal;
    }
    
    public static void setPayMerchBal(PayAcctBal payAcctBal,String payCompany,String payMerch){
         redisTemplate.opsForHash().put(RedisConstants.cache_bal_payMerch, payCompany + RedisConstants.link_symbol + payMerch, payAcctBal);
    }
    /**
	 * 
	 * @Description 创建支付账户余额
	 * @param merchant
	 * @return
	 */
	public static PayAcctBal createPayAcctBal(String payMerch){
		PayAcctBal payAcctBal = new PayAcctBal();
		payAcctBal.setUserId(0);
		payAcctBal.setUsername(payMerch);
		payAcctBal.setUserType(UserType.payMerch.id());
		payAcctBal.setBalance(BigDecimal.ZERO);
		payAcctBal.setAvailBal(BigDecimal.ZERO);
		return payAcctBal;
	}
	
	/**
	 * @Description 获取代理余额
	 * @param merchNo
	 */
	public static PayAcctBal getAgentBal(String username) {
		return (PayAcctBal) redisTemplate.opsForHash().get(RedisConstants.cache_bal_agent, username);
	}
	
	public static void setAgentBal(PayAcctBal payAcctBal){
		 redisTemplate.opsForHash().put(RedisConstants.cache_bal_agent, payAcctBal.getUsername(), payAcctBal);
	}
	
	/**
	 * 
	 * @Description 获取银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getBanks(Integer cardType, String payCompany){
		return (List<String>) redisTemplate.opsForHash().get(RedisConstants.cache_banks + cardType, payCompany);
	}
	
	/**
	 * 
	 * @Description 设置银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @param banks
	 */
	public static void setBanks(Integer cardType, String payCompany,List<String> banks){
		redisTemplate.opsForHash().put(RedisConstants.cache_banks + cardType, payCompany,banks);
	}
	
	/**
	 * 
	 * @Description 获取银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getBanks(Integer cardType, String payCompany,String payMerch){
		return (List<String>) redisTemplate.opsForHash().get(RedisConstants.cache_banks + cardType, payCompany + RedisConstants.link_symbol + payMerch);
	}
	
	/**
	 * 
	 * @Description 设置银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @param banks
	 */
	public static void setBanks(Integer cardType, String payCompany,String payMerch,List<String> banks){
		redisTemplate.opsForHash().put(RedisConstants.cache_banks + cardType, payCompany + RedisConstants.link_symbol + payMerch,banks);
	}
	/**
	 * 
	 * @Description 支付金额
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 */
	public static void setMonAmountOccupy(String merchNo,String outChannel,String monAmount){
		setMonAmountOccupy(merchNo, outChannel, monAmount, 6 * 60);
	}
	
	/**
	 * 
	 * @Description 支付金额
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @param validTime 有效时间 单位秒
	 */
	public static void setMonAmountOccupy(String merchNo,String outChannel,String monAmount,int validTime){
		redisTemplate.opsForHash().put(RedisConstants.cache_monAmount_occupy + merchNo + RedisConstants.link_symbol + outChannel,
				monAmount, DateUtil.getCurrentTimeInt() + validTime);
	}
	
	/***
	 * 
	 * @Description 获取过期时间(剩余时间)
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @return
	 */
	public static int getMonAmountOccupyValidTime(String merchNo,String outChannel,String monAmount){
		Integer validTime = (Integer) redisTemplate.opsForHash().get(RedisConstants.cache_monAmount_occupy + merchNo + RedisConstants.link_symbol + outChannel,
				monAmount);
		return validTime == null? 0 : validTime - DateUtil.getCurrentTimeInt();
	}
	
	/***
	 * 
	 * @Description 判断支付金额是否占用
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @return
	 */
	public static boolean ifMonAmountOccupy(String merchNo,String outChannel,String monAmount){
		Integer validTime = (Integer) redisTemplate.opsForHash().get(RedisConstants.cache_monAmount_occupy + merchNo + RedisConstants.link_symbol + outChannel,
				monAmount);
		return validTime != null && (validTime - DateUtil.getCurrentTimeInt()) > 0;
	}
	
	/**
	 * 
	 * @Description 支付金额
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @param validTime 有效时间 单位秒
	 */
	public static void delMonAmountOccupy(String merchNo,String outChannel,String monAmount){
		redisTemplate.opsForHash().delete(RedisConstants.cache_monAmount_occupy + merchNo + RedisConstants.link_symbol + outChannel,monAmount);
	}
	
	/**
	 * @Description 设置支付金额订单号
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @param orderNo
	 */
	public static void setMonAmountOrderNo(String merchNo, String outChannel,String monAmount, String orderNo) {
		redisTemplate.opsForHash().put(RedisConstants.cache_monAmount_orderNo + merchNo + RedisConstants.link_symbol + outChannel,
				monAmount, orderNo);
		
	}
	
	/**
	 * @Description 获取支付金额订单号
	 * @param merchNo
	 * @param monAmount
	 * @param outChannel
	 * @return 
	 */
	public static String getMonAmountOrderNo(String merchNo, String outChannel,String monAmount) {
		return (String) redisTemplate.opsForHash().get(RedisConstants.cache_monAmount_orderNo + merchNo + RedisConstants.link_symbol + outChannel,monAmount);		
	}
	
	/**
	 * @Description 删除支付金额订单号
	 * @param merchNo
	 * @param outChannel
	 * @param orderNo
	 */
	public static void delMonAmountOrderNo(String merchNo, String outChannel,String monAmount) {
		redisTemplate.opsForHash().delete(RedisConstants.cache_monAmount_orderNo + merchNo + RedisConstants.link_symbol + outChannel,monAmount);
		
	}
	
	/**
	 * @Description 支付金额业务单号
	 * @param merchNo
	 * @param outChannel
	 * @param businessNo
	 */
	public static void setQrBusinessNo(String merchNo, String outChannel,String businessNo) {
		redisTemplate.opsForHash().put(RedisConstants.cache_qr_businessNo + merchNo + RedisConstants.link_symbol + outChannel,
				businessNo, DateUtil.getCurrentTimeInt());
	}
	
	/**
	 * @Description 支付金额业务单号是否存在
	 * @param merchNo
	 * @param outChannel
	 * @param businessNo
	 */
	public static boolean ifQrBusinessNo(String merchNo, String outChannel, String businessNo) {
		return redisTemplate.opsForHash().get(RedisConstants.cache_qr_businessNo + merchNo + RedisConstants.link_symbol + outChannel,
				businessNo) == null;
	}
	
	/**
	 * @Description 删除支付金额业务单号
	 * @param merchNo
	 * @param outChannel
	 * @param businessNo
	 */
	public static void delQrBusinessNo(String merchNo, String outChannel,String businessNo) {
		redisTemplate.opsForHash().delete(RedisConstants.cache_qr_businessNo + merchNo + RedisConstants.link_symbol + outChannel,businessNo);
	}
	
	public static void setMerchCharge(MerchCharge merchCharge){
		redisTemplate.opsForHash().put(RedisConstants.cache_charge +  merchCharge.getMerchNo(), merchCharge.getBusinessNo(), merchCharge);
	}
	
	public static MerchCharge getMerchCharge(String merchNo,String businessNo){
		return (MerchCharge) redisTemplate.opsForHash().get(RedisConstants.cache_charge +  merchNo, businessNo);
	}
	
	public static void delMerchCharge(MerchCharge merchCharge){
		redisTemplate.opsForHash().delete(RedisConstants.cache_charge +  merchCharge.getMerchNo(), merchCharge.getBusinessNo());
	}
	
	/**
	 * 
	 * @Description 获取支付参数配置的值
	 * @param key
	 * @return
	 */
	public static String getPayValue(String key){
		String value = getPayCommonValue(key);
		if(ParamUtil.isEmpty(value)){
			value = getPayFilePathValue(key);
		}
		if(ParamUtil.isEmpty(value)){
			value = getPayIpValue(key);
		}
		return value;
	}
	/**
	 * 
	 * @Description 获取改支付公司下的商户号
	 * @param payCompany
	 * @return
	 */
	public static Set<Object> getMechNoByCompany(String payCompany){
    	return redisTemplate.boundSetOps(RedisConstants.cache_payConfig + PayConfigType.merchantNo.id() + payCompany).members();
	}
	
	/***
	 * 
	 * @Description 获取资金账户信息
	 * @param template
	 */
	public static PayAcctBal getPayFoundBal(){
		return (PayAcctBal) redisTemplate.opsForValue().get(RedisConstants.cache_bal_foundAcct);
	}


    public static void setPayFoundBal(PayAcctBal payAcctBal) {
        redisTemplate.opsForValue().set(RedisConstants.cache_bal_foundAcct, payAcctBal);
    }


    public static void syncConfig(ConfigDO config, boolean delateFlag) {
        if (config == null) {
            return;
        }
        if (ParamUtil.isNotEmpty(config.getParentItem())) {
            if (delateFlag) {
                redisTemplate.boundHashOps(RedisConstants.cache_config_parent + config.getParentItem()).delete(config.getConfigItem());
            } else {
                redisTemplate.boundHashOps(RedisConstants.cache_config_parent + config.getParentItem()).put(config.getConfigItem(), config.getConfigValue());
            }
        }
    }

    public static String getConfigValue(String configItem, String parentItem) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_config_parent + parentItem).get(configItem);
    }

    public static String getSysConfigValue(String configItem) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_config_parent + ConfigParent.sysConfig.name()).get(configItem);
    }

	public static String getEmailConfigValue(String configItem) {
		return (String) redisTemplate.boundHashOps(RedisConstants.cache_config_parent + ConfigParent.mailConfig.name()).get(configItem);
	}

    public static void delConfig(String configItem, String parentItem) {
        if (ParamUtil.isNotEmpty(parentItem)) {
            redisTemplate.boundHashOps(RedisConstants.cache_config_parent + parentItem).delete(configItem);
        }
    }

    public static String getSMSConfigValue(String configItem) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_config_parent + ConfigParent.smsConfig.name()).get(configItem);
    }


    /**
     * @param payProperty
     * @Description 同步支付配置信息
     */
    public static void syncPayConfig(PayPropertyDO payProperty) {
        if (payProperty != null) {
            Integer configType = payProperty.getConfigType();
            if (configType == null) {
                return;
            }
            String key = payProperty.getConfigKey();
            if (ParamUtil.isNotEmpty(payProperty.getMerchantno())) {
                key = payProperty.getMerchantno() + key;
            }
            String value = payProperty.getValue();
            if (PayConfigType.pass.id() == payProperty.getConfigType()) {
                value = AesUtil.decrypt(value);
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig).put(key, value);
            } else if (PayConfigType.ip.id() == payProperty.getConfigType()) {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig).delete(key);
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig + PayConfigType.ip.id()).put(key, value);
            } else if (PayConfigType.filePath.id() == payProperty.getConfigType()) {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig).delete(key);
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig + PayConfigType.filePath.id()).put(key, value);
            } else if (PayConfigType.fileValue.id() == payProperty.getConfigType()) {
                String payFilePath = getSysConfigValue(CfgKeyConst.payFilePath) + value;
                try {
                    redisTemplate.boundHashOps(RedisConstants.cache_payConfig).put(key,
                            ParamUtil.readTxtFileFilter(payFilePath));
                } catch (Exception e) {
                    logger.error("支付配置文件加载失败！{}", payFilePath);
//                    throw new RuntimeException("支付配置文件加载失败" + payFilePath);
                }
            } else if (PayConfigType.merchantNo.id() == payProperty.getConfigType()) {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig).put(key, value);
                redisTemplate.boundSetOps(RedisConstants.cache_payConfig + PayConfigType.merchantNo.id() + payProperty.getPayCompany()).add(value);
            } else {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig).put(key, value);
            }
        }

    }

    public static String getPayFilePathValue(String key) {
        String value = getPayValue(key, PayConfigType.filePath);
        if (ParamUtil.isNotEmpty(value)) {
            value = getSysConfigValue(CfgKeyConst.payFilePath) + value;
        }
        return value;
    }

    public static String getPayIpValue(String key) {
        String value = getPayValue(key, PayConfigType.ip);
        if (ParamUtil.isNotEmpty(value)) {
            value = getSysConfigValue(CfgKeyConst.ip) + value;
        }
        return value;
    }

    public static String getPayValue(String key, PayConfigType configType) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_payConfig + configType.id()).get(key);
    }

    public static String getPayCommonValue(String key) {
        return (String) redisTemplate.boundHashOps(RedisConstants.cache_payConfig).get(key);
    }

    /**
     * @param payPropertyDO
     * @Description 删除支付配置
     */
    public static void delPayConfig(PayPropertyDO payPropertyDO) {
        if (payPropertyDO != null) {
            delPayConfig(payPropertyDO.getConfigKey(), payPropertyDO);
        }

    }

    /**
     * @param configKey
     * @param payProperty
     * @Description 删除支付配置
     */
    public static void delPayConfig(String configKey, PayPropertyDO payProperty) {
        if (payProperty == null) {
            return;
        }
        if (ParamUtil.isNotEmpty(payProperty.getMerchantno())) {
            configKey = payProperty.getMerchantno() + configKey;
        }
        Integer configType = payProperty.getConfigType();
        if (configType != null) {
            if (PayConfigType.ip.id() == configType) {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig + PayConfigType.ip.id()).delete(configKey);
            } else if (PayConfigType.filePath.id() == configType) {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig + PayConfigType.filePath.id()).delete(configKey);
            } else if (PayConfigType.merchantNo.id() == configType) {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig).delete(configKey);
                redisTemplate.boundSetOps(RedisConstants.cache_payConfig + PayConfigType.merchantNo.id() + payProperty.getPayCompany()).remove(payProperty.getValue());
            } else {
                redisTemplate.boundHashOps(RedisConstants.cache_payConfig).delete(configKey);
            }
        } else {
            redisTemplate.boundHashOps(RedisConstants.cache_payConfig).delete(configKey);
        }
    }

    public static Map<Object, Object> getCacheMap(String key) {
        return redisTemplate.boundHashOps(key).entries();
    }

    public static ConfigDO getCacheConfig(String key) {
        return (ConfigDO) redisTemplate.boundValueOps(RedisConstants.cache_config).
                getOperations().boundValueOps(key).get();
    }

    public static Map<String, Object> getCacheMapDesc(String key) {
        Map<Object, Object> cacheMap = getCacheMap(key);
        Map<String, Object> descMap = new HashMap<String, Object>();
        if (!cacheMap.isEmpty()) {
            ConfigDO configDO = null;
            for (Entry<Object, Object> entry : cacheMap.entrySet()) {
                configDO = getCacheConfig((String) entry.getKey());
                if (configDO != null) {
                    descMap.put((String) entry.getValue(), configDO.getConfigName());
                }
            }
            return descMap;
        } else {
            return descMap;
        }
    }

    public static void setRedisTemplate(RedisTemplate<String, Object> template) {
        redisTemplate = template;
    }

    public static RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    public static RedisTemplate<String, Object> getRedisNotifyTemplate() {
        return redisNotifyTemplate;
    }

    public static void setRedisNotifyTemplate(RedisTemplate<String, Object> template) {
        redisNotifyTemplate = template;
    }


}
