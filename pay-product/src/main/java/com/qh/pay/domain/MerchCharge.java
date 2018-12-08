package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName MerchCharge
 * @Description 商户充值记录
 * @Date 2017年12月22日 下午2:35:25
 * @version 1.0.0
 */
public class MerchCharge implements Serializable{
	/**
	 */
	private static final long serialVersionUID = 1L;
	/***商户号****/
	private String merchNo;
	/***充值金额******/
	private BigDecimal amount;
	/***业务号******/
	private String businessNo;
	/***订单状态****/
	private Integer orderState;
	/***清算状态***/
	private Integer clearState;
	/***渠道****/
	private String outChannel;
	/***创建时间****/
	private Integer crtDate;
	/***备注信息***/
	private String memo;
	/***消息提示信息***/
	private String msg;
	public String getMerchNo() {
		return merchNo;
	}
	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getBusinessNo() {
		return businessNo;
	}
	public void setBusinessNo(String businessNo) {
		this.businessNo = businessNo;
	}
	public Integer getOrderState() {
		return orderState;
	}
	public void setOrderState(Integer orderState) {
		this.orderState = orderState;
	}
	public Integer getClearState() {
		return clearState;
	}
	public void setClearState(Integer clearState) {
		this.clearState = clearState;
	}
	public String getOutChannel() {
		return outChannel;
	}
	public void setOutChannel(String outChannel) {
		this.outChannel = outChannel;
	}
	public Integer getCrtDate() {
		return crtDate;
	}
	public void setCrtDate(Integer crtDate) {
		this.crtDate = crtDate;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
