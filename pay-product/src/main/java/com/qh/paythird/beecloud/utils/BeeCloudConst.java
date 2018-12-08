package com.qh.paythird.beecloud.utils;

public class BeeCloudConst {

	public static final String RESULT_CODE_SUCC = "0";
	
	/**
	 * 信用卡
	 */
	public static final String CARD_TYPE_CREDIT = "1";
	
	/**
	 * 借记卡
	 */
	public static final String CARD_TYPE_DEBIT = "2";
	
	/**
	 * 京东快捷支付类型
	 */
	public static final String CHANNEL_BC_EXPRESS = "BC_EXPRESS";
	
	/**
	 * 比可支付APP ID
	 */
	public static final String APP_ID = "beecloud_app_id";
	
	/**
	 * 比可支付APP SECRET
	 */
	public static final String APP_SECRET = "beecloud_app_secret";
	
	public static final String MASTER_SECRET= "beecloud_master_secret";
	
	/**
	 * 比可支付京东快捷支付请求地址
	 */
	public static final String REQ_URL = "beecloud_jd_requrl";
	
	/**
	 * 比可支付京东快捷支付确认支付地址
	 */
	public static final String CONFIRM_URL = "beecloud_jd_confirm";
	
	/**
	 * 比可支付京东快捷 代付请求地址
	 */
	public static final String REQ_DF_URL = "beecloud_df_req_url";
	
}
