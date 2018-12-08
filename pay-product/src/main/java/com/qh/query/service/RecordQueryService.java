package com.qh.query.service;

import java.util.List;
import java.util.Map;

import com.qh.common.utils.Query;
import com.qh.pay.domain.FooterDO;
import com.qh.pay.domain.RecordFoundAcctDO;
import com.qh.pay.domain.RecordMerchBalDO;

/**
 * @ClassName RecordQueryService
 * @Description 资金流水查询
 * @Date 2017年12月26日 下午2:25:00
 * @version 1.0.0
 */
public interface RecordQueryService {

	/**
	 * @Description 商户余额列表
	 * @param query
	 * @return
	 */
	List<RecordMerchBalDO> merchBalList(Query query);

	FooterDO merchBalListFooter(Map<String, Object> params);

	/**
	 * @Description 商户余额列表数量
	 * @param query
	 * @return
	 */
	int merchBalCount(Query query);

	/**
	 * @Description 商户可用余额列表
	 * @param query
	 * @return
	 */
	List<RecordMerchBalDO> merchAvailBalList(Query query);

	FooterDO merchAvailBalListFooter(Map<String, Object> params);

	/**
	 * @Description 商户可用余额列表
	 * @param query
	 * @return
	 */
	int merchAvailBalCount(Query query);

	
	/**
	 * @Description 资金账户余额列表
	 * @param query
	 * @return
	 */
	List<RecordFoundAcctDO> foundAcctList(Query query);

	FooterDO foundAcctListFooter(Map<String, Object> params);

	/**
	 * @Description 资金账户余额列表数量
	 * @param query
	 * @return
	 */
	int foundAcctCount(Query query);

	/**
	 * @Description 资金账户可用余额列表
	 * @param query
	 * @return
	 */
	List<RecordFoundAcctDO> foundAvailAcctList(Query query);

	FooterDO foundAvailAcctListFooter(Map<String, Object> params);

	/**
	 * @Description 资金账户可用余额列表
	 * @param query
	 * @return
	 */
	int foundAvailAcctCount(Query query);
}
