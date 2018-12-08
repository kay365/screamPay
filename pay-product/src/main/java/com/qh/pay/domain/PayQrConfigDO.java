package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;


/**
 * 聚富扫码通道配置
 * 
 * @date 2017-12-14 14:37:38
 */
public class PayQrConfigDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//主键id
	private Integer id;
	//商户号
	private String merchNo;
	//支付渠道
	private String outChannel;
	//收款账号
	private String accountNo;
	//收款名称
	private String accountName;
	//收款人电话
	private String accountPhone;
	//客服电话
	private String serviceTel;
	//备注信息
	private String memo;
	//收款二维码图片  金额和图片路径
	private Map<String,Integer> qrs;

	//支付成本费率  一般为 0
	private BigDecimal costRate;
	//聚富代理费率
	private BigDecimal jfRate;
	//聚富回调apiKey
	private String apiKey;
	/**
	 * 设置：主键id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * 获取：主键id
	 */
	public Integer getId() {
		return id;
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
	 * 设置：支付渠道
	 */
	public void setOutChannel(String outChannel) {
		this.outChannel = outChannel;
	}
	/**
	 * 获取：支付渠道
	 */
	public String getOutChannel() {
		return outChannel;
	}
	/**
	 * 设置：收款账号
	 */
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	/**
	 * 获取：收款账号
	 */
	public String getAccountNo() {
		return accountNo;
	}
	/**
	 * 设置：收款名称
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	/**
	 * 设置：收款人电话
	 */
	public void setAccountPhone(String accountPhone) {
		this.accountPhone = accountPhone;
	}
	/**
	 * 获取：收款名称
	 */
	public String getAccountName() {
		return accountName;
	}
	/**
	 * 获取：收款人电话
	 */
	public String getAccountPhone() {
		return accountPhone;
	}

	/**
	 * 设置：客户电话
	 */
	public void setServiceTel(String serviceTel) {
		this.serviceTel = serviceTel;
	}
	/**
	 * 获取：客户电话
	 */
	public String getServiceTel() {
		return serviceTel;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * 设置：备注信息
	 */
	public void setMemo(String memo) {
		this.memo = memo;
	}
	/**
	 * 获取：备注信息
	 */
	public String getMemo() {
		return memo;
	}
	/**
	 * 设置：收款二维码图片
	 */
	public void setQrs(Map<String,Integer> qrs) {
		this.qrs = qrs;
	}
	/**
	 * 获取：收款二维码图片
	 */
	public Map<String,Integer> getQrs() {
		return qrs;
	}
	public BigDecimal getCostRate() {
		return costRate;
	}
	public void setCostRate(BigDecimal costRate) {
		this.costRate = costRate;
	}
	public BigDecimal getJfRate() {
		return jfRate;
	}
	public void setJfRate(BigDecimal jfRate) {
		this.jfRate = jfRate;
	}
	
}
