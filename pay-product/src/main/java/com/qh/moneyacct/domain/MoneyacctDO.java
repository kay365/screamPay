package com.qh.moneyacct.domain;
/**
 * 
 * @ClassName MoneyacctDO
 * @Description 钱包账户对象
 * @Date 2018年2月23日 下午8:13:24
 * @version 1.0.0
 */

import java.math.BigDecimal;

public class MoneyacctDO {
    /***用户Id***/
    private Integer userId;
    /***聚富商户号***/
    private String merchNo;
    /***聚富代理商***/
    private String agentNo;
    /***支付公司****/
    private String payCompany;
    /***支付商户号****/
    private String payMerch;
    /***支付通道***/
    private String outChannel;
    /***聚富商户名称****/
    private String name;
    /***账户总入账*******/
    private BigDecimal totalEntry;
    /***账户总出账*****/
    private BigDecimal totalOff;
    /***账户总手续费**/
    private BigDecimal totalHandFee;
    /***账户总余额****/
    private BigDecimal balance;
    /***账户可用余额*****/
    private BigDecimal availBal;
    /***账户不可用余额-待结算****/
    private BigDecimal forClear;
    /***账户冻结-交易中*****/
    private BigDecimal inTrading;
    
    public MoneyacctDO initZero() {
        this.totalEntry = BigDecimal.ZERO;
        this.totalOff = BigDecimal.ZERO;
        this.totalHandFee = BigDecimal.ZERO;
        this.balance = BigDecimal.ZERO;
        this.availBal = BigDecimal.ZERO;
        this.forClear = BigDecimal.ZERO;
        this.inTrading = BigDecimal.ZERO;
        return this;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getMerchNo() {
        return merchNo;
    }
    
    public void setMerchNo(String merchNo) {
        this.merchNo = merchNo;
    }
    
    public String getAgentNo() {
        return agentNo;
    }

    
    public void setAgentNo(String agentNo) {
        this.agentNo = agentNo;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getTotalEntry() {
        return totalEntry;
    }
    
    public void setTotalEntry(BigDecimal totalEntry) {
        this.totalEntry = totalEntry;
    }
    
    public BigDecimal getTotalOff() {
        return totalOff;
    }
    
    public void setTotalOff(BigDecimal totalOff) {
        this.totalOff = totalOff;
    }
    
    public BigDecimal getTotalHandFee() {
        return totalHandFee;
    }
    
    public void setTotalHandFee(BigDecimal totalHandFee) {
        this.totalHandFee = totalHandFee;
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
    
    public BigDecimal getForClear() {
        return forClear;
    }
    
    public void setForClear(BigDecimal forClear) {
        this.forClear = forClear;
    }
    
    public BigDecimal getInTrading() {
        return inTrading;
    }
    
    public void setInTrading(BigDecimal inTrading) {
        this.inTrading = inTrading;
    }

    public String getPayCompany() {
        return payCompany;
    }

    public void setPayCompany(String payCompany) {
        this.payCompany = payCompany;
    }

    public String getPayMerch() {
        return payMerch;
    }
    
    public void setPayMerch(String payMerch) {
        this.payMerch = payMerch;
    }

    public String getOutChannel() {
        return outChannel;
    }

    public void setOutChannel(String outChannel) {
        this.outChannel = outChannel;
    }
    
    
}
