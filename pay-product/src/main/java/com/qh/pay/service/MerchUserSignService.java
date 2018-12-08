package com.qh.pay.service;

import com.qh.pay.domain.MerchUserSignDO;

import java.util.List;
import java.util.Map;

/**
 * 商户号下的用户签约信息
 * 
 * @date 2017-11-02 11:21:44
 */
public interface MerchUserSignService {
	
	List<MerchUserSignDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(MerchUserSignDO merchUserSign);
	
	int update(MerchUserSignDO merchUserSign);
	
	/**
	 * @Description 查询用户签约信息
	 * @param merchUserSign
	 * @return
	 */
	MerchUserSignDO get(MerchUserSignDO merchUserSign);

	/**
	 * @Description 查询用户签约信息
	 * @param merchUserSign
	 * @return
	 */
	List<MerchUserSignDO> getMerchUserSigns(MerchUserSignDO merchUserSign);
}
