package com.qh.common.domain;

import java.io.Serializable;



/**
 * 用户银行卡
 * 
 * @date 2018-01-10 14:39:21
 */
public class UserBankDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//商户用户标识
	private String username;
	//银行卡号
	private String bankNo;
	//银行代码
	private String bankCode;
	//银行卡类型0 储蓄卡 1 信用卡
	private Integer cardType;
	//持卡人姓名
	private String acctName;
	//账户类型0对私 1对公
	private Integer acctType;
	//证件类型 1身份证
	private Integer certType;
	//证件号码
	private String certNo;
	//手机号码
	private String phone;
	//支付联行号
	private String unionpayNo;
	//支行名称
	private String bankBranch;
	//信用卡背面cvv2码后三位
	private String cvv2;
	//有效期，年月，四位数，例：2112
	private String validDate;
	//其他一些支付信息
	private String info;
	private Integer city;
	private Integer province;
	private String bankProvince;
	private String bankCity;

	/**
	 * 设置：商户用户标识
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * 获取：商户用户标识
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * 设置：银行卡号
	 */
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}
	/**
	 * 获取：银行卡号
	 */
	public String getBankNo() {
		return bankNo;
	}
	/**
	 * 设置：银行代码
	 */
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	/**
	 * 获取：银行代码
	 */
	public String getBankCode() {
		return bankCode;
	}
	/**
	 * 设置：银行卡类型0 储蓄卡 1 信用卡
	 */
	public void setCardType(Integer cardType) {
		this.cardType = cardType;
	}
	/**
	 * 获取：银行卡类型0 储蓄卡 1 信用卡
	 */
	public Integer getCardType() {
		return cardType;
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
	/**
	 * 设置：账户类型0对私 1对公
	 */
	public void setAcctType(Integer acctType) {
		this.acctType = acctType;
	}
	/**
	 * 获取：账户类型0对私 1对公
	 */
	public Integer getAcctType() {
		return acctType;
	}
	/**
	 * 设置：证件类型 1身份证
	 */
	public void setCertType(Integer certType) {
		this.certType = certType;
	}
	/**
	 * 获取：证件类型 1身份证
	 */
	public Integer getCertType() {
		return certType;
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
	/**
	 * 设置：手机号码
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	/**
	 * 获取：手机号码
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * 设置：支付联行号
	 */
	public void setUnionpayNo(String unionpayNo) {
		this.unionpayNo = unionpayNo;
	}
	/**
	 * 获取：支付联行号
	 */
	public String getUnionpayNo() {
		return unionpayNo;
	}
	/**
	 * 设置：支行名称
	 */
	public void setBankBranch(String bankBranch) {
		this.bankBranch = bankBranch;
	}
	/**
	 * 获取：支行名称
	 */
	public String getBankBranch() {
		return bankBranch;
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
	/**
	 * 设置：有效期，年月，四位数，例：2112
	 */
	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}
	/**
	 * 获取：有效期，年月，四位数，例：2112
	 */
	public String getValidDate() {
		return validDate;
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
	public Integer getCity() {
		return city;
	}
	public void setCity(Integer city) {
		this.city = city;
	}
	public Integer getProvince() {
		return province;
	}
	public void setProvince(Integer province) {
		this.province = province;
	}
	public String getBankProvince() {
		return bankProvince;
	}
	public void setBankProvince(String bankProvince) {
		this.bankProvince = bankProvince;
	}
	public String getBankCity() {
		return bankCity;
	}
	public void setBankCity(String bankCity) {
		this.bankCity = bankCity;
	}
}
