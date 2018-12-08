package com.qh.pay.dao;

import com.qh.pay.domain.RecordPayMerchBalDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 第三方支付可用余额流水
 * @date 2018-02-27 15:31:29
 */
@Mapper
public interface RecordPayMerchAvailBalDao {

	RecordPayMerchBalDO get(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo,@Param("feeType")String feeType);
	
	List<RecordPayMerchBalDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(RecordPayMerchBalDO recordPayMerchAvailBal);
	
	int saveBatch(List<RecordPayMerchBalDO> recordMerchBals);
	
	int update(RecordPayMerchBalDO recordPayMerchAvailBal);
	
	int remove(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo,@Param("feeType")String feeType);
	
}
