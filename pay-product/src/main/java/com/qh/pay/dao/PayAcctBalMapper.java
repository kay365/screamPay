package com.qh.pay.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import com.qh.common.config.JsonTypeHandler;
import com.qh.pay.domain.PayAcctBal;

/**
 * @ClassName PayAcctBalMapper
 * @Description 账户余额
 * @Date 2017年11月6日 上午11:31:33
 * @version 1.0.0
 */
@Mapper
public interface PayAcctBalMapper {

	List<PayAcctBal> list(Map<String, Object> map);

	int count(Map<String, Object> map);

	// 根据用户名查询
	@Select("select `user_id`,`username`,`balance`,`user_type`,avail_bal,company_pay_avail_bal from pay_acct_bal where username = #{value}")
	@Results({
			@Result(column = "company_pay_avail_bal", jdbcType = JdbcType.VARCHAR, property = "companyPayAvailBal", typeHandler = JsonTypeHandler.class) })
	PayAcctBal getByUserName(String username);

	// 平台资金账户
	@Select("select `user_id`,`username`,`balance`,`user_type`,avail_bal,company_pay_avail_bal from pay_acct_bal where user_type = #{value} limit 1")
	@Results({
			@Result(column = "company_pay_avail_bal", jdbcType = JdbcType.VARCHAR, property = "companyPayAvailBal", typeHandler = JsonTypeHandler.class) })
	PayAcctBal singleByType(Integer userType);

	// 插入
	@Insert("insert into pay_acct_bal(`user_id`, `username`, `user_type`, `balance`,avail_bal,company_pay_avail_bal) values (" 
		+"	#{userId},	#{username}, #{userType}, #{balance},#{availBal},"
		+ "#{companyPayAvailBal,typeHandler=com.qh.common.config.JsonTypeHandler})")
	int save(PayAcctBal payAcctBal);

	// 更新
	@Update("update pay_acct_bal `balance` = #{balance},availBal = #{avail_bal},"
			+ "#{companyPayAvailBal,typeHandler=com.qh.common.config.JsonTypeHandler}	where username = #{username}")
	int update(@Param("username") String username, @Param("balance") BigDecimal balance,@Param("availBal") BigDecimal availBal, @Param("companyPayAvailBal")Map<String,BigDecimal> companyPayAvailBal);

	// 删除
	int remove(String username);

	// 批量删除
	int batchRemove(String[] usernames);

	// 资金余额列表
	List<PayAcctBal> listByType(Integer userType);
}
