/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-webgateway
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-27 上午11:02:55
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-27        Initailized
 */
package com.qh.paythird.sand.utils;

/**
 * @author pan.xl
 *
 */
public class SandPayRequestHead {

	public String version;  // 版本号
	public String method;  // 接口名称
	public String productId;  // 产品编码
	public String accessType;  // 接入类型
	public String mid;  // 商户ID
	public String plMid;  // 平台商户
	public String channelType;  // 渠道类型
	public String reqTime;  // 请求时间
    public String accessChannelNo;   //接入平台
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getAccessType() {
		return accessType;
	}
	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getPlMid() {
		return plMid;
	}
	public void setPlMid(String plMid) {
		this.plMid = plMid;
	}
	public String getChannelType() {
		return channelType;
	}
	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}
	public String getReqTime() {
		return reqTime;
	}
	public void setReqTime(String reqTime) {
		this.reqTime = reqTime;
	}

	public String getAccessChannelNo() {
		return accessChannelNo;
	}

	public void setAccessChannelNo(String accessChannelNo) {
		this.accessChannelNo = accessChannelNo;
	}
}
