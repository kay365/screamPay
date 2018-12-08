package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class Agent implements Serializable{
	private static final long serialVersionUID = 1L;
	/*** id 主键 ***/
	private Integer agentId;
	/*** 商户号 ***/
	private String agentNumber;
	/**状态  1 启用  2禁用**/
	private Integer status;
	/**审核状态  1通过  2 待审核  3 不通过**/
	private Integer auditStatus;
	/**代理商级别    1 一级  2 二级**/
	private Integer level;
	/**上级代理  不填，默认一级代理(最大支持二级代理)**/
	private String parentAgent;
	
	//创建时间
		private Date createTime;
		//修改时间
		private Date modifyTime;
		//代理商类型  1个人 2企业   [代理商开户信息开始]
		private Integer agentType;
		//管理员姓名
		private String managerName;
		//管理员电话  唯一
		private String managerPhone;
		//密码
		private String managerPass;
		//合同有效时间   2018-02-02~2020-02-02
		private String contractEffectiveTime;
		//启用时间 [代理商开户信息结束]
		private Date enableTime;
		//商户名称   [代理商联系人信息开始]
		private String merchantsName;
		//代理商简称
		private String merchantsShortName;
		//商户父级行业
		private String merchantsIndustry;
		//商户父级行业代号
		private Integer merchantsIndustryCode;
		//商户子级行业
		private String merchantsSubIndustry;
		//商户子级行业代码
		private Integer merchantsSubIndustryCode;
		//联系人
		private String contacts;
		//联系人电话
		private String contactsPhone;
		//联系人邮箱
		private String contactsEmail;
		//联系人QQ   [代理商联系人信息结束]
		private String contactsQq;
		//省份   [实名信息 开始]
		private String province;
		//省份代号
		private String provinceCode;
		//城市
		private String city;
		//城市代号
		private String cityCode;
		//法人姓名 （实名）
		private String legalerName;
		//法人证件类型  1 身份证
		private Integer legalerCardType;
		//法人证件号码
		private String legalerCardNumber;
		//法人证件有效时间
		private String legalerCardEffectiveTime;
		//法人证件正面照
		private String legalerCardPicFront;
		//法人证件背面照  [实名信息 结束]
		private String legalerCardPicBack;
		//账户类型   1对公  2对私  [结算账户 开始]
		private Integer accountType;
		//账户开户省份
		private String accountProvince;
		//账户开户省份代码
		private String accountProvinceCode;
		//账户开户城市
		private String accountCity;
		//账户开户城市代码
		private String accountCityCode;
		//账户开户银行
		private String accountBank;
		//账户开户银行代号
		private String accountBankCode;
		//账户开户银行  支行
		private String accountBankBranch;
		//账户支行ID
		private String accountBankBranchCode;
		//账户开户人
		private String accountOpenPerson;
		//账户开户账号(结算账号)
		private String accountOpenNumber;
		//开户人身份证号码
		private String accountOpenCardNumber;
		//银行预留手机号
		private String accountOpenPhone;
		//卡/证影印件  [结算账户 结束]
		private String accountPic;
	
	/***手续费率   T+1    (费率信息 开始)***/
	private Map<String,Map<String,BigDecimal>> tOne;
	/***手续费率   D+0***/
	private Map<String,Map<String,Map<String,BigDecimal>>> dZero;
	/***手续费率   代付       (费率信息 结束)***/
	private Map<String,BigDecimal> paid;
	
	/***账户余额     -------------显示字段**/
	private BigDecimal balance;
	/***可用余额**/
	private BigDecimal availBal;
	
	public Integer getAgentId() {
		return agentId;
	}
	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}
	public String getAgentNumber() {
		return agentNumber;
	}
	public void setAgentNumber(String agentNumber) {
		this.agentNumber = agentNumber;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getAuditStatus() {
		return auditStatus;
	}
	public void setAuditStatus(Integer auditStatus) {
		this.auditStatus = auditStatus;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getParentAgent() {
		return parentAgent;
	}
	public void setParentAgent(String parentAgent) {
		this.parentAgent = parentAgent;
	}
	public String getMerchantsName() {
		return merchantsName;
	}
	public void setMerchantsName(String merchantsName) {
		this.merchantsName = merchantsName;
	}
	public Map<String, Map<String, BigDecimal>> gettOne() {
		return tOne;
	}
	public void settOne(Map<String, Map<String, BigDecimal>> tOne) {
		this.tOne = tOne;
	}
	public Map<String, Map<String, Map<String,BigDecimal>>> getdZero() {
		return dZero;
	}
	public void setdZero(Map<String, Map<String, Map<String,BigDecimal>>> dZero) {
		this.dZero = dZero;
	}
	public Map<String, BigDecimal> getPaid() {
		return paid;
	}
	public void setPaid(Map<String, BigDecimal> paid) {
		this.paid = paid;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public BigDecimal getAvailBal() {
		return availBal;
	}
	public void setAvailBal(BigDecimal availBal) {
		this.availBal = availBal;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Integer getAgentType() {
		return agentType;
	}
	public void setAgentType(Integer agentType) {
		this.agentType = agentType;
	}
	public String getManagerName() {
		return managerName;
	}
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
	public String getManagerPhone() {
		return managerPhone;
	}
	public void setManagerPhone(String managerPhone) {
		this.managerPhone = managerPhone;
	}
	public String getManagerPass() {
		return managerPass;
	}
	public void setManagerPass(String managerPass) {
		this.managerPass = managerPass;
	}
	public String getContractEffectiveTime() {
		return contractEffectiveTime;
	}
	public void setContractEffectiveTime(String contractEffectiveTime) {
		this.contractEffectiveTime = contractEffectiveTime;
	}
	public Date getEnableTime() {
		return enableTime;
	}
	public void setEnableTime(Date enableTime) {
		this.enableTime = enableTime;
	}
	public String getMerchantsShortName() {
		return merchantsShortName;
	}
	public void setMerchantsShortName(String merchantsShortName) {
		this.merchantsShortName = merchantsShortName;
	}
	public String getMerchantsIndustry() {
		return merchantsIndustry;
	}
	public void setMerchantsIndustry(String merchantsIndustry) {
		this.merchantsIndustry = merchantsIndustry;
	}
	public Integer getMerchantsIndustryCode() {
		return merchantsIndustryCode;
	}
	public void setMerchantsIndustryCode(Integer merchantsIndustryCode) {
		this.merchantsIndustryCode = merchantsIndustryCode;
	}
	public String getMerchantsSubIndustry() {
		return merchantsSubIndustry;
	}
	public void setMerchantsSubIndustry(String merchantsSubIndustry) {
		this.merchantsSubIndustry = merchantsSubIndustry;
	}
	public Integer getMerchantsSubIndustryCode() {
		return merchantsSubIndustryCode;
	}
	public void setMerchantsSubIndustryCode(Integer merchantsSubIndustryCode) {
		this.merchantsSubIndustryCode = merchantsSubIndustryCode;
	}
	public String getContacts() {
		return contacts;
	}
	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	public String getContactsPhone() {
		return contactsPhone;
	}
	public void setContactsPhone(String contactsPhone) {
		this.contactsPhone = contactsPhone;
	}
	public String getContactsEmail() {
		return contactsEmail;
	}
	public void setContactsEmail(String contactsEmail) {
		this.contactsEmail = contactsEmail;
	}
	public String getContactsQq() {
		return contactsQq;
	}
	public void setContactsQq(String contactsQq) {
		this.contactsQq = contactsQq;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public String getLegalerName() {
		return legalerName;
	}
	public void setLegalerName(String legalerName) {
		this.legalerName = legalerName;
	}
	public Integer getLegalerCardType() {
		return legalerCardType;
	}
	public void setLegalerCardType(Integer legalerCardType) {
		this.legalerCardType = legalerCardType;
	}
	public String getLegalerCardNumber() {
		return legalerCardNumber;
	}
	public void setLegalerCardNumber(String legalerCardNumber) {
		this.legalerCardNumber = legalerCardNumber;
	}
	public String getLegalerCardEffectiveTime() {
		return legalerCardEffectiveTime;
	}
	public void setLegalerCardEffectiveTime(String legalerCardEffectiveTime) {
		this.legalerCardEffectiveTime = legalerCardEffectiveTime;
	}
	public String getLegalerCardPicFront() {
		return legalerCardPicFront;
	}
	public void setLegalerCardPicFront(String legalerCardPicFront) {
		this.legalerCardPicFront = legalerCardPicFront;
	}
	public String getLegalerCardPicBack() {
		return legalerCardPicBack;
	}
	public void setLegalerCardPicBack(String legalerCardPicBack) {
		this.legalerCardPicBack = legalerCardPicBack;
	}
	public Integer getAccountType() {
		return accountType;
	}
	public void setAccountType(Integer accountType) {
		this.accountType = accountType;
	}
	public String getAccountProvince() {
		return accountProvince;
	}
	public void setAccountProvince(String accountProvince) {
		this.accountProvince = accountProvince;
	}
	public String getAccountProvinceCode() {
		return accountProvinceCode;
	}
	public void setAccountProvinceCode(String accountProvinceCode) {
		this.accountProvinceCode = accountProvinceCode;
	}
	public String getAccountCity() {
		return accountCity;
	}
	public void setAccountCity(String accountCity) {
		this.accountCity = accountCity;
	}
	public String getAccountCityCode() {
		return accountCityCode;
	}
	public void setAccountCityCode(String accountCityCode) {
		this.accountCityCode = accountCityCode;
	}
	public String getAccountBank() {
		return accountBank;
	}
	public void setAccountBank(String accountBank) {
		this.accountBank = accountBank;
	}
	public String getAccountBankCode() {
		return accountBankCode;
	}
	public void setAccountBankCode(String accountBankCode) {
		this.accountBankCode = accountBankCode;
	}
	public String getAccountBankBranch() {
		return accountBankBranch;
	}
	public void setAccountBankBranch(String accountBankBranch) {
		this.accountBankBranch = accountBankBranch;
	}
	public String getAccountBankBranchCode() {
		return accountBankBranchCode;
	}
	public void setAccountBankBranchCode(String accountBankBranchCode) {
		this.accountBankBranchCode = accountBankBranchCode;
	}
	public String getAccountOpenPerson() {
		return accountOpenPerson;
	}
	public void setAccountOpenPerson(String accountOpenPerson) {
		this.accountOpenPerson = accountOpenPerson;
	}
	public String getAccountOpenNumber() {
		return accountOpenNumber;
	}
	public void setAccountOpenNumber(String accountOpenNumber) {
		this.accountOpenNumber = accountOpenNumber;
	}
	public String getAccountOpenCardNumber() {
		return accountOpenCardNumber;
	}
	public void setAccountOpenCardNumber(String accountOpenCardNumber) {
		this.accountOpenCardNumber = accountOpenCardNumber;
	}
	public String getAccountOpenPhone() {
		return accountOpenPhone;
	}
	public void setAccountOpenPhone(String accountOpenPhone) {
		this.accountOpenPhone = accountOpenPhone;
	}
	public String getAccountPic() {
		return accountPic;
	}
	public void setAccountPic(String accountPic) {
		this.accountPic = accountPic;
	}
	
}
