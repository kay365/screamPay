package com.qh.pay.service;

import java.math.BigDecimal;

import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.domain.MerchCharge;
import com.qh.pay.domain.Merchant;

/**
 * @ClassName PayQrService
 * @Description 扫码通道支付
 * @Date 2017年12月19日 上午10:27:03
 * @version 1.0.0
 */
public interface PayQrService {

	/**
	 * @Description 支付通道扫码
	 * @param order
	 * @param merchant 
	 * @return
	 */
	R qrOrder(Order order, Merchant merchant);

	/**
	 * @Description 释放占用金额
	 * @param order
	 * @param merchant
	 */
	void releaseMonAmount(Order order);

	/**
	 * @Description 扫码通道异步回调
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @param businessNo
	 * @param msg 
	 */
	void notifyQr(String merchNo, String outChannel, String monAmount, String businessNo, String msg);

	/**
	 * @Description 获取充值金额
	 * @param monAmount
	 * @param merchNo
	 * @param outChannel 
	 */
	R getChargeMon(String monAmount, String merchNo, String outChannel);

	/**
	 * @Description 充值金额回调
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @param businessNo
	 * @param msg 
	 */
	void notifyChargeQr(String merchNo, String outChannel, String monAmount, String businessNo, String msg);

	/**
	 * @Description 初始化商户充值记录
	 * @param chargeMerchNo
	 * @param outChannel
	 * @param monAmount
	 * @param businessNo
	 */
	static MerchCharge initMerchCharge(String chargeMerchNo, String outChannel, String monAmount, String businessNo) {
		MerchCharge merchCharge = new MerchCharge();
		merchCharge.setMerchNo(chargeMerchNo);
		merchCharge.setOutChannel(outChannel);
		merchCharge.setAmount(new BigDecimal(monAmount));
		merchCharge.setBusinessNo(businessNo);
		merchCharge.setCrtDate(DateUtil.getCurrentTimeInt());
		return merchCharge;
	}

	/**
	 * @Description 商户充值保存
	 * @param merchNo
	 * @param orderNo
	 */
	void chargeDataMsg(String merchNo, String orderNo);

	/**
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param order
	 * @return
	 */
	boolean saveQrOrderData(Order order);

	/**
	 * @Description 人工充值
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @param businessNo
	 * @return
	 */
	R superChargeQr(String merchNo, String outChannel, String monAmount, String businessNo);

	/**
	 * @param merchant 
	 * @Description 手动同步订单
	 * @param order
	 * @param businessNo
	 * @return
	 */
	R syncOrder(Merchant merchant, Order order, String businessNo);

}
