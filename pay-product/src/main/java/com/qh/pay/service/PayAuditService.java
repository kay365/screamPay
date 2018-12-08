package com.qh.pay.service;

import com.qh.common.utils.R;
import com.qh.pay.domain.PayAuditDO;

import java.util.List;
import java.util.Map;

/**
 * 支付审核
 * 
 * @date 2017-11-16 15:59:04
 */
public interface PayAuditService {
	
	PayAuditDO get(String orderNo, String merchNo, Integer auditType);
	
	List<PayAuditDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	/**
	 * @Description 支付审核
	 * @param orderNo
	 * @param merchNo
	 * @param auditType
	 * @return
	 */
	int audit(String orderNo, String merchNo, Integer auditType, Integer auditResult,String company);

	/**
	 * @Description 批量审核
	 * @param orderNos
	 * @param merchNos
	 * @param auditTypes
	 * @param auditResult 
	 */
	int batchAudit(String[] orderNos, String[] merchNos, Integer[] auditTypes, Integer auditResult,String[] companys);

	/**
	 * @Description 线下转账
	 * @param orderNo
	 * @param merchNo
	 * @param auditType
	 * @return
	 */
	R offlineTransfer(String orderNo, String merchNo, Integer auditType);
}
