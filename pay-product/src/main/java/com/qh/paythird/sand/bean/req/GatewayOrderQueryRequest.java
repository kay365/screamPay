/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-webgateway
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-27 下午3:48:46
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-27        Initailized
 */
package com.qh.paythird.sand.bean.req;

import com.alibaba.fastjson.annotation.JSONField;
import com.qh.paythird.sand.bean.resp.GatewayOrderQueryResponse;
import com.qh.paythird.sand.utils.SandPayRequest;

/**
 * @author pan.xl
 *
 */
public class GatewayOrderQueryRequest extends SandPayRequest<GatewayOrderQueryResponse> {
	
	private GatewayOrderQueryRequestBody body;
	
	public GatewayOrderQueryRequestBody getBody() {
		return body;
	}
	public void setBody(GatewayOrderQueryRequestBody body) {
		this.body = body;
	}

	public static class GatewayOrderQueryRequestBody {
		private String orderCode;  // 商户订单号
		private String extend;  // 扩展域
		public String getOrderCode() {
			return orderCode;
		}
		public void setOrderCode(String orderCode) {
			this.orderCode = orderCode;
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
	public Class<GatewayOrderQueryResponse> getResponseClass() {
		return GatewayOrderQueryResponse.class;
	}

	/* (non-Javadoc)
	 * @see cn.com.sandpay.cashier.SandPayRequest#getTxnDesc()
	 */
	@Override
	@JSONField(serialize=false)
	public String getTxnDesc() {
		return "gwOrderQuery";
	}

}
