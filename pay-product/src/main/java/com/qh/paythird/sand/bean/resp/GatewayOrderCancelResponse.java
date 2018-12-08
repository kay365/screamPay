/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-webgateway
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-27 下午3:52:06
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-27        Initailized
 */
package com.qh.paythird.sand.bean.resp;


import com.qh.paythird.sand.utils.SandPayResponse;

/**
 * @author pan.xl
 *
 */
public class GatewayOrderCancelResponse extends SandPayResponse {

	private GatewayOrderCancelResponseBody body;
	
	public GatewayOrderCancelResponseBody getBody() {
		return body;
	}
	public void setBody(GatewayOrderCancelResponseBody body) {
		this.body = body;
	}

	public static class GatewayOrderCancelResponseBody {
		private String orderCode;  // 商户订单号
		private String tradeNo;  // 交易流水号
		private String oriorderStatus;  // 原订单状态
		private String clearDate;  // 清算日期
		private String extend;  // 扩展域
		public String getOrderCode() {
			return orderCode;
		}
		public void setOrderCode(String orderCode) {
			this.orderCode = orderCode;
		}
		public String getTradeNo() {
			return tradeNo;
		}
		public void setTradeNo(String tradeNo) {
			this.tradeNo = tradeNo;
		}
		public String getOriorderStatus() {
			return oriorderStatus;
		}
		public void setOriorderStatus(String oriorderStatus) {
			this.oriorderStatus = oriorderStatus;
		}
		public String getClearDate() {
			return clearDate;
		}
		public void setClearDate(String clearDate) {
			this.clearDate = clearDate;
		}
		public String getExtend() {
			return extend;
		}
		public void setExtend(String extend) {
			this.extend = extend;
		}
	}
}
