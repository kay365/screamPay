/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-webgateway
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-27 下午3:51:39
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-27        Initailized
 */
package com.qh.paythird.sand.bean.req;

import com.alibaba.fastjson.annotation.JSONField;
import com.qh.paythird.sand.bean.resp.GatewayOrderCancelResponse;
import com.qh.paythird.sand.utils.SandPayRequest;

/**
 * @author pan.xl
 *
 */
public class GatewayOrderCancelRequest extends SandPayRequest<GatewayOrderCancelResponse> {
	
	private GatewayOrderCancelRequestBody body;
	
	public GatewayOrderCancelRequestBody getBody() {
		return body;
	}
	public void setBody(GatewayOrderCancelRequestBody body) {
		this.body = body;
	}

	public static class GatewayOrderCancelRequestBody {
		private String orderCode;  // 商户订单号
		private String oriOrderCode;  // 原商户订单号
		private String oriTotalAmount;  // 原订单金额
		private String extend;  // 扩展域
		public String getOrderCode() {
			return orderCode;
		}
		public void setOrderCode(String orderCode) {
			this.orderCode = orderCode;
		}
		public String getOriOrderCode() {
			return oriOrderCode;
		}
		public void setOriOrderCode(String oriOrderCode) {
			this.oriOrderCode = oriOrderCode;
		}
		public String getOriTotalAmount() {
			return oriTotalAmount;
		}
		public void setOriTotalAmount(String oriTotalAmount) {
			this.oriTotalAmount = oriTotalAmount;
		}
		public String getExtend() {
			return extend;
		}
		public void setExtend(String extend) {
			this.extend = extend;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.sandpay.cashier.SandPayRequest#getResponseClass()
	 */
	@Override
	@JSONField(serialize=false)
	public Class<GatewayOrderCancelResponse> getResponseClass() {
		return GatewayOrderCancelResponse.class;
	}

	/* (non-Javadoc)
	 * @see cn.com.sandpay.cashier.SandPayRequest#getTxnDesc()
	 */
	@Override
	@JSONField(serialize=false)
	public String getTxnDesc() {
		return "gwOrderCancel";
	}

}
