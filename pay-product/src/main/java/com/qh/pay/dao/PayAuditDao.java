package com.qh.pay.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qh.pay.domain.PayAuditDO;

/**
 * 支付审核
 * @date 2017-11-16 15:59:04
 */
@Mapper
public interface PayAuditDao {

	PayAuditDO get(@Param("orderNo") String orderNo,@Param("merchNo") String merchNo,
			@Param("auditType") Integer auditType);
	
	List<PayAuditDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(PayAuditDO payAudit);
	
	int update(PayAuditDO payAudit);
}
