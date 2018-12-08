package com.qh.pay.domain;

import java.io.Serializable;
import java.util.Map;



/**
 * 支付银行
 * 
 * @date 2017-12-27 11:45:47
 */
public class PayBankDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//支付公司
	private String company;
	//支付商户号
	private String payMerch;
	//银行卡类型
	private Integer cardType;
	//银行卡列表
	private Map<String,String> banks;
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getPayMerch() {
		return payMerch;
	}
	public void setPayMerch(String payMerch) {
		this.payMerch = payMerch;
	}
	public Integer getCardType() {
		return cardType;
	}
	public void setCardType(Integer cardType) {
		this.cardType = cardType;
	}
	public Map<String, String> getBanks() {
		return banks;
	}
	public void setBanks(Map<String, String> banks) {
		this.banks = banks;
	}
	
}
