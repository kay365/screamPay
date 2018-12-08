package com.qh.query.querydao;

import com.qh.pay.domain.FooterDO;
import com.qh.pay.domain.RecordMerchBalDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 聚富商户余额流水
 * @date 2017-11-14 11:32:01
 */
@Mapper
public interface RecordQueryMerchBalDao {

	RecordMerchBalDO get(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo,@Param("feeType")String feeType);
	
	List<RecordMerchBalDO> list(Map<String,Object> map);

	FooterDO listFooter(Map<String,Object> map);
	
	int count(Map<String,Object> map);
}
