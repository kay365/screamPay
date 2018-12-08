package com.qh.pay.domain;

import java.io.Serializable;


/**
 * 商户号下的用户签约信息
 * 
 * @date 2017-11-02 11:21:44
 */
public class MerchUserSignDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//聚富商户
	private String merchNo;
	//商户用户标识
	private String userId;
	//支付公司
	private String payCompany;
	//支付商户
	private String payMerch;
	//快捷签约
	private String sign;
	//其他一些支付信息
	private String info;
	//持卡人姓名
	private String acctName;
	//账户类型
	private Integer acctType;
	//银行卡号
	private String bankNo;
	//银行代码
	private String bankCode;
	//证件类型 1身份证
	private Integer certType;
	//证件号码
	private String certNo;
	//手机号码
	private String phone;
	//信用卡背面cvv2码后三位
	private String cvv2;
	//有效期，年月，四位数，例：2112
	private String validDate;
	//1-快捷支付 2-代扣扣款
	private Integer collType;
	//卡号类型
	private Integer cardType;
	//绑卡确认验证码
	private String checkCode;
	//银行名称 用于展示
	private String bankName;
	/**
	 * 设置：聚富商户
	 */
	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}
	/**
	 * 获取：聚富商户
	 */
	public String getMerchNo() {
		return merchNo;
	}
	/**
	 * 设置：商户用户标识
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * 获取：商户用户标识
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * 设置：支付公司
	 */
	public void setPayCompany(String payCompany) {
		this.payCompany = payCompany;
	}
	/**
	 * 获取：支付公司
	 */
	public String getPayCompany() {
		return payCompany;
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
	 * 设置：快捷签约
	 */
	public void setSign(String sign) {
		this.sign = sign;
	}
	/**
	 * 获取：快捷签约
	 */
	public String getSign() {
		return sign;
	}
	/**
	 * 设置：其他一些支付信息
	 */
	public void setInfo(String info) {
		this.info = info;
	}
	/**
	 * 获取：其他一些支付信息
	 */
	public String getInfo() {
		return info;
	}
	/**
	 * 设置：持卡人姓名
	 */
	public void setAcctName(String acctName) {
		this.acctName = acctName;
	}
	/**
	 * 获取：持卡人姓名
	 */
	public String getAcctName() {
		return acctName;
	}
	
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	/**
	 * 设置：证件号码
	 */
	public void setCertNo(String certNo) {
		this.certNo = certNo;
	}
	/**
	 * 获取：证件号码
	 */
	public String getCertNo() {
		return certNo;
	}
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	/**
	 * 设置：信用卡背面cvv2码后三位
	 */
	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}
	/**
	 * 获取：信用卡背面cvv2码后三位
	 */
	public String getCvv2() {
		return cvv2;
	}
	
	public String getValidDate() {
		return validDate;
	}
	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}
	public Integer getAcctType() {
		return acctType;
	}
	public void setAcctType(Integer acctType) {
		this.acctType = acctType;
	}
	public Integer getCertType() {
		return certType;
	}
	public void setCertType(Integer certType) {
		this.certType = certType;
	}
	public Integer getCollType() {
		return collType;
	}
	public void setCollType(Integer collType) {
		this.collType = collType;
	}
	public Integer getCardType() {
		return cardType;
	}
	public void setCardType(Integer cardType) {
		this.cardType = cardType;
	}
	public String getCheckCode() {
		return checkCode;
	}
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	
}
