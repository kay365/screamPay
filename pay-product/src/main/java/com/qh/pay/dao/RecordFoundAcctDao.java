package com.qh.pay.dao;

import com.qh.pay.domain.RecordFoundAcctDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 平台资金流水
 * @date 2017-11-14 11:32:01
 */
@Mapper
public interface RecordFoundAcctDao {

	RecordFoundAcctDO get(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo,@Param("feeType")String feeType);
	
	List<RecordFoundAcctDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(RecordFoundAcctDO recordFoundAcct);
	
	int update(RecordFoundAcctDO recordFoundAcct);
	
	int remove(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo,@Param("feeType")String feeType);
	
}
