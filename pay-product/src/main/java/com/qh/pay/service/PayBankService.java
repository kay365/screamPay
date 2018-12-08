package com.qh.pay.service;

import com.qh.pay.domain.PayBankDO;

import java.util.List;
import java.util.Map;

/**
 * 支付银行
 * 
 * @date 2017-12-27 11:45:47
 */
public interface PayBankService {
	
	PayBankDO get(String company,String payMerch,Integer cardType);
	
	List<String> getBanks(String company,String payMerch,Integer cardType);
	
	List<String> getBanks(String company,Integer cardType);
	
	List<PayBankDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(PayBankDO payBank);
	
	int update(PayBankDO payBank);
	
	int remove(String company,String payMerch,Integer cardType);

	/**
	 * @Description 设置银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @param banks
	 */
	void setBanks(int cardType, String payCompany, List<String> banks);
	
	
	/**
	 * @Description 设置银行卡列表
	 * @param cardType
	 * @param payCompany
	 * @param banks
	 */
	void setBanks(int cardType, String payCompany, String payMerch, List<String> banks);
}
