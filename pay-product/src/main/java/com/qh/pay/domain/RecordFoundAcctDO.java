package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;



/**
 * 平台资金流水
 * 
 * @date 2017-11-14 11:32:01
 */
public class RecordFoundAcctDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//订单号
	private String orderNo;
	//聚富商户号
	private String merchNo;
	//费用类型
	private Integer feeType;
	//订单类型
	private Integer orderType;
	//用户名
	private String username;
	//变动前金额(元)
	private BigDecimal beforeAmt;
	//变动金额(元)
	private BigDecimal tranAmt;
	//变动后金额(元)
	private BigDecimal afterAmt;
	//账户余额盈亏 0就是出账 1 为入账
	private Integer profitLoss;
	//创建时间
	private Integer crtDate;

	/**
	 * 设置：订单号
	 */
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	/**
	 * 获取：订单号
	 */
	public String getOrderNo() {
		return orderNo;
	}
	/**
	 * 设置：聚富商户号
	 */
	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}
	/**
	 * 获取：聚富商户号
	 */
	public String getMerchNo() {
		return merchNo;
	}
	/**
	 * 设置：费用类型
	 */
	public void setFeeType(Integer feeType) {
		this.feeType = feeType;
	}
	/**
	 * 获取：费用类型
	 */
	public Integer getFeeType() {
		return feeType;
	}
	/**
	 * 设置：订单类型
	 */
	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}
	/**
	 * 获取：订单类型
	 */
	public Integer getOrderType() {
		return orderType;
	}
	/**
	 * 设置：用户名
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * 获取：用户名
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * 设置：变动前金额(元)
	 */
	public void setBeforeAmt(BigDecimal beforeAmt) {
		this.beforeAmt = beforeAmt;
	}
	/**
	 * 获取：变动前金额(元)
	 */
	public BigDecimal getBeforeAmt() {
		return beforeAmt;
	}
	/**
	 * 设置：变动金额(元)
	 */
	public void setTranAmt(BigDecimal tranAmt) {
		this.tranAmt = tranAmt;
	}
	/**
	 * 获取：变动金额(元)
	 */
	public BigDecimal getTranAmt() {
		return tranAmt;
	}
	/**
	 * 设置：变动后金额(元)
	 */
	public void setAfterAmt(BigDecimal afterAmt) {
		this.afterAmt = afterAmt;
	}
	/**
	 * 获取：变动后金额(元)
	 */
	public BigDecimal getAfterAmt() {
		return afterAmt;
	}
	/**
	 * 设置：账户余额盈亏 0就是出账 1 为入账
	 */
	public void setProfitLoss(Integer profitLoss) {
		this.profitLoss = profitLoss;
	}
	/**
	 * 获取：账户余额盈亏 0就是出账 1 为入账
	 */
	public Integer getProfitLoss() {
		return profitLoss;
	}
	/**
	 * 设置：创建时间
	 */
	public void setCrtDate(Integer crtDate) {
		this.crtDate = crtDate;
	}
	/**
	 * 获取：创建时间
	 */
	public Integer getCrtDate() {
		return crtDate;
	}
}
