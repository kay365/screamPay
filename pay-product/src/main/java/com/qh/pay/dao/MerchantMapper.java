package com.qh.pay.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import com.qh.common.config.JsonTypeHandler;
import com.qh.pay.domain.Merchant;

@Mapper
public interface MerchantMapper {
	
	@Select("select `merch_no`,`user_id`,`public_key`,`merchant_registered_number`,`manager_pass`,`contacts_email`,channel_switch,status,audit_status,pay_channel_type,parent_agent,payment_method,merchants_name,merchants_short_name"
			+ ",t_one,d_zero,paid,support_paid,support_withdrawal,day_limit,month_limit"
			+ " from merchant where user_id = #{value}")
	@Results({
		@Result(column = "t_one", jdbcType = JdbcType.VARCHAR, property = "tOne", typeHandler = JsonTypeHandler.class),
		@Result(column = "d_zero", jdbcType = JdbcType.VARCHAR, property = "dZero", typeHandler = JsonTypeHandler.class), 
		@Result(column = "paid", jdbcType = JdbcType.VARCHAR, property = "paid", typeHandler = JsonTypeHandler.class), 
		@Result(column = "channel_switch", jdbcType = JdbcType.VARCHAR, property = "channelSwitch", typeHandler = JsonTypeHandler.class) 
	})
    Merchant get(Integer id);
	
	@Select("select `merch_no`,`user_id`,`public_key`,`merchant_registered_number`,`contacts_email`,channel_switch,status,audit_status,pay_channel_type,parent_agent,payment_method,merchants_name,merchants_short_name"
			+ ",t_one,d_zero,paid,support_paid,support_withdrawal,day_limit,month_limit,paid_channel "
			+ " from merchant where merch_no = #{value}")
	@Results({
		@Result(column = "t_one", jdbcType = JdbcType.VARCHAR, property = "tOne", typeHandler = JsonTypeHandler.class),
		@Result(column = "d_zero", jdbcType = JdbcType.VARCHAR, property = "dZero", typeHandler = JsonTypeHandler.class),
		@Result(column = "paid", jdbcType = JdbcType.VARCHAR, property = "paid", typeHandler = JsonTypeHandler.class),
		@Result(column = "channel_switch", jdbcType = JdbcType.VARCHAR, property = "channelSwitch", typeHandler = JsonTypeHandler.class) ,
		@Result(column = "paid_channel", jdbcType = JdbcType.VARCHAR, property = "paidChannel", typeHandler = JsonTypeHandler.class)
	})
    Merchant getByMerchNo(String merchNo);
	Merchant getById(String merchNo);

	List<Merchant> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	@Select("select count(1) from merchant where merch_no = #{value}")
	int exist(String merchNo);

	/*@Insert("insert into merchant(`merch_no`,`merchants_name`,`user_id`,`parent_agent`,`public_key`,fee_rate,hand_rate,channel_switch,paid_channel,pay_channel_type) "
			+ "values(#{merchNo},#{merchantsName},#{userId},#{parentAgent},#{publicKey},#{feeRate,typeHandler=com.qh.common.config.JsonTypeHandler},"
			+ "#{handRate,typeHandler=com.qh.common.config.JsonTypeHandler},#{channelSwitch,typeHandler=com.qh.common.config.JsonTypeHandler},#{paidChannel,typeHandler=com.qh.common.config.JsonTypeHandler})")*/
	int save(Merchant merchant);
	
	int update(Merchant merchant);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
	
	int batchqiyong(Integer[] ids);
	int batchjinyong(Integer[] ids);
	
	int batchAudit(Map<String,Object> map);
	
	int batchWithdrawal(Map<String,Object> map);
	
	int batchPaid(Map<String,Object> map);
	
	int removeByMerchNo(String merchNo);
	
	int batchRemoveByMerchNo(String[] merchNos);

}
