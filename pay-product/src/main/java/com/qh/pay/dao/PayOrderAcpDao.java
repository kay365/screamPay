package com.qh.pay.dao;

import com.qh.pay.api.Order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 代付订单
 * @date 2017-11-20 15:55:46
 */
@Mapper
public interface PayOrderAcpDao {

	Order get(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo);
	
	List<Order> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(Order payOrderAcp);
	
	int update(Order payOrderAcp);
	
	int updateNoticeState(Order payOrder);
	
}
