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
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;

@Mapper
public interface AgentMapper {
	
	@Select("select `agent_number`,`agent_id`,status,audit_status,level,parent_agent,merchants_name,t_one,d_zero,paid "
			+ " from agent where agent_id = #{value}")
	@Results({
		@Result(column = "t_one", jdbcType = JdbcType.VARCHAR, property = "tOne", typeHandler = JsonTypeHandler.class),
		@Result(column = "d_zero", jdbcType = JdbcType.VARCHAR, property = "dZero", typeHandler = JsonTypeHandler.class), 
		@Result(column = "paid", jdbcType = JdbcType.VARCHAR, property = "paid", typeHandler = JsonTypeHandler.class) 
	})
    Agent get(Integer id);
	
	@Select("select `agent_id`,`agent_number`,`status`,`audit_status`,`level`,`parent_agent`,`create_time`,"+
		"`modify_time`,`agent_type`,`manager_name`,`manager_phone`,`manager_pass`,`contract_effective_time`,"+
		"`enable_time`,`merchants_name`,`merchants_short_name`,`merchants_industry`,`merchants_industry_code`,"+
		"`merchants_sub_industry`,`merchants_sub_industry_code`,`contacts`,`contacts_phone`,`contacts_email`,"+
		"`contacts_qq`,`province`,`province_code`,`city`,`city_code`,`legaler_name`,`legaler_card_type`,"+
		"`legaler_card_number`,`legaler_card_effective_time`,`legaler_card_pic_front`,`legaler_card_pic_back`,"+
		"`account_type`,`account_province`,`account_province_code`,`account_city`,`account_city_code`,`account_bank`,"+
		"`account_bank_code`,`account_bank_branch`,`account_bank_branch_code`,`account_open_person`,"+
		"`account_open_number`,`account_open_card_number`,`account_open_phone`,`account_pic`,`t_one`,`d_zero`,`paid`"
			+ " from agent where agent_id = #{value}")
	@Results({
		@Result(column = "t_one", jdbcType = JdbcType.VARCHAR, property = "tOne", typeHandler = JsonTypeHandler.class),
		@Result(column = "d_zero", jdbcType = JdbcType.VARCHAR, property = "dZero", typeHandler = JsonTypeHandler.class), 
		@Result(column = "paid", jdbcType = JdbcType.VARCHAR, property = "paid", typeHandler = JsonTypeHandler.class) 
	})
	Agent getById(Integer id);
	
	@Select("select `agent_number`,`agent_id`,status,audit_status,level,parent_agent,merchants_name,t_one,d_zero,paid "
			+ " from agent where agent_number = #{value}")
	@Results({
		@Result(column = "t_one", jdbcType = JdbcType.VARCHAR, property = "tOne", typeHandler = JsonTypeHandler.class),
		@Result(column = "d_zero", jdbcType = JdbcType.VARCHAR, property = "dZero", typeHandler = JsonTypeHandler.class), 
		@Result(column = "paid", jdbcType = JdbcType.VARCHAR, property = "paid", typeHandler = JsonTypeHandler.class) 
	})
	Agent getByMerchNo(String merchNo);
    
	List<Agent> list(Map<String,Object> map);
	List<Agent> listAgent(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	@Select("select count(1) from Agent where agent_number = #{value}")
	int exist(String merchNo);
	
	/*@Insert("insert into merchant(`merch_no`,`name`,`user_id`,`agent_user`,`public_key`,fee_rate,hand_rate,channel_switch) "
			+ "values(#{merchNo},#{name},#{userId},#{agentUser},#{publicKey},#{feeRate,typeHandler=com.qh.common.config.JsonTypeHandler},"
			+ "#{handRate,typeHandler=com.qh.common.config.JsonTypeHandler},#{channelSwitch,typeHandler=com.qh.common.config.JsonTypeHandler})")*/
	int save(Agent merchant);
	
	int update(Agent merchant);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
	int batchqiyong(Integer[] ids);
	int batchjinyong(Integer[] ids);
	int batchAudit(Map<String,Object> map);
	
	int removeByMerchNo(String merchNo);
	
	int batchRemoveByMerchNo(String[] merchNos);

}
