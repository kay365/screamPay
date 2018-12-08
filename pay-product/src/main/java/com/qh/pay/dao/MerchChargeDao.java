package com.qh.pay.dao;

import org.apache.ibatis.annotations.Insert;

import com.qh.pay.domain.MerchCharge;

/**
 * @ClassName MerchChargeDao
 * @Description 充值记录保存dao
 * @Date 2017年12月22日 下午3:08:28
 * @version 1.0.0
 */
public interface MerchChargeDao {
	
	// 插入
	@Insert("insert into merch_charge(business_no,merch_no, out_channel, amount, order_state,clear_state,crt_date,memo,msg) values (" 
		+"	#{businessNo},	#{merchNo}, #{outChannel}, #{amount},#{orderState},#{clearState},#{crtDate},#{memo},#{msg})")
	int save(MerchCharge merchCharge);
}
