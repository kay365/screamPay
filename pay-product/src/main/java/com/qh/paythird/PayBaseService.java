package com.qh.paythird;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.domain.MerchUserSignDO;

/**
 * @ClassName PayOrderService
 * @Description 订单支付
 * @Date 2017年11月6日 下午2:45:34
 * @version 1.0.0
 */
public interface PayBaseService {

	/**
	 * @Description 支付调用入口
	 * @param order
	 * @return 
	 */
	R order( Order order);

	/**
	 * @Description 支付后台回调处理接口
	 * @param order
	 * @param request
	 * @param requestBody
	 * @return
	 */
	R notify(Order order, HttpServletRequest request, String requestBody);

	/**
	 * @Description 支付订单查询
	 * @param order
	 */
	R query(Order order);

	/**
	 * @Description 代付订单
	 * @param order
	 * @return
	 */
	R orderAcp(Order order);


	/**
	 * @Description 代付订单回调通知
	 * @param order
	 * @param request
	 * @param requestBody
	 * @return
	 */
	R notifyAcp(Order order, HttpServletRequest request, String requestBody);

	/**
	 * @Description 代付订单查询
	 * @param order
	 * @return
	 */
	R acpQuery(Order order);

	/**
	 * @Description 更新银行卡列表
	 * @param order
	 * @param bank_savings
	 * @param bank_credits
	 */
	void refreshBanks(Order order, List<String> bank_savings, List<String> bank_credits);

	/**
	 * @Description 绑卡
	 * @param order
	 * @param userSign
	 * @return
	 */
	R cardBind(Order order, MerchUserSignDO userSign);

	/**
	 * @Description 绑卡确认
	 * @param order
	 * @param userSign
	 * @return
	 */
	R cardBindConfirm(Order order, MerchUserSignDO userSign);

	/**
	 * @Description 短信重发
	 * @param order
	 * @param sendType 1、绑卡类型 2、支付类型
	 * @return
	 */
	R cardMsgResend(Order order, Integer sendType);

	


}
