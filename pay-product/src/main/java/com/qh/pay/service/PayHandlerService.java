package com.qh.pay.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.qh.pay.api.Order;
import com.qh.pay.domain.MerchCharge;
import com.qh.pay.domain.MerchUserSignDO;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.RecordFoundAcctDO;
import com.qh.pay.domain.RecordMerchBalDO;
import com.qh.pay.domain.RecordPayMerchBalDO;

/**
 * @ClassName PayFeeService
 * @Date 2017年11月9日 下午10:06:37
 * @version 1.0.0
 */
public interface PayHandlerService {
	
	/**
	 * @Description 初始化扫码通道
	 * @param order
	 * @param jo
	 * @return
	 */
	String initQrOrder(Order order, JSONObject jo);
	
	/**
	 * @Description 初始化 支付\代付订单信息
	 * @param order
	 * @param jo
	 * @return
	 */
	String initOrder(Order order, JSONObject jo);
	
	/**
	 * @Description 检查绑卡签约信息
	 * @param userSign
	 * @return
	 */
	String checkUserSign(MerchUserSignDO userSign);

	/**
	 * 支付公司 下单个商户号 资金池轮询 记录
	 * @param order
	 */
	void companyMerchCapitalPoolRecord(Order order);
	
	/**
	 * 商户 单月/单日限额 记录
	 * @param order
	 */
	void merchLimitRecord(Order order);
	
	/**
	 * @Description 商户余额减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordMerchBalDO balForMerchSub(Order order, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 商户余额增加
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordMerchBalDO balForMerchAdd(Order order, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 代理资金减少
	 * @param order
	 * @param amount
	 * @param agentUser
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO balForAgentSub(Order order, BigDecimal amount, String agentUser, int feeType, int orderType);

	/**
	 * @Description 代理资金增加
	 * @param order
	 * @param amount
	 * @param agentUser
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO balForAgentAdd(Order order, BigDecimal amount, String agentUser, int feeType, int orderType);

	/**
	 * @Description 平台余额减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO balForPlatSub(Order order, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 平台余额增加
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO balForPlatAdd(Order order, BigDecimal amount, int feeType, int orderType);
	
	/**
	 * 
	 * @Description 处理第三方支付公司资金流水 减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordPayMerchBalDO balForPayMerchSub(Order order, BigDecimal amount, int feeType,int orderType);
	
	/**
	 * @Description 处理第三方支付公司资金流水 增加
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordPayMerchBalDO balForPayMerchAdd(Order order, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 支付订单清算
	 * @param company
	 */
	void orderClear(String company);
	
	/**
	 * @Description 支付订单清算
	 * @param company
	 */
	void orderClear(String company,Date date);
	
	/**
	 * @Description 商户可用余额增加
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordMerchBalDO availBalForMerchAdd(Order order, BigDecimal amount, int feeType, int orderType);
	
	/**
	 * @Description 平台可用余额增加
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO availBalForPlatAdd(Order order, BigDecimal amount, int feeType, int orderType);
	
	/**
	 * 
	 * @Description 第三方支付公司可用余额变更 增加
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	public RecordPayMerchBalDO availBalForPayMerchAdd(Order order, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 代理可用余额资金增加
	 * @param order
	 * @param amount
	 * @param agentUser
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO availBalForAgentAdd(Order order, BigDecimal amount, String agentUser, int feeType, int orderType);
	
	/**
	 * 
	 * @Description 处理平台可用余额资金流水 减少
	 * @param order
	 * @param amount
	 * @param agentUser
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO availBalForPlatSub(Order order, BigDecimal amount,String agentUser,int feeType, int orderType);
	/**
	 * 
	 * @Description 处理平台可用余额资金流水 减少
	 * @param order
	 * @param amount
	 * @param agentUser
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	void availBalForPlatSub(Order order, RecordFoundAcctDO rdFoundBal, PayAcctBal pab);
	/**
	 * 
	 * @Description 处理代理可用余额资金流水 减少
	 * @param order
	 * @param amount
	 * @param agentUser
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordFoundAcctDO availBalForAgentSub(Order order, BigDecimal amount,String agentUser,int feeType, int orderType);
	
	/**
	 * 
	 * @Description 商户代理可用余额资金变更 减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	void availBalForAgentSub(Order order, RecordFoundAcctDO rdFoundBal, PayAcctBal pab);
	
	/**
	 * 
	 * @Description 商户可用余额变更 减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	public RecordMerchBalDO availBalForMerchSub(Order order, BigDecimal amount, int feeType, int orderType);
	
	/**
	 * 
	 * @Description 商户可用余额变更 减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	public void availBalForMerchSub(Order order, RecordMerchBalDO rdMerchBal, PayAcctBal pab);
	
	/**
	 * 
	 * @Description 第三方支付公司可用余额变更 减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	public RecordPayMerchBalDO availBalForPayMerchSub(Order order, BigDecimal amount, int feeType, int orderType);

	/**
	 * 
	 * @Description 第三方支付公司可用余额变更 减少
	 * @param order
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	public void availBalForPayMerchSub(Order order, RecordPayMerchBalDO rdPayMerchAcct, PayAcctBal pab);
	
	/**
	 * @Description 扣除商户可用余额
	 * @param order
	 * @param qhAmount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordMerchBalDO availBalForMerchSubForQr(Order order, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 商户充值增加余额
	 * @param merchCharge
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordMerchBalDO balForMerchChargeAdd(MerchCharge merchCharge, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 商户充值增加可用余额
	 * @param merchCharge
	 * @param amount
	 * @param feeType
	 * @param orderType
	 * @return
	 */
	RecordMerchBalDO availBalForMerchChargeAdd(MerchCharge merchCharge, BigDecimal amount, int feeType, int orderType);

	/**
	 * @Description 检查用户提现订单信息
	 * @param order
	 * @return
	 */
	String checkUserWithDrawOrder(Order order);
	
	
	int updateClearStateBatch(String company,List<Order> updateOrders);

	
	void availBalForOrderClearSucc(List<Order> updateOrders);

}
