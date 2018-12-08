package com.qh.pay.service;

import com.qh.common.utils.R;
import com.qh.pay.domain.PayQrConfigDO;

import java.util.List;
import java.util.Map;

/**
 * 聚富扫码通道配置
 * 
 * @date 2017-12-14 14:37:38
 */
public interface PayQrConfigService {
	
	PayQrConfigDO get(Integer id);
	
	PayQrConfigDO get(String outChannel,String merchNo);
	
	List<PayQrConfigDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(PayQrConfigDO payQrConfig);
	
	int update(PayQrConfigDO payQrConfig);
	
	int remove(String outChannel,String merchNo);

	/**
	 * @Description 上传二维码收款图片
	 * @param outChannel
	 * @param merchNo
	 * @param moneyAmount
	 * @return
	 */
	R updateQrs(String outChannel, String merchNo, String moneyAmount);

	/**
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param outChannel
	 * @param merchNo
	 * @param moneyAmount
	 * @return
	 */
	R removeQrs(String outChannel, String merchNo, String moneyAmount);

	R removeQrs(String outChannel, String merchNo, List<String> moneyAmounts);
}
