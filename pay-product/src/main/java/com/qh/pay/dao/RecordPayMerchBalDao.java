package com.qh.pay.dao;

import com.qh.pay.domain.RecordPayMerchBalDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第三方支付余额流水
 * @date 2018-02-27 15:31:47
 */
@Mapper
public interface RecordPayMerchBalDao {

	RecordPayMerchBalDO get(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo,@Param("feeType")String feeType);
	
	List<RecordPayMerchBalDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(RecordPayMerchBalDO recordPayMerchBal);
	
	int saveBatch(List<RecordPayMerchBalDO> recordMerchBals);
	
	int update(RecordPayMerchBalDO recordPayMerchBal);
	
	int remove(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo,@Param("feeType")String feeType);
	
}
