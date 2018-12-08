package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 支付公司配置
 * 
 * @date 2017-11-06 16:00:33
 */
public class PayConfigCompanyDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//支付公司
	private String company;
	//支付商户
	private String payMerch;
	//渠道
	private String outChannel;
	//支付成本费率
	private BigDecimal costRate;
	//支付成本费率 单位  1% 2元
	private Integer costRateUnit;
	//聚富代理费率
	private BigDecimal qhRate;
	//聚富代理费率 单位  1% 2元
	private Integer qhRateUnit;
	//单笔最大支付额
	private Integer maxPayAmt;
	//单笔最新支付额
	private Integer minPayAmt;
	//创建时间
	private Date crtTime;
	//支付时间段
	private String payPeriod;
	//是否关闭 0 不关闭， 1 关闭
	private Integer ifClose;
	//是否关闭 0 A, 1 B, 2 C, 3 D
	private Integer payChannelType;
	//回调域名
	private String callbackDomain;
	//资金池 (支付公司开给这个商户号的单日限额)
	private Integer capitalPool;
	//结算方式 结算方式D0 T1 D1 T0
	private Integer paymentMethod;
	//最小手续费
	private BigDecimal minFee;
	//最小手续费
	private BigDecimal maxFee;
	//清算比例
	private BigDecimal clearRatio;

	/**
	 * 设置：支付公司
	 */
	public void setCompany(String company) {
		this.company = company;
	}
	/**
	 * 获取：支付公司
	 */
	public String getCompany() {
		return company;
	}
	/**
	 * 设置：支付商户
	 */
	public void setPayMerch(String payMerch) {
		this.payMerch = payMerch;
	}
	/**
	 * 获取：支付商户
	 */
	public String getPayMerch() {
		return payMerch;
	}
	/**
	 * 设置：渠道
	 */
	public void setOutChannel(String outChannel) {
		this.outChannel = outChannel;
	}
	/**
	 * 获取：渠道
	 */
	public String getOutChannel() {
		return outChannel;
	}
	/**
	 * 设置：支付成本费率
	 */
	public void setCostRate(BigDecimal costRate) {
		this.costRate = costRate;
	}
	/**
	 * 获取：支付成本费率
	 */
	public BigDecimal getCostRate() {
		return costRate;
	}
	/**
	 * 设置：聚富代理费率
	 */
	public void setQhRate(BigDecimal qhRate) {
		this.qhRate = qhRate;
	}
	/**
	 * 获取：聚富代理费率
	 */
	public BigDecimal getQhRate() {
		return qhRate;
	}
	/**
	 * 设置：单笔最大支付额
	 */
	public void setMaxPayAmt(Integer maxPayAmt) {
		this.maxPayAmt = maxPayAmt;
	}
	/**
	 * 获取：单笔最大支付额
	 */
	public Integer getMaxPayAmt() {
		return maxPayAmt;
	}
	/**
	 * 设置：单笔最新支付额
	 */
	public void setMinPayAmt(Integer minPayAmt) {
		this.minPayAmt = minPayAmt;
	}
	/**
	 * 获取：单笔最新支付额
	 */
	public Integer getMinPayAmt() {
		return minPayAmt;
	}
	/**
	 * 设置：创建时间
	 */
	public void setCrtTime(Date crtTime) {
		this.crtTime = crtTime;
	}
	/**
	 * 获取：创建时间
	 */
	public Date getCrtTime() {
		return crtTime;
	}
	/**
	 * 设置：
	 */
	public void setPayPeriod(String payPeriod) {
		this.payPeriod = payPeriod;
	}
	/**
	 * 获取：
	 */
	public String getPayPeriod() {
		return payPeriod;
	}
	/**
	 * 设置：是否关闭 0 不关闭， 1 关闭
	 */
	public void setIfClose(Integer ifClose) {
		this.ifClose = ifClose;
	}
	/**
	 * 获取：是否关闭 0 不关闭， 1 关闭
	 */
	public Integer getIfClose() {
		return ifClose;
	}
	
	public Integer getPayChannelType() {
		return payChannelType;
	}
	public void setPayChannelType(Integer payChannelType) {
		this.payChannelType = payChannelType;
	}
	public String getCallbackDomain() {
		return callbackDomain;
	}
	public void setCallbackDomain(String callbackDomain) {
		this.callbackDomain = callbackDomain;
	}
	public Integer getCapitalPool() {
		return capitalPool;
	}
	public void setCapitalPool(Integer capitalPool) {
		this.capitalPool = capitalPool;
	}
	public Integer getCostRateUnit() {
		return costRateUnit;
	}
	public void setCostRateUnit(Integer costRateUnit) {
		this.costRateUnit = costRateUnit;
	}
	public Integer getQhRateUnit() {
		return qhRateUnit;
	}
	public void setQhRateUnit(Integer qhRateUnit) {
		this.qhRateUnit = qhRateUnit;
	}
	public Integer getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(Integer paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public BigDecimal getMinFee() {
		return minFee;
	}
	public void setMinFee(BigDecimal minFee) {
		this.minFee = minFee;
	}
	public BigDecimal getMaxFee() {
		return maxFee;
	}
	public void setMaxFee(BigDecimal maxFee) {
		this.maxFee = maxFee;
	}
	public BigDecimal getClearRatio() {
		return clearRatio;
	}
	public void setClearRatio(BigDecimal clearRatio) {
		this.clearRatio = clearRatio;
	}
}
