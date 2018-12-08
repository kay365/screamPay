package com.qh.paythird.dianxin.utils;

public class DianXinConst {

	/**
	 * 支付成功 状态
	 */
	public final static String RETURN_CODE = "0";
	
	/****点芯支付成功***/
    public final static String TRADE_STATUS_SUCC = "SUCCESS";
    /****点芯转入退款***/
    public final static String TRADE_STATUS_REF = "REFUND";
    /****点芯 未支付***/
    public final static String TRADE_STATUS_NOTP = "NOTPAY";
    /****点芯 已关闭***/
    public final static String TRADE_STATUS_CLO = "CLOSED";
    /****点芯  支付失败 其他原因***/
    public final static String TRADE_STATUS_ERROR = "PAYERROR";
	
	/**
	 * 支付接口版本
	 */
	public static final String VERSION = "dianxin_version";
	
	public static final String DT_REQ_URL = "dianxin_dt_req_url";
	
	public static final String ALI_KEY = "dianxin_ali_key";
	
	
	/**
	 *-------------------------平台  点芯-----------------------------------------
	 */
	public static final String B_VERSION = "dianxin_b_version";
	
	public static final String B_ORDER_DOWN = "dianxin_b_order_down";
	
	public static final String B_KEY = "dianxin_b_key";
	
	public static final String B_APPID = "dianxin_b_appid";
	
	public static final String B_GET_CODE = "dianxin_b_get_code";
	
	public static final String B_QUERY_ORDER_STATUS = "dianxin_b_query_order_status";
	
	public static final String B_RSA_PUBLIC_KEY = "dianxin_b_rsa_public_key";
	
	public static final String B_ACP_URL = "dianxin_b_acp_url";
	
}
