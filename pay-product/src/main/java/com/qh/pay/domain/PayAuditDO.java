package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 支付审核
 * 
 * @date 2017-11-16 15:59:04
 */
public class PayAuditDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//订单号
	private String orderNo;
	//商户号
	private String merchNo;
	//审核类型
	private Integer auditType;
	//审核结果
	private Integer auditResult;
	//审核人
	private String auditor;
	//审核时间
	private Date auditTime;
	//创建时间
	private Integer crtTime;
	//备注
	private String memo;
	
	private BigDecimal amount;
	
	private String merchName;
	
	public BigDecimal poundage;

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
	 * 设置：商户号
	 */
	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}
	/**
	 * 获取：商户号
	 */
	public String getMerchNo() {
		return merchNo;
	}
	/**
	 * 设置：审核类型
	 */
	public void setAuditType(Integer auditType) {
		this.auditType = auditType;
	}
	/**
	 * 获取：审核类型
	 */
	public Integer getAuditType() {
		return auditType;
	}
	/**
	 * 设置：审核结果
	 */
	public void setAuditResult(Integer auditResult) {
		this.auditResult = auditResult;
	}
	/**
	 * 获取：
	 */
	public Integer getAuditResult() {
		return auditResult;
	}
	/**
	 * 设置：审核人
	 */
	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}
	/**
	 * 获取：审核人
	 */
	public String getAuditor() {
		return auditor;
	}
	/**
	 * 设置：审核时间
	 */
	public void setAuditTime(Date auditTime) {
		this.auditTime = auditTime;
	}
	/**
	 * 获取：审核时间
	 */
	public Date getAuditTime() {
		return auditTime;
	}
	/**
	 * 设置：创建时间
	 */
	public void setCrtTime(Integer crtTime) {
		this.crtTime = crtTime;
	}
	/**
	 * 获取：创建时间
	 */
	public Integer getCrtTime() {
		return crtTime;
	}
	/**
	 * 设置：备注
	 */
	public void setMemo(String memo) {
		this.memo = memo;
	}
	/**
	 * 获取：备注
	 */
	public String getMemo() {
		return memo;
	}
	public String getMerchName() {
		return merchName;
	}
	public void setMerchName(String merchName) {
		this.merchName = merchName;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getPoundage() {
		return poundage;
	}
	public void setPoundage(BigDecimal poundage) {
		this.poundage = poundage;
	}
}
