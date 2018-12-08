package com.qh.pay.domain;

import java.io.Serializable;


/**
 * 支付参数配置
 * 
 * @date 2017-10-27 17:52:44
 */
public class PayPropertyDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//
	private Integer id;
	//支付公司 0 为系统默认参数
	private String payCompany;
	//商户号
	private String merchantno;
	// 0为普通文本 1 密码 2域名Ip 3 文件路径 4文件内容 5 商户号
	private Integer configType;
	//配置标识
	private String configKey;
	//值
	private String value;
	//名称说明
	private String name;

	/**
	 * 设置：
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * 获取：
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * 设置：支付公司 0 为系统默认参数
	 */
	public void setPayCompany(String payCompany) {
		this.payCompany = payCompany;
	}
	/**
	 * 获取：支付公司 0 为系统默认参数
	 */
	public String getPayCompany() {
		return payCompany;
	}
	/**
	 * 设置：商户号
	 */
	public void setMerchantno(String merchantno) {
		this.merchantno = merchantno;
	}
	/**
	 * 获取：商户号
	 */
	public String getMerchantno() {
		return merchantno;
	}
	/**
	 * 设置： 0为普通文本 1 密码 2域名Ip 3 文件路径 4文件内容 5 商户号
	 */
	public void setConfigType(Integer configType) {
		this.configType = configType;
	}
	/**
	 * 获取： 0为普通文本 1 密码 2域名Ip 3 文件路径 4文件内容 5 商户号
	 */
	public Integer getConfigType() {
		return configType;
	}
	/**
	 * 设置：配置标识
	 */
	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}
	/**
	 * 获取：配置标识
	 */
	public String getConfigKey() {
		return configKey;
	}
	/**
	 * 设置：值
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * 获取：值
	 */
	public String getValue() {
		return value;
	}
	/**
	 * 设置：名称说明
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取：名称说明
	 */
	public String getName() {
		return name;
	}
}
