package com.qh.pay.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.qh.pay.api.Order;

/**
 * 掉单
 * @date 2018-01-02 10:21:06
 */
@Mapper
public interface PayOrderLoseDao {

	@Select("select `order_no`,`merch_no`,`amount`,`business_no`,`pay_company`,`out_channel`,`msg`,crt_date,qh_amount,order_type,order_state "
			+ " from pay_order_lose where order_no = #{orderNo} and merch_no = #{merchNo}")
	Order get(@Param("orderNo") String orderNo,@Param("merchNo") String merchNo);
	
	@Select("select `order_no`,`merch_no`,`amount`,`business_no`,`pay_company`,`out_channel`,`msg`,crt_date,qh_amount,order_type,order_state "
			+ " from pay_order_lose where business_no = #{businessNo} and merch_no = #{merchNo} and out_channel = #{outChannel}")
	Order getByBusinessNo(@Param("businessNo")String businessNo,@Param("merchNo") String merchNo,@Param("outChannel") String outChannel);
	
	@Insert("insert into pay_order_lose(`order_no`,`merch_no`,`amount`,`business_no`,`pay_company`,`out_channel`,`msg`,crt_date,qh_amount,order_type,order_state)"
		+ "values(#{orderNo},#{merchNo},#{amount},#{businessNo},#{payCompany},#{outChannel},#{msg},#{crtDate},#{qhAmount},#{orderType},#{orderState})")
	int save(Order order);
	
	@Update("update pay_order_lose set order_no = #{orderNo},qh_amount=#{qhAmount},order_state = #{orderState} where business_no = #{businessNo} and merch_no = #{merchNo}")
	int update(Order order);
}
