package com.qh.query.service;

import java.util.List;
import java.util.Map;

import com.qh.pay.domain.FooterDO;
import org.apache.ibatis.annotations.Param;

import com.qh.common.utils.Query;
import com.qh.pay.api.Order;


public interface OrderQueryService {
	
	Order get(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo);
	
	/**
	 * 在途支付
	 * @param endDateInt 
	 * @param beginDateInt 
	 * @param merchNo 
	 * @param map
	 * @return
	 */
	List<Object> getOrders(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map);

	List<Object> getOrdersFooter(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map);

	/**
	 * @param endDateInt 
	 * @param beginDateInt 
	 * @param merchNo 
	 * @Description 数量
	 * @return
	 */
	int getOrdersCount(String merchNo, int beginDateInt, int endDateInt);
	
	List<Order> list(Map<String,Object> map);

	FooterDO listFooter(Map<String,Object> map);

	int count(Map<String,Object> map);


	
	Order getAcp(@Param("orderNo")String orderNo,@Param("merchNo")String merchNo);
	
	/**
	 * @Description 查询代付
	 * @param merchNo
	 * @param beginDateInt
	 * @param endDateInt
	 * @param query
	 * @return
	 */
	List<Object> getAcpOrders(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map);

	List<Object> getAcpOrdersFooter(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map);

	/**
	 * @Description 代付订单数量
	 * @param merchNo
	 * @param beginDateInt
	 * @param endDateInt
	 * @return
	 */
	int getAcpOrdersCount(String merchNo, int beginDateInt, int endDateInt);

	/**
	 * @Description 代付数据库查询
	 * @param query
	 * @return
	 */
	List<Order> listAcp(Map<String,Object> map);

	FooterDO listAcpFooter(Map<String,Object> map);

	/**
	 * @Description 代付数据库查询数量
	 * @param query
	 * @return
	 */
	int countAcp(Map<String,Object> map);

	/**
	 * @Description 掉单列表
	 * @param query
	 * @return
	 */
	List<Order> listLose(Query query);

	/**
	 * @Description 掉单数量
	 * @param query
	 * @return
	 */
	int countLose(Query query);
}
