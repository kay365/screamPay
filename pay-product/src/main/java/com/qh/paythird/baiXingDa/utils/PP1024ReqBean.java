package com.qh.paythird.baiXingDa.utils;


/**
 * 获取订单详情请求Bean
 * 
 * @author liuyq
 *
 */
public class PP1024ReqBean {

	private HeadReq head;
	private String orderNo;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public HeadReq getHead() {
		return head;
	}

	public void setHead(HeadReq head) {
		this.head = head;
	}

	public PP1024ReqBean(String tranCode, String userId,String orderId) {
		HeadReq head = new HeadReq(tranCode, userId,orderId);
		this.setHead(head);
	}

}
