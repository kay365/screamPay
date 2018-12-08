package com.qh.query.querydao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.qh.pay.api.Order;

/**
 * @ClassName OrderLoseQueryDao
 * @Description 掉单查询
 * @Date 2018年1月2日 上午10:56:59
 * @version 1.0.0
 */
@Mapper
public interface OrderLoseQueryDao {
	
	@Select("select `order_no`,`merch_no`,`amount`,`business_no`,`pay_company`,`out_channel`,`msg`,crt_date,qh_amount,order_type "
			+ " from pay_order_lose where order_no = #{orderNo} and merch_no = {merchNo}")
	Order get(@Param("orderNo") String orderNo,@Param("merchNo") String merchNo);
	
	@Select("select `order_no`,`merch_no`,`amount`,`business_no`,`pay_company`,`out_channel`,`msg`,crt_date,qh_amount,order_type "
			+ " from pay_order_lose where business_no = #{businessNo} and merch_no = {merchNo}")
	Order getByBusinessNo(@Param("businessNo")String businessNo,@Param("merchNo") String merchNo);
	
	List<Order> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
}
