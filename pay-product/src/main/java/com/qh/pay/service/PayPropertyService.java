package com.qh.pay.service;

import com.qh.pay.domain.PayPropertyDO;

import java.util.List;
import java.util.Map;

/**
 * 支付参数配置
 * 
 * @date 2017-10-27 17:52:44
 */
public interface PayPropertyService {
	
	PayPropertyDO get(Integer id);
	
	PayPropertyDO get(String configKey);
	
	List<PayPropertyDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(PayPropertyDO payProperty);
	
	int update(PayPropertyDO payProperty);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
	
	/**
	 * @Description 是否存在
	 * @param params
	 * @return
	 */
	boolean exist(String merchantno,String configKey);
}
