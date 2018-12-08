package com.qh.pay.dao;

import com.qh.common.config.JsonTypeHandler;
import com.qh.pay.domain.PayQrConfigDO;

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

/**
 * 聚富扫码通道配置
 * @date 2017-12-14 14:37:38
 */
@Mapper
public interface PayQrConfigDao {

	@Select("select `id`,`merch_no`,`out_channel`,`account_no`,`account_name`,`account_phone`,`service_tel`,`memo`,`qrs`,cost_rate,jf_rate,`api_key` from pay_qr_config where id = #{value}")
	@Results({
		@Result(column = "qrs", jdbcType = JdbcType.VARCHAR, property = "qrs", typeHandler = JsonTypeHandler.class) })
	PayQrConfigDO get(Integer id);
	
	@Select("select `id`,`merch_no`,`out_channel`,`account_no`,`account_name`,`account_phone`,`service_tel`,`memo`,`qrs`,cost_rate,jf_rate,`api_key` from pay_qr_config "
			+ "where out_channel = #{outChannel} and merch_no = #{merchNo}")
	@Results({
			@Result(column = "qrs", jdbcType = JdbcType.VARCHAR, property = "qrs", typeHandler = JsonTypeHandler.class) })
	PayQrConfigDO getByCode(@Param("outChannel") String outChannel, @Param("merchNo") String merchNo);
	
	List<PayQrConfigDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	@Insert("insert into pay_qr_config(`merch_no`, `out_channel`,`account_no`,`account_name`,`account_phone`,`service_tel`,`memo`,cost_rate,jf_rate,`api_key`,`qrs`)values("
			+ "#{merchNo},#{outChannel},#{accountNo},#{accountName},#{accountPhone},#{serviceTel},#{memo},#{costRate},#{jfRate},#{apiKey},"
			+ "#{qrs,typeHandler=com.qh.common.config.JsonTypeHandler})")
	int save(PayQrConfigDO payQrConfig);
	
	int updateByCode(PayQrConfigDO payQrConfig);
	
	int remove(@Param("outChannel") String outChannel, @Param("merchNo") String merchNo);
	
	@Select("select count(1) from pay_qr_config where  merch_no = #{merchNo}  and out_channel = #{outChannel}")
	int countByMerchNo(@Param("outChannel") String outChannel, @Param("merchNo") String merchNo);

	/**
	 * @Description 更新二维码图片
	 * @param outChannel
	 * @param merchNo
	 * @param qrs
	 * @return
	 */
	@Update("update pay_qr_config set `qrs` = #{qrs,typeHandler=com.qh.common.config.JsonTypeHandler} where merch_no = #{merchNo} and out_channel = #{outChannel} ")
	int updateQrs(@Param("outChannel")String outChannel, @Param("merchNo")String merchNo, @Param("qrs")Map<String, Integer> qrs);
}
