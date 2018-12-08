package com.qh.pay.dao;

import com.qh.pay.api.Order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付订单
 * @date 2017-11-14 11:32:01
 */
@Mapper
public interface PayOrderDao {

	Order get(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo);
	
	List<Order> list(Map<String,Object> map);
	
	int updateClearState(Order payOrder);
	
	int updateClearStateBatch(List<Order> orders);
	
	int updateNoticeState(Order payOrder);
	
	int save(Order payOrder);
	
	int update(Order payOrder);
	
}
