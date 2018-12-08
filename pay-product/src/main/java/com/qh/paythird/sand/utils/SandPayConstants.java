/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : paychannel-cmsb-sdk
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-10-19 上午11:26:32
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-10-19        Initailized
 */
package com.qh.paythird.sand.utils;


/**
 *
 * @ClassName ：Constants
 * @author : pxl
 * @Date : 2016-10-19 上午11:26:32
 * @version 2.0.0
 *
 */
public class SandPayConstants {
	
	public static final String SUCCESS_RESP_CODE = "000000";
	public static final String DEFAULT_VERSION = "1.0";
	
	public static String GATEWAY_ORDERPAY = "sandpay.trade.pay"; //	统一下单
	public static String GATEWAY_ORDERQUERY = "sandpay.trade.query"; //	订单查询
	public static String GATEWAY_ORDERCANCEL = "sandpay.trade.cancel";//	订单撤销
	public static String GATEWAY_ORDERREFUND = "sandpay.trade.refund"; //	退货
	public static String GATEWAY_ORDERDOWNLOAD = "sandpay.trade.download"; //	对账单下载
	
	public static String GATEWAYPAY_PRODUCTID = "00000007";  // 网关支付
	public static String H5BANKCARD_PRODUCTID = "00000008";  // H5银行卡支付
	public static String QUICKPAY_PRODUCTID = "00000009";  // 快捷支付
	
}
