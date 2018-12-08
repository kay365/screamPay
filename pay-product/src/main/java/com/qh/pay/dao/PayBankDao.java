package com.qh.pay.dao;

import com.qh.common.config.JsonTypeHandler;
import com.qh.pay.domain.PayBankDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

/**
 * 支付银行
 * @date 2017-12-27 11:45:47
 */
@Mapper
public interface PayBankDao {

	@Select("select `company`,`pay_merch`,`card_type`,`banks` from pay_bank where company = #{company} and pay_merch = #{payMerch} and card_type = #{cardType}")
	@Results({
		@Result(column = "banks", jdbcType = JdbcType.VARCHAR, property = "banks", typeHandler = JsonTypeHandler.class) })
	PayBankDO get(@Param("company") String company,@Param("payMerch") String payMerch, @Param("cardType") Integer cardType);
	
	
	List<PayBankDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	@Insert("insert into pay_bank(company, pay_merch, card_type, banks)values(#{company},#{payMerch},#{cardType},"
			+ "#{banks,typeHandler=com.qh.common.config.JsonTypeHandler})")
	int save(PayBankDO payBank);
	
	@Update("update pay_bank set banks = #{banks,typeHandler=com.qh.common.config.JsonTypeHandler} where company = #{company} and pay_merch = #{payMerch} and card_type = #{cardType}")
	int update(PayBankDO payBank);
	
	@Delete("delete from pay_bank where company = #{company} and pay_merch = #{payMerch} and card_type = #{cardType}")
	int remove(@Param("company") String company,@Param("payMerch") String payMerch, @Param("cardType") Integer cardType);
	
}
