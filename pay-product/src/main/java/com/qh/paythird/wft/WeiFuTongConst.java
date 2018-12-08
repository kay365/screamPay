package com.qh.paythird.wft;



/**
 * 
 * @author reddy
 *
 */
public class WeiFuTongConst {

	
	/**
	 * 支付密钥
	 */
	public static final String wft_version = "3";
	/**
	 * 支付密钥
	 */
	public static final String wft_type_acp = "withdraw";
	/**
	 * 支付密钥
	 */
	public static final String wft_key = "wft_key";
	/**
	 * 支付密钥
	 */
	public static final String wft_publickey = "wft_publickey";
	
	/**
	 * 支付请求地址
	 */
	public static final String wft_reqUrl = "wft_domain";
	/**
	 * 支付商户号
	 */
	public static final String wft_merchantCode = "wft_merchantCode";
	/**
	 * 支付商户号
	 */
	public static final String wft_appId = "wft_appId";
	/**
	 * 字符编码
	 */
	public static final String charset = "UTF-8";
	/**
	 * 加密方式
	 */
	public static final String signType = "MD5";
	/**
	 * 成功
	 */
	public static final String T = "true";
	/**
	 * 失败
	 */
	public static final String F = "false";
	/**
	 * 支付地址
	 */
	public static final String payOrder = "remote/make_prepare_pay.remote";
	/**
	 * 支付跳转地址
	 */
	public static final String payJump = "remote/pay_page_skip.remote";
	/**
	 * 支付查询地址
	 */
	public static final String payQuery = "remote/query_order_status.remote";
	/**
	 * 代付
	 */
	public static final String payAcp = "remote/merchant_withdraw.remote";
	/**
	 * 二维码请求地址
	 */
	public static final String payQr = "remotePay/getCode.remote";
	

	
}
