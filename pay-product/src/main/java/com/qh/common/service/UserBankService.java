package com.qh.common.service;


import com.qh.common.domain.UserBankDO;
import com.qh.pay.api.Order;

import java.util.List;
import java.util.Map;

/**
 * 用户银行卡
 * 
 * @date 2018-01-10 14:39:21
 */
public interface UserBankService {
	
	UserBankDO get(String username,String bankNo);
	
	List<UserBankDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(UserBankDO userBank);
	
	int update(UserBankDO userBank);

	/**
	 * @Description 保存银行银联信息
	 * @param order
	 */
	int save(String phone,String username,Order order);
	
}
